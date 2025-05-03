package com.ict.system.controller;

import com.ict.system.model.InventoryCheckRecord;
import com.ict.system.payload.response.MessageResponse;
import com.ict.system.service.InventoryCheckRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-check-record")
public class InventoryCheckRecordController {
    @Autowired
    private InventoryCheckRecordService recordService;

    @PostMapping
    public InventoryCheckRecord createRecord(@RequestBody InventoryCheckRecord record) {
        return recordService.createRecord(record);
    }

    @PutMapping("/{id}")
    public InventoryCheckRecord updateRecord(@PathVariable Long id, @RequestBody InventoryCheckRecord record) {
        return recordService.updateRecord(id, record);
    }

    @DeleteMapping("/{id}")
    public void deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
    }

    @GetMapping("/{id}")
    public InventoryCheckRecord getRecord(@PathVariable Long id) {
        return recordService.getRecord(id);
    }

    @GetMapping("/by-task/{taskId}")
    public ResponseEntity<?> getRecordsByTaskId(@PathVariable Long taskId) {
        try {
            List<InventoryCheckRecord> records = recordService.getRecordsByTaskId(taskId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            System.err.println("获取任务盘点记录失败，任务ID: " + taskId + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取任务盘点记录失败: " + e.getMessage()));
        }
    }

    @GetMapping("/by-asset/{assetId}")
    public ResponseEntity<?> getRecordsByAssetId(@PathVariable Long assetId) {
        try {
            List<InventoryCheckRecord> records = recordService.getRecordsByAssetId(assetId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            System.err.println("获取资产盘点记录失败，资产ID: " + assetId + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取资产盘点记录失败: " + e.getMessage()));
        }
    }
}