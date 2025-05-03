package com.ict.system.service.v2;

import com.ict.system.model.v2.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface AuditLogService {

    /**
     * 记录审计日志
     * @param username 用户名
     * @param action 操作类型
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param details 详细信息
     * @param ipAddress IP地址
     * @return 审计日志记录
     */
    AuditLog log(String username, String action, String resourceType, String resourceId, String details, String ipAddress);

    /**
     * 获取用户的审计日志
     * @param username 用户名
     * @return 审计日志列表
     */
    List<AuditLog> getUserAuditLogs(String username);

    /**
     * 分页获取用户的审计日志
     * @param username 用户名
     * @param pageable 分页参数
     * @return 审计日志分页结果
     */
    Page<AuditLog> getUserAuditLogs(String username, Pageable pageable);

    /**
     * 获取特定时间段内的审计日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审计日志列表
     */
    List<AuditLog> getAuditLogsBetween(Date startTime, Date endTime);

    /**
     * 获取特定时间段内特定用户的审计日志
     * @param username 用户名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审计日志列表
     */
    List<AuditLog> getUserAuditLogsBetween(String username, Date startTime, Date endTime);

    /**
     * 获取特定操作类型的审计日志
     * @param action 操作类型
     * @return 审计日志列表
     */
    List<AuditLog> getAuditLogsByAction(String action);

    /**
     * 获取特定资源类型的审计日志
     * @param resourceType 资源类型
     * @return 审计日志列表
     */
    List<AuditLog> getAuditLogsByResourceType(String resourceType);

    /**
     * 获取特定资源ID的审计日志
     * @param resourceId 资源ID
     * @return 审计日志列表
     */
    List<AuditLog> getAuditLogsByResourceId(String resourceId);

    /**
     * 获取特定IP地址的审计日志
     * @param ipAddress IP地址
     * @return 审计日志列表
     */
    List<AuditLog> getAuditLogsByIpAddress(String ipAddress);

    /**
     * 获取用户最近的审计日志
     * @param username 用户名
     * @return 审计日志列表
     */
    List<AuditLog> getRecentAuditLogs(String username);

    /**
     * 获取特定资源类型和操作的审计日志
     * @param resourceType 资源类型
     * @param action 操作类型
     * @return 审计日志列表
     */
    List<AuditLog> getAuditLogsByResourceTypeAndAction(String resourceType, String action);

    /**
     * 清理旧的审计日志
     * @param days 保留天数
     * @return 清理的记录数
     */
    int cleanupOldAuditLogs(int days);
}
