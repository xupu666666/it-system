package com.ict.system.security.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ict.system.service.v2.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

@Component("jwtAuthenticationFilterV2")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // 公共路径，不需要认证
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/request-reset-password",
            "/api/auth/reset-password",
            "/api/test/public",
            "/api/inventory/**",  // 添加资产管理相关接口到公共路径，用于测试
            "/api/inventory-check-task/**",  // 添加资产盘点任务相关接口到公共路径
            "/api/inventory-check-record/**"  // 添加资产盘点记录相关接口到公共路径
    );

    @Autowired
    @Qualifier("jwtTokenProviderV2")
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    @Qualifier("userDetailsServiceV2")
    private UserDetailsService userDetailsService;

    @Autowired
    @Qualifier("userSessionServiceV2")
    private UserSessionService userSessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("处理请求: {} {}", method, path);

        // 记录请求头信息，帮助调试
        if (logger.isDebugEnabled()) {
            logger.debug("请求头信息:");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                logger.debug("  {}: {}", headerName, request.getHeader(headerName));
            }
        }

        // OPTIONS请求直接放行
        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.debug("OPTIONS请求，跳过JWT认证");
            filterChain.doFilter(request, response);
            return;
        }

        // 检查是否为公共路径
        if (isPublicPath(path)) {
            logger.debug("公共路径: {}, 跳过JWT认证", path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = parseJwt(request);
            logger.debug("JWT令牌解析: {}", jwt != null ? "成功" : "失败");

            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                // 验证会话是否有效
                boolean sessionValid = userSessionService.validateSession(jwt);

                if (!sessionValid) {
                    logger.debug("会话无效或已过期");
                    return;
                }

                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                logger.debug("从令牌中获取用户名: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("用户认证成功: {}", username);

                // 更新会话活动时间
                userSessionService.updateSessionActivity(jwt);
            } else {
                logger.debug("请求中没有有效的JWT令牌");
            }
        } catch (Exception e) {
            logger.error("无法设置用户认证: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中解析JWT令牌
     * @param request HTTP请求
     * @return JWT令牌
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

    /**
     * 检查是否为公共路径
     * @param path 请求路径
     * @return 是否为公共路径
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(publicPath ->
            publicPath.endsWith("/**")
                ? path.startsWith(publicPath.substring(0, publicPath.length() - 3))
                : path.equals(publicPath)
        );
    }
}
