package com.ict.system.repository;

import com.ict.system.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByAssetNo(String assetNo);

    List<InventoryItem> findByType(String type);

    List<InventoryItem> findByDepartment(String department);

    List<InventoryItem> findByStatus(String status);

    List<InventoryItem> findByAssignee(String assignee);

    boolean existsByAssetNo(String assetNo);

    boolean existsBySerialNumber(String serialNumber);
}