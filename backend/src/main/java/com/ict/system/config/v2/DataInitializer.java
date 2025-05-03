package com.ict.system.config.v2;

import com.ict.system.model.v2.Permission;
import com.ict.system.model.v2.Role;
import com.ict.system.model.v2.User;
import com.ict.system.service.v2.PermissionService;
import com.ict.system.service.v2.RoleService;
import com.ict.system.service.v2.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    @Qualifier("userServiceV2")
    private UserService userService;

    @Autowired
    @Qualifier("roleServiceV2")
    private RoleService roleService;

    @Autowired
    @Qualifier("permissionServiceV2")
    private PermissionService permissionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化V2数据...");

        try {
            // 只有在数据库完全为空时才初始化所有数据
            if (roleService.count() == 0 && permissionService.count() == 0 && userService.count() == 0) {
                log.info("数据库为空，初始化所有数据");

                // 初始化权限
                initializePermissions();

                // 初始化角色
                initializeRoles();

                // 初始化管理员用户
                initializeAdminUser();

                log.info("V2数据初始化成功");
            } else if (!userService.existsByUsername("admin")) {
                // 如果没有admin用户，只创建admin用户
                log.info("未找到管理员用户，仅创建管理员用户");
                initializeAdminUser();
                log.info("管理员用户创建成功");
            } else {
                log.info("数据库已有数据，跳过初始化");
            }
        } catch (Exception e) {
            log.error("V2数据初始化失败: {}", e.getMessage(), e);
            // 不抛出异常，允许应用程序继续启动
        }
    }

    /**
     * 初始化权限
     */
    private void initializePermissions() {
        log.info("初始化权限...");

        // 系统管理权限
        createPermissionIfNotExists("system.user.view", "查看用户", "system");
        createPermissionIfNotExists("system.user.add", "添加用户", "system");
        createPermissionIfNotExists("system.user.edit", "编辑用户", "system");
        createPermissionIfNotExists("system.user.delete", "删除用户", "system");

        // 安全管理权限
        createPermissionIfNotExists("system.security.view", "查看安全日志", "security");
        createPermissionIfNotExists("system.security.manage", "管理安全设置", "security");

        // 资产管理权限
        createPermissionIfNotExists("inventory.view", "查看资产", "inventory");
        createPermissionIfNotExists("inventory.add", "添加资产", "inventory");
        createPermissionIfNotExists("inventory.edit", "编辑资产", "inventory");
        createPermissionIfNotExists("inventory.delete", "删除资产", "inventory");

        // 维修管理权限
        createPermissionIfNotExists("maintenance.view", "查看维修单", "maintenance");
        createPermissionIfNotExists("maintenance.add", "添加维修单", "maintenance");
        createPermissionIfNotExists("maintenance.edit", "编辑维修单", "maintenance");
        createPermissionIfNotExists("maintenance.delete", "删除维修单", "maintenance");

        // 网络设备管理权限
        createPermissionIfNotExists("network.view", "查看网络设备", "network");
        createPermissionIfNotExists("network.add", "添加网络设备", "network");
        createPermissionIfNotExists("network.edit", "编辑网络设备", "network");
        createPermissionIfNotExists("network.delete", "删除网络设备", "network");

        // 物品领用管理权限
        createPermissionIfNotExists("supplies.view", "查看物品领用", "supplies");
        createPermissionIfNotExists("supplies.add", "添加物品领用", "supplies");
        createPermissionIfNotExists("supplies.edit", "编辑物品领用", "supplies");
        createPermissionIfNotExists("supplies.delete", "删除物品领用", "supplies");
        createPermissionIfNotExists("supplies.apply", "申请物品", "supplies");
        createPermissionIfNotExists("supplies.approve", "审批物品申请", "supplies");
        createPermissionIfNotExists("supplies.return", "归还物品", "supplies");

        log.info("权限初始化完成");
    }

    /**
     * 创建权限（如果不存在）
     * @param name 权限名称
     * @param description 权限描述
     * @param group 权限分组
     * @return 权限对象
     */
    private Permission createPermissionIfNotExists(String name, String description, String group) {
        Optional<Permission> existingPermission = permissionService.findByName(name);

        if (existingPermission.isPresent()) {
            return existingPermission.get();
        }

        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription(description);
        permission.setGroup(group);

        return permissionService.save(permission);
    }

    /**
     * 初始化角色
     */
    private void initializeRoles() {
        log.info("初始化角色...");

        // 管理员角色
        Role adminRole = createRoleIfNotExists("ADMIN", "系统管理员");

        // 为管理员角色分配所有权限
        Set<Permission> allPermissions = new HashSet<>(permissionService.findAll());
        roleService.addPermissionsToRole(adminRole.getId(), allPermissions);

        // 经理角色
        Role managerRole = createRoleIfNotExists("MANAGER", "部门经理");

        // 为经理角色分配权限
        Set<Permission> managerPermissions = new HashSet<>();
        managerPermissions.add(permissionService.findByName("system.user.view").orElseThrow());
        managerPermissions.add(permissionService.findByName("inventory.view").orElseThrow());
        managerPermissions.add(permissionService.findByName("inventory.add").orElseThrow());
        managerPermissions.add(permissionService.findByName("inventory.edit").orElseThrow());
        managerPermissions.add(permissionService.findByName("maintenance.view").orElseThrow());
        managerPermissions.add(permissionService.findByName("maintenance.add").orElseThrow());
        managerPermissions.add(permissionService.findByName("maintenance.edit").orElseThrow());
        managerPermissions.add(permissionService.findByName("network.view").orElseThrow());
        managerPermissions.add(permissionService.findByName("supplies.view").orElseThrow());
        managerPermissions.add(permissionService.findByName("supplies.add").orElseThrow());
        managerPermissions.add(permissionService.findByName("supplies.edit").orElseThrow());
        managerPermissions.add(permissionService.findByName("supplies.approve").orElseThrow());

        roleService.addPermissionsToRole(managerRole.getId(), managerPermissions);

        // IT角色
        Role itRole = createRoleIfNotExists("IT", "IT人员");

        // 为IT角色分配权限
        Set<Permission> itPermissions = new HashSet<>();
        itPermissions.add(permissionService.findByName("system.user.view").orElseThrow());
        itPermissions.add(permissionService.findByName("inventory.view").orElseThrow());
        itPermissions.add(permissionService.findByName("inventory.add").orElseThrow());
        itPermissions.add(permissionService.findByName("inventory.edit").orElseThrow());
        itPermissions.add(permissionService.findByName("inventory.delete").orElseThrow());
        itPermissions.add(permissionService.findByName("maintenance.view").orElseThrow());
        itPermissions.add(permissionService.findByName("maintenance.add").orElseThrow());
        itPermissions.add(permissionService.findByName("maintenance.edit").orElseThrow());
        itPermissions.add(permissionService.findByName("maintenance.delete").orElseThrow());
        itPermissions.add(permissionService.findByName("network.view").orElseThrow());
        itPermissions.add(permissionService.findByName("network.add").orElseThrow());
        itPermissions.add(permissionService.findByName("network.edit").orElseThrow());
        itPermissions.add(permissionService.findByName("network.delete").orElseThrow());
        itPermissions.add(permissionService.findByName("supplies.view").orElseThrow());
        itPermissions.add(permissionService.findByName("supplies.add").orElseThrow());
        itPermissions.add(permissionService.findByName("supplies.edit").orElseThrow());

        roleService.addPermissionsToRole(itRole.getId(), itPermissions);

        // 普通用户角色
        Role userRole = createRoleIfNotExists("USER", "普通用户");

        // 为普通用户角色分配权限
        Set<Permission> userPermissions = new HashSet<>();
        userPermissions.add(permissionService.findByName("inventory.view").orElseThrow());
        userPermissions.add(permissionService.findByName("maintenance.view").orElseThrow());
        userPermissions.add(permissionService.findByName("maintenance.add").orElseThrow());
        userPermissions.add(permissionService.findByName("network.view").orElseThrow());
        userPermissions.add(permissionService.findByName("supplies.view").orElseThrow());
        userPermissions.add(permissionService.findByName("supplies.apply").orElseThrow());
        userPermissions.add(permissionService.findByName("supplies.return").orElseThrow());

        roleService.addPermissionsToRole(userRole.getId(), userPermissions);

        log.info("角色初始化完成");
    }

    /**
     * 创建角色（如果不存在）
     * @param name 角色名称
     * @param description 角色描述
     * @return 角色对象
     */
    private Role createRoleIfNotExists(String name, String description) {
        Optional<Role> existingRole = roleService.findByName(name);

        if (existingRole.isPresent()) {
            return existingRole.get();
        }

        Role role = new Role();
        role.setName(name);
        role.setDescription(description);

        return roleService.save(role);
    }

    /**
     * 初始化管理员用户
     */
    private void initializeAdminUser() {
        log.info("初始化管理员用户...");

        if (!userService.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setName("系统管理员");
            admin.setEmail("admin@example.com");
            admin.setDepartment("IT部门");
            admin.setEnabled(true);
            admin.setCreatedAt(new Date());
            admin.setUpdatedAt(new Date());

            // 分配管理员角色
            Set<Role> roles = new HashSet<>();
            Role adminRole = roleService.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("管理员角色不存在"));
            roles.add(adminRole);
            admin.setRoles(roles);

            userService.save(admin);
            log.info("管理员用户创建成功");
        } else {
            log.info("管理员用户已存在，跳过创建");
        }
    }
}
