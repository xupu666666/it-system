package com.ict.system.service.v2.impl;

import com.ict.system.model.v2.AuditLog;
import com.ict.system.repository.v2.AuditLogRepository;
import com.ict.system.service.v2.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service("auditLogServiceV2")
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    @Autowired
    @Qualifier("auditLogRepositoryV2")
    private AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public AuditLog log(String username, String action, String resourceType, String resourceId, String details, String ipAddress) {
        log.info("记录审计日志: 用户={}, 操作={}, 资源类型={}, 资源ID={}, IP={}", username, action, resourceType, resourceId, ipAddress);
        
        AuditLog auditLog = new AuditLog(username, action, resourceType, resourceId, details, ipAddress);
        return auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLog> getUserAuditLogs(String username) {
        log.info("获取用户审计日志: {}", username);
        return auditLogRepository.findByUsername(username);
    }

    @Override
    public Page<AuditLog> getUserAuditLogs(String username, Pageable pageable) {
        log.info("分页获取用户审计日志: {}, 页码: {}, 每页大小: {}", username, pageable.getPageNumber(), pageable.getPageSize());
        return auditLogRepository.findByUsername(username, pageable);
    }

    @Override
    public List<AuditLog> getAuditLogsBetween(Date startTime, Date endTime) {
        log.info("获取特定时间段内的审计日志: {} - {}", startTime, endTime);
        return auditLogRepository.findByTimestampBetween(startTime, endTime);
    }

    @Override
    public List<AuditLog> getUserAuditLogsBetween(String username, Date startTime, Date endTime) {
        log.info("获取特定时间段内特定用户的审计日志: {}, {} - {}", username, startTime, endTime);
        return auditLogRepository.findByUsernameAndTimestampBetween(username, startTime, endTime);
    }

    @Override
    public List<AuditLog> getAuditLogsByAction(String action) {
        log.info("获取特定操作类型的审计日志: {}", action);
        return auditLogRepository.findByAction(action);
    }

    @Override
    public List<AuditLog> getAuditLogsByResourceType(String resourceType) {
        log.info("获取特定资源类型的审计日志: {}", resourceType);
        return auditLogRepository.findByResourceType(resourceType);
    }

    @Override
    public List<AuditLog> getAuditLogsByResourceId(String resourceId) {
        log.info("获取特定资源ID的审计日志: {}", resourceId);
        return auditLogRepository.findByResourceId(resourceId);
    }

    @Override
    public List<AuditLog> getAuditLogsByIpAddress(String ipAddress) {
        log.info("获取特定IP地址的审计日志: {}", ipAddress);
        return auditLogRepository.findByIpAddress(ipAddress);
    }

    @Override
    public List<AuditLog> getRecentAuditLogs(String username) {
        log.info("获取用户最近的审计日志: {}", username);
        return auditLogRepository.findTop10ByUsernameOrderByTimestampDesc(username);
    }

    @Override
    public List<AuditLog> getAuditLogsByResourceTypeAndAction(String resourceType, String action) {
        log.info("获取特定资源类型和操作的审计日志: {}, {}", resourceType, action);
        return auditLogRepository.findByResourceTypeAndAction(resourceType, action);
    }

    @Override
    @Transactional
    public int cleanupOldAuditLogs(int days) {
        log.info("清理{}天前的审计日志", days);
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        Date cutoffDate = calendar.getTime();
        
        List<AuditLog> oldRecords = auditLogRepository.findByTimestampBetween(new Date(0), cutoffDate);
        int count = oldRecords.size();
        
        if (count > 0) {
            auditLogRepository.deleteAll(oldRecords);
            log.info("已清理{}条旧的审计日志记录", count);
        } else {
            log.info("没有需要清理的旧审计日志记录");
        }
        
        return count;
    }
}
