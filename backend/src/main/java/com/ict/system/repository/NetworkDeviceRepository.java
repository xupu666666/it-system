package com.ict.system.repository;

import com.ict.system.model.NetworkDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NetworkDeviceRepository extends JpaRepository<NetworkDevice, Long> {

    Optional<NetworkDevice> findByIpAddress(String ipAddress);

    Optional<NetworkDevice> findByMacAddress(String macAddress);

    List<NetworkDevice> findByType(String type);

    List<NetworkDevice> findByLocation(String location);

    List<NetworkDevice> findByStatus(String status);

    boolean existsByIpAddress(String ipAddress);

    boolean existsByMacAddress(String macAddress);
}