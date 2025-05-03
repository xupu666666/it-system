package com.ict.system.service.v2;

import com.ict.system.model.v2.UserSession;

import java.util.List;
import java.util.Optional;

public interface UserSessionService {

    /**
     * 创建新会话
     * @param username 用户名
     * @param token 令牌
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @param expirationMinutes 过期时间（分钟）
     * @return 会话对象
     */
    UserSession createSession(String username, String token, String ipAddress, String userAgent, int expirationMinutes);

    /**
     * 根据令牌查找会话
     * @param token 令牌
     * @return 会话对象
     */
    Optional<UserSession> findByToken(String token);

    /**
     * 验证会话
     * @param token 令牌
     * @return 是否有效
     */
    boolean validateSession(String token);

    /**
     * 更新会话活动时间
     * @param token 令牌
     * @return 更新后的会话对象
     */
    Optional<UserSession> updateSessionActivity(String token);

    /**
     * 使会话失效
     * @param token 令牌
     * @return 是否成功
     */
    boolean invalidateSession(String token);

    /**
     * 使用户的所有会话失效
     * @param username 用户名
     * @return 失效的会话数
     */
    int invalidateUserSessions(String username);

    /**
     * 获取用户的所有活跃会话
     * @param username 用户名
     * @return 会话列表
     */
    List<UserSession> getUserActiveSessions(String username);

    /**
     * 获取所有活跃会话
     * @return 会话列表
     */
    List<UserSession> getAllActiveSessions();

    /**
     * 清理过期会话
     * @return 清理的会话数
     */
    int cleanupExpiredSessions();

    /**
     * 清理不活跃会话
     * @param inactiveMinutes 不活跃时间（分钟）
     * @return 清理的会话数
     */
    int cleanupInactiveSessions(int inactiveMinutes);

    /**
     * 获取用户活跃会话数
     * @param username 用户名
     * @return 会话数
     */
    long countUserActiveSessions(String username);

    /**
     * 获取特定IP地址的活跃会话
     * @param ipAddress IP地址
     * @return 会话列表
     */
    List<UserSession> getSessionsByIpAddress(String ipAddress);

    /**
     * 获取特定用户代理的活跃会话
     * @param userAgent 用户代理
     * @return 会话列表
     */
    List<UserSession> getSessionsByUserAgent(String userAgent);
}
