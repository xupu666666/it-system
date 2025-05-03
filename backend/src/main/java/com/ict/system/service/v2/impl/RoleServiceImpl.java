package com.ict.system.service.v2.impl;

import com.ict.system.model.v2.Permission;
import com.ict.system.model.v2.Role;
import com.ict.system.repository.v2.PermissionRepository;
import com.ict.system.repository.v2.RoleRepository;
import com.ict.system.service.v2.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service("roleServiceV2")
public class RoleServiceImpl implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    @Qualifier("roleRepositoryV2")
    private RoleRepository roleRepository;

    @Autowired
    @Qualifier("permissionRepositoryV2")
    private PermissionRepository permissionRepository;

    @Override
    public Optional<Role> findById(Long id) {
        log.info("通过ID查找角色: {}", id);
        return roleRepository.findById(id);
    }

    @Override
    public Optional<Role> findByName(String name) {
        log.info("通过名称查找角色: {}", name);
        return roleRepository.findByName(name);
    }

    @Override
    public List<Role> findAll() {
        log.info("查找所有角色");
        return roleRepository.findAll();
    }

    @Override
    @Transactional
    public Role save(Role role) {
        log.info("保存角色: {}", role.getName());
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public void delete(Role role) {
        log.info("删除角色: {}, ID: {}", role.getName(), role.getId());
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("删除角色, ID: {}", id);
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Role addPermissionToRole(Long roleId, Permission permission) {
        log.info("为角色添加权限, 角色ID: {}, 权限: {}", roleId, permission.getName());
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在，ID: " + roleId));
        
        role.getPermissions().add(permission);
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role addPermissionsToRole(Long roleId, Set<Permission> permissions) {
        log.info("为角色添加多个权限, 角色ID: {}, 权限数量: {}", roleId, permissions.size());
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在，ID: " + roleId));
        
        role.getPermissions().addAll(permissions);
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role removePermissionFromRole(Long roleId, Long permissionId) {
        log.info("从角色中移除权限, 角色ID: {}, 权限ID: {}", roleId, permissionId);
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在，ID: " + roleId));
        
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("权限不存在，ID: " + permissionId));
        
        role.getPermissions().remove(permission);
        return roleRepository.save(role);
    }

    @Override
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    @Override
    public long count() {
        return roleRepository.count();
    }
}
