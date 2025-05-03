package com.ict.system.controller.v2;

import com.ict.system.model.v2.User;
import com.ict.system.payload.response.MessageResponse;
import com.ict.system.service.v2.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户管理控制器
 */
@RestController("userControllerV2")
@RequestMapping("/api/system/users")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true", maxAge = 3600)
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    @Qualifier("userServiceV2")
    private UserService userService;

    /**
     * 获取用户列表
     * @param page 页码
     * @param size 每页大小
     * @return 用户列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('system.user.view')")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取用户列表, 页码: {}, 每页大小: {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Page<User> userPage = userService.findAll(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("users", userPage.getContent());
            response.put("currentPage", userPage.getNumber());
            response.put("totalItems", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());

            log.info("获取用户列表成功, 总数: {}", userPage.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取用户列表失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取用户列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system.user.view')")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        log.info("获取用户详情, ID: {}", id);

        try {
            Optional<User> user = userService.findById(id);

            if (user.isPresent()) {
                log.info("获取用户详情成功, ID: {}", id);
                return ResponseEntity.ok(user.get());
            } else {
                log.warn("获取用户详情失败, ID: {}, 错误: 用户不存在", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("用户不存在"));
            }
        } catch (Exception e) {
            log.error("获取用户详情失败, ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("获取用户详情失败: " + e.getMessage()));
        }
    }

    /**
     * 创建用户
     * @param user 用户对象
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system.user.add')")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        log.info("创建用户: {}", user.getUsername());

        try {
            // 检查用户名是否已存在
            if (userService.existsByUsername(user.getUsername())) {
                log.warn("创建用户失败, 用户名已存在: {}", user.getUsername());
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("用户名已存在"));
            }

            // 检查邮箱是否已存在
            if (user.getEmail() != null && !user.getEmail().isEmpty() &&
                    userService.existsByEmail(user.getEmail())) {
                log.warn("创建用户失败, 邮箱已存在: {}", user.getEmail());
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("邮箱已存在"));
            }

            User createdUser = userService.save(user);
            log.info("创建用户成功, ID: {}, 用户名: {}", createdUser.getId(), createdUser.getUsername());
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            log.error("创建用户失败, 用户名: {}, 错误: {}", user.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("创建用户失败: " + e.getMessage()));
        }
    }

    /**
     * 更新用户
     * @param id 用户ID
     * @param user 用户对象
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system.user.edit')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        log.info("更新用户, ID: {}, 用户名: {}", id, user.getUsername());

        try {
            Optional<User> existingUser = userService.findById(id);

            if (!existingUser.isPresent()) {
                log.warn("更新用户失败, ID: {}, 错误: 用户不存在", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("用户不存在"));
            }

            // 检查用户名是否已存在（排除当前用户）
            if (!existingUser.get().getUsername().equals(user.getUsername()) &&
                    userService.existsByUsername(user.getUsername())) {
                log.warn("更新用户失败, 用户名已存在: {}", user.getUsername());
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("用户名已存在"));
            }

            // 检查邮箱是否已存在（排除当前用户）
            if (user.getEmail() != null && !user.getEmail().isEmpty() &&
                    !existingUser.get().getEmail().equals(user.getEmail()) &&
                    userService.existsByEmail(user.getEmail())) {
                log.warn("更新用户失败, 邮箱已存在: {}", user.getEmail());
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("邮箱已存在"));
            }

            // 设置ID
            user.setId(id);

            // 保留原密码
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(existingUser.get().getPassword());
            }

            User updatedUser = userService.save(user);
            log.info("更新用户成功, ID: {}, 用户名: {}", updatedUser.getId(), updatedUser.getUsername());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("更新用户失败, ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("更新用户失败: " + e.getMessage()));
        }
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system.user.delete')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        log.info("删除用户, ID: {}", id);

        try {
            Optional<User> user = userService.findById(id);

            if (!user.isPresent()) {
                log.warn("删除用户失败, ID: {}, 错误: 用户不存在", id);
                return ResponseEntity.status(404)
                        .body(new MessageResponse("用户不存在"));
            }

            userService.deleteById(id);
            log.info("删除用户成功, ID: {}, 用户名: {}", id, user.get().getUsername());
            return ResponseEntity.ok(new MessageResponse("用户删除成功"));
        } catch (Exception e) {
            log.error("删除用户失败, ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("删除用户失败: " + e.getMessage()));
        }
    }

    /**
     * 批量删除用户
     * @param request 包含用户ID列表的请求
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system.user.delete')")
    public ResponseEntity<?> batchDeleteUsers(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        log.info("批量删除用户, IDs: {}", ids);

        try {
            userService.deleteByIds(ids);
            log.info("批量删除用户成功, 数量: {}", ids.size());
            return ResponseEntity.ok(new MessageResponse("批量删除用户成功"));
        } catch (Exception e) {
            log.error("批量删除用户失败, 错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("批量删除用户失败: " + e.getMessage()));
        }
    }

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param request 包含状态的请求
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system.user.edit')")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        Boolean status = request.get("status");
        log.info("更新用户状态, ID: {}, 状态: {}", id, status);

        try {
            User updatedUser = userService.updateStatus(id, status);
            log.info("更新用户状态成功, ID: {}, 用户名: {}, 状态: {}", id, updatedUser.getUsername(), status);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("更新用户状态失败, ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("更新用户状态失败: " + e.getMessage()));
        }
    }

    /**
     * 重置用户密码
     * @param id 用户ID
     * @param request 包含密码的请求
     * @return 重置结果
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('system.user.edit')")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String password = request.get("password");
        log.info("重置用户密码, ID: {}", id);

        try {
            User updatedUser = userService.resetPassword(id, password);
            log.info("重置用户密码成功, ID: {}, 用户名: {}", id, updatedUser.getUsername());
            return ResponseEntity.ok(new MessageResponse("密码重置成功"));
        } catch (Exception e) {
            log.error("重置用户密码失败, ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new MessageResponse("重置用户密码失败: " + e.getMessage()));
        }
    }
}