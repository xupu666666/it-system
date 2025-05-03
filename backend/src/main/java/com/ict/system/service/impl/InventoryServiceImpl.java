package com.ict.system.service.impl;

import com.ict.system.exception.ResourceNotFoundException;
import com.ict.system.model.InventoryCheckRecord;
import com.ict.system.model.InventoryItem;
import com.ict.system.model.User;
import com.ict.system.payload.request.InventoryCheckRequest;
import com.ict.system.payload.response.PagedResponse;
import com.ict.system.repository.InventoryCheckRecordRepository;
import com.ict.system.repository.InventoryRepository;
import com.ict.system.service.InventoryService;
import com.ict.system.service.UserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserService userService;

    private static final List<String> INVENTORY_TYPES = Arrays.asList(
            "电脑设备", "办公家具", "通信设备", "网络设备", "打印设备", "其他设备"
    );

    private static final List<String> INVENTORY_STATUSES = Arrays.asList(
            "在用", "闲置", "维修", "报废", "借出"
    );

    @Override
    public PagedResponse<InventoryItem> getInventoryItems(
            int page, int size, String assetNo, String name, String apcAccount,
            String factory, String department, String storageFloor, String keyword) {

        // 确保页码从0开始（Spring Data JPA的页码是从0开始的）
        if (page > 0) {
            page = page - 1;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<InventoryItem> inventoryItems;

        // 记录所有查询参数，便于调试
        logger.info("查询参数 - 页码: {}, 大小: {}, 资产编号: '{}', 资产描述: '{}', APC科目: '{}', 工厂: '{}', 部门: '{}', 楼层: '{}', 关键字: '{}'",
                  page, size, assetNo, name, apcAccount, factory, department, storageFloor, keyword);

        if (StringUtils.hasText(keyword)) {
            // 关键字搜索
            logger.info("执行关键字搜索: '{}'", keyword);
            inventoryItems = inventoryRepository.findByKeyword(keyword, pageable);
        } else if (StringUtils.hasText(assetNo) || StringUtils.hasText(name) ||
                   StringUtils.hasText(apcAccount) || StringUtils.hasText(factory) ||
                   StringUtils.hasText(department) || StringUtils.hasText(storageFloor)) {
            // 多字段搜索

            // 记录查询参数，便于调试
            logger.info("执行多字段搜索 - 资产编号: '{}', 资产描述: '{}', APC科目: '{}', 工厂: '{}', 部门: '{}', 楼层: '{}'",
                      assetNo, name, apcAccount, factory, department, storageFloor);

            // 将前端字段名映射到后端字段名
            String type = apcAccount;  // 前端的apcAccount对应后端的type或apcCode
            String floor = storageFloor;  // 前端的storageFloor对应后端的floor
            String status = "";  // 不使用状态过滤

            logger.info("映射后的字段 - type: '{}', floor: '{}'", type, floor);

            // 首先尝试使用资产编号精确查找
            if (StringUtils.hasText(assetNo)) {
                logger.info("尝试使用资产编号精确查找: '{}'", assetNo);

                // 打印所有资产编号，便于调试
                List<InventoryItem> allItems = inventoryRepository.findAll();
                logger.info("数据库中所有资产编号: {}",
                    allItems.stream().map(InventoryItem::getAssetNo).collect(Collectors.joining(", ")));

                Optional<InventoryItem> exactItem = inventoryRepository.findByAssetNo(assetNo);
                if (exactItem.isPresent()) {
                    logger.info("通过精确匹配找到资产: {}", exactItem.get().getAssetNo());
                    List<InventoryItem> singleItemList = new ArrayList<>();
                    singleItemList.add(exactItem.get());
                    return new PagedResponse<>(
                            singleItemList,
                            0,
                            1,
                            1,
                            1
                    );
                } else {
                    logger.info("精确匹配未找到资产编号: '{}'", assetNo);

                    // 尝试使用不区分大小写的比较
                    logger.info("尝试使用不区分大小写的比较查找资产编号");
                    Optional<InventoryItem> caseInsensitiveItem = allItems.stream()
                        .filter(item -> item.getAssetNo() != null &&
                                item.getAssetNo().equalsIgnoreCase(assetNo))
                        .findFirst();

                    if (caseInsensitiveItem.isPresent()) {
                        logger.info("通过不区分大小写比较找到资产: {}", caseInsensitiveItem.get().getAssetNo());
                        List<InventoryItem> singleItemList = new ArrayList<>();
                        singleItemList.add(caseInsensitiveItem.get());
                        return new PagedResponse<>(
                                singleItemList,
                                0,
                                1,
                                1,
                                1
                        );
                    }
                }
            }

            // 如果精确查找失败，使用多字段搜索
            inventoryItems = inventoryRepository.findByMultipleFields(
                    assetNo,
                    name,
                    type,
                    department,
                    status,
                    pageable
            );

            // 如果多字段搜索仍然没有结果，尝试使用模糊查询
            if (inventoryItems.getTotalElements() == 0 && StringUtils.hasText(assetNo)) {
                logger.info("多字段搜索未找到结果，尝试使用模糊查询: '{}'", assetNo);
                inventoryItems = inventoryRepository.findByAssetNoContaining(assetNo, pageable);

                if (inventoryItems.getTotalElements() > 0) {
                    logger.info("通过模糊查询找到 {} 条结果", inventoryItems.getTotalElements());
                } else {
                    logger.info("模糊查询也未找到结果");
                }
            }
        } else {
            // 获取所有
            logger.info("未提供查询条件，获取所有资产");
            inventoryItems = inventoryRepository.findAll(pageable);
        }

        List<InventoryItem> content = inventoryItems.getContent();

        // 记录返回的数据信息，便于调试
        logger.info("查询结果 - 总记录数: {}, 总页数: {}, 当前页: {}, 页大小: {}, 返回记录数: {}",
                  inventoryItems.getTotalElements(),
                  inventoryItems.getTotalPages(),
                  inventoryItems.getNumber(),
                  inventoryItems.getSize(),
                  content.size());

        // 如果使用了资产编号查询，优先使用直接SQL查询
        if (StringUtils.hasText(assetNo)) {
            logger.info("检测到资产编号查询，尝试使用直接SQL查询: '{}'", assetNo);

            // 尝试直接SQL精确匹配
            List<InventoryItem> directItems = inventoryRepository.findByAssetNoDirectSQL(assetNo);
            if (!directItems.isEmpty()) {
                logger.info("直接SQL查询找到 {} 条资产记录", directItems.size());
                return new PagedResponse<>(
                        directItems,
                        1,
                        directItems.size(),
                        directItems.size(),
                        1
                );
            }

            // 尝试直接SQL模糊匹配
            List<InventoryItem> likeItems = inventoryRepository.findByAssetNoContainingDirectSQL(assetNo);
            if (!likeItems.isEmpty()) {
                logger.info("SQL模糊查询找到 {} 条资产记录", likeItems.size());
                return new PagedResponse<>(
                        likeItems,
                        1,
                        likeItems.size(),
                        likeItems.size(),
                        1
                );
            }

            // 如果SQL查询都失败，尝试JPA查询
            if (content.isEmpty()) {
                logger.info("SQL查询未找到结果，尝试使用JPA查询");

                // 尝试精确匹配
                Optional<InventoryItem> exactItem = inventoryRepository.findByAssetNo(assetNo);
                if (exactItem.isPresent()) {
                    logger.info("JPA精确查询找到资产: {}", exactItem.get().getAssetNo());
                    content = new ArrayList<>();
                    content.add(exactItem.get());

                    return new PagedResponse<>(
                            content,
                            1,
                            1,
                            1,
                            1
                    );
                }

                // 尝试模糊匹配
                List<InventoryItem> items = inventoryRepository.findByAssetNoContaining(assetNo, PageRequest.of(0, 100)).getContent();

                if (!items.isEmpty()) {
                    logger.info("JPA模糊查询找到 {} 条资产记录", items.size());
                    return new PagedResponse<>(
                            items,
                            1,
                            items.size(),
                            items.size(),
                            1
                    );
                }

                logger.warn("所有查询方法都未找到资产编号为 '{}' 的记录", assetNo);
            }
        }

        // 修正页码，确保前端显示的页码从1开始
        int displayPage = inventoryItems.getNumber() + 1;

        return new PagedResponse<>(
                content,
                displayPage,
                inventoryItems.getSize(),
                inventoryItems.getTotalElements(),
                inventoryItems.getTotalPages()
        );
    }

    @Override
    public InventoryItem getInventoryItem(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("资产", "id", id));
    }

    @Override
    public InventoryItem createInventoryItem(InventoryItem inventoryItem) {
        // 检查资产编号是否已存在
        if (inventoryRepository.existsByAssetNo(inventoryItem.getAssetNo())) {
            throw new IllegalArgumentException("资产编号已存在");
        }

        // 设置创建和更新时间
        Date now = new Date();
        inventoryItem.setCreatedAt(now);
        inventoryItem.setUpdatedAt(now);

        return inventoryRepository.save(inventoryItem);
    }

    @Override
    public InventoryItem updateInventoryItem(Long id, InventoryItem inventoryItem) {
        InventoryItem existingItem = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("资产", "id", id));

        // 检查资产编号是否已存在（如果更改了资产编号）
        if (!existingItem.getAssetNo().equals(inventoryItem.getAssetNo()) &&
                inventoryRepository.existsByAssetNo(inventoryItem.getAssetNo())) {
            throw new IllegalArgumentException("资产编号已存在");
        }

        // 更新字段
        existingItem.setAssetNo(inventoryItem.getAssetNo());
        existingItem.setName(inventoryItem.getName());
        existingItem.setType(inventoryItem.getType());
        existingItem.setSpecifications(inventoryItem.getSpecifications());
        existingItem.setPurchasePrice(inventoryItem.getPurchasePrice());
        existingItem.setPurchaseDate(inventoryItem.getPurchaseDate());
        existingItem.setStatus(inventoryItem.getStatus());
        existingItem.setDepartment(inventoryItem.getDepartment());
        existingItem.setAssignee(inventoryItem.getAssignee());
        existingItem.setLocation(inventoryItem.getLocation());
        existingItem.setNotes(inventoryItem.getNotes());
        existingItem.setUpdatedAt(new Date());

        return inventoryRepository.save(existingItem);
    }

    @Autowired
    private InventoryCheckRecordRepository checkRecordRepository;

    @Override
    @Transactional
    public void deleteInventoryItem(Long id) {
        InventoryItem inventoryItem = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("资产", "id", id));

        // 先删除与该资产相关的盘点记录
        System.out.println("删除资产ID: " + id + ", 资产编号: " + inventoryItem.getAssetNo());
        List<InventoryCheckRecord> records = checkRecordRepository.findByAssetId(id);
        if (!records.isEmpty()) {
            System.out.println("找到 " + records.size() + " 条相关盘点记录，正在删除...");
            checkRecordRepository.deleteAll(records);
            System.out.println("相关盘点记录删除完成");
        }

        // 然后删除资产
        inventoryRepository.delete(inventoryItem);
        System.out.println("资产删除成功");
    }

    @Override
    @Transactional
    public void batchDeleteInventoryItems(List<Long> ids) {
        System.out.println("批量删除资产，ID列表: " + ids);

        for (Long id : ids) {
            try {
                deleteInventoryItem(id);
            } catch (Exception e) {
                System.err.println("删除资产ID: " + id + " 失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    @Transactional
    public void deleteAllInventoryItems() {
        System.out.println("删除所有资产...");

        // 先删除所有盘点记录
        System.out.println("删除所有盘点记录...");
        checkRecordRepository.deleteAll();
        System.out.println("所有盘点记录删除完成");

        // 然后删除所有资产
        inventoryRepository.deleteAll();
        System.out.println("所有资产删除完成");
    }

    @Override
    public InventoryItem checkInventoryItem(InventoryCheckRequest checkRequest) {
        InventoryItem inventoryItem = inventoryRepository.findById(checkRequest.getId())
                .orElseThrow(() -> new ResourceNotFoundException("资产", "id", checkRequest.getId()));

        // 更新盘点信息
        inventoryItem.setLocation(checkRequest.getLocation());
        inventoryItem.setLastCheckDate(new Date());

        try {
            // 获取当前用户
            User currentUser = userService.getCurrentUser();
            // 防止currentUser为null
            if (currentUser != null) {
                inventoryItem.setLastCheckBy(currentUser.getUsername());
            } else {
                // 如果无法获取当前用户，使用默认值
                inventoryItem.setLastCheckBy("系统用户");
                logger.warn("无法获取当前用户信息，使用默认用户名进行盘点");
            }
        } catch (Exception e) {
            // 捕获并记录异常，但不中断流程
            logger.error("获取当前用户信息失败: {}", e.getMessage(), e);
            inventoryItem.setLastCheckBy("系统用户");
        }

        // 如果盘点状态异常，更新资产状态
        if ("异常".equals(checkRequest.getStatus())) {
            inventoryItem.setStatus("维修");
        } else if ("未找到".equals(checkRequest.getStatus())) {
            inventoryItem.setStatus("待查");
        }

        // 更新备注
        if (StringUtils.hasText(checkRequest.getRemark())) {
            String notes = inventoryItem.getNotes();
            String newNote = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) +
                    " 盘点备注: " + checkRequest.getRemark();

            if (StringUtils.hasText(notes)) {
                notes = notes + "\n" + newNote;
            } else {
                notes = newNote;
            }

            inventoryItem.setNotes(notes);
        }

        inventoryItem.setUpdatedAt(new Date());

        return inventoryRepository.save(inventoryItem);
    }

    @Override
    public int importInventoryItems(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        List<InventoryItem> importedItems = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        int totalRows = 0;
        int skippedRows = 0;
        int processedRows = 0;

        logger.info("开始导入资产Excel文件: {}", file.getOriginalFilename());

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            totalRows = sheet.getLastRowNum();

            logger.info("Excel文件总行数: {}", totalRows);

            // 读取表头，获取列索引
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headerMap = new HashMap<>();

            // 记录所有表头，便于调试
            StringBuilder allHeaders = new StringBuilder("Excel表头: ");

            if (headerRow != null) {
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null) {
                        String headerValue = getCellValueAsString(cell);
                        headerMap.put(headerValue, i);
                        logger.info("表头映射: {} -> {}", headerValue, i);
                        allHeaders.append("[").append(i).append(":").append(headerValue).append("] ");

                        // 特别记录APC科目相关的表头，便于调试
                        if (headerValue.contains("APC") || headerValue.contains("apc") || headerValue.contains("科目")) {
                            logger.info("发现APC相关表头: [{}] 在第{}列, 字符长度:{}, 包含空格:{}",
                                       headerValue, i, headerValue.length(),
                                       headerValue.contains(" ") ? "是" : "否");
                        }

                        // 尝试从带括号的表头中提取中文部分和英文部分
                        // 如: "资产编号(Asset No.)" 提取为 "资产编号" 和 "Asset No."
                        if (headerValue.contains("(") && headerValue.contains(")")) {
                            String chinesePart = headerValue.substring(0, headerValue.indexOf("(")).trim();
                            String englishPart = headerValue.substring(
                                headerValue.indexOf("(") + 1,
                                headerValue.indexOf(")")
                            ).trim();

                            // 分别存储中文和英文表头映射
                            headerMap.put(chinesePart, i);
                            headerMap.put(englishPart, i);
                            logger.info("拆分表头 - 中文: {} -> {}, 英文: {} -> {}",
                                       chinesePart, i, englishPart, i);
                        }
                    }
                }
            }

            // 定义字段名称映射关系（中英文对照）
            Map<String, String> fieldMapping = new HashMap<>();
            // 资产编号相关映射
            fieldMapping.put("资产编号", "assetNo");
            fieldMapping.put("Asset No.", "assetNo");
            fieldMapping.put("AssetNo", "assetNo");
            fieldMapping.put("Asset Number", "assetNo");
            fieldMapping.put("资产", "assetNo");
            fieldMapping.put("编号", "assetNo");
            fieldMapping.put("资产No", "assetNo");
            fieldMapping.put("No", "assetNo");

            // 资本化日期相关映射
            fieldMapping.put("资本化日期", "purchaseDate");
            fieldMapping.put("Capitalization Date", "purchaseDate");
            fieldMapping.put("购入日期", "purchaseDate");
            fieldMapping.put("Purchase Date", "purchaseDate");
            fieldMapping.put("日期", "purchaseDate");
            fieldMapping.put("资本化", "purchaseDate");
            fieldMapping.put("Date", "purchaseDate");

            // APC科目相关映射
            fieldMapping.put("APC科目", "apcCode");
            fieldMapping.put("APC Code", "apcCode");
            fieldMapping.put("科目", "apcCode");
            fieldMapping.put("APC", "apcCode");
            fieldMapping.put("APC 科目", "apcCode"); // 注意这里有空格
            fieldMapping.put("Code", "apcCode");

            // 添加更多可能的APC科目变体，处理空格和大小写问题
            fieldMapping.put("apc科目", "apcCode");
            fieldMapping.put("apc 科目", "apcCode");
            fieldMapping.put("Apc科目", "apcCode");
            fieldMapping.put("Apc 科目", "apcCode");

            // 资产描述相关映射
            fieldMapping.put("资产描述", "name");
            fieldMapping.put("Asset Description", "name");
            fieldMapping.put("资产名称", "name");
            fieldMapping.put("Asset Name", "name");
            fieldMapping.put("描述", "name");
            fieldMapping.put("名称", "name");
            fieldMapping.put("Description", "name");
            fieldMapping.put("Name", "name");

            // 工厂相关映射
            fieldMapping.put("工厂", "factory");
            fieldMapping.put("Factory", "factory");
            fieldMapping.put("厂", "factory");
            fieldMapping.put("工厂代码", "factory");
            fieldMapping.put("Plant", "factory");

            // 成本中心相关映射
            fieldMapping.put("成本中心", "costCenter");
            fieldMapping.put("Cost Center", "costCenter");
            fieldMapping.put("成本", "costCenter");
            fieldMapping.put("中心", "costCenter");
            fieldMapping.put("Center", "costCenter");

            // 部门相关映射
            fieldMapping.put("部门", "department");
            fieldMapping.put("Department", "department");
            fieldMapping.put("部门名称", "department");
            fieldMapping.put("使用部门", "department");
            fieldMapping.put("Dept", "department");

            // 存放楼层相关映射
            fieldMapping.put("存放楼层", "floor");
            fieldMapping.put("Floor", "floor");
            fieldMapping.put("楼层", "floor");
            fieldMapping.put("层", "floor");
            fieldMapping.put("Level", "floor");

            // 存放地点相关映射
            fieldMapping.put("存放设备体地点", "location");
            fieldMapping.put("Storage Location", "location");
            fieldMapping.put("存放的具体地点", "location");
            fieldMapping.put("存放位置", "location");
            fieldMapping.put("地点", "location");
            fieldMapping.put("位置", "location");
            fieldMapping.put("存放", "location");
            fieldMapping.put("Location", "location");
            fieldMapping.put("Place", "location");

            // 负责人相关映射
            fieldMapping.put("负责人", "manager");
            fieldMapping.put("Manager", "manager");
            fieldMapping.put("管理人", "manager");
            fieldMapping.put("主管", "manager");
            fieldMapping.put("经理", "manager");
            fieldMapping.put("Owner", "manager");

            // 实际使用人相关映射
            fieldMapping.put("实际使用人", "assignee");
            fieldMapping.put("Actual User", "assignee");
            fieldMapping.put("使用人", "assignee");
            fieldMapping.put("用户", "assignee");
            fieldMapping.put("人员", "assignee");
            fieldMapping.put("User", "assignee");
            fieldMapping.put("Assignee", "assignee");

            // 备注相关映射
            fieldMapping.put("备注", "notes");
            fieldMapping.put("Notes", "notes");
            fieldMapping.put("注释", "notes");
            fieldMapping.put("说明", "notes");
            fieldMapping.put("附注", "notes");
            fieldMapping.put("Comment", "notes");
            fieldMapping.put("Remark", "notes");

            // 解析所有表头并映射到实际字段名
            Map<String, Integer> mappedHeaderIndices = new HashMap<>();

            // 首先，尝试通过表头文本直接映射
            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                String headerText = entry.getKey();
                Integer columnIndex = entry.getValue();
                String mappedField = fieldMapping.get(headerText);

                if (mappedField != null) {
                    mappedHeaderIndices.put(mappedField, columnIndex);
                    logger.info("直接映射成功: {} -> {} (列索引: {})", headerText, mappedField, columnIndex);
                }
            }

            // 然后，对于未能直接映射的表头，尝试更宽松的匹配方式
            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                String headerText = entry.getKey().toLowerCase();
                Integer columnIndex = entry.getValue();

                // 跳过已经成功映射的字段
                boolean alreadyMapped = false;
                for (Map.Entry<String, String> fieldEntry : fieldMapping.entrySet()) {
                    if (mappedHeaderIndices.containsKey(fieldEntry.getValue()) &&
                        headerMap.get(fieldEntry.getKey()) != null &&
                        headerMap.get(fieldEntry.getKey()).equals(columnIndex)) {
                        alreadyMapped = true;
                        break;
                    }
                }

                if (alreadyMapped) continue;

                // 尝试部分匹配
                for (Map.Entry<String, String> fieldEntry : fieldMapping.entrySet()) {
                    String key = fieldEntry.getKey().toLowerCase();
                    String value = fieldEntry.getValue();

                    // 如果该字段已经映射，跳过
                    if (mappedHeaderIndices.containsKey(value)) continue;

                    // 处理空格问题 - 移除所有空格后比较
                    String keyNoSpace = key.replaceAll("\\s+", "");
                    String headerNoSpace = headerText.replaceAll("\\s+", "");

                    // 如果表头包含关键词或关键词包含表头，或者去除空格后匹配
                    if (headerText.contains(key) || key.contains(headerText) ||
                        headerNoSpace.contains(keyNoSpace) || keyNoSpace.contains(headerNoSpace)) {
                        mappedHeaderIndices.put(value, columnIndex);
                        logger.info("部分匹配成功: {} -> {} (列索引: {})", headerText, value, columnIndex);
                        break;
                    }
                }
            }

            // 输出所有表头信息，便于调试
            logger.info(allHeaders.toString());

            // 记录最终映射结果
            logger.info("最终映射字段数量: {}, 字段列表: {}",
                      mappedHeaderIndices.size(),
                      String.join(", ", mappedHeaderIndices.keySet()));

            // 记录未映射的表头
            Set<String> unmappedHeaders = new HashSet<>(headerMap.keySet());
            for (Map.Entry<String, Integer> entry : mappedHeaderIndices.entrySet()) {
                for (Map.Entry<String, Integer> headerEntry : headerMap.entrySet()) {
                    if (headerEntry.getValue().equals(entry.getValue())) {
                        unmappedHeaders.remove(headerEntry.getKey());
                    }
                }
            }
            if (!unmappedHeaders.isEmpty()) {
                logger.warn("未能映射的表头: {}", String.join(", ", unmappedHeaders));
            }

            // 添加必要字段的默认映射（如果尚未映射）
            if (!mappedHeaderIndices.containsKey("assetNo") && headerRow != null && headerRow.getLastCellNum() > 0) {
                mappedHeaderIndices.put("assetNo", 0); // 默认第一列为资产编号
                logger.info("添加默认映射: assetNo -> 0 (第一列)");
            }

            if (!mappedHeaderIndices.containsKey("name") && headerRow != null && headerRow.getLastCellNum() > 1) {
                mappedHeaderIndices.put("name", 1); // 默认第二列为资产名称
                logger.info("添加默认映射: name -> 1 (第二列)");
            }

            // 跳过标题行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    InventoryItem item = new InventoryItem();

                    // 从映射的字段中读取数据
                    for (Map.Entry<String, Integer> entry : mappedHeaderIndices.entrySet()) {
                        String field = entry.getKey();
                        int columnIndex = entry.getValue();
                        String value = getCellValueAsString(row.getCell(columnIndex)).trim();

                        switch (field) {
                            case "assetNo":
                                item.setAssetNo(value);
                                break;
                            case "purchaseDate":
                                item.setPurchaseDate(value);
                                break;
                            case "apcCode":
                                item.setApcCode(value);
                                break;
                            case "name":
                                item.setName(value);
                                break;
                            case "factory":
                                item.setFactory(value);
                                break;
                            case "costCenter":
                                item.setCostCenter(value);
                                break;
                            case "department":
                                item.setDepartment(value);
                                break;
                            case "floor":
                                item.setFloor(value);
                                break;
                            case "location":
                                item.setLocation(value);
                                break;
                            case "manager":
                                item.setManager(value);
                                break;
                            case "assignee":
                                item.setAssignee(value);
                                break;
                            case "notes":
                                item.setNotes(value);
                                break;
                            default:
                                logger.warn("未知字段: {}", field);
                        }
                    }

                    // 设置默认值，防止导入后字段为空
                    if (item.getAssetNo() == null || item.getAssetNo().trim().isEmpty()) {
                        // 资产编号是必须的，如果没有则生成一个临时编号
                        String tempNo = "TEMP-" + System.currentTimeMillis() + "-" + i;
                        item.setAssetNo(tempNo);
                        logger.warn("第{}行缺少资产编号，已生成临时编号: {}", i, tempNo);
                    }

                    if (item.getName() == null || item.getName().trim().isEmpty()) {
                        item.setName("未命名资产-" + item.getAssetNo());
                        logger.warn("第{}行缺少资产名称，已设置默认值", i);
                    }

                    if (item.getFactory() == null || item.getFactory().trim().isEmpty()) {
                        item.setFactory("未知工厂");
                    }

                    if (item.getDepartment() == null || item.getDepartment().trim().isEmpty()) {
                        item.setDepartment("未分配部门");
                    }

                    if (item.getCostCenter() == null || item.getCostCenter().trim().isEmpty()) {
                        item.setCostCenter("无成本中心");
                    }

                    if (item.getLocation() == null || item.getLocation().trim().isEmpty()) {
                        item.setLocation("未知位置");
                    }

                    // 设置默认资产类型，避免验证失败
                    if (item.getType() == null || item.getType().trim().isEmpty()) {
                        item.setType("其他设备");
                    }

                    // 设置默认值
                    item.setStatus("在用");

                    // 设置创建和更新时间
                    Date now = new Date();
                    item.setCreatedAt(now);
                    item.setUpdatedAt(now);

                    // 数据验证
                    if (validateInventoryItem(item, errorMessages)) {
                        importedItems.add(item);
                        processedRows++;
                        logger.info("成功导入第{}行数据: {}", i + 1, item.getAssetNo());
                    } else {
                        skippedRows++;
                        logger.warn("第{}行数据验证失败，已跳过: {}", i + 1, errorMessages.get(errorMessages.size() - 1));
                    }

                } catch (Exception e) {
                    skippedRows++;
                    String errorMsg = String.format("处理第%d行时发生错误: %s", i + 1, e.getMessage());
                    errorMessages.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }

            // 批量保存有效数据
            if (!importedItems.isEmpty()) {
                inventoryRepository.saveAll(importedItems);
                logger.info("成功保存 {} 条资产记录", importedItems.size());
            }

            // 如果有错误，记录但不抛出异常，让导入过程继续
            if (!errorMessages.isEmpty() && errorMessages.size() > importedItems.size()) {
                logger.warn("导入过程中存在 {} 条错误信息:\n{}", errorMessages.size(), String.join("\n", errorMessages));
            }

            return processedRows;

        } catch (IOException e) {
            logger.error("读取Excel文件失败", e);
            throw new RuntimeException("读取Excel文件失败: " + e.getMessage(), e);
        }
    }

    private boolean validateInventoryItem(InventoryItem item, List<String> errorMessages) {
        boolean isValid = true;

        if (item.getAssetNo() == null || item.getAssetNo().trim().isEmpty()) {
            errorMessages.add("资产编号不能为空");
            isValid = false;
        } else if (inventoryRepository.existsByAssetNo(item.getAssetNo())) {
            errorMessages.add("资产编号已存在：" + item.getAssetNo());
            isValid = false;
        }

        if (item.getName() == null || item.getName().trim().isEmpty()) {
            errorMessages.add("资产名称不能为空");
            isValid = false;
        }

        // 放宽验证，允许类型为空
        if (item.getType() == null || item.getType().trim().isEmpty()) {
            item.setType("其他设备");
        }

        return isValid;
    }

    @Override
    public ResponseEntity<byte[]> exportInventoryItems() {
        List<InventoryItem> items = inventoryRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("资产清单");
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("资产编号");
            headerRow.createCell(2).setCellValue("名称");
            headerRow.createCell(3).setCellValue("类型");
            headerRow.createCell(4).setCellValue("规格型号");
            headerRow.createCell(5).setCellValue("购入日期");
            headerRow.createCell(6).setCellValue("价格");
            headerRow.createCell(7).setCellValue("状态");
            headerRow.createCell(8).setCellValue("部门");
            headerRow.createCell(9).setCellValue("位置");
            headerRow.createCell(10).setCellValue("使用人");
            
            // 填充数据
            int rowNum = 1;
            for (InventoryItem item : items) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getId());
                row.createCell(1).setCellValue(item.getAssetNo() != null ? item.getAssetNo() : "");
                row.createCell(2).setCellValue(item.getName() != null ? item.getName() : "");
                row.createCell(3).setCellValue(item.getType() != null ? item.getType() : "");
                row.createCell(4).setCellValue(item.getSpecifications() != null ? item.getSpecifications() : "");
                row.createCell(5).setCellValue(item.getPurchaseDate() != null ? item.getPurchaseDate() : "");
                
                Cell priceCell = row.createCell(6);
                if (item.getPurchasePrice() != null) {
                    priceCell.setCellValue(item.getPurchasePrice());
                }
                
                row.createCell(7).setCellValue(item.getStatus() != null ? item.getStatus() : "");
                row.createCell(8).setCellValue(item.getDepartment() != null ? item.getDepartment() : "");
                row.createCell(9).setCellValue(item.getLocation() != null ? item.getLocation() : "");
                row.createCell(10).setCellValue(item.getAssignee() != null ? item.getAssignee() : "");
            }
            
            // 自动调整列宽
            for (int i = 0; i < 11; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 将工作簿写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "资产清单.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("导出资产清单失败", e);
        }
    }

    @Override
    public List<String> getInventoryTypes() {
        return INVENTORY_TYPES;
    }

    @Override
    public List<String> getInventoryStatuses() {
        return INVENTORY_STATUSES;
    }

    @Override
    public List<InventoryItem> getAllInventoryItems() {
        return inventoryRepository.findAll();
    }

    @Override
    public List<String> getAllFactories() {
        try {
            List<String> factories = inventoryRepository.findAllFactories();
            logger.info("获取工厂列表成功，共 {} 条记录: {}", factories.size(), factories);
            return factories;
        } catch (Exception e) {
            logger.error("获取工厂列表失败: {}", e.getMessage(), e);
            return new ArrayList<>(); // 返回空列表而不是抛出异常
        }
    }

    @Override
    public List<String> getAllDepartments() {
        try {
            List<String> departments = inventoryRepository.findAllDepartments();
            logger.info("获取部门列表成功，共 {} 条记录: {}", departments.size(), departments);
            return departments;
        } catch (Exception e) {
            logger.error("获取部门列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getAllFloors() {
        try {
            List<String> floors = inventoryRepository.findAllFloors();
            logger.info("获取楼层列表成功，共 {} 条记录: {}", floors.size(), floors);
            return floors;
        } catch (Exception e) {
            logger.error("获取楼层列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // 增强版的单元格值获取方法，更健壮地处理各种类型的单元格值
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                String strValue = cell.getStringCellValue();
                // 尝试解析可能的日期字符串格式 (MM/DD/YYYY)
                if (strValue.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy");
                        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = inputFormat.parse(strValue);
                        return outputFormat.format(date);
                    } catch (ParseException e) {
                        // 解析失败，返回原始字符串
                        return strValue;
                    }
                }
                return strValue;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                }
                // 处理数值型单元格
                double value = cell.getNumericCellValue();
                // 如果是整数，去掉小数点后的零
                if (value == Math.floor(value)) {
                    return String.valueOf((int)value);
                }
                return String.valueOf(value);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((int)numericValue);
                    }
                    return String.valueOf(numericValue);
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }

    @Override
    public Optional<InventoryItem> findByAssetNo(String assetNo) {
        return inventoryRepository.findByAssetNo(assetNo);
    }

    // 另一个导出方法
    @Override
    public ResponseEntity<?> exportInventoryItems(
            List<Long> ids, String assetNo, String name, String type,
            String department, String status, String keyword) {

        List<InventoryItem> itemsToExport;

        if (ids != null && !ids.isEmpty()) {
            // 导出指定ID的资产
            itemsToExport = inventoryRepository.findAllById(ids);
        } else {
            // 根据搜索条件导出
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "updatedAt"));
            Page<InventoryItem> page;

            if (StringUtils.hasText(keyword)) {
                page = inventoryRepository.findByKeyword(keyword, pageable);
            } else if (StringUtils.hasText(assetNo) || StringUtils.hasText(name) ||
                       StringUtils.hasText(type) || StringUtils.hasText(department) ||
                       StringUtils.hasText(status)) {
                // 这里可能需要根据您的实际repository方法调整
                page = inventoryRepository.findAll(pageable);
            } else {
                page = inventoryRepository.findAll(pageable);
            }

            itemsToExport = page.getContent();
        }

        // 生成Excel文件
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("资产列表");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                    "资产编号", "资产名称", "资产类型", "规格型号", "价格", "购入日期",
                    "状态", "使用部门", "使用人", "存放位置", "备注", "盘点日期"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }
            
            // 填充数据
            int rowNum = 1;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (InventoryItem item : itemsToExport) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(item.getAssetNo() != null ? item.getAssetNo() : "");
                row.createCell(1).setCellValue(item.getName() != null ? item.getName() : "");
                row.createCell(2).setCellValue(item.getType() != null ? item.getType() : "");
                row.createCell(3).setCellValue(item.getSpecifications() != null ? item.getSpecifications() : "");
                
                Cell priceCell = row.createCell(4);
                if (item.getPurchasePrice() != null) {
                    priceCell.setCellValue(item.getPurchasePrice());
                }
                
                row.createCell(5).setCellValue(item.getPurchaseDate() != null ? item.getPurchaseDate() : "");
                row.createCell(6).setCellValue(item.getStatus() != null ? item.getStatus() : "");
                row.createCell(7).setCellValue(item.getDepartment() != null ? item.getDepartment() : "");
                row.createCell(8).setCellValue(item.getAssignee() != null ? item.getAssignee() : "");
                row.createCell(9).setCellValue(item.getLocation() != null ? item.getLocation() : "");
                row.createCell(10).setCellValue(item.getNotes() != null ? item.getNotes() : "");
                row.createCell(11).setCellValue(item.getLastCheckDate() != null ? dateFormat.format(item.getLastCheckDate()) : "");
            }
            
            // 调整列宽
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 写入响应
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            String filename = "资产列表_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(outputStream.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("导出资产失败", e);
        }
    }
}
