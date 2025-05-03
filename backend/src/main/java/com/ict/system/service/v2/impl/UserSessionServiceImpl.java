package com.ict.system.service.v2.impl;

import com.ict.system.model.v2.UserSession;
import com.ict.system.repository.v2.UserSessionRepository;
import com.ict.system.service.v2.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service("userSessionServiceV2")
public class UserSessionServiceImpl implements UserSessionService {

    private static final Logger log = LoggerFactory.getLogger(UserSessionServiceImpl.class);

    @Autowired
    @Qualifier("userSessionRepositoryV2")
    private UserSessionRepository userSessionRepository;

    @Override
    @Transactional
    public UserSession createSession(String username, String token, String ipAddress, String userAgent, int expirationMinutes) {
        log.info("创建用户会话: {}, IP: {}", username, ipAddress);
        
        // 计算过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expirationMinutes);
        Date expiresAt = calendar.getTime();
        
        // 创建会话
        UserSession session = new UserSession(username, token, ipAddress, userAgent, expiresAt);
        
        // 保存会话
        UserSession savedSession = userSessionRepository.save(session);
        log.info("用户会话创建成功: {}, 令牌: {}, 过期时间: {}", username, token, expiresAt);
        
        return savedSession;
    }

    @Override
    public Optional<UserSession> findByToken(String token) {
        log.debug("查找会话, 令牌: {}", token);
        return userSessionRepository.findByToken(token);
    }

    @Override
    public boolean validateSession(String token) {
        log.debug("验证会话, 令牌: {}", token);
        
        Optional<UserSession> sessionOpt = userSessionRepository.findByToken(token);
        
        if (!sessionOpt.isPresent()) {
            log.debug("会话不存在, 令牌: {}", token);
            return false;
        }
        
        UserSession session = sessionOpt.get();
        
        // 检查会话是否活跃
        if (!session.isActive()) {
            log.debug("会话不活跃, 令牌: {}", token);
            return false;
        }
        
        // 检查会话是否过期
        if (session.isExpired()) {
            log.debug("会话已过期, 令牌: {}", token);
            invalidateSession(token);
            return false;
        }
        
        // 更新最后活动时间
        updateSessionActivity(token);
        
        return true;
    }

    @Override
    @Transactional
    public Optional<UserSession> updateSessionActivity(String token) {
        log.debug("更新会话活动时间, 令牌: {}", token);
        
        Optional<UserSession> sessionOpt = userSessionRepository.findByToken(token);
        
        if (!sessionOpt.isPresent()) {
            log.debug("会话不存在, 令牌: {}", token);
            return Optional.empty();
        }
        
        UserSession session = sessionOpt.get();
        
        // 检查会话是否活跃
        if (!session.isActive()) {
            log.debug("会话不活跃, 令牌: {}", token);
            return Optional.empty();
        }
        
        // 检查会话是否过期
        if (session.isExpired()) {
            log.debug("会话已过期, 令牌: {}", token);
            invalidateSession(token);
            return Optional.empty();
        }
        
        // 更新最后活动时间
        session.updateLastActivity();
        UserSession updatedSession = userSessionRepository.save(session);
        
        return Optional.of(updatedSession);
    }

    @Override
    @Transactional
    public boolean invalidateSession(String token) {
        log.info("使会话失效, 令牌: {}", token);
        
        Optional<UserSession> sessionOpt = userSessionRepository.findByToken(token);
        
        if (!sessionOpt.isPresent()) {
            log.debug("会话不存在, 令牌: {}", token);
            return false;
        }
        
        UserSession session = sessionOpt.get();
        
        // 使会话失效
        session.invalidate();
        userSessionRepository.save(session);
        
        log.info("会话已失效, 令牌: {}, 用户: {}", token, session.getUsername());
        return true;
    }

    @Override
    @Transactional
    public int invalidateUserSessions(String username) {
        log.info("使用户的所有会话失效: {}", username);
        
        List<UserSession> sessions = userSessionRepository.findByUsernameAndActiveTrue(username);
        
        if (sessions.isEmpty()) {
            log.debug("用户没有活跃会话: {}", username);
            return 0;
        }
        
        // 使所有会话失效
        for (UserSession session : sessions) {
            session.invalidate();
        }
        
        userSessionRepository.saveAll(sessions);
        
        log.info("用户的所有会话已失效: {}, 数量: {}", username, sessions.size());
        return sessions.size();
    }

    @Override
    public List<UserSession> getUserActiveSessions(String username) {
        log.info("获取用户的活跃会话: {}", username);
        return userSessionRepository.findByUsernameAndActiveTrue(username);
    }

    @Override
    public List<UserSession> getAllActiveSessions() {
        log.info("获取所有活跃会话");
        return userSessionRepository.findByActiveTrue();
    }

    @Override
    @Transactional
    public int cleanupExpiredSessions() {
        log.info("清理过期会话");
        
        Date now = new Date();
        List<UserSession> expiredSessions = userSessionRepository.findByExpiresAtBeforeAndActiveTrue(now);
        
        if (expiredSessions.isEmpty()) {
            log.debug("没有过期会话");
            return 0;
        }
        
        // 使所有过期会话失效
        for (UserSession session : expiredSessions) {
            session.invalidate();
        }
        
        userSessionRepository.saveAll(expiredSessions);
        
        log.info("过期会话已清理, 数量: {}", expiredSessions.size());
        return expiredSessions.size();
    }

    @Override
    @Transactional
    public int cleanupInactiveSessions(int inactiveMinutes) {
        log.info("清理不活跃会话, 不活跃时间: {}分钟", inactiveMinutes);
        
        // 计算不活跃时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -inactiveMinutes);
        Date cutoffDate = calendar.getTime();
        
        List<UserSession> inactiveSessions = userSessionRepository.findByLastActivityAtBeforeAndActiveTrue(cutoffDate);
        
        if (inactiveSessions.isEmpty()) {
            log.debug("没有不活跃会话");
            return 0;
        }
        
        // 使所有不活跃会话失效
        for (UserSession session : inactiveSessions) {
            session.invalidate();
        }
        
        userSessionRepository.saveAll(inactiveSessions);
        
        log.info("不活跃会话已清理, 数量: {}", inactiveSessions.size());
        return inactiveSessions.size();
    }

    @Override
    public long countUserActiveSessions(String username) {
        log.debug("获取用户活跃会话数: {}", username);
        return userSessionRepository.countByUsernameAndActiveTrue(username);
    }

    @Override
    public List<UserSession> getSessionsByIpAddress(String ipAddress) {
        log.info("获取特定IP地址的活跃会话: {}", ipAddress);
        return userSessionRepository.findByIpAddressAndActiveTrue(ipAddress);
    }

    @Override
    public List<UserSession> getSessionsByUserAgent(String userAgent) {
        log.info("获取特定用户代理的活跃会话: {}", userAgent);
        return userSessionRepository.findByUserAgentContainingAndActiveTrue(userAgent);
    }
}
