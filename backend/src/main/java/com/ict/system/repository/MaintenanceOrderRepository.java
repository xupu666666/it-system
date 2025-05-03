package com.ict.system.repository;

import com.ict.system.model.MaintenanceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceOrderRepository extends JpaRepository<MaintenanceOrder, Long> {

    Optional<MaintenanceOrder> findByOrderNo(String orderNo);

    List<MaintenanceOrder> findByStatus(String status);

    List<MaintenanceOrder> findByRequester(String requester);

    List<MaintenanceOrder> findByAssignee(String assignee);

    List<MaintenanceOrder> findByAssetId(String assetId);

    List<MaintenanceOrder> findByRequesterDepartment(String department);

    List<MaintenanceOrder> findByRequestDateBetween(Date startDate, Date endDate);

    List<MaintenanceOrder> findByPriority(String priority);
}