// 此文件已被删除，V1版本API已弃用，请使用V2版本API
// 文件路径：backend\src\main\java\com\ict\system\controller\v2\InventoryControllerV2.java

package com.ict.system.controller;

import com.ict.system.model.InventoryItem;
import com.ict.system.payload.request.InventoryCheckRequest;
import com.ict.system.payload.response.MessageResponse;
import com.ict.system.payload.response.PagedResponse;
import com.ict.system.service.InventoryService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 资产盘点控制器
 *
 * 注意：所有API路径都遵循统一的格式：/api/{模块名}/{资源名}
 * 这样可以保持API路径的一致性，便于维护和管理
 *
 * 此控制器已被禁用，请使用V2版本的控制器
 */
// @RestController 已禁用，使用V2版本的控制器
@Component
@RequestMapping("/api/inventory-v1")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/items")
    @PreAuthorize("hasAuthority('inventory.view')")
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
        System.out.println("InventoryController - 接收到查询请求 - 资产编号: " + assetNo +
                          ", 资产描述: " + name +
                          ", APC科目: " + apcAccount +
                          ", 工厂: " + factory +
                          ", 部门: " + department +
                          ", 楼层: " + storageFloor +
                          ", 关键字: " + keyword);

        PagedResponse<InventoryItem> response = inventoryService.getInventoryItems(
                page, size, assetNo, name, apcAccount, factory, department, storageFloor, keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/items/{id}")
    @PreAuthorize("hasAuthority('inventory.view')")
    public ResponseEntity<InventoryItem> getInventoryItem(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryItem(id));
    }

    @PostMapping("/items")
    @PreAuthorize("hasAuthority('inventory.add')")
    public ResponseEntity<InventoryItem> createInventoryItem(@Valid @RequestBody InventoryItem inventoryItem) {
        return ResponseEntity.ok(inventoryService.createInventoryItem(inventoryItem));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasAuthority('inventory.edit')")
    public ResponseEntity<InventoryItem> updateInventoryItem(
            @PathVariable Long id,
            @Valid @RequestBody InventoryItem inventoryItem) {
        return ResponseEntity.ok(inventoryService.updateInventoryItem(id, inventoryItem));
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasAuthority('inventory.delete')")
    public ResponseEntity<MessageResponse> deleteInventoryItem(@PathVariable Long id) {
        inventoryService.deleteInventoryItem(id);
        return ResponseEntity.ok(new MessageResponse("资产删除成功"));
    }

    @DeleteMapping("/items/batch")
    @PreAuthorize("hasAuthority('inventory.delete')")
    public ResponseEntity<MessageResponse> batchDeleteInventoryItems(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        inventoryService.batchDeleteInventoryItems(ids);
        return ResponseEntity.ok(new MessageResponse("批量删除资产成功"));
    }

    @PostMapping("/check")
    // 移除权限限制，便于测试
    // @PreAuthorize("hasAuthority('inventory.check')")
    public ResponseEntity<InventoryItem> checkInventoryItem(@Valid @RequestBody InventoryCheckRequest checkRequest) {
        try {
            InventoryItem result = inventoryService.checkInventoryItem(checkRequest);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // 记录异常但不抛出，返回500状态码
            System.err.println("资产盘点失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(null);
        }
    }

    @PostMapping("/items/import")
    // 暂时移除权限限制，便于测试
    // @PreAuthorize("hasAuthority('inventory.import')")
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
    // 移除权限限制，便于测试
    // @PreAuthorize("hasAuthority('inventory.import')")
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

            // 创建标题行
            Row headerRow = sheet.createRow(0);

            // 设置列标题和样式
            String[] columns = {
                "资产编号(Asset No.)*", "资产名称(Asset Name)*", "资产类型(Asset Type)*",
                "规格型号(Specifications)", "价格(Price)", "购入日期(Purchase Date)",
                "状态(Status)", "使用部门(Department)", "使用人(User)",
                "存放位置(Location)", "备注(Notes)", "品牌(Brand)",
                "型号(Model)", "序列号(Serial No.)", "供应商(Supplier)"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                if (columns[i].contains("*")) {
                    cell.setCellStyle(requiredStyle);
                } else {
                    cell.setCellStyle(headerStyle);
                }
            }

            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("ASSET-2025-001"); // 资产编号
            exampleRow.createCell(1).setCellValue("笔记本电脑"); // 资产名称
            exampleRow.createCell(2).setCellValue("电脑设备"); // 资产类型
            exampleRow.createCell(3).setCellValue("16G/512G SSD/i7"); // 规格型号
            exampleRow.createCell(4).setCellValue(8999.00); // 价格
            exampleRow.createCell(5).setCellValue("2025-01-15"); // 购入日期
            exampleRow.createCell(6).setCellValue("在用"); // 状态
            exampleRow.createCell(7).setCellValue("ICT部"); // 使用部门
            exampleRow.createCell(8).setCellValue("张三"); // 使用人
            exampleRow.createCell(9).setCellValue("2楼办公区"); // 存放位置
            exampleRow.createCell(10).setCellValue("办公用机"); // 备注
            exampleRow.createCell(11).setCellValue("联想"); // 品牌
            exampleRow.createCell(12).setCellValue("ThinkPad X1 Carbon"); // 型号
            exampleRow.createCell(13).setCellValue("SN12345678"); // 序列号
            exampleRow.createCell(14).setCellValue("联想官方旗舰店"); // 供应商

            // 说明行
            Row noteRow = sheet.createRow(2);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("注意：带*的为必填字段。资产编号不可重复。");

            // 调整列宽
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入响应
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"资产导入模板.xlsx\"")
                    .body(outputStream.toByteArray());

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("获取导入模板失败: " + e.getMessage()));
        }
    }

    @GetMapping("/items/export")
    @PreAuthorize("hasAuthority('inventory.export')")
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
    @PreAuthorize("hasAuthority('inventory.view')")
    public ResponseEntity<List<String>> getInventoryTypes() {
        return ResponseEntity.ok(inventoryService.getInventoryTypes());
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAuthority('inventory.view')")
    public ResponseEntity<List<String>> getInventoryStatuses() {
        return ResponseEntity.ok(inventoryService.getInventoryStatuses());
    }

    @GetMapping("/template/download")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        String fileName = "资产导入模板.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

        // 创建工作簿
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sheet1");

        // 设置表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "资产",
            "资本化日期",
            "APC 科目",
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

        // 创建表头样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // 黄色背景样式 (用于右侧几列)
        CellStyle yellowStyle = workbook.createCellStyle();
        Font yellowFont = workbook.createFont();
        yellowFont.setBold(true);
        yellowStyle.setFont(yellowFont);
        yellowStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);

            // 为后面5列设置黄色背景
            if (i >= 7) {
                cell.setCellStyle(yellowStyle);
            } else {
                cell.setCellStyle(headerStyle);
            }
        }

        // 添加示例数据 (与截图一致)
        Row dataRow1 = sheet.createRow(1);
        dataRow1.createCell(0).setCellValue("5002");
        dataRow1.createCell(1).setCellValue("01/31/2015");
        dataRow1.createCell(2).setCellValue("160301");
        dataRow1.createCell(3).setCellValue("RD500 BGA返修平台");
        dataRow1.createCell(4).setCellValue("PS");
        dataRow1.createCell(5).setCellValue("8090130601");
        dataRow1.createCell(6).setCellValue("PS Engineering");

        Row dataRow2 = sheet.createRow(2);
        dataRow2.createCell(0).setCellValue("5003");
        dataRow2.createCell(1).setCellValue("12/01/2014");
        dataRow2.createCell(2).setCellValue("160301");
        dataRow2.createCell(3).setCellValue("自动上板机");
        dataRow2.createCell(4).setCellValue("PS");
        dataRow2.createCell(5).setCellValue("8090130102");
        dataRow2.createCell(6).setCellValue("PS APU SMT");

        Row dataRow3 = sheet.createRow(3);
        dataRow3.createCell(0).setCellValue("5115");
        dataRow3.createCell(1).setCellValue("06/01/1999");
        dataRow3.createCell(2).setCellValue("160301");
        dataRow3.createCell(3).setCellValue("BGA测试仪PAC8017-0");
        dataRow3.createCell(4).setCellValue("PS");
        dataRow3.createCell(5).setCellValue("8090130601");
        dataRow3.createCell(6).setCellValue("PS Engineering");

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 写入响应输出流
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @PostMapping("/items/deleteAll")
    // @PreAuthorize("hasAuthority('inventory.delete')")
    public ResponseEntity<MessageResponse> deleteAllInventoryItems() {
        try {
            inventoryService.deleteAllInventoryItems();
            return ResponseEntity.ok(new MessageResponse("成功删除所有资产记录"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("删除失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/items/deleteAll")
    // @PreAuthorize("hasAuthority('inventory.delete')")
    public ResponseEntity<MessageResponse> deleteAllInventoryItemsWithDelete() {
        try {
            inventoryService.deleteAllInventoryItems();
            return ResponseEntity.ok(new MessageResponse("成功删除所有资产记录"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("删除失败: " + e.getMessage()));
        }
    }

    // 专门用于全部删除的端点，使用一个不会与其他路径冲突的路径
    @RequestMapping(value = "/deleteAllDirectly", method = {RequestMethod.POST, RequestMethod.DELETE, RequestMethod.GET})
    public ResponseEntity<MessageResponse> deleteAllDirectly() {
        try {
            inventoryService.deleteAllInventoryItems();
            return ResponseEntity.ok(new MessageResponse("直接删除 - 成功删除所有资产记录"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("删除失败: " + e.getMessage()));
        }
    }

    @GetMapping("/items/findByAssetNo/{assetNo}")
    public ResponseEntity<?> findByAssetNo(@PathVariable String assetNo) {
        try {
            Optional<InventoryItem> item = inventoryService.findByAssetNo(assetNo);
            if (item.isPresent()) {
                InventoryItem asset = item.get();
                System.out.println("找到资产:" + asset.getAssetNo());
                System.out.println("- 名称: " + asset.getName());
                System.out.println("- 使用人: " + (asset.getAssignee() != null ? asset.getAssignee() : "无"));
                System.out.println("- 位置: " + (asset.getLocation() != null ? asset.getLocation() : "无"));
                System.out.println("- 楼层: " + (asset.getFloor() != null ? asset.getFloor() : "无"));
                
                return ResponseEntity.ok(item.get());
            } else {
                System.out.println("未找到资产编号为 " + assetNo + " 的资产");
                return ResponseEntity.status(404).body(new MessageResponse("资产不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("查询资产失败: " + e.getMessage()));
        }
    }
}
