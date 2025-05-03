package com.ict.system.config;

import com.ict.system.model.InventoryItem;
import com.ict.system.model.MaintenanceOrder;
import com.ict.system.model.NetworkDevice;
import com.ict.system.model.User;
import com.ict.system.repository.InventoryItemRepository;
import com.ict.system.repository.MaintenanceOrderRepository;
import com.ict.system.repository.NetworkDeviceRepository;
import com.ict.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

@Component
public class MySQLInit implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private NetworkDeviceRepository networkDeviceRepository;

    @Autowired
    private MaintenanceOrderRepository maintenanceOrderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            // 检查是否已有管理员用户
            boolean hasAdmin = userRepository.existsByUsername("admin");

            // 只有在数据库完全为空时才初始化所有数据
            if (userRepository.count() == 0) {
                System.out.println("数据库为空，开始初始化所有数据...");

                // 创建管理员用户
                createAdminUser();

                // 创建其他用户
                createManagerUser();
                createITUser();
                createGeneralUser();

                // 创建示例资产
                createSampleInventoryItems();

                // 创建示例网络设备
                createSampleNetworkDevices();

                // 创建示例维修单
                createSampleMaintenanceOrders();

                System.out.println("MySQL初始化数据成功！");
            }
            // 如果没有管理员用户但有其他用户，只创建管理员用户
            else if (!hasAdmin) {
                System.out.println("未找到管理员用户，仅创建管理员用户...");
                createAdminUser();
                System.out.println("管理员用户创建成功！");
            }
            else {
                System.out.println("MySQL数据库已有数据，跳过初始化");
            }
        } catch (Exception e) {
            System.err.println("MySQL初始化数据失败: " + e.getMessage());
            e.printStackTrace();
            System.err.println("应用程序将继续运行，但数据库功能可能不可用。");
            // 不抛出异常，允许应用程序继续启动
        }
    }

    private void createAdminUser() {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("123456"));
        adminUser.setName("系统管理员");
        adminUser.setEmail("admin@example.com");
        adminUser.setDepartment("系统部");
        adminUser.setRoles(Arrays.asList("admin"));

        // 所有权限
        adminUser.setPermissions(Arrays.asList(
                "maintenance.view", "maintenance.add", "maintenance.edit", "maintenance.delete",
                "maintenance.export", "maintenance.update-status",
                "inventory.view", "inventory.add", "inventory.edit", "inventory.delete",
                "inventory.import", "inventory.export", "inventory.check",
                "network.view", "network.add", "network.edit", "network.delete",
                "network.topology", "network.terminal",
                "supplies.view", "supplies.apply", "supplies.approve", "supplies.return",
                "system.user", "system.role", "system.department", "system.log"
        ));

        adminUser.setCreatedAt(new Date());
        adminUser.setUpdatedAt(new Date());

        userRepository.save(adminUser);
    }

    private void createManagerUser() {
        User managerUser = new User();
        managerUser.setUsername("manager");
        managerUser.setPassword(passwordEncoder.encode("123456"));
        managerUser.setName("部门经理");
        managerUser.setEmail("manager@example.com");
        managerUser.setDepartment("管理部");
        managerUser.setRoles(Arrays.asList("manager"));

        // 经理权限
        managerUser.setPermissions(Arrays.asList(
                "maintenance.view", "maintenance.add", "maintenance.edit", "maintenance.update-status",
                "inventory.view", "inventory.check", "inventory.export",
                "network.view", "network.topology",
                "supplies.view", "supplies.apply", "supplies.approve"
        ));

        managerUser.setCreatedAt(new Date());
        managerUser.setUpdatedAt(new Date());

        userRepository.save(managerUser);
    }

    private void createITUser() {
        User itUser = new User();
        itUser.setUsername("it");
        itUser.setPassword(passwordEncoder.encode("123456"));
        itUser.setName("IT专员");
        itUser.setEmail("it@example.com");
        itUser.setDepartment("IT部");
        itUser.setRoles(Arrays.asList("it_staff"));

        // IT人员权限
        itUser.setPermissions(Arrays.asList(
                "maintenance.view", "maintenance.add", "maintenance.edit", "maintenance.update-status",
                "inventory.view", "inventory.add", "inventory.edit", "inventory.check",
                "network.view", "network.add", "network.edit", "network.topology", "network.terminal",
                "supplies.view", "supplies.apply"
        ));

        itUser.setCreatedAt(new Date());
        itUser.setUpdatedAt(new Date());

        userRepository.save(itUser);
    }

    private void createGeneralUser() {
        User generalUser = new User();
        generalUser.setUsername("user");
        generalUser.setPassword(passwordEncoder.encode("123456"));
        generalUser.setName("普通员工");
        generalUser.setEmail("user@example.com");
        generalUser.setDepartment("普通部门");
        generalUser.setRoles(Arrays.asList("general_staff"));

        // 普通员工权限
        generalUser.setPermissions(Arrays.asList(
                "maintenance.view", "maintenance.add",
                "inventory.view",
                "supplies.view", "supplies.apply", "supplies.return"
        ));

        generalUser.setCreatedAt(new Date());
        generalUser.setUpdatedAt(new Date());

        userRepository.save(generalUser);
    }

    private void createSampleInventoryItems() {
        // 示例资产
        InventoryItem laptop1 = new InventoryItem();
        laptop1.setName("笔记本电脑");
        laptop1.setType("电脑设备");
        laptop1.setAssetNo("PC-2023-001");
        laptop1.setSerialNumber("LT12345678");
        laptop1.setBrand("联想");
        laptop1.setModel("ThinkPad X1 Carbon");
        laptop1.setPurchaseDate("2023-01-15");
        laptop1.setPurchasePrice(8999.0);
        laptop1.setSupplier("联想官方旗舰店");
        laptop1.setDepartment("研发部");
        laptop1.setLocation("2楼办公区");
        laptop1.setStatus("IN_USE");
        laptop1.setAssignee("张三");
        laptop1.setSpecifications("i7-11代 16G 512G SSD");
        laptop1.setNotes("研发主管专用");
        laptop1.setLastCheckDate(new Date());
        laptop1.setLastCheckBy("admin");
        laptop1.setCreatedAt(new Date());
        laptop1.setUpdatedAt(new Date());

        InventoryItem laptop2 = new InventoryItem();
        laptop2.setName("笔记本电脑");
        laptop2.setType("电脑设备");
        laptop2.setAssetNo("PC-2023-002");
        laptop2.setSerialNumber("LT12345679");
        laptop2.setBrand("戴尔");
        laptop2.setModel("XPS 13");
        laptop2.setPurchaseDate("2023-01-20");
        laptop2.setPurchasePrice(9999.0);
        laptop2.setSupplier("戴尔官方旗舰店");
        laptop2.setDepartment("市场部");
        laptop2.setLocation("3楼办公区");
        laptop2.setStatus("IN_USE");
        laptop2.setAssignee("李四");
        laptop2.setSpecifications("i7-11代 16G 1T SSD");
        laptop2.setNotes("市场总监专用");
        laptop2.setLastCheckDate(new Date());
        laptop2.setLastCheckBy("admin");
        laptop2.setCreatedAt(new Date());
        laptop2.setUpdatedAt(new Date());

        inventoryItemRepository.saveAll(Arrays.asList(laptop1, laptop2));
    }

    private void createSampleNetworkDevices() {
        // 示例网络设备
        NetworkDevice router = new NetworkDevice();
        router.setName("核心路由器");
        router.setType("ROUTER");
        router.setBrand("华为");
        router.setModel("AR3260");
        router.setSerialNumber("HW123456789");
        router.setIpAddress("192.168.1.1");
        router.setMacAddress("00:11:22:33:44:55");
        router.setSubnet("255.255.255.0");
        router.setGateway("192.168.1.254");
        router.setLocation("1楼机房");
        router.setRack("A-01");
        router.setPorts(24);
        router.setUsername("admin");
        router.setPassword("admin123"); // 实际应用中应加密存储
        router.setSshPort("22");
        router.setFirmware("V800R010C00SPC500");
        router.setStatus("ONLINE");
        router.setLastPingTime(new Date());
        router.setNotes("核心路由设备，勿随意重启");
        router.setCreatedAt(new Date());
        router.setUpdatedAt(new Date());

        NetworkDevice switch1 = new NetworkDevice();
        switch1.setName("交换机-2F");
        switch1.setType("SWITCH");
        switch1.setBrand("思科");
        switch1.setModel("Catalyst 3850");
        switch1.setSerialNumber("CS987654321");
        switch1.setIpAddress("192.168.1.2");
        switch1.setMacAddress("AA:BB:CC:DD:EE:FF");
        switch1.setSubnet("255.255.255.0");
        switch1.setGateway("192.168.1.254");
        switch1.setLocation("2楼弱电间");
        switch1.setRack("B-02");
        switch1.setPorts(48);
        switch1.setUsername("admin");
        switch1.setPassword("cisco123"); // 实际应用中应加密存储
        switch1.setSshPort("22");
        switch1.setFirmware("IOS-XE 16.9.3");
        switch1.setStatus("ONLINE");
        switch1.setLastPingTime(new Date());
        switch1.setNotes("2楼办公区接入交换机");
        switch1.setCreatedAt(new Date());
        switch1.setUpdatedAt(new Date());

        networkDeviceRepository.saveAll(Arrays.asList(router, switch1));
    }

    private void createSampleMaintenanceOrders() {
        // 示例维修单
        MaintenanceOrder order1 = new MaintenanceOrder();
        order1.setOrderNo("MO-2023-001");
        order1.setTitle("笔记本电脑蓝屏故障");
        order1.setDescription("笔记本电脑频繁蓝屏，需要检查硬件和系统");
        order1.setAssetId("PC-2023-001");
        order1.setAssetName("笔记本电脑");
        order1.setAssetType("电脑设备");
        order1.setPriority("HIGH");
        order1.setStatus("IN_PROGRESS");
        order1.setRequester("张三");
        order1.setRequesterDepartment("研发部");
        order1.setAssignee("it");
        order1.setRequestDate(new Date());
        order1.setStartDate(new Date());
        order1.setSupplier("联想售后服务");
        order1.setLocation("2楼办公区");

        MaintenanceOrder.MaintenanceHistory history1 = new MaintenanceOrder.MaintenanceHistory();
        history1.setTimestamp(new Date());
        history1.setStatus("PENDING");
        history1.setOperator("张三");
        history1.setComments("提交维修申请");

        MaintenanceOrder.MaintenanceHistory history2 = new MaintenanceOrder.MaintenanceHistory();
        history2.setTimestamp(new Date());
        history2.setStatus("IN_PROGRESS");
        history2.setOperator("it");
        history2.setComments("已接单，正在处理中");

        order1.setHistory(Arrays.asList(history1, history2));
        order1.setNotes("优先处理，影响工作");
        order1.setCreatedAt(new Date());
        order1.setUpdatedAt(new Date());

        maintenanceOrderRepository.save(order1);
    }
}
