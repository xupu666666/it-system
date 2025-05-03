package com.ict.system.repository.v2;

import com.ict.system.model.v2.IpRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository("ipRestrictionRepositoryV2")
public interface IpRestrictionRepository extends JpaRepository<IpRestriction, Long> {

    // 根据IP地址查找限制
    Optional<IpRestriction> findByIpAddress(String ipAddress);

    // 根据IP地址和类型查找限制
    Optional<IpRestriction> findByIpAddressAndType(String ipAddress, String type);

    // 根据类型查找限制
    List<IpRestriction> findByType(String type);

    // 根据类型和活跃状态查找限制
    List<IpRestriction> findByTypeAndActive(String type, boolean active);

    // 查找过期的限制
    List<IpRestriction> findByExpiresAtBeforeAndActive(Date date, boolean active);

    // 检查IP地址是否在白名单中
    boolean existsByIpAddressAndTypeAndActive(String ipAddress, String type, boolean active);
}
