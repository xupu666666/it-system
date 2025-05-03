package com.ict.system.controller.v2;

import com.ict.system.model.InventoryItem;
import com.ict.system.payload.request.InventoryCheckRequest;
import com.ict.system.payload.response.MessageResponse;
import com.ict.system.payload.response.PagedResponse;
import com.ict.system.service.InventoryService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 资产盘点控制器 V2
 *
 * 这个版本的控制器不需要特定权限就可以访问
 */
@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true", maxAge = 3600)
public class InventoryControllerV2 {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/items")
    public ResponseEntity<PagedResponse<InventoryItem>> getInventoryItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String assetNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String apcAccount,
            @RequestParam(required = false) String factory,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String storageFloor,
            @RequestParam(required = false) String keyword) {

        // 记录接收到的查询参数，便于调试
        System.out.println("接收到查询请求 - 资产编号: " + assetNo +
                          ", 资产描述: " + name +
                          ", APC科目: " + apcAccount +
                          ", 工厂: " + factory +
                          ", 部门: " + department +
                          ", 楼层: " + storageFloor +
                          ", 关键字: " + keyword);

        // 将前端参数映射到后端参数
        PagedResponse<InventoryItem> response = inventoryService.getInventoryItems(
                page, size, assetNo, name, apcAccount, factory, department, storageFloor, keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<InventoryItem> getInventoryItem(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryItem(id));
    }

    @PostMapping("/items")
    public ResponseEntity<InventoryItem> createInventoryItem(@Valid @RequestBody InventoryItem inventoryItem) {
        return ResponseEntity.ok(inventoryService.createInventoryItem(inventoryItem));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<InventoryItem> updateInventoryItem(
            @PathVariable Long id,
            @Valid @RequestBody InventoryItem inventoryItem) {
        return ResponseEntity.ok(inventoryService.updateInventoryItem(id, inventoryItem));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<MessageResponse> deleteInventoryItem(@PathVariable Long id) {
        inventoryService.deleteInventoryItem(id);
        return ResponseEntity.ok(new MessageResponse("资产删除成功"));
    }

    @DeleteMapping("/items/batch")
    public ResponseEntity<MessageResponse> batchDeleteInventoryItems(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        inventoryService.batchDeleteInventoryItems(ids);
        return ResponseEntity.ok(new MessageResponse("批量删除资产成功"));
    }

    @PostMapping("/check")
    public ResponseEntity<InventoryItem> checkInventoryItem(@Valid @RequestBody InventoryCheckRequest checkRequest) {
        try {
            InventoryItem result = inventoryService.checkInventoryItem(checkRequest);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // 记录异常但不抛出，返回500状态码
            System.err.println("V2 API - 资产盘点失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(null);
        }
    }

    // 资产报废 - PUT方法
    @PutMapping("/items/{id}/retire")
    public ResponseEntity<InventoryItem> retireInventoryItem(@PathVariable Long id, @RequestBody Map<String, String> retireData) {
        try {
            System.out.println("收到资产报废请求 (PUT): ID=" + id + ", 数据=" + retireData);

            // 获取报废原因和日期
            String reason = retireData.getOrDefault("reason", "正常报废");
            String retireDate = retireData.getOrDefault("retireDate", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
            String status = retireData.getOrDefault("status", "已报废");

            // 获取资产
            InventoryItem item = inventoryService.getInventoryItem(id);
            System.out.println("找到资产: " + item.getAssetNo());

            // 更新资产状态为"已报废"，使用前端传入的状态值或默认值
            item.setStatus(status);
            item.setNotes((item.getNotes() != null ? item.getNotes() + "; " : "") +
                           "报废日期: " + retireDate + ", 报废原因: " + reason);

            // 保存更新后的资产
            InventoryItem updatedItem = inventoryService.updateInventoryItem(id, item);
            System.out.println("资产报废成功: " + updatedItem.getAssetNo());
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            System.err.println("资产报废失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 资产报废 - POST方法 (提供兼容性)
    @PostMapping("/items/{id}/retire")
    public ResponseEntity<InventoryItem> retireInventoryItemPost(@PathVariable Long id, @RequestBody Map<String, String> retireData) {
        System.out.println("收到资产报废请求 (POST): ID=" + id + ", 数据=" + retireData);
        // 调用PUT方法的实现
        return retireInventoryItem(id, retireData);
    }

    @PostMapping("/items/import")
    public ResponseEntity<MessageResponse> importInventoryItems(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("请选择要导入的Excel文件"));
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
                return ResponseEntity.badRequest().body(new MessageResponse("请上传Excel文件(.xlsx或.xls格式)"));
            }

            int count = inventoryService.importInventoryItems(file);
            return ResponseEntity.ok(new MessageResponse("成功导入 " + count + " 条资产记录"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("导入失败: " + e.getMessage()));
        }
    }

    @GetMapping("/items/import-template")
    public ResponseEntity<?> getImportTemplate() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("资产导入模板");

            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 必填字段样式
            CellStyle requiredStyle = workbook.createCellStyle();
            Font requiredFont = workbook.createFont();
            requiredFont.setBold(true);
            requiredFont.setColor(IndexedColors.RED.getIndex());
            requiredStyle.setFont(requiredFont);

            // 黄色背景样式 (用于右侧几列)
            CellStyle yellowStyle = workbook.createCellStyle();
            Font yellowFont = workbook.createFont();
            yellowFont.setBold(true);
            yellowStyle.setFont(yellowFont);
            yellowStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            yellowStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "资产编号",
                "资本化日期",
                "APC 科目", // 注意：这里有空格，与前端模板保持一致
                "资产描述",
                "工厂",
                "成本中心",
                "部门",
                "存放楼层",
                "存放设备体地点",
                "负责人",
                "实际使用人",
                "备注"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

                // 为后面5列设置黄色背景，与前端模板保持一致
                if (i >= 7) {
                    cell.setCellStyle(yellowStyle);
                } else {
                    cell.setCellStyle(headerStyle);
                }

                // 自动调整列宽
                sheet.autoSizeColumn(i);
            }

            // 创建示例数据行，与前端模板保持一致
            // 示例1
            Row exampleRow1 = sheet.createRow(1);
            exampleRow1.createCell(0).setCellValue("5002");
            exampleRow1.createCell(1).setCellValue("01/31/2015");
            exampleRow1.createCell(2).setCellValue("160301");
            exampleRow1.createCell(3).setCellValue("RD500 BGA返修平台");
            exampleRow1.createCell(4).setCellValue("PS");
            exampleRow1.createCell(5).setCellValue("8090130601");
            exampleRow1.createCell(6).setCellValue("PS Engineering");

            // 示例2
            Row exampleRow2 = sheet.createRow(2);
            exampleRow2.createCell(0).setCellValue("5003");
            exampleRow2.createCell(1).setCellValue("12/01/2014");
            exampleRow2.createCell(2).setCellValue("160301");
            exampleRow2.createCell(3).setCellValue("自动上板机");
            exampleRow2.createCell(4).setCellValue("PS");
            exampleRow2.createCell(5).setCellValue("8090130102");
            exampleRow2.createCell(6).setCellValue("PS APU SMT");

            // 示例3
            Row exampleRow3 = sheet.createRow(3);
            exampleRow3.createCell(0).setCellValue("5115");
            exampleRow3.createCell(1).setCellValue("06/01/1999");
            exampleRow3.createCell(2).setCellValue("160301");
            exampleRow3.createCell(3).setCellValue("BGA测试仪PAC8017-0");
            exampleRow3.createCell(4).setCellValue("PS");
            exampleRow3.createCell(5).setCellValue("8090130601");
            exampleRow3.createCell(6).setCellValue("PS Engineering");

            // 将工作簿写入字节数组
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            // 创建响应
            byte[] bytes = outputStream.toByteArray();
            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition", "attachment; filename=资产导入模板.xlsx")
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("获取导入模板失败: " + e.getMessage()));
        }
    }

    @GetMapping("/items/export")
    public ResponseEntity<?> exportInventoryItems(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false) String assetNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {

        return inventoryService.exportInventoryItems(ids, assetNo, name, type, department, status, keyword);
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getInventoryTypes() {
        return ResponseEntity.ok(inventoryService.getInventoryTypes());
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getInventoryStatuses() {
        return ResponseEntity.ok(inventoryService.getInventoryStatuses());
    }

    @GetMapping("/factories")
    public ResponseEntity<List<String>> getAllFactories() {
        try {
            // 直接使用服务层方法获取所有工厂
            List<String> factoryList = inventoryService.getAllFactories();

            // 如果服务层方法返回的列表为空，则尝试从所有资产中提取
            if (factoryList == null || factoryList.isEmpty()) {
                System.out.println("服务层方法返回的工厂列表为空，尝试从所有资产中提取");

                // 获取所有资产项
                List<InventoryItem> allItems = inventoryService.getAllInventoryItems();
                System.out.println("获取所有资产项成功，共 " + allItems.size() + " 条记录");

                // 提取所有唯一的工厂
                Set<String> factories = new HashSet<>();
                for (InventoryItem item : allItems) {
                    if (item.getFactory() != null && !item.getFactory().trim().isEmpty()) {
                        factories.add(item.getFactory().trim());
                    }
                }

                factoryList = new ArrayList<>(factories);
                Collections.sort(factoryList); // 排序，便于查看
            }

            System.out.println("获取工厂列表成功，共 " + factoryList.size() + " 条记录: " + String.join(", ", factoryList));
            return ResponseEntity.ok(factoryList);
        } catch (Exception e) {
            System.err.println("获取工厂列表失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>()); // 返回空列表而不是错误，避免前端崩溃
        }
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getAllDepartments() {
        try {
            // 直接使用服务层方法获取所有部门
            List<String> departmentList = inventoryService.getAllDepartments();

            // 如果服务层方法返回的列表为空，则尝试从所有资产中提取
            if (departmentList == null || departmentList.isEmpty()) {
                System.out.println("服务层方法返回的部门列表为空，尝试从所有资产中提取");

                // 获取所有资产项
                List<InventoryItem> allItems = inventoryService.getAllInventoryItems();
                System.out.println("获取所有资产项成功，共 " + allItems.size() + " 条记录");

                // 提取所有唯一的部门
                Set<String> departments = new HashSet<>();
                for (InventoryItem item : allItems) {
                    if (item.getDepartment() != null && !item.getDepartment().trim().isEmpty()) {
                        departments.add(item.getDepartment().trim());
                    }
                }

                departmentList = new ArrayList<>(departments);
                Collections.sort(departmentList); // 排序，便于查看
            }

            System.out.println("获取部门列表成功，共 " + departmentList.size() + " 条记录: " + String.join(", ", departmentList));
            return ResponseEntity.ok(departmentList);
        } catch (Exception e) {
            System.err.println("获取部门列表失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>()); // 返回空列表而不是错误
        }
    }

    @GetMapping("/floors")
    public ResponseEntity<List<String>> getAllFloors() {
        try {
            // 直接使用服务层方法获取所有楼层
            List<String> floorList = inventoryService.getAllFloors();

            // 如果服务层方法返回的列表为空，则尝试从所有资产中提取
            if (floorList == null || floorList.isEmpty()) {
                System.out.println("服务层方法返回的楼层列表为空，尝试从所有资产中提取");

                // 获取所有资产项
                List<InventoryItem> allItems = inventoryService.getAllInventoryItems();
                System.out.println("获取所有资产项成功，共 " + allItems.size() + " 条记录");

                // 提取所有唯一的楼层
                Set<String> floors = new HashSet<>();
                for (InventoryItem item : allItems) {
                    if (item.getFloor() != null && !item.getFloor().trim().isEmpty()) {
                        floors.add(item.getFloor().trim());
                    }
                }

                floorList = new ArrayList<>(floors);
                Collections.sort(floorList); // 排序，便于查看
            }

            System.out.println("获取楼层列表成功，共 " + floorList.size() + " 条记录: " + String.join(", ", floorList));
            return ResponseEntity.ok(floorList);
        } catch (Exception e) {
            System.err.println("获取楼层列表失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>()); // 返回空列表而不是错误
        }
    }

    // 删除所有资产 - 明确的路径，避免与{id}路径冲突
    @RequestMapping(value = "/items/clear-all", method = {RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<MessageResponse> deleteAllItems() {
        try {
            inventoryService.deleteAllInventoryItems();
            return ResponseEntity.ok(new MessageResponse("所有资产已成功删除"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("删除失败: " + e.getMessage()));
        }
    }

    // 新增一个操作特定目的的端点，更少可能与其他路由冲突
    @RequestMapping(value = "/special/destroy-all-assets", method = {RequestMethod.POST, RequestMethod.DELETE, RequestMethod.GET})
    public ResponseEntity<MessageResponse> specialDeleteAllItems() {
        try {
            inventoryService.deleteAllInventoryItems();
            return ResponseEntity.ok(new MessageResponse("所有资产已成功删除 (special)"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("删除失败: " + e.getMessage()));
        }
    }

    // 测试端点
    @RequestMapping(value = "/items/test-clear", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<MessageResponse> testDeleteEndpoint() {
        return ResponseEntity.ok(new MessageResponse("测试端点可以访问"));
    }

    // 直接查询资产编号端点
    @GetMapping("/items/direct-query")
    public ResponseEntity<?> directQueryByAssetNo(@RequestParam String assetNo) {
        try {
            System.out.println("直接查询资产编号: " + assetNo);

            // 打印所有资产编号，便于调试
            List<InventoryItem> allItems = inventoryService.getAllInventoryItems();
            System.out.println("数据库中所有资产编号: " +
                allItems.stream().map(InventoryItem::getAssetNo).collect(Collectors.joining(", ")));

            // 尝试直接从所有资产中查找匹配的资产编号
            List<InventoryItem> matchedItems = allItems.stream()
                .filter(item -> {
                    if (item.getAssetNo() == null) return false;

                    // 尝试多种匹配方式
                    return item.getAssetNo().equals(assetNo) ||  // 精确匹配
                           item.getAssetNo().equalsIgnoreCase(assetNo) ||  // 不区分大小写
                           item.getAssetNo().contains(assetNo) ||  // 包含关系
                           assetNo.contains(item.getAssetNo());  // 反向包含
                })
                .collect(Collectors.toList());

            if (!matchedItems.isEmpty()) {
                System.out.println("直接匹配找到 " + matchedItems.size() + " 条记录");
                return ResponseEntity.ok(matchedItems);
            }

            // 使用服务层方法查询
            PagedResponse<InventoryItem> response = inventoryService.getInventoryItems(
                    0, 100, assetNo, "", "", "", "", "", "");

            if (response.getData() != null && !response.getData().isEmpty()) {
                System.out.println("查询成功，找到 " + response.getData().size() + " 条记录");
                return ResponseEntity.ok(response.getData());
            }

            System.out.println("所有查询方法都未找到资产编号为 '" + assetNo + "' 的记录");
            return ResponseEntity.ok(new MessageResponse("未找到资产编号为 '" + assetNo + "' 的记录"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("查询失败: " + e.getMessage()));
        }
    }
}
