-- 创建权限表
CREATE TABLE IF NOT EXISTS v2_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    `group` VARCHAR(255)
);

-- 创建角色表
CREATE TABLE IF NOT EXISTS v2_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 创建角色-权限关联表
CREATE TABLE IF NOT EXISTS v2_role_permissions (
    role_id BIGINT,
    permission_id BIGINT,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES v2_roles(id),
    FOREIGN KEY (permission_id) REFERENCES v2_permissions(id)
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS v2_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    department VARCHAR(255),
    avatar VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 创建用户-角色关联表
CREATE TABLE IF NOT EXISTS v2_user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES v2_users(id),
    FOREIGN KEY (role_id) REFERENCES v2_roles(id)
);

-- 创建用户会话表
CREATE TABLE IF NOT EXISTS v2_user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    token VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255),
    user_agent VARCHAR(255),
    created_at TIMESTAMP,
    expires_at TIMESTAMP,
    last_accessed_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES v2_users(id)
);

-- 创建登录历史表
CREATE TABLE IF NOT EXISTS v2_login_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    ip_address VARCHAR(255),
    user_agent VARCHAR(255),
    login_time TIMESTAMP,
    status VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES v2_users(id)
);

-- 创建登录尝试表
CREATE TABLE IF NOT EXISTS v2_login_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255),
    ip_address VARCHAR(255),
    attempt_time TIMESTAMP,
    success BOOLEAN
);

-- 创建IP限制表
CREATE TABLE IF NOT EXISTS v2_ip_restrictions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(255) NOT NULL,
    restriction_type VARCHAR(50) NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP,
    expires_at TIMESTAMP
);

-- 创建审计日志表
CREATE TABLE IF NOT EXISTS v2_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255),
    entity_id VARCHAR(255),
    details TEXT,
    ip_address VARCHAR(255),
    timestamp TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES v2_users(id)
);

-- 资产盘点任务表
CREATE TABLE IF NOT EXISTS inventory_check_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    factory VARCHAR(50) NOT NULL,         -- 工厂（PS/KC）
    department VARCHAR(255) NOT NULL,     -- 部门
    task_name VARCHAR(255),               -- 任务名称
    assigned_to VARCHAR(255),             -- 盘点人
    status VARCHAR(50) DEFAULT '进行中',  -- 任务状态（进行中/已完成/已取消）
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 资产盘点记录表
CREATE TABLE IF NOT EXISTS inventory_check_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,              -- 关联盘点任务
    asset_id BIGINT NOT NULL,             -- 关联资产
    check_status VARCHAR(50),             -- 盘点结果（正常/盘盈/盘亏/报废等）
    actual_user VARCHAR(255),             -- 实际使用人
    actual_location VARCHAR(255),         -- 实际存放地点
    actual_floor VARCHAR(255),            -- 实际楼层
    remark VARCHAR(1000),                 -- 备注
    photo_url VARCHAR(255),               -- 现场照片
    checked_by VARCHAR(255),              -- 盘点人
    checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES inventory_check_task(id),
    FOREIGN KEY (asset_id) REFERENCES inventory_items(id)
);
