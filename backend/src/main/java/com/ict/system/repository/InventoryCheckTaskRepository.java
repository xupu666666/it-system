package com.ict.system.repository;

import com.ict.system.model.InventoryCheckTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface InventoryCheckTaskRepository extends JpaRepository<InventoryCheckTask, Long> {
    // 可根据需要添加自定义查询方法
} 