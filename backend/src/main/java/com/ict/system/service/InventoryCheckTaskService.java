package com.ict.system.service;

import com.ict.system.model.InventoryCheckTask;
import java.util.List;
import java.util.Map;

public interface InventoryCheckTaskService {
    InventoryCheckTask createTask(InventoryCheckTask task);
    InventoryCheckTask updateTask(Long id, InventoryCheckTask task);
    void deleteTask(Long id);
    InventoryCheckTask getTask(Long id);
    List<InventoryCheckTask> getAllTasks();
    List<InventoryCheckTask> getTasksByFactoryAndDepartment(String factory, String department);

    /**
     * 添加资产到盘点任务
     * @param taskId 盘点任务ID
     * @param assetNos 资产编号列表
     * @param assetRemarks 资产备注映射（资产编号 -> 备注）
     * @return 成功添加的资产数量
     */
    int addAssetsToTask(Long taskId, List<String> assetNos, Map<String, String> assetRemarks);
}