package com.ict.system.service.impl;

import com.ict.system.model.NetworkDevice;
import com.ict.system.repository.NetworkDeviceRepository;
import com.ict.system.service.NetworkDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class NetworkDeviceServiceImpl implements NetworkDeviceService {

    private static final Logger log = LoggerFactory.getLogger(NetworkDeviceServiceImpl.class);

    @Autowired
    private NetworkDeviceRepository networkDeviceRepository;

    @Override
    public Optional<NetworkDevice> findById(Long id) {
        log.info("通过ID查找网络设备: {}", id);
        return networkDeviceRepository.findById(id);
    }

    @Override
    public Optional<NetworkDevice> findByIpAddress(String ipAddress) {
        log.info("通过IP地址查找网络设备: {}", ipAddress);
        return networkDeviceRepository.findByIpAddress(ipAddress);
    }

    @Override
    public Optional<NetworkDevice> findByMacAddress(String macAddress) {
        log.info("通过MAC地址查找网络设备: {}", macAddress);
        return networkDeviceRepository.findByMacAddress(macAddress);
    }

    @Override
    public List<NetworkDevice> findAll() {
        log.info("查找所有网络设备");
        return networkDeviceRepository.findAll();
    }

    @Override
    public List<NetworkDevice> findAll(int page, int size) {
        log.info("分页查找网络设备, 页码: {}, 每页大小: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return networkDeviceRepository.findAll(pageable).getContent();
    }

    @Override
    public List<NetworkDevice> findByType(String type) {
        log.info("通过类型查找网络设备: {}", type);
        return networkDeviceRepository.findByType(type);
    }

    @Override
    public List<NetworkDevice> findByLocation(String location) {
        log.info("通过位置查找网络设备: {}", location);
        return networkDeviceRepository.findByLocation(location);
    }

    @Override
    public List<NetworkDevice> findByStatus(String status) {
        log.info("通过状态查找网络设备: {}", status);
        return networkDeviceRepository.findByStatus(status);
    }

    @Override
    public NetworkDevice save(NetworkDevice device) {
        if (device.getId() == null) {
            // 新设备
            log.info("创建新网络设备: {}", device.getName());
            device.setCreatedAt(new Date());
        } else {
            log.info("更新网络设备: {}, ID: {}", device.getName(), device.getId());
        }
        device.setUpdatedAt(new Date());
        return networkDeviceRepository.save(device);
    }

    @Override
    public void deleteById(Long id) {
        log.info("删除网络设备, ID: {}", id);
        networkDeviceRepository.deleteById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        log.info("批量删除网络设备, IDs: {}", ids);
        List<NetworkDevice> devices = new ArrayList<>();
        for (Long id : ids) {
            networkDeviceRepository.findById(id).ifPresent(devices::add);
        }
        networkDeviceRepository.deleteAll(devices);
    }

    @Override
    public boolean testConnection(Long id) {
        log.info("测试网络设备连接, ID: {}", id);
        Optional<NetworkDevice> deviceOpt = networkDeviceRepository.findById(id);
        if (!deviceOpt.isPresent()) {
            log.warn("找不到ID为{}的网络设备", id);
            return false;
        }

        NetworkDevice device = deviceOpt.get();
        String ipAddress = device.getIpAddress();
        log.info("尝试ping设备: {}", ipAddress);

        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            boolean reachable = address.isReachable(5000); // 5秒超时
            
            // 更新设备状态和最后ping时间
            device.setLastPingTime(new Date());
            device.setStatus(reachable ? "ONLINE" : "OFFLINE");
            networkDeviceRepository.save(device);
            
            log.info("设备 {} 连接测试结果: {}", ipAddress, reachable ? "成功" : "失败");
            return reachable;
        } catch (IOException e) {
            log.error("测试设备连接时发生错误: {}", e.getMessage(), e);
            
            // 更新设备状态和最后ping时间
            device.setLastPingTime(new Date());
            device.setStatus("OFFLINE");
            networkDeviceRepository.save(device);
            
            return false;
        }
    }
}
