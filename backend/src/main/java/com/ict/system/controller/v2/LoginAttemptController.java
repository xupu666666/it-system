package com.ict.system.controller.v2;

import com.ict.system.payload.response.MessageResponse;
import com.ict.system.service.v2.LoginAttemptService;
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

@RestController("loginAttemptControllerV2")
@RequestMapping("/api/system/login-attempts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LoginAttemptController {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptController.class);

    @Autowired
    @Qualifier("loginAttemptServiceV2")
    private LoginAttemptService loginAttemptService;

    /**
     * 获取所有锁定的用户
     * @return 锁定的用户列表
     */
    @GetMapping("/locked")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getLockedUsers() {
        log.info("获取所有锁定的用户");

        try {
            List<String> lockedUsers = loginAttemptService.getLockedUsers();

            Map<String, Object> response = new HashMap<>();
            response.put("lockedUsers", lockedUsers);
            response.put("count", lockedUsers.size());

            log.info("获取锁定的用户成功, 数量: {}", lockedUsers.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取锁定的用户失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取锁定的用户失败: " + e.getMessage()));
        }
    }

    /**
     * 检查用户是否被锁定
     * @param username 用户名
     * @param ipAddress IP地址
     * @return 锁定状态
     */
    @GetMapping("/check")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> checkLockStatus(
            @RequestParam String username,
            @RequestParam(required = false) String ipAddress) {
        log.info("检查用户锁定状态: {}, IP: {}", username, ipAddress);

        try {
            // 如果未提供IP地址，使用默认值
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = "127.0.0.1";
            }

            boolean locked = loginAttemptService.isLocked(username, ipAddress);
            int remainingAttempts = loginAttemptService.getRemainingAttempts(username, ipAddress);
            int lockTimeRemaining = loginAttemptService.getLockTimeRemaining(username, ipAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("ipAddress", ipAddress);
            response.put("locked", locked);
            response.put("remainingAttempts", remainingAttempts);
            response.put("lockTimeRemaining", lockTimeRemaining);

            log.info("检查用户锁定状态成功: {}, IP: {}, 锁定: {}, 剩余尝试次数: {}, 剩余锁定时间: {}分钟",
                    username, ipAddress, locked, remainingAttempts, lockTimeRemaining);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查用户锁定状态失败: {}, IP: {}, 错误: {}", username, ipAddress, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("检查用户锁定状态失败: " + e.getMessage()));
        }
    }

    /**
     * 锁定用户
     * @param username 用户名
     * @param ipAddress IP地址
     * @param minutes 锁定时间（分钟）
     * @return 锁定结果
     */
    @PostMapping("/lock")
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> lockUser(
            @RequestParam String username,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(defaultValue = "30") int minutes) {
        log.info("锁定用户: {}, IP: {}, 时长: {}分钟", username, ipAddress, minutes);

        try {
            // 如果未提供IP地址，使用默认值
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = "127.0.0.1";
            }

            loginAttemptService.lockUser(username, ipAddress, minutes);

            log.info("锁定用户成功: {}, IP: {}, 时长: {}分钟", username, ipAddress, minutes);
            return ResponseEntity.ok(new MessageResponse("用户已锁定"));
        } catch (Exception e) {
            log.error("锁定用户失败: {}, IP: {}, 错误: {}", username, ipAddress, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("锁定用户失败: " + e.getMessage()));
        }
    }

    /**
     * 解锁用户
     * @param username 用户名
     * @param ipAddress IP地址
     * @return 解锁结果
     */
    @PostMapping("/unlock")
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> unlockUser(
            @RequestParam String username,
            @RequestParam(required = false) String ipAddress) {
        log.info("解锁用户: {}, IP: {}", username, ipAddress);

        try {
            // 如果未提供IP地址，使用默认值
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = "127.0.0.1";
            }

            loginAttemptService.unlockUser(username, ipAddress);

            log.info("解锁用户成功: {}, IP: {}", username, ipAddress);
            return ResponseEntity.ok(new MessageResponse("用户已解锁"));
        } catch (Exception e) {
            log.error("解锁用户失败: {}, IP: {}, 错误: {}", username, ipAddress, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("解锁用户失败: " + e.getMessage()));
        }
    }

    /**
     * 解锁所有过期的锁定
     * @return 解锁结果
     */
    @PostMapping("/unlock-expired")
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> unlockExpiredLocks() {
        log.info("解锁所有过期的锁定");

        try {
            int count = loginAttemptService.unlockExpiredLocks();

            log.info("解锁过期的锁定成功, 数量: {}", count);
            return ResponseEntity.ok(new MessageResponse("成功解锁" + count + "个过期的锁定"));
        } catch (Exception e) {
            log.error("解锁过期的锁定失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("解锁过期的锁定失败: " + e.getMessage()));
        }
    }

    /**
     * 清理旧的登录尝试记录
     * @param days 保留天数
     * @return 清理结果
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> cleanupOldAttempts(@RequestParam(defaultValue = "30") int days) {
        log.info("清理{}天前的登录尝试记录", days);

        try {
            int count = loginAttemptService.cleanupOldAttempts(days);

            log.info("清理旧的登录尝试记录成功, 清理数量: {}", count);
            return ResponseEntity.ok(new MessageResponse("成功清理" + count + "条旧的登录尝试记录"));
        } catch (Exception e) {
            log.error("清理旧的登录尝试记录失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("清理旧的登录尝试记录失败: " + e.getMessage()));
        }
    }
}
