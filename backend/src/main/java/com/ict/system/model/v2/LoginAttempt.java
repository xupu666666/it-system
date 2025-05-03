package com.ict.system.model.v2;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "v2_login_attempts")
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private int attemptCount;

    @Column(nullable = false)
    private boolean locked;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date lastAttemptTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lockExpiryTime;

    // 构造函数
    public LoginAttempt() {
        this.attemptCount = 0;
        this.locked = false;
        this.lastAttemptTime = new Date();
    }

    public LoginAttempt(String username, String ipAddress) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.attemptCount = 1;
        this.locked = false;
        this.lastAttemptTime = new Date();
    }

    /**
     * 增加尝试次数
     */
    public void incrementAttemptCount() {
        this.attemptCount++;
        this.lastAttemptTime = new Date();
    }

    /**
     * 重置尝试次数
     */
    public void resetAttemptCount() {
        this.attemptCount = 0;
        this.locked = false;
        this.lockExpiryTime = null;
    }

    /**
     * 锁定账户
     * @param minutes 锁定时间（分钟）
     */
    public void lock(int minutes) {
        this.locked = true;

        Date now = new Date();
        Date expiryTime = new Date(now.getTime() + minutes * 60 * 1000);
        this.lockExpiryTime = expiryTime;
    }

    /**
     * 检查锁定是否过期
     * @return 是否过期
     */
    public boolean isLockExpired() {
        if (!this.locked || this.lockExpiryTime == null) {
            return true;
        }

        Date now = new Date();
        return now.after(this.lockExpiryTime);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Date getLastAttemptTime() {
        return lastAttemptTime;
    }

    public void setLastAttemptTime(Date lastAttemptTime) {
        this.lastAttemptTime = lastAttemptTime;
    }

    public Date getLockExpiryTime() {
        return lockExpiryTime;
    }

    public void setLockExpiryTime(Date lockExpiryTime) {
        this.lockExpiryTime = lockExpiryTime;
    }
}
