package com.ict.system.service.impl;

import com.ict.system.model.MaintenanceOrder;
import com.ict.system.repository.MaintenanceOrderRepository;
import com.ict.system.service.MaintenanceOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceOrderServiceImpl implements MaintenanceOrderService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceOrderServiceImpl.class);

    @Autowired
    private MaintenanceOrderRepository maintenanceOrderRepository;

    @Override
    public Optional<MaintenanceOrder> findById(Long id) {
        log.info("通过ID查找维修单: {}", id);
        return maintenanceOrderRepository.findById(id);
    }

    @Override
    public Optional<MaintenanceOrder> findByOrderNo(String orderNo) {
        log.info("通过单号查找维修单: {}", orderNo);
        return maintenanceOrderRepository.findByOrderNo(orderNo);
    }

    @Override
    public List<MaintenanceOrder> findAll() {
        log.info("查找所有维修单");
        return maintenanceOrderRepository.findAll();
    }

    @Override
    public List<MaintenanceOrder> findAll(int page, int size) {
        log.info("分页查找维修单, 页码: {}, 每页大小: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return maintenanceOrderRepository.findAll(pageable).getContent();
    }

    @Override
    public List<MaintenanceOrder> findByStatus(String status) {
        log.info("通过状态查找维修单: {}", status);
        return maintenanceOrderRepository.findByStatus(status);
    }

    @Override
    public List<MaintenanceOrder> findByRequester(String requester) {
        log.info("通过申请人查找维修单: {}", requester);
        return maintenanceOrderRepository.findByRequester(requester);
    }

    @Override
    public List<MaintenanceOrder> findByAssignee(String assignee) {
        log.info("通过处理人查找维修单: {}", assignee);
        return maintenanceOrderRepository.findByAssignee(assignee);
    }

    @Override
    public List<MaintenanceOrder> findByAssetId(String assetId) {
        log.info("通过资产ID查找维修单: {}", assetId);
        return maintenanceOrderRepository.findByAssetId(assetId);
    }

    @Override
    public List<MaintenanceOrder> findByRequesterDepartment(String department) {
        log.info("通过申请部门查找维修单: {}", department);
        return maintenanceOrderRepository.findByRequesterDepartment(department);
    }

    @Override
    public List<MaintenanceOrder> findByRequestDateBetween(Date startDate, Date endDate) {
        log.info("通过申请日期范围查找维修单: {} - {}", startDate, endDate);
        return maintenanceOrderRepository.findByRequestDateBetween(startDate, endDate);
    }

    @Override
    public List<MaintenanceOrder> findByPriority(String priority) {
        log.info("通过优先级查找维修单: {}", priority);
        return maintenanceOrderRepository.findByPriority(priority);
    }

    @Override
    public MaintenanceOrder save(MaintenanceOrder order) {
        if (order.getId() == null) {
            // 新维修单
            log.info("创建新维修单: {}", order.getTitle());
            order.setCreatedAt(new Date());
            
            // 生成维修单号
            if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
                String orderNo = generateOrderNo();
                order.setOrderNo(orderNo);
            }
        } else {
            log.info("更新维修单: {}, ID: {}", order.getTitle(), order.getId());
        }
        order.setUpdatedAt(new Date());
        return maintenanceOrderRepository.save(order);
    }

    @Override
    public void deleteById(Long id) {
        log.info("删除维修单, ID: {}", id);
        maintenanceOrderRepository.deleteById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        log.info("批量删除维修单, IDs: {}", ids);
        List<MaintenanceOrder> orders = new ArrayList<>();
        for (Long id : ids) {
            maintenanceOrderRepository.findById(id).ifPresent(orders::add);
        }
        maintenanceOrderRepository.deleteAll(orders);
    }

    @Override
    public MaintenanceOrder updateStatus(Long id, String status, String operator, String comments) {
        log.info("更新维修单状态, ID: {}, 状态: {}, 操作人: {}", id, status, operator);
        Optional<MaintenanceOrder> orderOpt = maintenanceOrderRepository.findById(id);
        if (!orderOpt.isPresent()) {
            log.warn("找不到ID为{}的维修单", id);
            return null;
        }

        MaintenanceOrder order = orderOpt.get();
        
        // 更新状态
        order.setStatus(status);
        
        // 根据状态更新相关字段
        if ("IN_PROGRESS".equals(status) && order.getStartDate() == null) {
            order.setStartDate(new Date());
        } else if ("COMPLETED".equals(status) && order.getCompletionDate() == null) {
            order.setCompletionDate(new Date());
        }
        
        // 添加历史记录
        MaintenanceOrder.MaintenanceHistory history = new MaintenanceOrder.MaintenanceHistory();
        history.setTimestamp(new Date());
        history.setStatus(status);
        history.setOperator(operator);
        history.setComments(comments);
        
        List<MaintenanceOrder.MaintenanceHistory> historyList = order.getHistory();
        if (historyList == null) {
            historyList = new ArrayList<>();
        }
        historyList.add(history);
        order.setHistory(historyList);
        
        // 更新时间
        order.setUpdatedAt(new Date());
        
        return maintenanceOrderRepository.save(order);
    }
    
    /**
     * 生成维修单号
     * @return 维修单号
     */
    private String generateOrderNo() {
        String prefix = "MO-" + new java.text.SimpleDateFormat("yyyyMMdd").format(new Date()) + "-";
        long count = maintenanceOrderRepository.count() + 1;
        return prefix + String.format("%03d", count % 1000);
    }
}
