package com.ict.system.repository.v2;

import com.ict.system.model.v2.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository("loginAttemptRepositoryV2")
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    // 根据用户名查找登录尝试
    Optional<LoginAttempt> findByUsername(String username);

    // 根据IP地址查找登录尝试
    List<LoginAttempt> findByIpAddress(String ipAddress);

    // 根据用户名和IP地址查找登录尝试
    Optional<LoginAttempt> findByUsernameAndIpAddress(String username, String ipAddress);

    // 查找锁定的登录尝试
    List<LoginAttempt> findByLocked(boolean locked);

    // 查找特定时间之前的登录尝试
    List<LoginAttempt> findByLastAttemptTimeBefore(Date date);

    // 查找锁定过期的登录尝试
    List<LoginAttempt> findByLockedTrueAndLockExpiryTimeBefore(Date date);
}
