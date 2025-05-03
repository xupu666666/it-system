package com.ict.system.service;

import com.ict.system.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 用户对象
     */
    Optional<User> findById(Long id);

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象
     */
    Optional<User> findByUsername(String username);

    /**
     * 获取所有用户
     * @return 用户列表
     */
    List<User> findAll();

    /**
     * 分页获取用户
     * @param page 页码
     * @param size 每页大小
     * @return 用户列表
     */
    List<User> findAll(int page, int size);

    /**
     * 保存用户
     * @param user 用户对象
     * @return 保存后的用户对象
     */
    User save(User user);

    /**
     * 删除用户
     * @param id 用户ID
     */
    void deleteById(Long id);

    /**
     * 批量删除用户
     * @param ids 用户ID列表
     */
    void deleteByIds(List<Long> ids);

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 状态
     * @return 更新后的用户对象
     */
    User updateStatus(Long id, boolean status);

    /**
     * 重置用户密码
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 更新后的用户对象
     */
    User resetPassword(Long id, String newPassword);

    /**
     * 获取当前登录用户
     * @return 当前用户对象
     */
    User getCurrentUser();
}
