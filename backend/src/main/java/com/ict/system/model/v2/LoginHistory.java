package com.ict.system.model.v2;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "v2_login_history")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String userAgent;

    @Column(nullable = false)
    private String status; // SUCCESS, FAILED

    private String failureReason;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date loginTime;

    // 构造函数
    public LoginHistory() {
        this.loginTime = new Date();
    }

    public LoginHistory(String username, String ipAddress, String userAgent, String status) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.status = status;
        this.loginTime = new Date();
    }

    public LoginHistory(String username, String ipAddress, String userAgent, String status, String failureReason) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.status = status;
        this.failureReason = failureReason;
        this.loginTime = new Date();
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

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }
}
