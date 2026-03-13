-- 重新创建表
CREATE TABLE `sys_user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    nickname VARCHAR(50),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    sort INT DEFAULT 0
);

CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    category_id BIGINT,
    status INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_spec (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    spec_name VARCHAR(50) NOT NULL,
    spec_values VARCHAR(1000)
);

CREATE TABLE address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    is_default INT DEFAULT 0
);

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    specs VARCHAR(1000),
    address VARCHAR(500),
    remark VARCHAR(500),
    status INT DEFAULT 0,
    price DECIMAL(10,2),
    quantity INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    handled_at TIMESTAMP
);

-- 添加索引提升查询性能
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);

-- 商品规格模板表
CREATE TABLE IF NOT EXISTS product_spec_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '模板名称（奶茶、咖啡、甜品）',
    spec_name VARCHAR(50) NOT NULL COMMENT '规格名称（甜度、温度、加料）',
    spec_values TEXT COMMENT '规格值（逗号分隔）',
    sort INT DEFAULT 0 COMMENT '排序',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格模板表';

-- 插入规格模板（奶茶类预设规格）
INSERT INTO product_spec_template (name, spec_name, spec_values, sort) VALUES
('奶茶', '甜度', '不另外加糖,五分糖,七分糖,正常糖,多糖', 1),
('奶茶', '温度', '去冰,少冰,正常冰,热饮,温饮', 2),
('奶茶', '加料', '无,珍珠,椰果,布丁,豆花,西米露', 3),
('咖啡', '甜度', '不另外加糖,五分糖,七分糖,正常糖,多糖', 1),
('咖啡', '温度', '去冰,少冰,正常冰,热饮,温饮', 2),
('咖啡', '加料', '无,珍珠,椰果,牛奶', 3),
('甜品', '甜度', '不另外加糖,五分糖,七分糖,正常糖,多糖', 1),
('甜品', '温度', '冰,常温,热', 2);

-- 添加时间戳字段的默认值
UPDATE product_spec_template SET created_time = NOW(), updated_time = NOW() WHERE created_time IS NULL;

-- 插入管理员
INSERT INTO `sys_user` (username, password, role, nickname, email) VALUES
('admin', '$2a$10$EqKcp1WFKVQISheT3QxG/e6DXhK5FZ.8X.6L7mX5YzFXqLQzJxMGO', 'admin', '管理员', 'admin@example.com');

-- 插入分类
INSERT INTO category (name, sort) VALUES
('奶茶', 1), ('咖啡', 2), ('甜品', 3), ('其他', 4);