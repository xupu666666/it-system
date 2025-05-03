package com.ict.system.service.v2;

import java.util.List;
import java.util.Map;

public interface AnomalyDetectionService {

    /**
     * 检测异常登录
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 是否异常
     */
    boolean detectAnomalousLogin(String username, String ipAddress, String userAgent);

    /**
     * 获取用户的常用IP地址
     * @param username 用户名
     * @return IP地址列表
     */
    List<String> getUserCommonIpAddresses(String username);

    /**
     * 获取用户的常用设备
     * @param username 用户名
     * @return 设备列表
     */
    List<String> getUserCommonDevices(String username);

    /**
     * 获取用户的登录模式
     * @param username 用户名
     * @return 登录模式
     */
    Map<String, Object> getUserLoginPattern(String username);

    /**
     * 获取所有异常登录
     * @return 异常登录列表
     */
    List<Map<String, Object>> getAllAnomalousLogins();

    /**
     * 获取用户的异常登录
     * @param username 用户名
     * @return 异常登录列表
     */
    List<Map<String, Object>> getUserAnomalousLogins(String username);

    /**
     * 标记异常登录为已处理
     * @param id 异常登录ID
     * @return 是否成功
     */
    boolean markAnomalousLoginAsHandled(Long id);

    /**
     * 清理旧的异常登录记录
     * @param days 保留天数
     * @return 清理的记录数
     */
    int cleanupOldAnomalousLogins(int days);
}
