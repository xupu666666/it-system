package com.ict.system.service.v2.impl;

import com.ict.system.model.v2.Role;
import com.ict.system.model.v2.User;
import com.ict.system.payload.request.LoginRequest;
import com.ict.system.payload.request.SignupRequest;
import com.ict.system.payload.response.JwtResponse;
import com.ict.system.security.v2.JwtTokenProvider;
import com.ict.system.service.v2.AnomalyDetectionService;
import com.ict.system.service.v2.AuditLogService;
import com.ict.system.service.v2.AuthService;
import com.ict.system.service.v2.IpRestrictionService;
import com.ict.system.service.v2.LoginAttemptService;
import com.ict.system.service.v2.LoginHistoryService;
import com.ict.system.service.v2.RoleService;
import com.ict.system.service.v2.UserService;
import com.ict.system.service.v2.UserSessionService;
import com.ict.system.util.RequestContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service("authServiceV2")
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    @Qualifier("authenticationManagerBeanV2")
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("userServiceV2")
    private UserService userService;

    @Autowired
    @Qualifier("roleServiceV2")
    private RoleService roleService;

    @Autowired
    @Qualifier("passwordEncoderV2")
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier("jwtTokenProviderV2")
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    @Qualifier("loginHistoryServiceV2")
    private LoginHistoryService loginHistoryService;

    @Autowired
    @Qualifier("auditLogServiceV2")
    private AuditLogService auditLogService;

    @Autowired
    @Qualifier("loginAttemptServiceV2")
    private LoginAttemptService loginAttemptService;

    @Autowired
    @Qualifier("userSessionServiceV2")
    private UserSessionService userSessionService;

    @Autowired
    @Qualifier("ipRestrictionServiceV2")
    private IpRestrictionService ipRestrictionService;

    @Autowired
    @Qualifier("anomalyDetectionServiceV2")
    private AnomalyDetectionService anomalyDetectionService;

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        log.info("用户登录: {}", loginRequest.getUsername());

        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        // 检查IP是否在黑名单中
        if (ipRestrictionService.isBlacklisted(ipAddress)) {
            String errorMessage = "IP地址已被禁止访问";

            // 记录登录失败
            loginHistoryService.recordLoginFailure(
                    loginRequest.getUsername(),
                    ipAddress,
                    userAgent,
                    "IP地址已被禁止访问"
            );

            log.warn("登录失败，IP地址已被禁止访问: {}, IP: {}", loginRequest.getUsername(), ipAddress);

            throw new LockedException(errorMessage);
        }

        // 检查账户是否被锁定
        if (loginAttemptService.isLocked(loginRequest.getUsername(), ipAddress)) {
            int remainingMinutes = loginAttemptService.getLockTimeRemaining(loginRequest.getUsername(), ipAddress);
            String errorMessage = "账户已被锁定，请" + remainingMinutes + "分钟后再试";

            // 记录登录失败
            loginHistoryService.recordLoginFailure(
                    loginRequest.getUsername(),
                    ipAddress,
                    userAgent,
                    "账户已被锁定"
            );

            log.warn("登录失败，账户已被锁定: {}, IP: {}, 剩余锁定时间: {}分钟",
                    loginRequest.getUsername(), ipAddress, remainingMinutes);

            throw new LockedException(errorMessage);
        }

        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 设置认证信息到上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成JWT令牌
            String jwt = jwtTokenProvider.generateToken(authentication);

            // 获取用户信息
            User user = (User) authentication.getPrincipal();

            // 创建用户会话
            userSessionService.createSession(
                    user.getUsername(),
                    jwt,
                    ipAddress,
                    userAgent,
                    60 * 24 // 24小时过期
            );

            // 记录登录成功
            loginHistoryService.recordLoginSuccess(user.getUsername(), ipAddress, userAgent);

            // 重置登录尝试次数
            loginAttemptService.recordSuccessfulAttempt(user.getUsername(), ipAddress);

            // 检测异常登录
            boolean isAnomalous = anomalyDetectionService.detectAnomalousLogin(user.getUsername(), ipAddress, userAgent);

            if (isAnomalous) {
                log.warn("检测到异常登录: {}, IP: {}", user.getUsername(), ipAddress);

                // 记录审计日志
                auditLogService.log(
                        user.getUsername(),
                        "ANOMALOUS_LOGIN",
                        "USER",
                        user.getId().toString(),
                        "检测到异常登录，IP: " + ipAddress + ", 设备: " + userAgent,
                        ipAddress
                );
            }

            // 记录审计日志
            auditLogService.log(
                    user.getUsername(),
                    "LOGIN",
                    "USER",
                    user.getId().toString(),
                    "用户登录成功",
                    ipAddress
            );

            // 构建响应
            return new JwtResponse(
                    jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getName(),
                    user.getEmail(),
                    user.getDepartment(),
                    user.getAvatar(),
                    user.getRoleNames(),
                    user.getPermissionNames()
            );
        } catch (BadCredentialsException e) {
            // 记录登录失败
            loginHistoryService.recordLoginFailure(
                    loginRequest.getUsername(),
                    ipAddress,
                    userAgent,
                    "用户名或密码错误"
            );

            // 记录失败尝试
            boolean locked = loginAttemptService.recordFailedAttempt(loginRequest.getUsername(), ipAddress);

            if (locked) {
                int remainingMinutes = loginAttemptService.getLockTimeRemaining(loginRequest.getUsername(), ipAddress);
                String errorMessage = "登录失败次数过多，账户已被锁定" + remainingMinutes + "分钟";

                log.warn("登录失败次数过多，账户已被锁定: {}, IP: {}, 锁定时间: {}分钟",
                        loginRequest.getUsername(), ipAddress, remainingMinutes);

                throw new LockedException(errorMessage);
            }

            int remainingAttempts = loginAttemptService.getRemainingAttempts(loginRequest.getUsername(), ipAddress);
            String errorMessage = "用户名或密码错误，还剩" + remainingAttempts + "次尝试机会";

            log.warn("登录失败: {}, IP: {}, 剩余尝试次数: {}",
                    loginRequest.getUsername(), ipAddress, remainingAttempts);

            throw new BadCredentialsException(errorMessage);
        }
    }

    /**
     * 获取客户端IP地址
     * @return IP地址
     */
    private String getClientIpAddress() {
        // 使用工具类获取客户端IP地址
        return RequestContextUtil.getClientIpAddress();
    }

    /**
     * 获取用户代理
     * @return 用户代理
     */
    private String getUserAgent() {
        // 使用工具类获取用户代理
        return RequestContextUtil.getUserAgent();
    }

    @Override
    @Transactional
    public User register(SignupRequest signupRequest) {
        log.info("用户注册: {}", signupRequest.getUsername());

        // 检查用户名是否已存在
        if (userService.existsByUsername(signupRequest.getUsername())) {
            log.warn("用户名已存在: {}", signupRequest.getUsername());
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (signupRequest.getEmail() != null && userService.existsByEmail(signupRequest.getEmail())) {
            log.warn("邮箱已存在: {}", signupRequest.getEmail());
            throw new RuntimeException("邮箱已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setDepartment(signupRequest.getDepartment());
        user.setEnabled(true);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        // 分配默认角色
        Set<Role> roles = new HashSet<>();
        Role userRole = roleService.findByName("USER")
                .orElseThrow(() -> new RuntimeException("默认角色不存在"));
        roles.add(userRole);
        user.setRoles(roles);

        // 保存用户
        User savedUser = userService.save(user);

        // 记录审计日志
        String ipAddress = getClientIpAddress();
        auditLogService.log(
                user.getUsername(),
                "REGISTER",
                "USER",
                savedUser.getId().toString(),
                "用户注册成功",
                ipAddress
        );

        return savedUser;
    }

    @Override
    public String refreshToken(String username) {
        log.info("刷新令牌: {}", username);

        // 获取用户信息
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 创建认证对象
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());

        // 生成新的JWT令牌
        String newToken = jwtTokenProvider.generateToken(authentication);

        // 创建新的用户会话
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        userSessionService.createSession(
                username,
                newToken,
                ipAddress,
                userAgent,
                60 * 24 // 24小时过期
        );

        // 记录审计日志
        auditLogService.log(
                username,
                "REFRESH_TOKEN",
                "USER",
                user.getId().toString(),
                "用户刷新令牌",
                ipAddress
        );

        return newToken;
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }

        return null;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userService.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userService.existsByEmail(email);
    }
}
