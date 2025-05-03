package com.ict.system.service;

import com.ict.system.model.InventoryItem;
import com.ict.system.payload.request.InventoryCheckRequest;
import com.ict.system.payload.response.PagedResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface InventoryService {

    /**
     * 获取资产列表
     * @param page 页码
     * @param size 每页大小
     * @param assetNo 资产编号
     * @param name 资产名称
     * @param apcAccount APC科目
     * @param factory 工厂
     * @param department 使用部门
     * @param storageFloor 存放楼层
     * @param keyword 关键字
     * @return 分页资产列表
     */
    PagedResponse<InventoryItem> getInventoryItems(
            int page, int size, String assetNo, String name, String apcAccount,
            String factory, String department, String storageFloor, String keyword);

    /**
     * 获取资产详情
     * @param id 资产ID
     * @return 资产对象
     */
    InventoryItem getInventoryItem(Long id);

    /**
     * 创建资产
     * @param inventoryItem 资产对象
     * @return 创建后的资产对象
     */
    InventoryItem createInventoryItem(InventoryItem inventoryItem);

    /**
     * 更新资产
     * @param id 资产ID
     * @param inventoryItem 资产对象
     * @return 更新后的资产对象
     */
    InventoryItem updateInventoryItem(Long id, InventoryItem inventoryItem);

    /**
     * 删除资产
     * @param id 资产ID
     */
    void deleteInventoryItem(Long id);

    /**
     * 批量删除资产
     * @param ids 资产ID列表
     */
    void batchDeleteInventoryItems(List<Long> ids);

    /**
     * 删除所有资产
     */
    void deleteAllInventoryItems();

    /**
     * 资产盘点
     * @param checkRequest 盘点请求
     * @return 盘点后的资产对象
     */
    InventoryItem checkInventoryItem(InventoryCheckRequest checkRequest);

    /**
     * 导入资产
     * @param file Excel文件
     * @return 导入的记录数
     */
    int importInventoryItems(MultipartFile file);

    /**
     * 导出资产
     * @param ids 资产ID列表
     * @param assetNo 资产编号
     * @param name 资产名称
     * @param type 资产类型
     * @param department 使用部门
     * @param status 状态
     * @param keyword 关键字
     * @return 响应实体
     */
    ResponseEntity<?> exportInventoryItems(
            List<Long> ids, String assetNo, String name, String type,
            String department, String status, String keyword);

    /**
     * 获取资产类型列表
     * @return 类型列表
     */
    List<String> getInventoryTypes();

    /**
     * 获取资产状态列表
     * @return 状态列表
     */
    List<String> getInventoryStatuses();

    /**
     * 获取所有资产
     * @return 所有资产列表
     */
    List<InventoryItem> getAllInventoryItems();

    /**
     * 获取所有工厂列表
     * @return 工厂列表
     */
    List<String> getAllFactories();

    /**
     * 获取所有部门列表
     * @return 部门列表
     */
    List<String> getAllDepartments();

    /**
     * 获取所有楼层列表
     * @return 楼层列表
     */
    List<String> getAllFloors();

    ResponseEntity<byte[]> exportInventoryItems();

    Optional<InventoryItem> findByAssetNo(String assetNo);
}
