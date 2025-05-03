package com.ict.system.service.v2;

import com.ict.system.model.v2.Permission;
import com.ict.system.model.v2.Role;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleService {

    /**
     * 根据ID查找角色
     * @param id 角色ID
     * @return 角色对象
     */
    Optional<Role> findById(Long id);

    /**
     * 根据名称查找角色
     * @param name 角色名称
     * @return 角色对象
     */
    Optional<Role> findByName(String name);

    /**
     * 获取所有角色
     * @return 角色列表
     */
    List<Role> findAll();

    /**
     * 保存角色
     * @param role 角色对象
     * @return 保存后的角色对象
     */
    Role save(Role role);

    /**
     * 删除角色
     * @param role 角色对象
     */
    void delete(Role role);

    /**
     * 根据ID删除角色
     * @param id 角色ID
     */
    void deleteById(Long id);

    /**
     * 为角色添加权限
     * @param roleId 角色ID
     * @param permission 权限对象
     * @return 更新后的角色对象
     */
    Role addPermissionToRole(Long roleId, Permission permission);

    /**
     * 为角色添加多个权限
     * @param roleId 角色ID
     * @param permissions 权限集合
     * @return 更新后的角色对象
     */
    Role addPermissionsToRole(Long roleId, Set<Permission> permissions);

    /**
     * 从角色中移除权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 更新后的角色对象
     */
    Role removePermissionFromRole(Long roleId, Long permissionId);

    /**
     * 检查角色名称是否存在
     * @param name 角色名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 获取角色数量
     * @return 角色数量
     */
    long count();
}
