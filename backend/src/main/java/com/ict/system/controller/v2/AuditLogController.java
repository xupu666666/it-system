package com.ict.system.controller.v2;

import com.ict.system.model.v2.AuditLog;
import com.ict.system.payload.response.MessageResponse;
import com.ict.system.service.v2.AuditLogService;
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

@RestController("auditLogControllerV2")
@RequestMapping("/api/system/audit-logs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuditLogController {

    private static final Logger log = LoggerFactory.getLogger(AuditLogController.class);

    @Autowired
    @Qualifier("auditLogServiceV2")
    private AuditLogService auditLogService;

    /**
     * 获取所有审计日志
     * @param page 页码
     * @param size 每页大小
     * @param sort 排序字段
     * @param direction 排序方向
     * @return 审计日志列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        log.info("获取所有审计日志, 页码: {}, 每页大小: {}, 排序: {}, 方向: {}", page, size, sort, direction);

        try {
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<AuditLog> logsPage = auditLogService.getUserAuditLogs(null, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("auditLogs", logsPage.getContent());
            response.put("currentPage", logsPage.getNumber());
            response.put("totalItems", logsPage.getTotalElements());
            response.put("totalPages", logsPage.getTotalPages());

            log.info("获取审计日志成功, 总数: {}", logsPage.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取审计日志失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取审计日志失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户的审计日志
     * @param username 用户名
     * @param page 页码
     * @param size 每页大小
     * @return 审计日志列表
     */
    @GetMapping("/user/{username}")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getUserAuditLogs(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取用户审计日志: {}, 页码: {}, 每页大小: {}", username, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

            Page<AuditLog> logsPage = auditLogService.getUserAuditLogs(username, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("auditLogs", logsPage.getContent());
            response.put("currentPage", logsPage.getNumber());
            response.put("totalItems", logsPage.getTotalElements());
            response.put("totalPages", logsPage.getTotalPages());

            log.info("获取用户审计日志成功, 用户: {}, 总数: {}", username, logsPage.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取用户审计日志失败, 用户: {}, 错误: {}", username, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取用户审计日志失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户最近的审计日志
     * @param username 用户名
     * @return 审计日志列表
     */
    @GetMapping("/user/{username}/recent")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getRecentAuditLogs(@PathVariable String username) {
        log.info("获取用户最近的审计日志: {}", username);

        try {
            List<AuditLog> logs = auditLogService.getRecentAuditLogs(username);

            log.info("获取用户最近的审计日志成功, 用户: {}, 数量: {}", username, logs.size());
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("获取用户最近的审计日志失败, 用户: {}, 错误: {}", username, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取用户最近的审计日志失败: " + e.getMessage()));
        }
    }

    /**
     * 获取特定时间段内的审计日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审计日志列表
     */
    @GetMapping("/time-range")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getAuditLogsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime) {
        log.info("获取特定时间段内的审计日志: {} - {}", startTime, endTime);

        try {
            List<AuditLog> logs = auditLogService.getAuditLogsBetween(startTime, endTime);

            log.info("获取特定时间段内的审计日志成功, 数量: {}", logs.size());
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("获取特定时间段内的审计日志失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取特定时间段内的审计日志失败: " + e.getMessage()));
        }
    }

    /**
     * 获取特定操作类型的审计日志
     * @param action 操作类型
     * @return 审计日志列表
     */
    @GetMapping("/action/{action}")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getAuditLogsByAction(@PathVariable String action) {
        log.info("获取特定操作类型的审计日志: {}", action);

        try {
            List<AuditLog> logs = auditLogService.getAuditLogsByAction(action);

            log.info("获取特定操作类型的审计日志成功, 操作类型: {}, 数量: {}", action, logs.size());
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("获取特定操作类型的审计日志失败, 操作类型: {}, 错误: {}", action, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取特定操作类型的审计日志失败: " + e.getMessage()));
        }
    }

    /**
     * 获取特定资源类型的审计日志
     * @param resourceType 资源类型
     * @return 审计日志列表
     */
    @GetMapping("/resource-type/{resourceType}")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getAuditLogsByResourceType(@PathVariable String resourceType) {
        log.info("获取特定资源类型的审计日志: {}", resourceType);

        try {
            List<AuditLog> logs = auditLogService.getAuditLogsByResourceType(resourceType);

            log.info("获取特定资源类型的审计日志成功, 资源类型: {}, 数量: {}", resourceType, logs.size());
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("获取特定资源类型的审计日志失败, 资源类型: {}, 错误: {}", resourceType, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取特定资源类型的审计日志失败: " + e.getMessage()));
        }
    }

    /**
     * 获取特定资源ID的审计日志
     * @param resourceId 资源ID
     * @return 审计日志列表
     */
    @GetMapping("/resource-id/{resourceId}")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getAuditLogsByResourceId(@PathVariable String resourceId) {
        log.info("获取特定资源ID的审计日志: {}", resourceId);

        try {
            List<AuditLog> logs = auditLogService.getAuditLogsByResourceId(resourceId);

            log.info("获取特定资源ID的审计日志成功, 资源ID: {}, 数量: {}", resourceId, logs.size());
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("获取特定资源ID的审计日志失败, 资源ID: {}, 错误: {}", resourceId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取特定资源ID的审计日志失败: " + e.getMessage()));
        }
    }

    /**
     * 获取特定IP地址的审计日志
     * @param ipAddress IP地址
     * @return 审计日志列表
     */
    @GetMapping("/ip/{ipAddress}")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getAuditLogsByIpAddress(@PathVariable String ipAddress) {
        log.info("获取特定IP地址的审计日志: {}", ipAddress);

        try {
            List<AuditLog> logs = auditLogService.getAuditLogsByIpAddress(ipAddress);

            log.info("获取特定IP地址的审计日志成功, IP: {}, 数量: {}", ipAddress, logs.size());
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("获取特定IP地址的审计日志失败, IP: {}, 错误: {}", ipAddress, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取特定IP地址的审计日志失败: " + e.getMessage()));
        }
    }

    /**
     * 清理旧的审计日志
     * @param days 保留天数
     * @return 清理结果
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> cleanupOldAuditLogs(@RequestParam(defaultValue = "90") int days) {
        log.info("清理{}天前的审计日志", days);

        try {
            int count = auditLogService.cleanupOldAuditLogs(days);

            log.info("清理旧的审计日志成功, 清理数量: {}", count);
            return ResponseEntity.ok(new MessageResponse("成功清理" + count + "条旧的审计日志记录"));
        } catch (Exception e) {
            log.error("清理旧的审计日志失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("清理旧的审计日志失败: " + e.getMessage()));
        }
    }
}
