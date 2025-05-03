package com.ict.system.service.v2;

import java.util.List;

public interface LoginAttemptService {

    /**
     * 记录登录失败
     * @param username 用户名
     * @param ipAddress IP地址
     * @return 是否锁定
     */
    boolean recordFailedAttempt(String username, String ipAddress);

    /**
     * 记录登录成功
     * @param username 用户名
     * @param ipAddress IP地址
     */
    void recordSuccessfulAttempt(String username, String ipAddress);

    /**
     * 检查用户是否被锁定
     * @param username 用户名
     * @param ipAddress IP地址
     * @return 是否锁定
     */
    boolean isLocked(String username, String ipAddress);

    /**
     * 获取剩余尝试次数
     * @param username 用户名
     * @param ipAddress IP地址
     * @return 剩余尝试次数
     */
    int getRemainingAttempts(String username, String ipAddress);

    /**
     * 获取锁定剩余时间（分钟）
     * @param username 用户名
     * @param ipAddress IP地址
     * @return 锁定剩余时间（分钟）
     */
    int getLockTimeRemaining(String username, String ipAddress);

    /**
     * 手动锁定用户
     * @param username 用户名
     * @param ipAddress IP地址
     * @param minutes 锁定时间（分钟）
     */
    void lockUser(String username, String ipAddress, int minutes);

    /**
     * 手动解锁用户
     * @param username 用户名
     * @param ipAddress IP地址
     */
    void unlockUser(String username, String ipAddress);

    /**
     * 获取所有锁定的用户
     * @return 锁定的用户列表
     */
    List<String> getLockedUsers();

    /**
     * 清理过期的登录尝试记录
     * @param days 保留天数
     * @return 清理的记录数
     */
    int cleanupOldAttempts(int days);

    /**
     * 解锁过期的锁定
     * @return 解锁的记录数
     */
    int unlockExpiredLocks();
}
