package com.ict.system.service.v2.impl;

import com.ict.system.model.v2.LoginHistory;
import com.ict.system.repository.v2.LoginHistoryRepository;
import com.ict.system.service.v2.LoginHistoryService;
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

@Service("loginHistoryServiceV2")
public class LoginHistoryServiceImpl implements LoginHistoryService {

    private static final Logger log = LoggerFactory.getLogger(LoginHistoryServiceImpl.class);

    @Autowired
    @Qualifier("loginHistoryRepositoryV2")
    private LoginHistoryRepository loginHistoryRepository;

    @Override
    @Transactional
    public LoginHistory recordLoginSuccess(String username, String ipAddress, String userAgent) {
        log.info("记录登录成功: {}, IP: {}", username, ipAddress);
        
        LoginHistory loginHistory = new LoginHistory(username, ipAddress, userAgent, "SUCCESS");
        return loginHistoryRepository.save(loginHistory);
    }

    @Override
    @Transactional
    public LoginHistory recordLoginFailure(String username, String ipAddress, String userAgent, String reason) {
        log.info("记录登录失败: {}, IP: {}, 原因: {}", username, ipAddress, reason);
        
        LoginHistory loginHistory = new LoginHistory(username, ipAddress, userAgent, "FAILED", reason);
        return loginHistoryRepository.save(loginHistory);
    }

    @Override
    public List<LoginHistory> getUserLoginHistory(String username) {
        log.info("获取用户登录历史: {}", username);
        return loginHistoryRepository.findByUsername(username);
    }

    @Override
    public Page<LoginHistory> getUserLoginHistory(String username, Pageable pageable) {
        log.info("分页获取用户登录历史: {}, 页码: {}, 每页大小: {}", username, pageable.getPageNumber(), pageable.getPageSize());
        return loginHistoryRepository.findByUsername(username, pageable);
    }

    @Override
    public List<LoginHistory> getLoginHistoryBetween(Date startTime, Date endTime) {
        log.info("获取特定时间段内的登录历史: {} - {}", startTime, endTime);
        return loginHistoryRepository.findByLoginTimeBetween(startTime, endTime);
    }

    @Override
    public List<LoginHistory> getUserLoginHistoryBetween(String username, Date startTime, Date endTime) {
        log.info("获取特定时间段内特定用户的登录历史: {}, {} - {}", username, startTime, endTime);
        return loginHistoryRepository.findByUsernameAndLoginTimeBetween(username, startTime, endTime);
    }

    @Override
    public List<LoginHistory> getLoginHistoryByStatus(String status) {
        log.info("获取特定状态的登录历史: {}", status);
        return loginHistoryRepository.findByStatus(status);
    }

    @Override
    public List<LoginHistory> getLoginHistoryByIpAddress(String ipAddress) {
        log.info("获取特定IP地址的登录历史: {}", ipAddress);
        return loginHistoryRepository.findByIpAddress(ipAddress);
    }

    @Override
    public List<LoginHistory> getRecentLoginHistory(String username) {
        log.info("获取用户最近的登录历史: {}", username);
        return loginHistoryRepository.findTop10ByUsernameOrderByLoginTimeDesc(username);
    }

    @Override
    public List<LoginHistory> getLoginFailures(String username) {
        log.info("获取用户的登录失败记录: {}", username);
        return loginHistoryRepository.findByUsernameAndStatusOrderByLoginTimeDesc(username, "FAILED");
    }

    @Override
    public long countLoginFailures(String username, Date startTime, Date endTime) {
        log.info("统计特定时间段内的登录失败次数: {}, {} - {}", username, startTime, endTime);
        return loginHistoryRepository.countByUsernameAndStatusAndLoginTimeBetween(username, "FAILED", startTime, endTime);
    }

    @Override
    @Transactional
    public int cleanupOldLoginHistory(int days) {
        log.info("清理{}天前的登录历史", days);
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        Date cutoffDate = calendar.getTime();
        
        List<LoginHistory> oldRecords = loginHistoryRepository.findByLoginTimeBetween(new Date(0), cutoffDate);
        int count = oldRecords.size();
        
        if (count > 0) {
            loginHistoryRepository.deleteAll(oldRecords);
            log.info("已清理{}条旧的登录历史记录", count);
        } else {
            log.info("没有需要清理的旧登录历史记录");
        }
        
        return count;
    }
}
