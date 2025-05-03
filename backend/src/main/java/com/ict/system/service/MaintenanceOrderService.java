package com.ict.system.service;

import com.ict.system.model.MaintenanceOrder;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MaintenanceOrderService {
    
    /**
     * 根据ID查找维修单
     * @param id 维修单ID
     * @return 维修单对象
     */
    Optional<MaintenanceOrder> findById(Long id);
    
    /**
     * 根据单号查找维修单
     * @param orderNo 维修单号
     * @return 维修单对象
     */
    Optional<MaintenanceOrder> findByOrderNo(String orderNo);
    
    /**
     * 获取所有维修单
     * @return 维修单列表
     */
    List<MaintenanceOrder> findAll();
    
    /**
     * 分页获取维修单
     * @param page 页码
     * @param size 每页大小
     * @return 维修单列表
     */
    List<MaintenanceOrder> findAll(int page, int size);
    
    /**
     * 根据状态查找维修单
     * @param status 维修单状态
     * @return 维修单列表
     */
    List<MaintenanceOrder> findByStatus(String status);
    
    /**
     * 根据申请人查找维修单
     * @param requester 申请人
     * @return 维修单列表
     */
    List<MaintenanceOrder> findByRequester(String requester);
    
    /**
     * 根据处理人查找维修单
     * @param assignee 处理人
     * @return 维修单列表
     */
    List<MaintenanceOrder> findByAssignee(String assignee);
    
    /**
     * 根据资产ID查找维修单
     * @param assetId 资产ID
     * @return 维修单列表
     */
    List<MaintenanceOrder> findByAssetId(String assetId);
    
    /**
     * 根据申请部门查找维修单
     * @param department 申请部门
     * @return 维修单列表
     */
    List<MaintenanceOrder> findByRequesterDepartment(String department);
    
    /**
     * 根据申请日期范围查找维修单
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 维修单列表
     */
    List<MaintenanceOrder> findByRequestDateBetween(Date startDate, Date endDate);
    
    /**
     * 根据优先级查找维修单
     * @param priority 优先级
     * @return 维修单列表
     */
    List<MaintenanceOrder> findByPriority(String priority);
    
    /**
     * 保存维修单
     * @param order 维修单对象
     * @return 保存后的维修单对象
     */
    MaintenanceOrder save(MaintenanceOrder order);
    
    /**
     * 删除维修单
     * @param id 维修单ID
     */
    void deleteById(Long id);
    
    /**
     * 批量删除维修单
     * @param ids 维修单ID列表
     */
    void deleteByIds(List<Long> ids);
    
    /**
     * 更新维修单状态
     * @param id 维修单ID
     * @param status 状态
     * @param operator 操作人
     * @param comments 备注
     * @return 更新后的维修单对象
     */
    MaintenanceOrder updateStatus(Long id, String status, String operator, String comments);
}
