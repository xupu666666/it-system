package com.ict.system.controller.v2;

import com.ict.system.model.v2.UserSession;
import com.ict.system.payload.response.MessageResponse;
import com.ict.system.service.v2.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("userSessionControllerV2")
@RequestMapping("/api/system/sessions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserSessionController {

    private static final Logger log = LoggerFactory.getLogger(UserSessionController.class);

    @Autowired
    @Qualifier("userSessionServiceV2")
    private UserSessionService userSessionService;

    /**
     * 获取所有活跃会话
     * @return 会话列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getAllActiveSessions() {
        log.info("获取所有活跃会话");

        try {
            List<UserSession> sessions = userSessionService.getAllActiveSessions();

            Map<String, Object> response = new HashMap<>();
            response.put("sessions", sessions);
            response.put("count", sessions.size());

            log.info("获取活跃会话成功, 数量: {}", sessions.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取活跃会话失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取活跃会话失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户的活跃会话
     * @param username 用户名
     * @return 会话列表
     */
    @GetMapping("/user/{username}")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getUserActiveSessions(@PathVariable String username) {
        log.info("获取用户的活跃会话: {}", username);

        try {
            List<UserSession> sessions = userSessionService.getUserActiveSessions(username);

            Map<String, Object> response = new HashMap<>();
            response.put("sessions", sessions);
            response.put("count", sessions.size());

            log.info("获取用户活跃会话成功, 用户: {}, 数量: {}", username, sessions.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取用户活跃会话失败, 用户: {}, 错误: {}", username, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取用户活跃会话失败: " + e.getMessage()));
        }
    }

    /**
     * 使会话失效
     * @param token 令牌
     * @return 失效结果
     */
    @PostMapping("/invalidate")
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> invalidateSession(@RequestParam String token) {
        log.info("使会话失效, 令牌: {}", token);

        try {
            boolean result = userSessionService.invalidateSession(token);

            if (result) {
                log.info("会话已失效, 令牌: {}", token);
                return ResponseEntity.ok(new MessageResponse("会话已失效"));
            } else {
                log.warn("会话不存在或已失效, 令牌: {}", token);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("会话不存在或已失效"));
            }
        } catch (Exception e) {
            log.error("使会话失效失败, 令牌: {}, 错误: {}", token, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("使会话失效失败: " + e.getMessage()));
        }
    }

    /**
     * 使用户的所有会话失效
     * @param username 用户名
     * @return 失效结果
     */
    @PostMapping("/user/{username}/invalidate")
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> invalidateUserSessions(@PathVariable String username) {
        log.info("使用户的所有会话失效: {}", username);

        try {
            int count = userSessionService.invalidateUserSessions(username);

            log.info("用户的所有会话已失效: {}, 数量: {}", username, count);
            return ResponseEntity.ok(new MessageResponse("成功使" + count + "个会话失效"));
        } catch (Exception e) {
            log.error("使用户的所有会话失效失败, 用户: {}, 错误: {}", username, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("使用户的所有会话失效失败: " + e.getMessage()));
        }
    }

    /**
     * 获取特定IP地址的活跃会话
     * @param ipAddress IP地址
     * @return 会话列表
     */
    @GetMapping("/ip/{ipAddress}")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getSessionsByIpAddress(@PathVariable String ipAddress) {
        log.info("获取特定IP地址的活跃会话: {}", ipAddress);

        try {
            List<UserSession> sessions = userSessionService.getSessionsByIpAddress(ipAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("sessions", sessions);
            response.put("count", sessions.size());

            log.info("获取特定IP地址的活跃会话成功, IP: {}, 数量: {}", ipAddress, sessions.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取特定IP地址的活跃会话失败, IP: {}, 错误: {}", ipAddress, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取特定IP地址的活跃会话失败: " + e.getMessage()));
        }
    }

    /**
     * 清理过期会话
     * @return 清理结果
     */
    @PostMapping("/cleanup-expired")
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> cleanupExpiredSessions() {
        log.info("清理过期会话");

        try {
            int count = userSessionService.cleanupExpiredSessions();

            log.info("过期会话已清理, 数量: {}", count);
            return ResponseEntity.ok(new MessageResponse("成功清理" + count + "个过期会话"));
        } catch (Exception e) {
            log.error("清理过期会话失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("清理过期会话失败: " + e.getMessage()));
        }
    }

    /**
     * 清理不活跃会话
     * @param minutes 不活跃时间（分钟）
     * @return 清理结果
     */
    @PostMapping("/cleanup-inactive")
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> cleanupInactiveSessions(@RequestParam(defaultValue = "60") int minutes) {
        log.info("清理不活跃会话, 不活跃时间: {}分钟", minutes);

        try {
            int count = userSessionService.cleanupInactiveSessions(minutes);

            log.info("不活跃会话已清理, 数量: {}", count);
            return ResponseEntity.ok(new MessageResponse("成功清理" + count + "个不活跃会话"));
        } catch (Exception e) {
            log.error("清理不活跃会话失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("清理不活跃会话失败: " + e.getMessage()));
        }
    }
}
