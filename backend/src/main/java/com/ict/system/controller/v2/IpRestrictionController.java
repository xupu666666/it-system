package com.ict.system.controller.v2;

import com.ict.system.model.v2.IpRestriction;
import com.ict.system.payload.response.MessageResponse;
import com.ict.system.service.v2.IpRestrictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController("ipRestrictionControllerV2")
@RequestMapping("/api/system/ip-restrictions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class IpRestrictionController {

    private static final Logger log = LoggerFactory.getLogger(IpRestrictionController.class);

    @Autowired
    @Qualifier("ipRestrictionServiceV2")
    private IpRestrictionService ipRestrictionService;

    /**
     * 获取所有白名单
     * @return 白名单列表
     */
    @GetMapping("/whitelist")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getAllWhitelist() {
        log.info("获取所有白名单");

        try {
            List<IpRestriction> whitelist = ipRestrictionService.getAllWhitelist();

            Map<String, Object> response = new HashMap<>();
            response.put("whitelist", whitelist);
            response.put("count", whitelist.size());

            log.info("获取白名单成功, 数量: {}", whitelist.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取白名单失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取白名单失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有黑名单
     * @return 黑名单列表
     */
    @GetMapping("/blacklist")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> getAllBlacklist() {
        log.info("获取所有黑名单");

        try {
            List<IpRestriction> blacklist = ipRestrictionService.getAllBlacklist();

            Map<String, Object> response = new HashMap<>();
            response.put("blacklist", blacklist);
            response.put("count", blacklist.size());

            log.info("获取黑名单成功, 数量: {}", blacklist.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取黑名单失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取黑名单失败: " + e.getMessage()));
        }
    }

    /**
     * 添加IP到白名单
     * @param ipAddress IP地址
     * @param description 描述
     * @param expiryDays 过期天数
     * @return 添加结果
     */
    @PostMapping("/whitelist")
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> addToWhitelist(
            @RequestParam String ipAddress,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "0") int expiryDays) {
        log.info("添加IP到白名单: {}, 过期天数: {}", ipAddress, expiryDays);

        try {
            IpRestriction restriction = ipRestrictionService.addToWhitelist(ipAddress, description, expiryDays);

            log.info("添加IP到白名单成功: {}", ipAddress);
            return ResponseEntity.ok(restriction);
        } catch (Exception e) {
            log.error("添加IP到白名单失败, IP: {}, 错误: {}", ipAddress, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("添加IP到白名单失败: " + e.getMessage()));
        }
    }

    /**
     * 添加IP到黑名单
     * @param ipAddress IP地址
     * @param description 描述
     * @param expiryDays 过期天数
     * @return 添加结果
     */
    @PostMapping("/blacklist")
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> addToBlacklist(
            @RequestParam String ipAddress,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "0") int expiryDays) {
        log.info("添加IP到黑名单: {}, 过期天数: {}", ipAddress, expiryDays);

        try {
            IpRestriction restriction = ipRestrictionService.addToBlacklist(ipAddress, description, expiryDays);

            log.info("添加IP到黑名单成功: {}", ipAddress);
            return ResponseEntity.ok(restriction);
        } catch (Exception e) {
            log.error("添加IP到黑名单失败, IP: {}, 错误: {}", ipAddress, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("添加IP到黑名单失败: " + e.getMessage()));
        }
    }

    /**
     * 移除IP限制
     * @param ipAddress IP地址
     * @return 移除结果
     */
    @DeleteMapping
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> removeRestriction(@RequestParam String ipAddress) {
        log.info("移除IP限制: {}", ipAddress);

        try {
            boolean result = ipRestrictionService.removeRestriction(ipAddress);

            if (result) {
                log.info("移除IP限制成功: {}", ipAddress);
                return ResponseEntity.ok(new MessageResponse("IP限制已移除"));
            } else {
                log.warn("IP限制不存在: {}", ipAddress);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("IP限制不存在"));
            }
        } catch (Exception e) {
            log.error("移除IP限制失败, IP: {}, 错误: {}", ipAddress, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("移除IP限制失败: " + e.getMessage()));
        }
    }

    /**
     * 检查IP限制状态
     * @param ipAddress IP地址
     * @return 限制状态
     */
    @GetMapping("/check")
    @PreAuthorize("hasAuthority('system.security.view')")
    public ResponseEntity<?> checkIpRestriction(@RequestParam String ipAddress) {
        log.info("检查IP限制状态: {}", ipAddress);

        try {
            boolean whitelisted = ipRestrictionService.isWhitelisted(ipAddress);
            boolean blacklisted = ipRestrictionService.isBlacklisted(ipAddress);

            Map<String, Object> response = new HashMap<>();
            response.put("ipAddress", ipAddress);
            response.put("whitelisted", whitelisted);
            response.put("blacklisted", blacklisted);

            Optional<IpRestriction> restriction = ipRestrictionService.getRestrictionByIp(ipAddress);
            if (restriction.isPresent()) {
                response.put("restriction", restriction.get());
            }

            log.info("检查IP限制状态成功: {}, 白名单: {}, 黑名单: {}", ipAddress, whitelisted, blacklisted);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查IP限制状态失败, IP: {}, 错误: {}", ipAddress, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("检查IP限制状态失败: " + e.getMessage()));
        }
    }

    /**
     * 清理过期的IP限制
     * @return 清理结果
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasAuthority('system.security.manage')")
    public ResponseEntity<?> cleanupExpiredRestrictions() {
        log.info("清理过期的IP限制");

        try {
            int count = ipRestrictionService.cleanupExpiredRestrictions();

            log.info("清理过期的IP限制成功, 数量: {}", count);
            return ResponseEntity.ok(new MessageResponse("成功清理" + count + "个过期的IP限制"));
        } catch (Exception e) {
            log.error("清理过期的IP限制失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("清理过期的IP限制失败: " + e.getMessage()));
        }
    }
}
