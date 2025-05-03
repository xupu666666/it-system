package com.ict.system.service.v2.impl;

import com.ict.system.model.v2.User;
import com.ict.system.repository.v2.UserRepository;
import com.ict.system.service.v2.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import com.ict.system.repository.v2.RoleRepository;
import com.ict.system.model.v2.Role;

@Service("userServiceV2")
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    @Qualifier("userRepositoryV2")
    private UserRepository userRepository;

    @Autowired
    @Qualifier("passwordEncoderV2")
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Optional<User> findById(Long id) {
        log.info("通过ID查找用户: {}", id);
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        log.info("通过用户名查找用户: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> findAll() {
        log.info("查找所有用户");
        return userRepository.findAll();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        log.info("分页查找用户, 页码: {}, 每页大小: {}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            // 新用户
            log.info("创建新用户: {}", user.getUsername());
            user.setCreatedAt(new Date());

            // 加密密码
            if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        } else {
            log.info("更新用户: {}, ID: {}", user.getUsername(), user.getId());

            // 如果更新用户时提供了新密码，则加密
            Optional<User> existingUser = userRepository.findById(user.getId());
            if (existingUser.isPresent()) {
                User existing = existingUser.get();

                // 如果密码没有变化，保留原密码
                if (user.getPassword() == null || user.getPassword().isEmpty() ||
                    user.getPassword().equals(existing.getPassword())) {
                    user.setPassword(existing.getPassword());
                } else if (!user.getPassword().startsWith("$2a$")) {
                    // 如果密码已更改且未加密，则加密
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                }

                // 保留创建时间
                user.setCreatedAt(existing.getCreatedAt());
            }
        }

        // 修正：roles 处理
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Set<Role> persistentRoles = new HashSet<>();
            for (Role role : user.getRoles()) {
                if (role.getId() != null) {
                    Role persistentRole = roleRepository.findById(role.getId())
                        .orElseThrow(() -> new RuntimeException("角色不存在，ID: " + role.getId()));
                    persistentRoles.add(persistentRole);
                } else if (role.getName() != null) {
                    Role persistentRole = roleRepository.findByName(role.getName())
                        .orElseThrow(() -> new RuntimeException("角色不存在，名称: " + role.getName()));
                    persistentRoles.add(persistentRole);
                } else {
                    throw new RuntimeException("角色信息不完整，无法分配角色");
                }
            }
            user.setRoles(persistentRoles);
        }

        user.setUpdatedAt(new Date());
        User savedUser = userRepository.save(user);
        log.info("用户保存成功: {}, ID: {}", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    public void delete(User user) {
        log.info("删除用户: {}, ID: {}", user.getUsername(), user.getId());
        userRepository.delete(user);
        log.info("用户删除成功");
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("删除用户, ID: {}", id);
        try {
            // 检查用户是否存在
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                log.error("删除用户失败, ID: {}, 错误: 用户不存在", id);
                throw new RuntimeException("用户不存在，ID: " + id);
            }

            User user = userOpt.get();
            log.info("找到要删除的用户: {}, ID: {}", user.getUsername(), user.getId());

            // 删除用户
            userRepository.delete(user);
            log.info("用户删除成功, ID: {}", id);
        } catch (Exception e) {
            log.error("删除用户失败, ID: {}, 错误类型: {}, 错误信息: {}", id, e.getClass().getName(), e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        log.info("批量删除用户, IDs: {}", ids);
        List<User> users = new ArrayList<>();

        // 收集要删除的用户
        for (Long id : ids) {
            userRepository.findById(id).ifPresent(users::add);
        }

        if (users.isEmpty()) {
            log.warn("没有找到要删除的用户");
            return;
        }

        try {
            userRepository.deleteAll(users);
            log.info("批量删除用户成功, 数量: {}", users.size());
        } catch (Exception e) {
            log.error("批量删除用户失败, 错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public User updateStatus(Long id, boolean enabled) {
        log.info("更新用户状态, ID: {}, 状态: {}", id, enabled);
        Optional<User> userOpt = userRepository.findById(id);

        if (!userOpt.isPresent()) {
            log.error("更新用户状态失败, ID: {}, 错误: 用户不存在", id);
            throw new RuntimeException("用户不存在，ID: " + id);
        }

        User user = userOpt.get();
        user.setEnabled(enabled);
        user.setUpdatedAt(new Date());

        User savedUser = userRepository.save(user);
        log.info("用户状态更新成功, ID: {}, 新状态: {}", id, enabled);

        return savedUser;
    }

    @Override
    @Transactional
    public User resetPassword(Long id, String newPassword) {
        log.info("重置用户密码, ID: {}", id);
        Optional<User> userOpt = userRepository.findById(id);

        if (!userOpt.isPresent()) {
            log.error("重置用户密码失败, ID: {}, 错误: 用户不存在", id);
            throw new RuntimeException("用户不存在，ID: " + id);
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(new Date());

        User savedUser = userRepository.save(user);
        log.info("用户密码重置成功, ID: {}", id);

        return savedUser;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public long count() {
        return userRepository.count();
    }
}
