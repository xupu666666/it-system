package com.ict.system.model;

import javax.persistence.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "inventory_check_record")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class InventoryCheckRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER) // 改为EAGER加载，避免懒加载异常
    @JoinColumn(name = "task_id")
    @JsonBackReference  // 添加这个注解，表示这是关系的被管理端
    private InventoryCheckTask task;
    @ManyToOne(fetch = FetchType.EAGER) // 改为EAGER加载，避免懒加载异常
    @JoinColumn(name = "asset_id")
    private InventoryItem asset;
    private String checkStatus;      // 盘点结果（正常/盘盈/盘亏/报废等）
    private String actualUser;       // 实际使用人
    private String actualLocation;   // 实际存放地点
    private String actualFloor;      // 实际楼层
    private String remark;           // 备注
    private String photoUrl;         // 现场照片
    private String checkedBy;        // 盘点人
    @Temporal(TemporalType.TIMESTAMP)
    private Date checkedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public InventoryCheckTask getTask() { return task; }
    public void setTask(InventoryCheckTask task) { this.task = task; }
    public InventoryItem getAsset() { return asset; }
    public void setAsset(InventoryItem asset) { this.asset = asset; }
    public String getCheckStatus() { return checkStatus; }
    public void setCheckStatus(String checkStatus) { this.checkStatus = checkStatus; }
    public String getActualUser() { return actualUser; }
    public void setActualUser(String actualUser) { this.actualUser = actualUser; }
    public String getActualLocation() { return actualLocation; }
    public void setActualLocation(String actualLocation) { this.actualLocation = actualLocation; }
    public String getActualFloor() { return actualFloor; }
    public void setActualFloor(String actualFloor) { this.actualFloor = actualFloor; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getCheckedBy() { return checkedBy; }
    public void setCheckedBy(String checkedBy) { this.checkedBy = checkedBy; }
    public Date getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Date checkedAt) { this.checkedAt = checkedAt; }
}