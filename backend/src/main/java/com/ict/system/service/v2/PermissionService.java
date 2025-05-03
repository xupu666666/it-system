package com.ict.system.service.v2;

import com.ict.system.model.v2.Permission;

import java.util.List;
import java.util.Optional;

public interface PermissionService {

    /**
     * 根据ID查找权限
     * @param id 权限ID
     * @return 权限对象
     */
    Optional<Permission> findById(Long id);

    /**
     * 根据名称查找权限
     * @param name 权限名称
     * @return 权限对象
     */
    Optional<Permission> findByName(String name);

    /**
     * 获取所有权限
     * @return 权限列表
     */
    List<Permission> findAll();

    /**
     * 根据分组获取权限
     * @param group 分组名称
     * @return 权限列表
     */
    List<Permission> findByGroup(String group);

    /**
     * 保存权限
     * @param permission 权限对象
     * @return 保存后的权限对象
     */
    Permission save(Permission permission);

    /**
     * 删除权限
     * @param permission 权限对象
     */
    void delete(Permission permission);

    /**
     * 根据ID删除权限
     * @param id 权限ID
     */
    void deleteById(Long id);

    /**
     * 检查权限名称是否存在
     * @param name 权限名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 获取权限数量
     * @return 权限数量
     */
    long count();
}
