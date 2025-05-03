package com.ict.system.repository.v2;

import com.ict.system.model.v2.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository("auditLogRepositoryV2")
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // 根据用户名查找审计日志
    List<AuditLog> findByUsername(String username);

    // 分页查询用户的审计日志
    Page<AuditLog> findByUsername(String username, Pageable pageable);

    // 查找特定时间段内的审计日志
    List<AuditLog> findByTimestampBetween(Date startTime, Date endTime);

    // 查找特定时间段内特定用户的审计日志
    List<AuditLog> findByUsernameAndTimestampBetween(String username, Date startTime, Date endTime);

    // 查找特定操作类型的审计日志
    List<AuditLog> findByAction(String action);

    // 查找特定资源类型的审计日志
    List<AuditLog> findByResourceType(String resourceType);

    // 查找特定资源ID的审计日志
    List<AuditLog> findByResourceId(String resourceId);

    // 查找特定IP地址的审计日志
    List<AuditLog> findByIpAddress(String ipAddress);

    // 查找最近的审计日志
    List<AuditLog> findTop10ByUsernameOrderByTimestampDesc(String username);

    // 查找特定资源类型和操作的审计日志
    List<AuditLog> findByResourceTypeAndAction(String resourceType, String action);
}
