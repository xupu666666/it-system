package com.ict.system.service.v2;

import com.ict.system.model.v2.IpRestriction;

import java.util.List;
import java.util.Optional;

public interface IpRestrictionService {

    /**
     * 添加IP到白名单
     * @param ipAddress IP地址
     * @param description 描述
     * @param expiryDays 过期天数（0表示永不过期）
     * @return IP限制对象
     */
    IpRestriction addToWhitelist(String ipAddress, String description, int expiryDays);

    /**
     * 添加IP到黑名单
     * @param ipAddress IP地址
     * @param description 描述
     * @param expiryDays 过期天数（0表示永不过期）
     * @return IP限制对象
     */
    IpRestriction addToBlacklist(String ipAddress, String description, int expiryDays);

    /**
     * 从白名单或黑名单中移除IP
     * @param ipAddress IP地址
     * @return 是否成功
     */
    boolean removeRestriction(String ipAddress);

    /**
     * 检查IP是否在白名单中
     * @param ipAddress IP地址
     * @return 是否在白名单中
     */
    boolean isWhitelisted(String ipAddress);

    /**
     * 检查IP是否在黑名单中
     * @param ipAddress IP地址
     * @return 是否在黑名单中
     */
    boolean isBlacklisted(String ipAddress);

    /**
     * 获取所有白名单
     * @return 白名单列表
     */
    List<IpRestriction> getAllWhitelist();

    /**
     * 获取所有黑名单
     * @return 黑名单列表
     */
    List<IpRestriction> getAllBlacklist();

    /**
     * 根据IP地址获取限制
     * @param ipAddress IP地址
     * @return 限制对象
     */
    Optional<IpRestriction> getRestrictionByIp(String ipAddress);

    /**
     * 更新IP限制
     * @param ipRestriction IP限制对象
     * @return 更新后的限制对象
     */
    IpRestriction updateRestriction(IpRestriction ipRestriction);

    /**
     * 清理过期的IP限制
     * @return 清理的记录数
     */
    int cleanupExpiredRestrictions();
}
