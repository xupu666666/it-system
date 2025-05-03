package com.ict.system.service.impl;

import com.ict.system.model.InventoryCheckRecord;
import com.ict.system.model.InventoryCheckTask;
import com.ict.system.model.InventoryItem;
import com.ict.system.repository.InventoryCheckRecordRepository;
import com.ict.system.repository.InventoryCheckTaskRepository;
import com.ict.system.repository.InventoryRepository;
import com.ict.system.service.InventoryCheckTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InventoryCheckTaskServiceImpl implements InventoryCheckTaskService {
    @Autowired
    private InventoryCheckTaskRepository taskRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryCheckRecordRepository recordRepository;

    @Override
    public InventoryCheckTask createTask(InventoryCheckTask task) {
        // 设置创建时间和更新时间
        Date now = new Date();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        
        // 设置默认状态，如果状态为空
        if (task.getStatus() == null || task.getStatus().isEmpty()) {
            task.setStatus("待盘点");
        }
        
        // 保存任务
        InventoryCheckTask savedTask = taskRepository.save(task);
        System.out.println("创建盘点任务成功: ID=" + savedTask.getId() + 
                          ", 工厂=" + savedTask.getFactory() + 
                          ", 部门=" + savedTask.getDepartment() + 
                          ", 名称=" + savedTask.getTaskName());
        return savedTask;
    }

    @Override
    public InventoryCheckTask updateTask(Long id, InventoryCheckTask task) {
        Optional<InventoryCheckTask> optional = taskRepository.findById(id);
        if (optional.isPresent()) {
            InventoryCheckTask existing = optional.get();
            existing.setFactory(task.getFactory());
            existing.setDepartment(task.getDepartment());
            existing.setTaskName(task.getTaskName());
            existing.setAssignedTo(task.getAssignedTo());
            existing.setStatus(task.getStatus());
            existing.setUpdatedAt(task.getUpdatedAt());
            return taskRepository.save(existing);
        }
        throw new RuntimeException("盘点任务不存在");
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        // 查找并删除任务，通过级联删除自动删除相关的盘点记录
        InventoryCheckTask task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("盘点任务不存在"));
                
        System.out.println("正在删除盘点任务: ID=" + id + ", 名称=" + task.getTaskName());
        
        // 直接删除任务，级联关系会自动删除相关的盘点记录
        taskRepository.delete(task);
        
        System.out.println("盘点任务删除成功");
    }

    @Override
    public InventoryCheckTask getTask(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    @Override
    public List<InventoryCheckTask> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public List<InventoryCheckTask> getTasksByFactoryAndDepartment(String factory, String department) {
        return taskRepository.findAll().stream()
                .filter(t -> t.getFactory().equals(factory) && t.getDepartment().equals(department))
                .toList();
    }

    @Override
    @Transactional
    public int addAssetsToTask(Long taskId, List<String> assetNos, Map<String, String> assetRemarks) {
        // 检查任务是否存在
        InventoryCheckTask task = getTask(taskId);
        if (task == null) {
            throw new RuntimeException("盘点任务不存在");
        }

        int count = 0;
        List<InventoryCheckRecord> newRecords = new ArrayList<>();

        for (String assetNo : assetNos) {
            // 查找资产
            System.out.println("查找资产编号: " + assetNo);

            // 直接使用findAll并过滤，避免可能的Repository方法不存在问题
            List<InventoryItem> matchingItems = inventoryRepository.findAll().stream()
                .filter(item -> item.getAssetNo() != null && item.getAssetNo().equals(assetNo))
                .toList();

            if (matchingItems.isEmpty()) {
                System.out.println("未找到资产编号为 " + assetNo + " 的资产，跳过");
                continue;
            }

            InventoryItem asset = matchingItems.get(0);
            System.out.println("找到资产: ID=" + asset.getId() + ", 名称=" + asset.getName());

            // 检查该资产是否已经在盘点任务中
            boolean exists = recordRepository.findAll().stream()
                    .anyMatch(r -> r.getTask().getId().equals(taskId) &&
                             r.getAsset() != null &&
                             r.getAsset().getId().equals(asset.getId()));

            if (exists) {
                System.out.println("资产 " + assetNo + " 已经在盘点任务中，跳过");
                continue;
            }

            // 创建新的盘点记录
            InventoryCheckRecord record = new InventoryCheckRecord();
            record.setTask(task);
            record.setAsset(asset);
            record.setCheckStatus("正常");
            
            // 自动填充使用人和位置信息
            System.out.println("资产信息 - 使用人: " + (asset.getAssignee() != null ? asset.getAssignee() : "无"));
            System.out.println("资产信息 - 位置: " + (asset.getLocation() != null ? asset.getLocation() : "无"));
            
            record.setActualUser(asset.getAssignee());
            record.setActualLocation(asset.getLocation());
            
            // 如果有楼层信息，也填充
            if (asset.getFloor() != null && !asset.getFloor().isEmpty()) {
                System.out.println("资产信息 - 楼层: " + asset.getFloor());
                record.setActualFloor(asset.getFloor());
            } else {
                System.out.println("资产信息 - 楼层: 无");
            }

            // 设置备注（如果有）
            if (assetRemarks.containsKey(assetNo)) {
                record.setRemark(assetRemarks.get(assetNo));
            }
            
            // 设置盘点时间
            record.setCheckedAt(new Date());

            newRecords.add(record);
            count++;
        }

        if (!newRecords.isEmpty()) {
            recordRepository.saveAll(newRecords);
            System.out.println("批量保存了 " + newRecords.size() + " 条盘点记录");
        }

        return count;
    }
}