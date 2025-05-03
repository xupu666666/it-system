package com.ict.system.repository;

import com.ict.system.model.SupplyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplyRequestRepository extends JpaRepository<SupplyRequest, Long> {

    Optional<SupplyRequest> findByRequestNo(String requestNo);

    List<SupplyRequest> findByStatus(String status);

    List<SupplyRequest> findByRequester(String requester);

    List<SupplyRequest> findByApprover(String approver);

    List<SupplyRequest> findByRequesterDepartment(String department);

    List<SupplyRequest> findByRequestDateBetween(Date startDate, Date endDate);

    List<SupplyRequest> findByApproveDate(Date approveDate);
}