package com.ict.system.model.v2;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "v2_audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE, VIEW

    @Column(nullable = false)
    private String resourceType; // USER, ROLE, PERMISSION, etc.

    private String resourceId;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private String ipAddress;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date timestamp;

    // 构造函数
    public AuditLog() {
        this.timestamp = new Date();
    }

    public AuditLog(String username, String action, String resourceType, String resourceId, String details, String ipAddress) {
        this.username = username;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.details = details;
        this.ipAddress = ipAddress;
        this.timestamp = new Date();
    }
}
