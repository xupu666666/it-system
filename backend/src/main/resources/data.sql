-- 此文件用于初始化数据库
-- 注意：此文件仅在应用程序启动时执行一次，如果表已存在则不会执行

-- 确保数据库使用UTF-8编码
SET NAMES utf8mb4;

-- 只有在表为空时才插入数据
INSERT INTO inventory_items (id, asset_no, name, type, specifications, purchase_price, purchase_date, status, department, assignee, location, notes, created_at, updated_at, apc_code, factory, cost_center, manager, floor, last_check_date, last_check_by)
SELECT * FROM (SELECT
    1 as id,
    '160277' as asset_no,
    '笔记本电脑' as name,
    '电脑设备' as type,
    'ThinkPad X1 Carbon' as specifications,
    12000.00 as purchase_price,
    '2022-01-01' as purchase_date,
    '在用' as status,
    'IT部门' as department,
    '张三' as assignee,
    '3楼办公室' as location,
    '新购置的高性能笔记本' as notes,
    NOW() as created_at,
    NOW() as updated_at,
    '160301' as apc_code,
    'PS' as factory,
    '8090130601' as cost_center,
    '李四' as manager,
    '3楼' as floor,
    NULL as last_check_date,
    NULL as last_check_by
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items WHERE id = 1
) LIMIT 1;

-- 插入更多测试数据（只有在表中没有这些记录时才插入）
INSERT INTO inventory_items (id, asset_no, name, type, specifications, purchase_price, purchase_date, status, department, assignee, location, notes, created_at, updated_at, apc_code, factory, cost_center, manager, floor, last_check_date, last_check_by)
SELECT * FROM (SELECT
    2 as id,
    '160278' as asset_no,
    '显示器' as name,
    '电脑设备' as type,
    'Dell P2419H' as specifications,
    1500.00 as purchase_price,
    '2022-01-01' as purchase_date,
    '在用' as status,
    'IT部门' as department,
    '张三' as assignee,
    '3楼办公室' as location,
    '配套显示器' as notes,
    NOW() as created_at,
    NOW() as updated_at,
    '160301' as apc_code,
    'PS' as factory,
    '8090130601' as cost_center,
    '李四' as manager,
    '3楼' as floor,
    NULL as last_check_date,
    NULL as last_check_by
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items WHERE id = 2
) LIMIT 1;

INSERT INTO inventory_items (id, asset_no, name, type, specifications, purchase_price, purchase_date, status, department, assignee, location, notes, created_at, updated_at, apc_code, factory, cost_center, manager, floor, last_check_date, last_check_by)
SELECT * FROM (SELECT
    3 as id,
    '160279' as asset_no,
    '键盘' as name,
    '电脑设备' as type,
    'Logitech MX Keys' as specifications,
    800.00 as purchase_price,
    '2022-01-01' as purchase_date,
    '在用' as status,
    'IT部门' as department,
    '张三' as assignee,
    '3楼办公室' as location,
    '无线键盘' as notes,
    NOW() as created_at,
    NOW() as updated_at,
    '160301' as apc_code,
    'PS' as factory,
    '8090130601' as cost_center,
    '李四' as manager,
    '3楼' as floor,
    NULL as last_check_date,
    NULL as last_check_by
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM inventory_items WHERE id = 3
) LIMIT 1;

-- 设置自增ID起始值
ALTER TABLE inventory_items AUTO_INCREMENT = 6;
