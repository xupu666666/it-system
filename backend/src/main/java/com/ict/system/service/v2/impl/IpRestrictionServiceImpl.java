package com.ict.system.service.v2.impl;

import com.ict.system.model.v2.IpRestriction;
import com.ict.system.repository.v2.IpRestrictionRepository;
import com.ict.system.service.v2.IpRestrictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service("ipRestrictionServiceV2")
public class IpRestrictionServiceImpl implements IpRestrictionService {

    private static final Logger log = LoggerFactory.getLogger(IpRestrictionServiceImpl.class);

    @Autowired
    @Qualifier("ipRestrictionRepositoryV2")
    private IpRestrictionRepository ipRestrictionRepository;

    @Override
    @Transactional
    public IpRestriction addToWhitelist(String ipAddress, String description, int expiryDays) {
        log.info("添加IP到白名单: {}, 过期天数: {}", ipAddress, expiryDays);
        
        // 检查IP是否已存在
        Optional<IpRestriction> existingRestriction = ipRestrictionRepository.findByIpAddress(ipAddress);
        
        if (existingRestriction.isPresent()) {
            IpRestriction restriction = existingRestriction.get();
            
            // 如果已经是白名单，更新描述和过期时间
            if ("WHITELIST".equals(restriction.getType())) {
                restriction.setDescription(description);
                restriction.setExpiryDays(expiryDays);
                restriction.setActive(true);
                
                log.info("更新白名单IP: {}", ipAddress);
                return ipRestrictionRepository.save(restriction);
            }
            
            // 如果是黑名单，转换为白名单
            restriction.setType("WHITELIST");
            restriction.setDescription(description);
            restriction.setExpiryDays(expiryDays);
            restriction.setActive(true);
            
            log.info("将IP从黑名单转换为白名单: {}", ipAddress);
            return ipRestrictionRepository.save(restriction);
        }
        
        // 创建新的白名单记录
        IpRestriction restriction = new IpRestriction(ipAddress, "WHITELIST", description);
        restriction.setExpiryDays(expiryDays);
        
        log.info("创建新的白名单IP: {}", ipAddress);
        return ipRestrictionRepository.save(restriction);
    }

    @Override
    @Transactional
    public IpRestriction addToBlacklist(String ipAddress, String description, int expiryDays) {
        log.info("添加IP到黑名单: {}, 过期天数: {}", ipAddress, expiryDays);
        
        // 检查IP是否已存在
        Optional<IpRestriction> existingRestriction = ipRestrictionRepository.findByIpAddress(ipAddress);
        
        if (existingRestriction.isPresent()) {
            IpRestriction restriction = existingRestriction.get();
            
            // 如果已经是黑名单，更新描述和过期时间
            if ("BLACKLIST".equals(restriction.getType())) {
                restriction.setDescription(description);
                restriction.setExpiryDays(expiryDays);
                restriction.setActive(true);
                
                log.info("更新黑名单IP: {}", ipAddress);
                return ipRestrictionRepository.save(restriction);
            }
            
            // 如果是白名单，转换为黑名单
            restriction.setType("BLACKLIST");
            restriction.setDescription(description);
            restriction.setExpiryDays(expiryDays);
            restriction.setActive(true);
            
            log.info("将IP从白名单转换为黑名单: {}", ipAddress);
            return ipRestrictionRepository.save(restriction);
        }
        
        // 创建新的黑名单记录
        IpRestriction restriction = new IpRestriction(ipAddress, "BLACKLIST", description);
        restriction.setExpiryDays(expiryDays);
        
        log.info("创建新的黑名单IP: {}", ipAddress);
        return ipRestrictionRepository.save(restriction);
    }

    @Override
    @Transactional
    public boolean removeRestriction(String ipAddress) {
        log.info("移除IP限制: {}", ipAddress);
        
        Optional<IpRestriction> existingRestriction = ipRestrictionRepository.findByIpAddress(ipAddress);
        
        if (!existingRestriction.isPresent()) {
            log.warn("IP限制不存在: {}", ipAddress);
            return false;
        }
        
        IpRestriction restriction = existingRestriction.get();
        
        // 将限制标记为非活跃
        restriction.setActive(false);
        ipRestrictionRepository.save(restriction);
        
        log.info("IP限制已移除: {}", ipAddress);
        return true;
    }

    @Override
    public boolean isWhitelisted(String ipAddress) {
        // 检查IP是否在白名单中且活跃
        boolean whitelisted = ipRestrictionRepository.existsByIpAddressAndTypeAndActive(ipAddress, "WHITELIST", true);
        
        if (whitelisted) {
            // 检查是否过期
            Optional<IpRestriction> restriction = ipRestrictionRepository.findByIpAddressAndType(ipAddress, "WHITELIST");
            
            if (restriction.isPresent() && restriction.get().isExpired()) {
                // 如果过期，移除限制
                removeRestriction(ipAddress);
                return false;
            }
            
            return true;
        }
        
        return false;
    }

    @Override
    public boolean isBlacklisted(String ipAddress) {
        // 检查IP是否在黑名单中且活跃
        boolean blacklisted = ipRestrictionRepository.existsByIpAddressAndTypeAndActive(ipAddress, "BLACKLIST", true);
        
        if (blacklisted) {
            // 检查是否过期
            Optional<IpRestriction> restriction = ipRestrictionRepository.findByIpAddressAndType(ipAddress, "BLACKLIST");
            
            if (restriction.isPresent() && restriction.get().isExpired()) {
                // 如果过期，移除限制
                removeRestriction(ipAddress);
                return false;
            }
            
            return true;
        }
        
        return false;
    }

    @Override
    public List<IpRestriction> getAllWhitelist() {
        log.info("获取所有白名单");
        return ipRestrictionRepository.findByTypeAndActive("WHITELIST", true);
    }

    @Override
    public List<IpRestriction> getAllBlacklist() {
        log.info("获取所有黑名单");
        return ipRestrictionRepository.findByTypeAndActive("BLACKLIST", true);
    }

    @Override
    public Optional<IpRestriction> getRestrictionByIp(String ipAddress) {
        log.info("根据IP地址获取限制: {}", ipAddress);
        return ipRestrictionRepository.findByIpAddress(ipAddress);
    }

    @Override
    @Transactional
    public IpRestriction updateRestriction(IpRestriction ipRestriction) {
        log.info("更新IP限制: {}", ipRestriction.getIpAddress());
        return ipRestrictionRepository.save(ipRestriction);
    }

    @Override
    @Transactional
    public int cleanupExpiredRestrictions() {
        log.info("清理过期的IP限制");
        
        Date now = new Date();
        List<IpRestriction> expiredRestrictions = ipRestrictionRepository.findByExpiresAtBeforeAndActive(now, true);
        
        if (expiredRestrictions.isEmpty()) {
            log.debug("没有过期的IP限制");
            return 0;
        }
        
        // 将所有过期限制标记为非活跃
        for (IpRestriction restriction : expiredRestrictions) {
            restriction.setActive(false);
        }
        
        ipRestrictionRepository.saveAll(expiredRestrictions);
        
        log.info("过期的IP限制已清理, 数量: {}", expiredRestrictions.size());
        return expiredRestrictions.size();
    }
}
