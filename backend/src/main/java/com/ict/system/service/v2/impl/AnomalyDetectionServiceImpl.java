package com.ict.system.service.v2.impl;

import com.ict.system.model.v2.LoginHistory;
import com.ict.system.repository.v2.LoginHistoryRepository;
import com.ict.system.service.v2.AnomalyDetectionService;
import com.ict.system.service.v2.IpRestrictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("anomalyDetectionServiceV2")
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionServiceImpl.class);

    @Autowired
    @Qualifier("loginHistoryRepositoryV2")
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    @Qualifier("ipRestrictionServiceV2")
    private IpRestrictionService ipRestrictionService;

    @Override
    public boolean detectAnomalousLogin(String username, String ipAddress, String userAgent) {
        log.info("检测异常登录: {}, IP: {}", username, ipAddress);
        
        // 如果IP在白名单中，不视为异常
        if (ipRestrictionService.isWhitelisted(ipAddress)) {
            log.info("IP在白名单中，不视为异常: {}", ipAddress);
            return false;
        }
        
        // 如果IP在黑名单中，视为异常
        if (ipRestrictionService.isBlacklisted(ipAddress)) {
            log.warn("IP在黑名单中，视为异常: {}", ipAddress);
            return true;
        }
        
        // 获取用户的登录历史
        List<LoginHistory> loginHistory = loginHistoryRepository.findTop10ByUsernameOrderByLoginTimeDesc(username);
        
        // 如果用户没有登录历史，不视为异常
        if (loginHistory.isEmpty()) {
            log.info("用户没有登录历史，不视为异常: {}", username);
            return false;
        }
        
        // 检查IP地址是否是用户常用的
        boolean isCommonIp = isCommonIpAddress(loginHistory, ipAddress);
        
        // 检查用户代理是否是用户常用的
        boolean isCommonUserAgent = isCommonUserAgent(loginHistory, userAgent);
        
        // 如果IP地址和用户代理都不是常用的，视为异常
        if (!isCommonIp && !isCommonUserAgent) {
            log.warn("IP地址和用户代理都不是常用的，视为异常: {}, IP: {}", username, ipAddress);
            return true;
        }
        
        // 如果IP地址不是常用的，但用户代理是常用的，可能是用户在不同地点登录
        if (!isCommonIp && isCommonUserAgent) {
            log.info("IP地址不是常用的，但用户代理是常用的，可能是用户在不同地点登录: {}, IP: {}", username, ipAddress);
            return false;
        }
        
        // 如果IP地址是常用的，但用户代理不是常用的，可能是用户使用了不同的设备
        if (isCommonIp && !isCommonUserAgent) {
            log.info("IP地址是常用的，但用户代理不是常用的，可能是用户使用了不同的设备: {}, IP: {}", username, ipAddress);
            return false;
        }
        
        // 如果IP地址和用户代理都是常用的，不视为异常
        log.info("IP地址和用户代理都是常用的，不视为异常: {}, IP: {}", username, ipAddress);
        return false;
    }

    /**
     * 检查IP地址是否是用户常用的
     * @param loginHistory 登录历史
     * @param ipAddress IP地址
     * @return 是否常用
     */
    private boolean isCommonIpAddress(List<LoginHistory> loginHistory, String ipAddress) {
        // 统计IP地址出现的次数
        Map<String, Long> ipCounts = loginHistory.stream()
                .collect(Collectors.groupingBy(LoginHistory::getIpAddress, Collectors.counting()));
        
        // 如果IP地址出现次数超过总次数的30%，视为常用
        long totalLogins = loginHistory.size();
        long ipCount = ipCounts.getOrDefault(ipAddress, 0L);
        
        return (double) ipCount / totalLogins >= 0.3;
    }

    /**
     * 检查用户代理是否是用户常用的
     * @param loginHistory 登录历史
     * @param userAgent 用户代理
     * @return 是否常用
     */
    private boolean isCommonUserAgent(List<LoginHistory> loginHistory, String userAgent) {
        // 统计用户代理出现的次数
        Map<String, Long> userAgentCounts = loginHistory.stream()
                .collect(Collectors.groupingBy(LoginHistory::getUserAgent, Collectors.counting()));
        
        // 如果用户代理出现次数超过总次数的30%，视为常用
        long totalLogins = loginHistory.size();
        long userAgentCount = userAgentCounts.getOrDefault(userAgent, 0L);
        
        return (double) userAgentCount / totalLogins >= 0.3;
    }

    @Override
    public List<String> getUserCommonIpAddresses(String username) {
        log.info("获取用户的常用IP地址: {}", username);
        
        // 获取用户的登录历史
        List<LoginHistory> loginHistory = loginHistoryRepository.findByUsername(username);
        
        // 统计IP地址出现的次数
        Map<String, Long> ipCounts = loginHistory.stream()
                .collect(Collectors.groupingBy(LoginHistory::getIpAddress, Collectors.counting()));
        
        // 按出现次数排序
        return ipCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserCommonDevices(String username) {
        log.info("获取用户的常用设备: {}", username);
        
        // 获取用户的登录历史
        List<LoginHistory> loginHistory = loginHistoryRepository.findByUsername(username);
        
        // 统计用户代理出现的次数
        Map<String, Long> userAgentCounts = loginHistory.stream()
                .collect(Collectors.groupingBy(LoginHistory::getUserAgent, Collectors.counting()));
        
        // 按出现次数排序
        return userAgentCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getUserLoginPattern(String username) {
        log.info("获取用户的登录模式: {}", username);
        
        // 获取用户的登录历史
        List<LoginHistory> loginHistory = loginHistoryRepository.findByUsername(username);
        
        Map<String, Object> pattern = new HashMap<>();
        
        // 统计IP地址出现的次数
        Map<String, Long> ipCounts = loginHistory.stream()
                .collect(Collectors.groupingBy(LoginHistory::getIpAddress, Collectors.counting()));
        
        // 统计用户代理出现的次数
        Map<String, Long> userAgentCounts = loginHistory.stream()
                .collect(Collectors.groupingBy(LoginHistory::getUserAgent, Collectors.counting()));
        
        // 统计登录时间分布
        Map<Integer, Long> hourCounts = loginHistory.stream()
                .collect(Collectors.groupingBy(history -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(history.getLoginTime());
                    return calendar.get(Calendar.HOUR_OF_DAY);
                }, Collectors.counting()));
        
        // 统计登录状态分布
        Map<String, Long> statusCounts = loginHistory.stream()
                .collect(Collectors.groupingBy(LoginHistory::getStatus, Collectors.counting()));
        
        pattern.put("ipAddresses", ipCounts);
        pattern.put("userAgents", userAgentCounts);
        pattern.put("hourDistribution", hourCounts);
        pattern.put("statusDistribution", statusCounts);
        
        return pattern;
    }

    @Override
    public List<Map<String, Object>> getAllAnomalousLogins() {
        // 这里应该从数据库中获取所有异常登录记录
        // 由于我们没有专门的异常登录表，这里只是模拟返回
        log.info("获取所有异常登录");
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getUserAnomalousLogins(String username) {
        // 这里应该从数据库中获取用户的异常登录记录
        // 由于我们没有专门的异常登录表，这里只是模拟返回
        log.info("获取用户的异常登录: {}", username);
        return new ArrayList<>();
    }

    @Override
    public boolean markAnomalousLoginAsHandled(Long id) {
        // 这里应该将异常登录记录标记为已处理
        // 由于我们没有专门的异常登录表，这里只是模拟返回
        log.info("标记异常登录为已处理: {}", id);
        return true;
    }

    @Override
    public int cleanupOldAnomalousLogins(int days) {
        // 这里应该清理旧的异常登录记录
        // 由于我们没有专门的异常登录表，这里只是模拟返回
        log.info("清理{}天前的异常登录记录", days);
        return 0;
    }
}
