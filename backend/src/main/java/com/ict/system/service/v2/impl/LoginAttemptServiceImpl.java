package com.ict.system.service.v2.impl;

import com.ict.system.model.v2.LoginAttempt;
import com.ict.system.repository.v2.LoginAttemptRepository;
import com.ict.system.service.v2.LoginAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("loginAttemptServiceV2")
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptServiceImpl.class);

    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login.lock-duration:30}")
    private int lockDurationMinutes;

    @Autowired
    @Qualifier("loginAttemptRepositoryV2")
    private LoginAttemptRepository loginAttemptRepository;

    @Override
    @Transactional
    public boolean recordFailedAttempt(String username, String ipAddress) {
        log.info("记录登录失败尝试: {}, IP: {}", username, ipAddress);
        
        Optional<LoginAttempt> attemptOpt = loginAttemptRepository.findByUsernameAndIpAddress(username, ipAddress);
        LoginAttempt attempt;
        
        if (attemptOpt.isPresent()) {
            attempt = attemptOpt.get();
            
            // 检查是否已锁定
            if (attempt.isLocked() && !attempt.isLockExpired()) {
                log.info("用户已被锁定: {}, IP: {}", username, ipAddress);
                return true;
            }
            
            // 如果锁定已过期，重置尝试次数
            if (attempt.isLocked() && attempt.isLockExpired()) {
                log.info("锁定已过期，重置尝试次数: {}, IP: {}", username, ipAddress);
                attempt.resetAttemptCount();
            }
            
            // 增加尝试次数
            attempt.incrementAttemptCount();
        } else {
            // 创建新的尝试记录
            attempt = new LoginAttempt(username, ipAddress);
        }
        
        // 检查是否达到最大尝试次数
        if (attempt.getAttemptCount() >= maxAttempts) {
            log.warn("达到最大尝试次数，锁定用户: {}, IP: {}, 尝试次数: {}", username, ipAddress, attempt.getAttemptCount());
            attempt.lock(lockDurationMinutes);
        }
        
        loginAttemptRepository.save(attempt);
        
        return attempt.isLocked();
    }

    @Override
    @Transactional
    public void recordSuccessfulAttempt(String username, String ipAddress) {
        log.info("记录登录成功: {}, IP: {}", username, ipAddress);
        
        Optional<LoginAttempt> attemptOpt = loginAttemptRepository.findByUsernameAndIpAddress(username, ipAddress);
        
        if (attemptOpt.isPresent()) {
            LoginAttempt attempt = attemptOpt.get();
            attempt.resetAttemptCount();
            loginAttemptRepository.save(attempt);
            log.info("重置登录尝试次数: {}, IP: {}", username, ipAddress);
        }
    }

    @Override
    public boolean isLocked(String username, String ipAddress) {
        Optional<LoginAttempt> attemptOpt = loginAttemptRepository.findByUsernameAndIpAddress(username, ipAddress);
        
        if (!attemptOpt.isPresent()) {
            return false;
        }
        
        LoginAttempt attempt = attemptOpt.get();
        
        // 检查是否锁定且锁定未过期
        if (attempt.isLocked() && !attempt.isLockExpired()) {
            log.info("用户被锁定: {}, IP: {}", username, ipAddress);
            return true;
        }
        
        // 如果锁定已过期，解锁用户
        if (attempt.isLocked() && attempt.isLockExpired()) {
            log.info("锁定已过期，解锁用户: {}, IP: {}", username, ipAddress);
            unlockUser(username, ipAddress);
            return false;
        }
        
        return false;
    }

    @Override
    public int getRemainingAttempts(String username, String ipAddress) {
        Optional<LoginAttempt> attemptOpt = loginAttemptRepository.findByUsernameAndIpAddress(username, ipAddress);
        
        if (!attemptOpt.isPresent()) {
            return maxAttempts;
        }
        
        LoginAttempt attempt = attemptOpt.get();
        
        // 如果已锁定且锁定未过期，返回0
        if (attempt.isLocked() && !attempt.isLockExpired()) {
            return 0;
        }
        
        // 如果锁定已过期，返回最大尝试次数
        if (attempt.isLocked() && attempt.isLockExpired()) {
            return maxAttempts;
        }
        
        return Math.max(0, maxAttempts - attempt.getAttemptCount());
    }

    @Override
    public int getLockTimeRemaining(String username, String ipAddress) {
        Optional<LoginAttempt> attemptOpt = loginAttemptRepository.findByUsernameAndIpAddress(username, ipAddress);
        
        if (!attemptOpt.isPresent()) {
            return 0;
        }
        
        LoginAttempt attempt = attemptOpt.get();
        
        // 如果未锁定或锁定已过期，返回0
        if (!attempt.isLocked() || attempt.isLockExpired()) {
            return 0;
        }
        
        // 计算剩余时间（分钟）
        Date now = new Date();
        long diffInMillis = attempt.getLockExpiryTime().getTime() - now.getTime();
        
        if (diffInMillis <= 0) {
            return 0;
        }
        
        return (int) (diffInMillis / (60 * 1000));
    }

    @Override
    @Transactional
    public void lockUser(String username, String ipAddress, int minutes) {
        log.info("手动锁定用户: {}, IP: {}, 时长: {}分钟", username, ipAddress, minutes);
        
        Optional<LoginAttempt> attemptOpt = loginAttemptRepository.findByUsernameAndIpAddress(username, ipAddress);
        LoginAttempt attempt;
        
        if (attemptOpt.isPresent()) {
            attempt = attemptOpt.get();
        } else {
            attempt = new LoginAttempt(username, ipAddress);
        }
        
        attempt.lock(minutes);
        loginAttemptRepository.save(attempt);
    }

    @Override
    @Transactional
    public void unlockUser(String username, String ipAddress) {
        log.info("手动解锁用户: {}, IP: {}", username, ipAddress);
        
        Optional<LoginAttempt> attemptOpt = loginAttemptRepository.findByUsernameAndIpAddress(username, ipAddress);
        
        if (attemptOpt.isPresent()) {
            LoginAttempt attempt = attemptOpt.get();
            attempt.resetAttemptCount();
            loginAttemptRepository.save(attempt);
            log.info("用户已解锁: {}, IP: {}", username, ipAddress);
        }
    }

    @Override
    public List<String> getLockedUsers() {
        List<LoginAttempt> lockedAttempts = loginAttemptRepository.findByLocked(true);
        
        // 过滤出锁定未过期的用户
        return lockedAttempts.stream()
                .filter(attempt -> !attempt.isLockExpired())
                .map(LoginAttempt::getUsername)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int cleanupOldAttempts(int days) {
        log.info("清理{}天前的登录尝试记录", days);
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        Date cutoffDate = calendar.getTime();
        
        List<LoginAttempt> oldAttempts = loginAttemptRepository.findByLastAttemptTimeBefore(cutoffDate);
        int count = oldAttempts.size();
        
        if (count > 0) {
            loginAttemptRepository.deleteAll(oldAttempts);
            log.info("已清理{}条旧的登录尝试记录", count);
        } else {
            log.info("没有需要清理的旧登录尝试记录");
        }
        
        return count;
    }

    @Override
    @Transactional
    public int unlockExpiredLocks() {
        log.info("解锁过期的锁定");
        
        Date now = new Date();
        List<LoginAttempt> expiredLocks = loginAttemptRepository.findByLockedTrueAndLockExpiryTimeBefore(now);
        int count = expiredLocks.size();
        
        if (count > 0) {
            for (LoginAttempt attempt : expiredLocks) {
                attempt.resetAttemptCount();
                log.info("解锁过期的锁定: {}, IP: {}", attempt.getUsername(), attempt.getIpAddress());
            }
            
            loginAttemptRepository.saveAll(expiredLocks);
            log.info("已解锁{}条过期的锁定", count);
        } else {
            log.info("没有需要解锁的过期锁定");
        }
        
        return count;
    }
}
