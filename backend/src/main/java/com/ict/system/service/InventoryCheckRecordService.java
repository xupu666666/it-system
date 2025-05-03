package com.ict.system.service;

import com.ict.system.model.InventoryCheckRecord;
import java.util.List;

public interface InventoryCheckRecordService {
    InventoryCheckRecord createRecord(InventoryCheckRecord record);
    InventoryCheckRecord updateRecord(Long id, InventoryCheckRecord record);
    void deleteRecord(Long id);
    InventoryCheckRecord getRecord(Long id);
    List<InventoryCheckRecord> getRecordsByTaskId(Long taskId);
    List<InventoryCheckRecord> getRecordsByAssetId(Long assetId);
} 