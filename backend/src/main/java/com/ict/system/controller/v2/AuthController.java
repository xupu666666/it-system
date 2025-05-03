package com.ict.system.controller.v2;

import com.ict.system.model.v2.User;
import com.ict.system.payload.request.LoginRequest;
import com.ict.system.payload.request.SignupRequest;
import com.ict.system.payload.response.JwtResponse;
import com.ict.system.payload.response.MessageResponse;
import com.ict.system.service.v2.AuthService;
import com.ict.system.service.v2.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 *
 * 注意：所有API路径都遵循统一的格式：/api/{模块名}/{资源名}
 * 这样可以保持API路径的一致性，便于维护和管理
 */
@RestController("authControllerV2")
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true", maxAge = 3600)
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    @Qualifier("authServiceV2")
    private AuthService authService;

    @Autowired
    @Qualifier("userSessionServiceV2")
    private UserSessionService userSessionService;

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return JWT响应
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("收到登录请求: {}", loginRequest.getUsername());

        try {
            JwtResponse response = authService.login(loginRequest);
            log.info("用户登录成功: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("用户登录失败，凭证无效: {}, 错误: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401)
                    .body(new MessageResponse(e.getMessage()));
        } catch (LockedException e) {
            log.warn("用户登录失败，账户已锁定: {}, 错误: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(423) // 423 Locked
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("用户登录失败: {}, 错误: {}", loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("登录失败: " + e.getMessage()));
        }
    }

    /**
     * 用户注册
     * @param signupRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest signupRequest) {
        log.info("收到注册请求: {}", signupRequest.getUsername());

        // 检查用户名是否已存在
        if (authService.existsByUsername(signupRequest.getUsername())) {
            log.warn("注册失败，用户名已存在: {}", signupRequest.getUsername());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("用户名已存在"));
        }

        // 检查邮箱是否已存在
        if (signupRequest.getEmail() != null && !signupRequest.getEmail().isEmpty() &&
                authService.existsByEmail(signupRequest.getEmail())) {
            log.warn("注册失败，邮箱已存在: {}", signupRequest.getEmail());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("邮箱已存在"));
        }

        try {
            // 注册用户
            User user = authService.register(signupRequest);
            log.info("用户注册成功: {}, ID: {}", user.getUsername(), user.getId());
            return ResponseEntity.ok(new MessageResponse("用户注册成功"));
        } catch (Exception e) {
            log.error("用户注册失败: {}, 错误: {}", signupRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("注册失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo() {
        log.info("获取当前用户信息");

        User user = authService.getCurrentUser();

        if (user == null) {
            log.warn("获取用户信息失败，未认证");
            return ResponseEntity.status(401)
                    .body(new MessageResponse("未认证"));
        }

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> userInfo = new HashMap<>();

        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());
        userInfo.put("department", user.getDepartment());
        userInfo.put("avatar", user.getAvatar());

        response.put("userInfo", userInfo);
        response.put("roles", user.getRoleNames());
        response.put("permissions", user.getPermissionNames());

        log.info("用户信息获取成功: {}", user.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * 刷新令牌
     * @return 新的JWT令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken() {
        log.info("刷新令牌");

        User user = authService.getCurrentUser();

        if (user == null) {
            log.warn("刷新令牌失败，未认证");
            return ResponseEntity.status(401)
                    .body(new MessageResponse("未认证"));
        }

        try {
            String newToken = authService.refreshToken(user.getUsername());

            Map<String, String> response = new HashMap<>();
            response.put("token", newToken);

            log.info("令牌刷新成功: {}", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("令牌刷新失败: {}, 错误: {}", user.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("令牌刷新失败: " + e.getMessage()));
        }
    }

    /**
     * 用户登出
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("用户登出");

        try {
            // 从请求头中获取令牌
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // 使会话失效
                boolean result = userSessionService.invalidateSession(token);

                if (result) {
                    log.info("用户登出成功，会话已失效");
                } else {
                    log.warn("用户登出，但会话不存在或已失效");
                }
            } else {
                log.warn("用户登出，但未提供令牌");
            }

            return ResponseEntity.ok(new MessageResponse("登出成功"));
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage(), e);
            // 即使发生错误，也返回成功，因为客户端会删除令牌
            return ResponseEntity.ok(new MessageResponse("登出成功"));
        }
    }

    /**
     * 请求重置密码
     * @param request 包含邮箱的请求
     * @return 请求结果
     */
    @PostMapping("/request-reset-password")
    public ResponseEntity<?> requestResetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("请求重置密码: {}", email);

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("邮箱不能为空"));
        }

        try {
            // 检查邮箱是否存在
            if (!authService.existsByEmail(email)) {
                log.warn("请求重置密码失败，邮箱不存在: {}", email);
                // 为了安全，我们不告诉用户邮箱是否存在
                return ResponseEntity.ok(new MessageResponse("如果该邮箱已注册，我们将向您发送重置密码的链接"));
            }

            // TODO: 生成重置令牌并发送邮件
            // 这里应该生成一个唯一的令牌，保存到数据库，并发送包含重置链接的邮件
            // 由于邮件发送功能需要额外配置，这里只模拟发送邮件
            log.info("模拟发送重置密码邮件到: {}", email);

            return ResponseEntity.ok(new MessageResponse("如果该邮箱已注册，我们将向您发送重置密码的链接"));
        } catch (Exception e) {
            log.error("请求重置密码失败: {}, 错误: {}", email, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("请求重置密码失败: " + e.getMessage()));
        }
    }

    /**
     * 重置密码
     * @param request 包含令牌、邮箱和新密码的请求
     * @return 重置结果
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String email = request.get("email");
        String password = request.get("password");

        log.info("重置密码: {}", email);

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("令牌不能为空"));
        }

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("邮箱不能为空"));
        }

        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("密码不能为空"));
        }

        try {
            // TODO: 验证令牌并重置密码
            // 这里应该验证令牌是否有效，然后重置用户密码
            // 由于我们没有实现令牌生成和验证，这里只模拟重置密码
            log.info("模拟重置密码: {}", email);

            return ResponseEntity.ok(new MessageResponse("密码重置成功"));
        } catch (Exception e) {
            log.error("重置密码失败: {}, 错误: {}", email, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("重置密码失败: " + e.getMessage()));
        }
    }
}
