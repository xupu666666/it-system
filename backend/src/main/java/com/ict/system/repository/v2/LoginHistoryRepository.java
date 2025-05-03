package com.ict.system.repository.v2;

import com.ict.system.model.v2.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository("loginHistoryRepositoryV2")
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    // 根据用户名查找登录历史
    List<LoginHistory> findByUsername(String username);

    // 分页查询用户的登录历史
    Page<LoginHistory> findByUsername(String username, Pageable pageable);

    // 查找特定时间段内的登录历史
    List<LoginHistory> findByLoginTimeBetween(Date startTime, Date endTime);

    // 查找特定时间段内特定用户的登录历史
    List<LoginHistory> findByUsernameAndLoginTimeBetween(String username, Date startTime, Date endTime);

    // 查找特定状态的登录历史
    List<LoginHistory> findByStatus(String status);

    // 查找特定IP地址的登录历史
    List<LoginHistory> findByIpAddress(String ipAddress);

    // 查找最近的登录历史
    List<LoginHistory> findTop10ByUsernameOrderByLoginTimeDesc(String username);

    // 查找失败的登录尝试
    List<LoginHistory> findByUsernameAndStatusOrderByLoginTimeDesc(String username, String status);

    // 统计特定时间段内的登录失败次数
    long countByUsernameAndStatusAndLoginTimeBetween(String username, String status, Date startTime, Date endTime);
}
