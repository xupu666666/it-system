package com.ict.system.model;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "supply_requests")
public class SupplyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String requestNo;

    private String requester;
    private String requesterDepartment;

    @Temporal(TemporalType.TIMESTAMP)
    private Date requestDate;

    private String status; // PENDING, APPROVED, REJECTED, COMPLETED, RETURNED
    private String approver;

    @Temporal(TemporalType.TIMESTAMP)
    private Date approveDate;

    private String purpose;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "supply_request_id")
    private List<SupplyItem> items = new ArrayList<>();

    @Column(length = 1000)
    private String notes;

    @Temporal(TemporalType.TIMESTAMP)
    private Date returnDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Data
    @Entity
    @Table(name = "supply_items")
    public static class SupplyItem {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;
        private String type;
        private Integer quantity;
        private String unit;
        private String status; // PENDING, APPROVED, REJECTED, ISSUED
    }
}