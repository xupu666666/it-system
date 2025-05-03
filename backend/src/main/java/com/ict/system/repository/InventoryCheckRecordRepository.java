package com.ict.system.repository;

import com.ict.system.model.InventoryCheckRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryCheckRecordRepository extends JpaRepository<InventoryCheckRecord, Long> {
    // 添加根据任务ID查询记录的方法
    @Query("SELECT r FROM InventoryCheckRecord r WHERE r.task.id = :taskId")
    List<InventoryCheckRecord> findByTaskId(@Param("taskId") Long taskId);

    // 添加根据资产ID查询记录的方法
    @Query("SELECT r FROM InventoryCheckRecord r WHERE r.asset.id = :assetId")
    List<InventoryCheckRecord> findByAssetId(@Param("assetId") Long assetId);
}