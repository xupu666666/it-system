package com.ict.system.controller;

import com.ict.system.model.InventoryCheckRecord;
import com.ict.system.model.InventoryCheckTask;
import com.ict.system.model.InventoryItem;
import com.ict.system.payload.response.MessageResponse;
import com.ict.system.service.InventoryCheckTaskService;
import com.ict.system.service.InventoryService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

@RestController
@RequestMapping("/api/inventory-check-task")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"},
    allowedHeaders = "*",
    exposedHeaders = {"Content-Disposition", "Content-Type", "Content-Length"},
    allowCredentials = "true",
    maxAge = 3600,
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class InventoryCheckTaskController {
    @Autowired
    private InventoryCheckTaskService taskService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private com.ict.system.service.InventoryCheckRecordService recordService;

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody InventoryCheckTask task) {
        try {
            InventoryCheckTask createdTask = taskService.createTask(task);
            return ResponseEntity.ok(createdTask);
        } catch (Exception e) {
            System.err.println("创建盘点任务失败，错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("创建盘点任务失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody InventoryCheckTask task) {
        try {
            InventoryCheckTask updatedTask = taskService.updateTask(id, task);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            System.err.println("更新盘点任务失败，任务ID: " + id + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("更新盘点任务失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok(new MessageResponse("盘点任务删除成功"));
        } catch (Exception e) {
            System.err.println("删除盘点任务失败，任务ID: " + id + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("删除盘点任务失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTask(@PathVariable Long id) {
        try {
            InventoryCheckTask task = taskService.getTask(id);
            if (task == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            System.err.println("获取盘点任务详情失败，任务ID: " + id + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取盘点任务详情失败: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTasks() {
        try {
            List<InventoryCheckTask> tasks = taskService.getAllTasks();
            System.out.println("获取所有盘点任务成功，共 " + tasks.size() + " 条记录");
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            System.err.println("获取所有盘点任务失败，错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取所有盘点任务失败: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> getTasksByFactoryAndDepartment(
            @RequestParam String factory,
            @RequestParam String department) {
        try {
            List<InventoryCheckTask> tasks = taskService.getTasksByFactoryAndDepartment(factory, department);
            System.out.println("按工厂和部门获取盘点任务成功，工厂: " + factory + ", 部门: " + department + ", 共 " + tasks.size() + " 条记录");
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            System.err.println("获取盘点任务失败，工厂: " + factory + ", 部门: " + department + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取盘点任务失败: " + e.getMessage()));
        }
    }

    /**
     * 下载资产盘点导入模板
     * @return Excel模板文件
     */
    @GetMapping("/template")
    @CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"},
        allowedHeaders = "*",
        exposedHeaders = {"Content-Disposition", "Content-Type", "Content-Length"},
        allowCredentials = "true",
        maxAge = 3600,
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
    public ResponseEntity<byte[]> downloadTemplate() {
        System.out.println("=== 开始处理模板下载请求 ===");
        try {
            System.out.println("1. 开始创建Excel工作簿...");

            // 创建工作簿和工作表
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("资产盘点导入");

            System.out.println("2. 创建标题行...");
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("资产编号");
            headerRow.createCell(1).setCellValue("备注");

            System.out.println("3. 设置列宽...");
            // 设置列宽
            sheet.setColumnWidth(0, 20 * 256);
            sheet.setColumnWidth(1, 30 * 256);

            System.out.println("4. 添加示例数据...");
            // 添加示例数据
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("A12345");
            exampleRow.createCell(1).setCellValue("示例备注");

            System.out.println("5. 将工作簿写入字节数组...");
            // 将工作簿写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            byte[] excelBytes = outputStream.toByteArray();
            System.out.println("6. 模板生成成功，大小: " + excelBytes.length + " 字节");

            System.out.println("7. 设置HTTP响应头...");
            // 设置HTTP响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=asset_import_template.xlsx");
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            System.out.println("8. 返回响应...");
            System.out.println("=== 模板下载请求处理完成 ===");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentLength(excelBytes.length)
                    .body(excelBytes);

        } catch (Exception e) {
            System.err.println("!!! 生成资产盘点导入模板失败 !!!");
            System.err.println("错误类型: " + e.getClass().getName());
            System.err.println("错误消息: " + e.getMessage());
            System.err.println("堆栈跟踪:");
            e.printStackTrace();

            // 尝试记录更多上下文信息
            System.err.println("当前线程: " + Thread.currentThread().getName());
            System.err.println("可用内存: " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + "MB");
            System.err.println("总内存: " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB");

            return ResponseEntity
                    .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * 导入资产到盘点任务
     * @param file Excel文件
     * @param taskId 盘点任务ID
     * @return 导入结果
     */
    @PostMapping("/import-assets")
    @CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"},
        allowedHeaders = "*",
        exposedHeaders = {"Content-Disposition", "Content-Type", "Content-Length"},
        allowCredentials = "true",
        maxAge = 3600,
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
    public ResponseEntity<?> importAssetsToTask(
            @RequestParam("file") MultipartFile file,
            @RequestParam("taskId") String taskIdStr) {

        // 转换taskId为Long类型
        Long taskId;
        try {
            taskId = Long.parseLong(taskIdStr);
        } catch (NumberFormatException e) {
            System.err.println("无效的任务ID格式: " + taskIdStr);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("无效的任务ID格式: " + taskIdStr));
        }
        System.out.println("=== 开始处理资产导入请求 ===");
        System.out.println("任务ID字符串: " + taskIdStr);
        System.out.println("转换后任务ID: " + taskId);

        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            System.err.println("文件为空");
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("请选择要上传的Excel文件"));
        }

        // 检查任务是否存在
        InventoryCheckTask task;
        try {
            task = taskService.getTask(taskId);
            if (task == null) {
                System.err.println("任务不存在: " + taskId);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("任务不存在: " + taskId));
            }
            System.out.println("找到任务: " + task.getTaskName());
        } catch (Exception e) {
            System.err.println("查询任务失败: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(new MessageResponse("查询任务失败: " + e.getMessage()));
        }

        try {
            System.out.println("开始解析Excel文件...");

            // 创建工作簿
            Workbook workbook;
            try {
                workbook = WorkbookFactory.create(file.getInputStream());
            } catch (Exception e) {
                System.err.println("解析Excel文件失败: " + e.getMessage());
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("无法解析Excel文件，请确保文件格式正确: " + e.getMessage()));
            }

            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                System.err.println("Excel文件中没有工作表");
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Excel文件中没有工作表"));
            }

            // 解析标题行
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                System.err.println("Excel文件中没有标题行");
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Excel文件中没有标题行"));
            }

            // 确认标题行格式是否正确
            boolean formatValid = false;
            int assetNoColIndex = -1;
            int remarkColIndex = -1;

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null && cell.getStringCellValue() != null) {
                    String headerValue = cell.getStringCellValue().trim();
                    if ("资产编号".equals(headerValue)) {
                        assetNoColIndex = i;
                        formatValid = true;
                    } else if ("备注".equals(headerValue)) {
                        remarkColIndex = i;
                    }
                }
            }

            if (!formatValid || assetNoColIndex == -1) {
                System.err.println("Excel文件格式不正确，缺少必要的\"资产编号\"列");
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Excel文件格式不正确，缺少必要的\"资产编号\"列"));
            }

            // 逐行处理数据
            List<InventoryCheckRecord> importedRecords = new ArrayList<>();
            int successCount = 0;
            List<String> errors = new ArrayList<>();

            System.out.println("开始处理Excel数据行...");
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue; // 跳过空行
                }

                // 获取资产编号
                Cell assetNoCell = row.getCell(assetNoColIndex);
                if (assetNoCell == null) {
                    errors.add("第" + (i + 1) + "行: 资产编号为空");
                    continue;
                }

                String assetNo = null;

                // 处理不同类型的单元格
                switch (assetNoCell.getCellType()) {
                    case STRING:
                        assetNo = assetNoCell.getStringCellValue().trim();
                        break;
                    case NUMERIC:
                        // 如果是数字，转为字符串
                        assetNo = String.valueOf((long)assetNoCell.getNumericCellValue());
                        break;
                    default:
                        errors.add("第" + (i + 1) + "行: 资产编号格式无效");
                        continue;
                }

                if (assetNo == null || assetNo.isEmpty()) {
                    errors.add("第" + (i + 1) + "行: 资产编号为空");
                    continue;
                }

                // 查询资产
                InventoryItem asset = null;
                try {
                    Optional<InventoryItem> assetOptional = inventoryService.findByAssetNo(assetNo);
                    if (assetOptional.isPresent()) {
                        asset = assetOptional.get();
                    } else {
                        errors.add("第" + (i + 1) + "行: 系统中不存在资产编号为 " + assetNo + " 的资产");
                        // 创建一个临时资产对象，即使在系统中找不到也要创建记录
                        asset = new InventoryItem();
                        asset.setAssetNo(assetNo);
                        asset.setName("未知资产");
                        asset.setType("其他");
                        asset.setCreatedAt(new Date());
                        asset.setUpdatedAt(new Date());
                        // 保存临时资产
                        try {
                            asset = inventoryService.createInventoryItem(asset);
                            System.out.println("创建临时资产成功，ID: " + asset.getId() + ", 编号: " + asset.getAssetNo());
                        } catch (Exception e) {
                            System.err.println("创建临时资产失败: " + e.getMessage());
                            // 处理失败但继续执行
                        }
                    }
                } catch (Exception e) {
                    System.err.println("查询资产失败: " + e.getMessage());
                    errors.add("第" + (i + 1) + "行: 查询资产 " + assetNo + " 失败: " + e.getMessage());
                    continue;
                }

                // 获取备注
                String remark = "";
                if (remarkColIndex != -1) {
                    Cell remarkCell = row.getCell(remarkColIndex);
                    if (remarkCell != null) {
                        switch (remarkCell.getCellType()) {
                            case STRING:
                                remark = remarkCell.getStringCellValue().trim();
                                break;
                            case NUMERIC:
                                remark = String.valueOf(remarkCell.getNumericCellValue());
                                break;
                            default:
                                // 其他类型默认为空
                                break;
                        }
                    }
                }

                // 创建盘点记录
                InventoryCheckRecord record = new InventoryCheckRecord();
                record.setTask(task);
                record.setAsset(asset);
                record.setCheckStatus("正常"); // 默认状态
                record.setRemark(remark);

                // 自动填充实际使用人和存放地点
                if (asset != null) {
                    record.setActualUser(asset.getAssignee());
                    record.setActualLocation(asset.getLocation());
                    record.setActualFloor(asset.getFloor());
                }

                try {
                    // 保存到数据库
                    InventoryCheckRecord savedRecord = recordService.createRecord(record);
                    importedRecords.add(savedRecord);
                    successCount++;
                    System.out.println("导入资产成功: " + assetNo);
                } catch (Exception e) {
                    System.err.println("保存盘点记录失败: " + e.getMessage());
                    errors.add("第" + (i + 1) + "行: 保存盘点记录失败: " + e.getMessage());
                }
            }

            // 关闭工作簿
            workbook.close();

            // 构建导入结果
            Map<String, Object> result = new HashMap<>();
            result.put("count", successCount);
            result.put("errors", errors);

            // 输出导入结果
            System.out.println("=== 导入完成 ===");
            System.out.println("成功导入: " + successCount + " 条记录");
            System.out.println("错误数量: " + errors.size());
            if (!errors.isEmpty()) {
                System.out.println("错误详情: " + String.join("; ", errors));
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("导入资产时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new MessageResponse("导入资产时发生错误: " + e.getMessage()));
        }
    }
}