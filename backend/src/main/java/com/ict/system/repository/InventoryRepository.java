package com.ict.system.repository;

import com.ict.system.model.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByAssetNo(String assetNo);

    boolean existsByAssetNo(String assetNo);

    Page<InventoryItem> findByAssetNoContaining(String assetNo, Pageable pageable);

    Page<InventoryItem> findByNameContaining(String name, Pageable pageable);

    Page<InventoryItem> findByTypeContaining(String type, Pageable pageable);

    Page<InventoryItem> findByDepartmentContaining(String department, Pageable pageable);

    Page<InventoryItem> findByStatusContaining(String status, Pageable pageable);

    @Query("SELECT i FROM InventoryItem i WHERE " +
           "i.assetNo = ?1 OR " +  // 精确匹配资产编号
           "i.assetNo LIKE %?1% OR " +
           "i.name LIKE %?1% OR " +
           "i.type LIKE %?1% OR " +
           "i.department LIKE %?1% OR " +
           "i.location LIKE %?1% OR " +
           "i.specifications LIKE %?1%")
    Page<InventoryItem> findByKeyword(String keyword, Pageable pageable);

    @Query("SELECT i FROM InventoryItem i WHERE " +
           "(:assetNo IS NULL OR :assetNo = '' OR i.assetNo = :assetNo OR i.assetNo LIKE %:assetNo%) AND " +
           "(:name IS NULL OR :name = '' OR i.name LIKE %:name%) AND " +
           "(:type IS NULL OR :type = '' OR i.type LIKE %:type% OR i.apcCode LIKE %:type%) AND " +
           "(:department IS NULL OR :department = '' OR i.department LIKE %:department%) AND " +
           "(:status IS NULL OR :status = '' OR i.status LIKE %:status%)")
    Page<InventoryItem> findByMultipleFields(
            String assetNo, String name, String type, String department, String status, Pageable pageable);

    List<InventoryItem> findByIdIn(List<Long> ids);

    @Query("SELECT DISTINCT i.type FROM InventoryItem i")
    List<String> findAllTypes();

    @Query("SELECT DISTINCT i.status FROM InventoryItem i")
    List<String> findAllStatuses();

    // 使用原生SQL查询，直接查找资产编号
    @Query(value = "SELECT * FROM inventory_items WHERE asset_no = ?1", nativeQuery = true)
    List<InventoryItem> findByAssetNoDirectSQL(String assetNo);

    // 使用原生SQL查询，模糊匹配资产编号
    @Query(value = "SELECT * FROM inventory_items WHERE asset_no LIKE CONCAT('%', ?1, '%')", nativeQuery = true)
    List<InventoryItem> findByAssetNoContainingDirectSQL(String assetNo);

    // 获取所有唯一的工厂
    @Query("SELECT DISTINCT i.factory FROM InventoryItem i WHERE i.factory IS NOT NULL AND i.factory <> ''")
    List<String> findAllFactories();

    // 获取所有唯一的部门
    @Query("SELECT DISTINCT i.department FROM InventoryItem i WHERE i.department IS NOT NULL AND i.department <> ''")
    List<String> findAllDepartments();

    // 获取所有唯一的楼层
    @Query("SELECT DISTINCT i.floor FROM InventoryItem i WHERE i.floor IS NOT NULL AND i.floor <> ''")
    List<String> findAllFloors();
}
