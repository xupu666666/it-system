package com.ict.system.controller;

import com.ict.system.model.MaintenanceOrder;
import com.ict.system.payload.response.MessageResponse;
import com.ict.system.service.MaintenanceOrderService;
import com.ict.system.util.ExcelExportUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 维修费用控制器
 *
 * 注意：所有API路径都遵循统一的格式：/api/{模块名}/{资源名}
 * 这样可以保持API路径的一致性，便于维护和管理
 */
@RestController("maintenanceControllerV2")
@RequestMapping("/api/maintenance")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true", maxAge = 3600)
public class MaintenanceController {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceController.class);

    @Autowired
    private MaintenanceOrderService maintenanceOrderService;

    /**
     * 获取维修单列表
     * @param page 页码
     * @param size 每页大小
     * @param orderNo 维修单号
     * @param department 部门
     * @param applicant 申请人
     * @param type 维修类型
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param keyword 关键字
     * @return 维修单列表
     */
    @GetMapping("/orders")
    // 暂时移除权限控制，便于测试
    // @PreAuthorize("hasAuthority('maintenance.view')")
    public ResponseEntity<?> getMaintenanceOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String applicant,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Date startDate,
            @RequestParam(required = false) Date endDate,
            @RequestParam(required = false) String keyword) {

        log.info("获取维修单列表, 页码: {}, 每页大小: {}", page, size);

        try {
            // 创建分页请求
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

            // 获取所有维修单
            List<MaintenanceOrder> orders = maintenanceOrderService.findAll();

            // 根据筛选条件过滤
            List<MaintenanceOrder> filteredOrders = orders.stream()
                .filter(order -> {
                    // 检查订单号
                    if (orderNo != null && !orderNo.isEmpty() &&
                        !order.getOrderNo().toLowerCase().contains(orderNo.toLowerCase())) {
                        return false;
                    }

                    // 检查部门
                    if (department != null && !department.isEmpty() &&
                        !department.equals(order.getRequesterDepartment())) {
                        return false;
                    }

                    // 检查申请人
                    if (applicant != null && !applicant.isEmpty() &&
                        !applicant.equals(order.getRequester())) {
                        return false;
                    }

                    // 检查类型
                    if (type != null && !type.isEmpty() &&
                        !type.equals(order.getAssetType())) {
                        return false;
                    }

                    // 检查状态
                    if (status != null && !status.isEmpty() &&
                        !status.equals(order.getStatus())) {
                        return false;
                    }

                    // 检查日期范围
                    if (startDate != null && order.getCreatedAt() != null &&
                        order.getCreatedAt().before(startDate)) {
                        return false;
                    }
                    if (endDate != null && order.getCreatedAt() != null &&
                        order.getCreatedAt().after(endDate)) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

            // 转换为前端期望的字段格式
            List<Map<String, Object>> formattedOrders = new ArrayList<>();
            for (MaintenanceOrder order : filteredOrders) {
                Map<String, Object> formattedOrder = new HashMap<>();
                formattedOrder.put("id", order.getId());
                formattedOrder.put("orderNo", order.getOrderNo());
                formattedOrder.put("title", order.getTitle());
                formattedOrder.put("description", order.getDescription());
                formattedOrder.put("applicant", order.getRequester()); // 映射 requester -> applicant
                formattedOrder.put("department", order.getRequesterDepartment()); // 映射 requesterDepartment -> department
                formattedOrder.put("type", order.getAssetType()); // 映射 assetType -> type
                formattedOrder.put("equipmentCode", order.getAssetId()); // 映射 assetId -> equipmentCode
                formattedOrder.put("supplier", order.getSupplier());
                formattedOrder.put("handler", order.getAssignee()); // 映射 assignee -> handler
                formattedOrder.put("status", order.getStatus());
                formattedOrder.put("cost", order.getCost()); // 添加维修金额字段
                formattedOrder.put("createdAt", order.getCreatedAt());
                formattedOrder.put("updatedAt", order.getUpdatedAt());
                formattedOrder.put("notes", order.getNotes());
                formattedOrder.put("isWalkingBill", false); // 默认值，因为后端没有对应字段

                // 添加日志，查看每个维修单的数据
                log.info("维修单数据: {}", formattedOrder);

                formattedOrders.add(formattedOrder);
            }

            // 添加日志，查看总的维修单数量
            log.info("维修单总数: {}", filteredOrders.size());

            // 直接返回格式化后的维修单列表，不包装在 content 字段中
            return ResponseEntity.ok(formattedOrders);
        } catch (Exception e) {
            log.error("获取维修单列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取维修单列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取维修单详情
     * @param id 维修单ID
     * @return 维修单详情
     */
    @GetMapping("/orders/{id}")
    // 暂时移除权限控制，便于测试
    // @PreAuthorize("hasAuthority('maintenance.view')")
    public ResponseEntity<?> getMaintenanceOrder(@PathVariable Long id) {
        log.info("获取维修单详情, ID: {}", id);

        try {
            Optional<MaintenanceOrder> order = maintenanceOrderService.findById(id);

            if (order.isPresent()) {
                return ResponseEntity.ok(order.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取维修单详情失败, ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取维修单详情失败: " + e.getMessage()));
        }
    }

    /**
     * 创建维修单
     * @param order 维修单对象
     * @return 创建结果
     */
    @PostMapping("/orders")
    // 暂时移除权限控制，便于测试
    // @PreAuthorize("hasAuthority('maintenance.add')")
    public ResponseEntity<?> createMaintenanceOrder(@Valid @RequestBody Map<String, Object> orderData) {
        log.info("创建维修单: {}", orderData.get("orderNo"));

        try {
            // 将前端字段映射到后端实体
            MaintenanceOrder order = new MaintenanceOrder();
            order.setOrderNo((String) orderData.get("orderNo"));
            order.setTitle((String) orderData.getOrDefault("title", ""));
            order.setDescription((String) orderData.get("description"));
            order.setRequester((String) orderData.get("applicant")); // 映射 applicant -> requester
            order.setRequesterDepartment((String) orderData.get("department")); // 映射 department -> requesterDepartment
            order.setAssetType((String) orderData.get("type")); // 映射 type -> assetType
            // 映射 equipmentCode -> assetId，同时兼容前端可能发送的 assetId 字段
            if (orderData.containsKey("equipmentCode")) {
                order.setAssetId((String) orderData.get("equipmentCode"));
            } else if (orderData.containsKey("assetId")) {
                order.setAssetId((String) orderData.get("assetId"));
            }

            // 添加日志，查看前端发送的数据
            log.info("前端发送的维修单数据: {}", orderData);
            order.setSupplier((String) orderData.get("supplier"));
            order.setAssignee((String) orderData.get("handler")); // 映射 handler -> assignee
            order.setStatus((String) orderData.get("status"));
            order.setNotes((String) orderData.getOrDefault("notes", ""));

            // 设置创建时间和更新时间
            Date now = new Date();
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
            order.setRequestDate(now);

            MaintenanceOrder savedOrder = maintenanceOrderService.save(order);

            // 转换为前端期望的格式
            Map<String, Object> formattedOrder = new HashMap<>();
            formattedOrder.put("id", savedOrder.getId());
            formattedOrder.put("orderNo", savedOrder.getOrderNo());
            formattedOrder.put("title", savedOrder.getTitle());
            formattedOrder.put("description", savedOrder.getDescription());
            formattedOrder.put("applicant", savedOrder.getRequester());
            formattedOrder.put("department", savedOrder.getRequesterDepartment());
            formattedOrder.put("type", savedOrder.getAssetType());
            formattedOrder.put("equipmentCode", savedOrder.getAssetId());
            formattedOrder.put("supplier", savedOrder.getSupplier());
            formattedOrder.put("handler", savedOrder.getAssignee());
            formattedOrder.put("status", savedOrder.getStatus());
            formattedOrder.put("createdAt", savedOrder.getCreatedAt());
            formattedOrder.put("updatedAt", savedOrder.getUpdatedAt());
            formattedOrder.put("notes", savedOrder.getNotes());
            formattedOrder.put("isWalkingBill", orderData.getOrDefault("isWalkingBill", false));

            return ResponseEntity.ok(formattedOrder);
        } catch (Exception e) {
            log.error("创建维修单失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("创建维修单失败: " + e.getMessage()));
        }
    }

    /**
     * 更新维修单
     * @param id 维修单ID
     * @param order 维修单对象
     * @return 更新结果
     */
    @PutMapping("/orders/{id}")
    // 暂时移除权限控制，便于测试
    // @PreAuthorize("hasAuthority('maintenance.edit')")
    public ResponseEntity<?> updateMaintenanceOrder(@PathVariable Long id, @Valid @RequestBody Map<String, Object> orderData) {
        log.info("更新维修单, ID: {}", id);

        try {
            Optional<MaintenanceOrder> existingOrderOpt = maintenanceOrderService.findById(id);

            if (existingOrderOpt.isPresent()) {
                MaintenanceOrder existingOrder = existingOrderOpt.get();

                // 更新字段，保留原有值如果没有提供新值
                if (orderData.containsKey("orderNo")) {
                    existingOrder.setOrderNo((String) orderData.get("orderNo"));
                }
                if (orderData.containsKey("title")) {
                    existingOrder.setTitle((String) orderData.get("title"));
                }
                if (orderData.containsKey("description")) {
                    existingOrder.setDescription((String) orderData.get("description"));
                }
                if (orderData.containsKey("applicant")) {
                    existingOrder.setRequester((String) orderData.get("applicant")); // 映射 applicant -> requester
                }
                if (orderData.containsKey("department")) {
                    existingOrder.setRequesterDepartment((String) orderData.get("department")); // 映射 department -> requesterDepartment
                }
                if (orderData.containsKey("type")) {
                    existingOrder.setAssetType((String) orderData.get("type")); // 映射 type -> assetType
                }
                // 映射 equipmentCode -> assetId，同时兼容前端可能发送的 assetId 字段
                if (orderData.containsKey("equipmentCode")) {
                    existingOrder.setAssetId((String) orderData.get("equipmentCode"));
                } else if (orderData.containsKey("assetId")) {
                    existingOrder.setAssetId((String) orderData.get("assetId"));
                }

                // 添加日志，查看前端发送的数据
                log.info("更新维修单，前端发送的数据: {}", orderData);
                if (orderData.containsKey("supplier")) {
                    existingOrder.setSupplier((String) orderData.get("supplier"));
                }
                if (orderData.containsKey("handler")) {
                    existingOrder.setAssignee((String) orderData.get("handler")); // 映射 handler -> assignee
                }
                if (orderData.containsKey("status")) {
                    existingOrder.setStatus((String) orderData.get("status"));
                }
                if (orderData.containsKey("notes") || orderData.containsKey("remark")) {
                    existingOrder.setNotes((String) orderData.getOrDefault("notes", orderData.getOrDefault("remark", "")));
                }

                // 更新时间
                existingOrder.setUpdatedAt(new Date());

                MaintenanceOrder updatedOrder = maintenanceOrderService.save(existingOrder);

                // 转换为前端期望的格式
                Map<String, Object> formattedOrder = new HashMap<>();
                formattedOrder.put("id", updatedOrder.getId());
                formattedOrder.put("orderNo", updatedOrder.getOrderNo());
                formattedOrder.put("title", updatedOrder.getTitle());
                formattedOrder.put("description", updatedOrder.getDescription());
                formattedOrder.put("applicant", updatedOrder.getRequester());
                formattedOrder.put("department", updatedOrder.getRequesterDepartment());
                formattedOrder.put("type", updatedOrder.getAssetType());
                formattedOrder.put("equipmentCode", updatedOrder.getAssetId());
                formattedOrder.put("supplier", updatedOrder.getSupplier());
                formattedOrder.put("handler", updatedOrder.getAssignee());
                formattedOrder.put("status", updatedOrder.getStatus());
                formattedOrder.put("createdAt", updatedOrder.getCreatedAt());
                formattedOrder.put("updatedAt", updatedOrder.getUpdatedAt());
                formattedOrder.put("notes", updatedOrder.getNotes());
                formattedOrder.put("isWalkingBill", orderData.getOrDefault("isWalkingBill", false));

                return ResponseEntity.ok(formattedOrder);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("更新维修单失败, ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("更新维修单失败: " + e.getMessage()));
        }
    }

    /**
     * 删除维修单
     * @param id 维修单ID
     * @return 删除结果
     */
    @DeleteMapping("/orders/{id}")
    // 暂时移除权限控制，便于测试
    // @PreAuthorize("hasAuthority('maintenance.delete')")
    public ResponseEntity<?> deleteMaintenanceOrder(@PathVariable Long id) {
        log.info("删除维修单, ID: {}", id);

        try {
            Optional<MaintenanceOrder> existingOrder = maintenanceOrderService.findById(id);

            if (existingOrder.isPresent()) {
                maintenanceOrderService.deleteById(id);
                return ResponseEntity.ok(new MessageResponse("维修单删除成功"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("删除维修单失败, ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("删除维修单失败: " + e.getMessage()));
        }
    }

    /**
     * 批量删除维修单
     * @param ids 维修单ID列表
     * @return 删除结果
     */
    @DeleteMapping("/orders/batch")
    // 暂时移除权限控制，便于测试
    // @PreAuthorize("hasAuthority('maintenance.delete')")
    public ResponseEntity<?> batchDeleteMaintenanceOrders(@RequestBody Map<String, List<Long>> ids) {
        log.info("批量删除维修单, IDs: {}", ids.get("ids"));

        try {
            List<Long> idList = ids.get("ids");
            if (idList == null || idList.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("未提供要删除的维修单ID"));
            }

            for (Long id : idList) {
                maintenanceOrderService.deleteById(id);
            }

            return ResponseEntity.ok(new MessageResponse("批量删除维修单成功"));
        } catch (Exception e) {
            log.error("批量删除维修单失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("批量删除维修单失败: " + e.getMessage()));
        }
    }

    /**
     * 更新维修单状态
     * @param id 维修单ID
     * @param statusData 状态数据
     * @return 更新结果
     */
    @PutMapping("/orders/{id}/status")
    // 暂时移除权限控制，便于测试
    // @PreAuthorize("hasAuthority('maintenance.edit')")
    public ResponseEntity<?> updateMaintenanceStatus(@PathVariable Long id, @RequestBody Map<String, Object> statusData) {
        log.info("更新维修单状态, ID: {}, 状态: {}", id, statusData.get("status"));

        try {
            Optional<MaintenanceOrder> existingOrder = maintenanceOrderService.findById(id);

            if (existingOrder.isPresent()) {
                MaintenanceOrder order = existingOrder.get();
                order.setStatus((String) statusData.get("status"));

                // 如果有备注，更新备注
                if (statusData.containsKey("comment") && statusData.get("comment") != null) {
                    String comment = (String) statusData.get("comment");
                    order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") + comment);
                }

                // 如果状态是"已付款"并且提供了维修金额，则更新维修金额
                if ("PAID".equals(order.getStatus()) && statusData.containsKey("cost") && statusData.get("cost") != null) {
                    // 处理不同类型的数值（Integer, Double, String等）
                    Object costObj = statusData.get("cost");
                    Double cost = null;

                    if (costObj instanceof Number) {
                        cost = ((Number) costObj).doubleValue();
                    } else if (costObj instanceof String) {
                        try {
                            cost = Double.parseDouble((String) costObj);
                        } catch (NumberFormatException e) {
                            log.warn("无法解析维修金额: {}", costObj);
                        }
                    }

                    if (cost != null) {
                        order.setCost(cost);
                        log.info("更新维修单金额, ID: {}, 金额: {}", id, cost);
                    }
                }

                // 如果状态是"已完成"，设置完成日期
                if ("COMPLETED".equals(order.getStatus())) {
                    order.setCompletionDate(new Date());
                }

                // 更新时间
                order.setUpdatedAt(new Date());

                MaintenanceOrder updatedOrder = maintenanceOrderService.save(order);

                // 转换为前端期望的格式
                Map<String, Object> formattedOrder = new HashMap<>();
                formattedOrder.put("id", updatedOrder.getId());
                formattedOrder.put("orderNo", updatedOrder.getOrderNo());
                formattedOrder.put("title", updatedOrder.getTitle());
                formattedOrder.put("description", updatedOrder.getDescription());
                formattedOrder.put("applicant", updatedOrder.getRequester());
                formattedOrder.put("department", updatedOrder.getRequesterDepartment());
                formattedOrder.put("type", updatedOrder.getAssetType());
                formattedOrder.put("equipmentCode", updatedOrder.getAssetId());
                formattedOrder.put("supplier", updatedOrder.getSupplier());
                formattedOrder.put("handler", updatedOrder.getAssignee());
                formattedOrder.put("status", updatedOrder.getStatus());
                formattedOrder.put("cost", updatedOrder.getCost());
                formattedOrder.put("createdAt", updatedOrder.getCreatedAt());
                formattedOrder.put("updatedAt", updatedOrder.getUpdatedAt());
                formattedOrder.put("notes", updatedOrder.getNotes());

                return ResponseEntity.ok(formattedOrder);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("更新维修单状态失败, ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("更新维修单状态失败: " + e.getMessage()));
        }
    }

    /**
     * 获取维修类型列表
     * @return 维修类型列表
     */
    @GetMapping("/types")
    // 暂时移除权限控制，便于测试
    // @PreAuthorize("hasAuthority('maintenance.view')")
    public ResponseEntity<?> getMaintenanceTypes() {
        log.info("获取维修类型列表");

        try {
            // 返回预定义的维修类型列表
            List<String> types = Arrays.asList(
                "硬件维修",
                "软件故障",
                "网络问题",
                "打印设备",
                "显示设备",
                "其他"
            );

            return ResponseEntity.ok(types);
        } catch (Exception e) {
            log.error("获取维修类型列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取维修类型列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取维修状态列表
     * @return 维修状态列表
     */
    @GetMapping("/statuses")
    // 暂时移除权限控制，便于测试
    // @PreAuthorize("hasAuthority('maintenance.view')")
    public ResponseEntity<?> getMaintenanceStatuses() {
        log.info("获取维修状态列表");

        try {
            // 返回预定义的维修状态列表
            List<String> statuses = Arrays.asList(
                "PENDING",
                "IN_PROGRESS",
                "COMPLETED",
                "PAID",
                "CANCELLED"
            );

            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            log.error("获取维修状态列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取维修状态列表失败: " + e.getMessage()));
        }
    }

    /**
     * 维修费用统计接口
     * @return 统计信息
     */
    @GetMapping("/statistics")
    // 暂时移除权限控制，便于测试
    // @PreAuthorize("hasAuthority('maintenance.view')")
    public ResponseEntity<?> getMaintenanceStatistics(
            @RequestParam(defaultValue = "month") String trendType,
            @RequestParam(defaultValue = "count") String typePieMode) {
        List<MaintenanceOrder> orders = maintenanceOrderService.findAll();
        double totalCost = orders.stream().filter(o -> o.getCost() != null).mapToDouble(MaintenanceOrder::getCost).sum();
        long count = orders.size();
        double avgCost = count > 0 ? totalCost / count : 0;
        double maxCost = orders.stream().filter(o -> o.getCost() != null).mapToDouble(MaintenanceOrder::getCost).max().orElse(0);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCost", totalCost);
        stats.put("avgCost", avgCost);
        stats.put("maxCost", maxCost);
        stats.put("orderCount", count);

        // 费用趋势（按天/月/年统计）
        Map<String, Double> trendMap = new TreeMap<>();
        for (MaintenanceOrder order : orders) {
            if (order.getCreatedAt() != null && order.getCost() != null) {
                String key;
                if ("day".equalsIgnoreCase(trendType)) {
                    key = new java.text.SimpleDateFormat("yyyy-MM-dd").format(order.getCreatedAt());
                } else if ("year".equalsIgnoreCase(trendType)) {
                    key = new java.text.SimpleDateFormat("yyyy").format(order.getCreatedAt());
                } else {
                    key = new java.text.SimpleDateFormat("yyyy-MM").format(order.getCreatedAt());
                }
                trendMap.put(key, trendMap.getOrDefault(key, 0.0) + order.getCost());
            }
        }
        List<String> trendX = new ArrayList<>(trendMap.keySet());
        List<Double> trendY = trendX.stream().map(trendMap::get).collect(Collectors.toList());
        Map<String, Object> trendData = new HashMap<>();
        trendData.put("x", trendX);
        trendData.put("y", trendY);
        stats.put("trendData", trendData);

        // 类型分布（按数量或金额统计）
        List<Map<String, Object>> typePieList;
        if ("amount".equalsIgnoreCase(typePieMode)) {
            Map<String, Double> typeAmountMap = orders.stream()
                .filter(o -> o.getAssetType() != null && !o.getAssetType().trim().isEmpty() && o.getCost() != null)
                .collect(Collectors.groupingBy(MaintenanceOrder::getAssetType, Collectors.summingDouble(MaintenanceOrder::getCost)));
            typePieList = typeAmountMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("value", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
        } else {
            Map<String, Long> typeCountMap = orders.stream()
                .filter(o -> o.getAssetType() != null && !o.getAssetType().trim().isEmpty())
                .collect(Collectors.groupingBy(MaintenanceOrder::getAssetType, Collectors.counting()));
            typePieList = typeCountMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("value", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
        }
        Map<String, Object> typePieData = new HashMap<>();
        typePieData.put("data", typePieList);
        stats.put("typePieData", typePieData);

        return ResponseEntity.ok(stats);
    }

    /**
     * 获取所有维修单用过的部门、类型、供应商、状态（用于下拉选项同步）
     */
    @GetMapping("/orders/filters")
    @PreAuthorize("hasAuthority('maintenance.view')")
    public ResponseEntity<?> getMaintenanceOrderFilters() {
        List<MaintenanceOrder> orders = maintenanceOrderService.findAll();
        Set<String> departments = orders.stream()
            .map(MaintenanceOrder::getRequesterDepartment)
            .filter(Objects::nonNull)
            .filter(s -> !s.trim().isEmpty())
            .collect(Collectors.toSet());
        Set<String> types = orders.stream()
            .map(MaintenanceOrder::getAssetType)
            .filter(Objects::nonNull)
            .filter(s -> !s.trim().isEmpty())
            .collect(Collectors.toSet());
        Set<String> suppliers = orders.stream()
            .map(MaintenanceOrder::getSupplier)
            .filter(Objects::nonNull)
            .filter(s -> !s.trim().isEmpty())
            .collect(Collectors.toSet());
        Set<String> statuses = orders.stream()
            .map(MaintenanceOrder::getStatus)
            .filter(Objects::nonNull)
            .filter(s -> !s.trim().isEmpty())
            .collect(Collectors.toSet());
        Map<String, Object> result = new HashMap<>();
        result.put("departments", departments);
        result.put("types", types);
        result.put("suppliers", suppliers);
        result.put("statuses", statuses);
        return ResponseEntity.ok(result);
    }

    /**
     * 导出维修单Excel
     * 临时移除严格的权限检查，允许所有已登录用户访问
     */
    @GetMapping("/orders/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportMaintenanceOrders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String applicant,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            // 打印当前用户及其权限
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            java.util.List<String> authorities = new java.util.ArrayList<>();
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                authorities.add(authority.getAuthority());
            }
            log.info("当前登录用户: {}，权限: {}", username, authorities);

            // 临时注释掉权限检查，允许所有已登录用户导出
            // boolean hasExportPermission = false;
            // for (String authority : authorities) {
            //     if ("maintenance.export".equals(authority) || "ROLE_ADMIN".equals(authority)) {
            //         hasExportPermission = true;
            //         break;
            //     }
            // }

            // if (!hasExportPermission) {
            //     log.warn("用户 {} 尝试导出维修单Excel，但没有所需权限", username);
            //     String errorMsg = "您没有导出权限，请联系管理员";
            //     return ResponseEntity
            //         .status(HttpStatus.FORBIDDEN)
            //         .header("Content-Type", "text/plain; charset=UTF-8")
            //         .body(errorMsg.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            // }

            // 临时添加日志，记录所有用户都可以导出
            log.info("临时允许用户 {} 导出维修单Excel，无需特定权限", username);

            // 记录导出参数
            log.info("导出维修单Excel，参数: orderNo={}, department={}, applicant={}, type={}, status={}, startDate={}, endDate={}",
                    orderNo, department, applicant, type, status, startDate, endDate);

            List<MaintenanceOrder> orders = maintenanceOrderService.findAll();
            log.info("获取到维修单总数: {}", orders.size());

            List<MaintenanceOrder> filtered = filterOrders(orders, orderNo, department, applicant, type, status, startDate, endDate);
            log.info("过滤后的维修单数量: {}", filtered.size());

            if (filtered.isEmpty()) {
                log.warn("没有符合条件的维修单数据可导出");
                String errorMsg = "没有符合条件的数据可导出";
                return ResponseEntity
                    .status(HttpStatus.OK)
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body(errorMsg.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            byte[] excelBytes = ExcelExportUtil.generateMaintenanceExcel(filtered);
            log.info("Excel生成成功，大小: {} 字节", excelBytes.length);

            String filename = URLEncoder.encode("维修费用明细.xlsx", "UTF-8").replaceAll("\\+", "%20");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("导出维修单Excel失败: {}", e.getMessage(), e);
            String errorMsg = "导出失败: " + e.getMessage();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(errorMsg.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    // 新增多条件过滤工具方法
    private List<MaintenanceOrder> filterOrders(List<MaintenanceOrder> orders, String orderNo, String department, String applicant, String type, String status, String startDate, String endDate) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.util.Date start = null;
        java.util.Date end = null;
        try {
            if (startDate != null && !startDate.isEmpty()) start = sdf.parse(startDate);
        } catch (Exception ignored) { start = null; }
        try {
            if (endDate != null && !endDate.isEmpty()) end = sdf.parse(endDate);
        } catch (Exception ignored) { end = null; }
        final java.util.Date finalStart = start;
        final java.util.Date finalEnd = end;
        return orders.stream()
                .filter(order -> orderNo == null || orderNo.isEmpty() || (order.getOrderNo() != null && order.getOrderNo().contains(orderNo)))
                .filter(order -> department == null || department.isEmpty() || department.equals(order.getRequesterDepartment()))
                .filter(order -> applicant == null || applicant.isEmpty() || applicant.equals(order.getRequester()))
                .filter(order -> type == null || type.isEmpty() || type.equals(order.getAssetType()))
                .filter(order -> status == null || status.isEmpty() || status.equals(order.getStatus()))
                .filter(order -> finalStart == null || (order.getCreatedAt() != null && !order.getCreatedAt().before(finalStart)))
                .filter(order -> finalEnd == null || (order.getCreatedAt() != null && !order.getCreatedAt().after(finalEnd)))
                .collect(java.util.stream.Collectors.toList());
    }
}
