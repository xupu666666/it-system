package com.ict.system.model.v2;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "v2_user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String userAgent;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date expiresAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastActivityAt;

    @Column(nullable = false)
    private boolean active;

    // 构造函数
    public UserSession() {
        this.createdAt = new Date();
        this.lastActivityAt = new Date();
        this.active = true;
    }

    public UserSession(String username, String token, String ipAddress, String userAgent, Date expiresAt) {
        this.username = username;
        this.token = token;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = new Date();
        this.lastActivityAt = new Date();
        this.expiresAt = expiresAt;
        this.active = true;
    }

    /**
     * 更新最后活动时间
     */
    public void updateLastActivity() {
        this.lastActivityAt = new Date();
    }

    /**
     * 检查会话是否过期
     * @return 是否过期
     */
    public boolean isExpired() {
        Date now = new Date();
        return now.after(expiresAt);
    }

    /**
     * 使会话失效
     */
    public void invalidate() {
        this.active = false;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Date getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Date lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
