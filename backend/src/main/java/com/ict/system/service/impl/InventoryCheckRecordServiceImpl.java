package com.ict.system.service.impl;

import com.ict.system.model.InventoryCheckRecord;
import com.ict.system.repository.InventoryCheckRecordRepository;
import com.ict.system.service.InventoryCheckRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryCheckRecordServiceImpl implements InventoryCheckRecordService {
    @Autowired
    private InventoryCheckRecordRepository recordRepository;

    @Override
    public InventoryCheckRecord createRecord(InventoryCheckRecord record) {
        return recordRepository.save(record);
    }

    @Override
    public InventoryCheckRecord updateRecord(Long id, InventoryCheckRecord record) {
        Optional<InventoryCheckRecord> optional = recordRepository.findById(id);
        if (optional.isPresent()) {
            InventoryCheckRecord existing = optional.get();
            existing.setCheckStatus(record.getCheckStatus());
            existing.setActualUser(record.getActualUser());
            existing.setActualLocation(record.getActualLocation());
            existing.setActualFloor(record.getActualFloor());
            existing.setRemark(record.getRemark());
            existing.setPhotoUrl(record.getPhotoUrl());
            existing.setCheckedBy(record.getCheckedBy());
            existing.setCheckedAt(record.getCheckedAt());
            return recordRepository.save(existing);
        }
        throw new RuntimeException("盘点记录不存在");
    }

    @Override
    public void deleteRecord(Long id) {
        recordRepository.deleteById(id);
    }

    @Override
    public InventoryCheckRecord getRecord(Long id) {
        return recordRepository.findById(id).orElse(null);
    }

    @Override
    public List<InventoryCheckRecord> getRecordsByTaskId(Long taskId) {
        try {
            // 使用优化的Repository方法直接查询数据库
            return recordRepository.findByTaskId(taskId);
        } catch (Exception e) {
            // 记录异常并返回空列表，避免500错误
            System.err.println("获取任务盘点记录失败，任务ID: " + taskId + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // 返回空列表而不是抛出异常
        }
    }

    @Override
    public List<InventoryCheckRecord> getRecordsByAssetId(Long assetId) {
        try {
            // 使用优化的Repository方法直接查询数据库
            return recordRepository.findByAssetId(assetId);
        } catch (Exception e) {
            // 记录异常并返回空列表，避免500错误
            System.err.println("获取资产盘点记录失败，资产ID: " + assetId + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // 返回空列表而不是抛出异常
        }
    }
}