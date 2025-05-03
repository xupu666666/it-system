package com.ict.system.controller.v2;

import com.ict.system.payload.response.MessageResponse;
import com.ict.system.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 库存特殊操作控制器
 * 这个控制器专门用于处理一些特殊操作，避免与常规CRUD操作冲突
 */
@RestController
@RequestMapping("/api/inventory-special")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true", maxAge = 3600)
public class InventoryV2SpecialController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * 删除所有资产记录
     * 使用一个明确的路径，避免与动态路径变量冲突
     */
    @PostMapping("/items/destroyAll")
    public ResponseEntity<MessageResponse> destroyAllItems() {
        try {
            inventoryService.deleteAllInventoryItems();
            return ResponseEntity.ok(new MessageResponse("V2 API - 成功删除所有资产记录"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("删除失败: " + e.getMessage()));
        }
    }

    /**
     * 测试API是否可访问
     */
    @GetMapping("/test")
    public ResponseEntity<MessageResponse> testApi() {
        return ResponseEntity.ok(new MessageResponse("API可以访问"));
    }
}