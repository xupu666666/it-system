package com.ict.system.service.v2;

import com.ict.system.model.v2.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * @param pageable 分页参数
     * @return 用户分页结果
     */
    Page<User> findAll(Pageable pageable);

    /**
     * 保存用户
     * @param user 用户对象
     * @return 保存后的用户对象
     */
    User save(User user);

    /**
     * 删除用户
     * @param user 用户对象
     */
    void delete(User user);

    /**
     * 根据ID删除用户
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
     * @param enabled 状态
     * @return 更新后的用户对象
     */
    User updateStatus(Long id, boolean enabled);

    /**
     * 重置用户密码
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 更新后的用户对象
     */
    User resetPassword(Long id, String newPassword);

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 获取用户数量
     * @return 用户数量
     */
    long count();
}
