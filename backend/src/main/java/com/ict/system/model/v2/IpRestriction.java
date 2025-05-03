package com.ict.system.model.v2;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "v2_ip_restrictions")
public class IpRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ipAddress;

    @Column(nullable = false)
    private String type; // WHITELIST, BLACKLIST

    private String description;

    @Column(nullable = false)
    private boolean active;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    // 构造函数
    public IpRestriction() {
        this.createdAt = new Date();
        this.active = true;
    }

    public IpRestriction(String ipAddress, String type, String description) {
        this.ipAddress = ipAddress;
        this.type = type;
        this.description = description;
        this.createdAt = new Date();
        this.active = true;
    }

    /**
     * 检查是否过期
     * @return 是否过期
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }

        Date now = new Date();
        return now.after(expiresAt);
    }

    /**
     * 设置过期时间
     * @param days 过期天数
     */
    public void setExpiryDays(int days) {
        if (days <= 0) {
            this.expiresAt = null;
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        this.expiresAt = calendar.getTime();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
}
