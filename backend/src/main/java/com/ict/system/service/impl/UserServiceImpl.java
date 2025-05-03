package com.ict.system.service.impl;

import com.ict.system.model.User;
import com.ict.system.repository.UserRepository;
import com.ict.system.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findById(Long id) {
        log.info("通过ID查找用户: {}", id);
        Optional<User> user = userRepository.findById(id);
        log.info("查找结果: {}", user.isPresent() ? "找到" : "未找到");
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        log.info("通过用户名查找用户: {}", username);
        Optional<User> user = userRepository.findByUsername(username);
        log.info("查找结果: {}", user.isPresent() ? "找到" : "未找到");
        return user;
    }

    @Override
    public List<User> findAll() {
        log.info("查找所有用户");
        List<User> users = userRepository.findAll();
        log.info("找到用户数量: {}", users.size());
        return users;
    }

    @Override
    public List<User> findAll(int page, int size) {
        log.info("分页查找用户, 页码: {}, 每页大小: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        List<User> users = userRepository.findAll(pageable).getContent();
        log.info("找到用户数量: {}", users.size());
        return users;
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            // 新用户
            log.info("创建新用户: {}", user.getUsername());
            user.setCreatedAt(new Date());
        } else {
            log.info("更新用户: {}, ID: {}", user.getUsername(), user.getId());
        }
        user.setUpdatedAt(new Date());
        User savedUser = userRepository.save(user);
        log.info("用户保存成功: {}, ID: {}", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    @Override
    public void deleteById(Long id) {
        log.info("删除用户, ID: {}", id);
        try {
            // 先检查用户是否存在
            Optional<User> userOpt = userRepository.findById(id);
            if (!userOpt.isPresent()) {
                log.error("删除用户失败, ID: {}, 错误: 用户不存在", id);
                throw new RuntimeException("用户不存在，ID: " + id);
            }

            User user = userOpt.get();
            log.info("找到要删除的用户: {}, ID: {}", user.getUsername(), user.getId());

            // 检查是否有关联的权限和角色
            if (user.getPermissions() != null && !user.getPermissions().isEmpty()) {
                log.info("清除用户权限, 用户: {}, ID: {}", user.getUsername(), user.getId());
                user.setPermissions(new ArrayList<>());
                user = userRepository.save(user);
            }

            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                log.info("清除用户角色, 用户: {}, ID: {}", user.getUsername(), user.getId());
                user.setRoles(new ArrayList<>());
                user = userRepository.save(user);
            }

            // 删除用户
            log.info("执行删除用户操作, 用户: {}, ID: {}", user.getUsername(), user.getId());
            userRepository.delete(user);
            log.info("用户删除成功, ID: {}", id);
        } catch (Exception e) {
            log.error("删除用户失败, ID: {}, 错误类型: {}, 错误信息: {}", id, e.getClass().getName(), e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        log.info("批量删除用户, IDs: {}", ids);
        List<User> users = new ArrayList<>();

        // 先收集所有要删除的用户
        for (Long id : ids) {
            log.info("查找要删除的用户, ID: {}", id);
            userRepository.findById(id).ifPresent(user -> {
                log.info("找到要删除的用户: {}, ID: {}", user.getUsername(), user.getId());
                users.add(user);
            });
        }

        log.info("实际要删除的用户数量: {}", users.size());

        if (users.isEmpty()) {
            log.warn("没有找到要删除的用户");
            return;
        }

        try {
            // 先清除每个用户的权限和角色
            for (User user : users) {
                // 检查是否有关联的权限和角色
                if (user.getPermissions() != null && !user.getPermissions().isEmpty()) {
                    log.info("清除用户权限, 用户: {}, ID: {}", user.getUsername(), user.getId());
                    user.setPermissions(new ArrayList<>());
                    userRepository.save(user);
                }

                if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                    log.info("清除用户角色, 用户: {}, ID: {}", user.getUsername(), user.getId());
                    user.setRoles(new ArrayList<>());
                    userRepository.save(user);
                }
            }

            // 然后删除所有用户
            log.info("执行批量删除用户操作");
            userRepository.deleteAll(users);
            log.info("批量删除用户成功");
        } catch (Exception e) {
            log.error("批量删除用户失败, 错误类型: {}, 错误信息: {}", e.getClass().getName(), e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public User updateStatus(Long id, boolean status) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(status);
            user.setUpdatedAt(new Date());
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User resetPassword(Long id, String newPassword) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(new Date());
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}
