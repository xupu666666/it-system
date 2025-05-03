package com.ict.system.repository.v2;

import com.ict.system.model.v2.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository("userSessionRepositoryV2")
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    // 根据令牌查找会话
    Optional<UserSession> findByToken(String token);

    // 根据用户名查找活跃会话
    List<UserSession> findByUsernameAndActiveTrue(String username);

    // 根据用户名和令牌查找会话
    Optional<UserSession> findByUsernameAndToken(String username, String token);

    // 查找过期的会话
    List<UserSession> findByExpiresAtBeforeAndActiveTrue(Date date);

    // 查找特定时间之前的非活跃会话
    List<UserSession> findByLastActivityAtBeforeAndActiveTrue(Date date);

    // 查找特定IP地址的活跃会话
    List<UserSession> findByIpAddressAndActiveTrue(String ipAddress);

    // 查找特定用户代理的活跃会话
    List<UserSession> findByUserAgentContainingAndActiveTrue(String userAgent);

    // 统计用户的活跃会话数
    long countByUsernameAndActiveTrue(String username);

    // 查找所有活跃会话
    List<UserSession> findByActiveTrue();
}
