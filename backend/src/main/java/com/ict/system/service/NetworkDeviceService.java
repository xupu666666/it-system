package com.ict.system.service;

import com.ict.system.model.NetworkDevice;
import java.util.List;
import java.util.Optional;

public interface NetworkDeviceService {
    
    /**
     * 根据ID查找网络设备
     * @param id 设备ID
     * @return 设备对象
     */
    Optional<NetworkDevice> findById(Long id);
    
    /**
     * 根据IP地址查找网络设备
     * @param ipAddress IP地址
     * @return 设备对象
     */
    Optional<NetworkDevice> findByIpAddress(String ipAddress);
    
    /**
     * 根据MAC地址查找网络设备
     * @param macAddress MAC地址
     * @return 设备对象
     */
    Optional<NetworkDevice> findByMacAddress(String macAddress);
    
    /**
     * 获取所有网络设备
     * @return 设备列表
     */
    List<NetworkDevice> findAll();
    
    /**
     * 分页获取网络设备
     * @param page 页码
     * @param size 每页大小
     * @return 设备列表
     */
    List<NetworkDevice> findAll(int page, int size);
    
    /**
     * 根据类型查找网络设备
     * @param type 设备类型
     * @return 设备列表
     */
    List<NetworkDevice> findByType(String type);
    
    /**
     * 根据位置查找网络设备
     * @param location 设备位置
     * @return 设备列表
     */
    List<NetworkDevice> findByLocation(String location);
    
    /**
     * 根据状态查找网络设备
     * @param status 设备状态
     * @return 设备列表
     */
    List<NetworkDevice> findByStatus(String status);
    
    /**
     * 保存网络设备
     * @param device 设备对象
     * @return 保存后的设备对象
     */
    NetworkDevice save(NetworkDevice device);
    
    /**
     * 删除网络设备
     * @param id 设备ID
     */
    void deleteById(Long id);
    
    /**
     * 批量删除网络设备
     * @param ids 设备ID列表
     */
    void deleteByIds(List<Long> ids);
    
    /**
     * 测试设备连接
     * @param id 设备ID
     * @return 连接结果
     */
    boolean testConnection(Long id);
}
