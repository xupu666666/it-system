package com.ict.system.controller.v2;

import com.ict.system.model.v2.LoginHistory;
import com.ict.system.payload.response.MessageResponse;
import com.ict.system.service.v2.LoginHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("loginHistoryControllerV2")
@RequestMapping("/api/system/login-history")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LoginHistoryController {

    private static final Logger log = LoggerFactory.getLogger(LoginHistoryController.class);

    @Autowired
    @Qualifier("loginHistoryServiceV2")
    private LoginHistoryService loginHistoryService;

    /**
     * 获取所有登录历史
     * @param page 页码
     * @param size 每页大小
     * @param sort 排序字段
     * @param direction 排序方向
     * @return 登录历史列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getAllLoginHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "loginTime") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        log.info("获取所有登录历史, 页码: {}, 每页大小: {}, 排序: {}, 方向: {}", page, size, sort, direction);

        try {
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<LoginHistory> historyPage = loginHistoryService.getUserLoginHistory(null, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("loginHistory", historyPage.getContent());
            response.put("currentPage", historyPage.getNumber());
            response.put("totalItems", historyPage.getTotalElements());
            response.put("totalPages", historyPage.getTotalPages());

            log.info("获取登录历史成功, 总数: {}", historyPage.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取登录历史失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取登录历史失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户的登录历史
     * @param username 用户名
     * @param page 页码
     * @param size 每页大小
     * @return 登录历史列表
     */
    @GetMapping("/user/{username}")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getUserLoginHistory(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取用户登录历史: {}, 页码: {}, 每页大小: {}", username, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "loginTime"));

            Page<LoginHistory> historyPage = loginHistoryService.getUserLoginHistory(username, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("loginHistory", historyPage.getContent());
            response.put("currentPage", historyPage.getNumber());
            response.put("totalItems", historyPage.getTotalElements());
            response.put("totalPages", historyPage.getTotalPages());

            log.info("获取用户登录历史成功, 用户: {}, 总数: {}", username, historyPage.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取用户登录历史失败, 用户: {}, 错误: {}", username, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取用户登录历史失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户最近的登录历史
     * @param username 用户名
     * @return 登录历史列表
     */
    @GetMapping("/user/{username}/recent")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getRecentLoginHistory(@PathVariable String username) {
        log.info("获取用户最近的登录历史: {}", username);

        try {
            List<LoginHistory> history = loginHistoryService.getRecentLoginHistory(username);

            log.info("获取用户最近的登录历史成功, 用户: {}, 数量: {}", username, history.size());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("获取用户最近的登录历史失败, 用户: {}, 错误: {}", username, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取用户最近的登录历史失败: " + e.getMessage()));
        }
    }

    /**
     * 获取特定时间段内的登录历史
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录历史列表
     */
    @GetMapping("/time-range")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getLoginHistoryBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime) {
        log.info("获取特定时间段内的登录历史: {} - {}", startTime, endTime);

        try {
            List<LoginHistory> history = loginHistoryService.getLoginHistoryBetween(startTime, endTime);

            log.info("获取特定时间段内的登录历史成功, 数量: {}", history.size());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("获取特定时间段内的登录历史失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取特定时间段内的登录历史失败: " + e.getMessage()));
        }
    }

    /**
     * 获取特定状态的登录历史
     * @param status 状态
     * @return 登录历史列表
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getLoginHistoryByStatus(@PathVariable String status) {
        log.info("获取特定状态的登录历史: {}", status);

        try {
            List<LoginHistory> history = loginHistoryService.getLoginHistoryByStatus(status);

            log.info("获取特定状态的登录历史成功, 状态: {}, 数量: {}", status, history.size());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("获取特定状态的登录历史失败, 状态: {}, 错误: {}", status, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取特定状态的登录历史失败: " + e.getMessage()));
        }
    }

    /**
     * 获取特定IP地址的登录历史
     * @param ipAddress IP地址
     * @return 登录历史列表
     */
    @GetMapping("/ip/{ipAddress}")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getLoginHistoryByIpAddress(@PathVariable String ipAddress) {
        log.info("获取特定IP地址的登录历史: {}", ipAddress);

        try {
            List<LoginHistory> history = loginHistoryService.getLoginHistoryByIpAddress(ipAddress);

            log.info("获取特定IP地址的登录历史成功, IP: {}, 数量: {}", ipAddress, history.size());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("获取特定IP地址的登录历史失败, IP: {}, 错误: {}", ipAddress, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取特定IP地址的登录历史失败: " + e.getMessage()));
        }
    }

    /**
     * 清理旧的登录历史
     * @param days 保留天数
     * @return 清理结果
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> cleanupOldLoginHistory(@RequestParam(defaultValue = "90") int days) {
        log.info("清理{}天前的登录历史", days);

        try {
            int count = loginHistoryService.cleanupOldLoginHistory(days);

            log.info("清理旧的登录历史成功, 清理数量: {}", count);
            return ResponseEntity.ok(new MessageResponse("成功清理" + count + "条旧的登录历史记录"));
        } catch (Exception e) {
            log.error("清理旧的登录历史失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("清理旧的登录历史失败: " + e.getMessage()));
        }
    }
}
