# 情侣商城系统 - 需求文档

## 一、项目概述

这是一个"许愿清单"式的小商城系统：
- 女朋友可以在商城里添加想要的商品（主要是奶茶、外卖等）
- 管理员（你）收到通知后帮她去电商平台下单
- 不涉及真实支付，只是一个"代下单"工具

## 二、用户角色

| 角色 | 数量 | 权限 |
|------|------|------|
| 管理员 | 1个 | 管理商品、查看所有订单、配置系统 |
| 普通用户 | 1个 | 浏览商品、下单、管理自己的地址 |

## 三、核心功能需求

### 3.1 用户系统

- **登录** - 用户名+密码登录，返回JWT token
- **注册** - 用户名、昵称、邮箱、密码注册
- **角色区分** - 区分 ADMIN 和 USER 角色

### 3.2 商品管理

- **商品列表** - 展示所有上架商品，支持分类筛选
- **添加商品** - 管理员添加商品（名称、图片、价格）
- **编辑商品** - 管理员修改商品信息
- **删除商品** - 管理员删除商品
- **商品规格** - 每个商品可以配置不同规格
  - 例如奶茶：甜度（无糖/三分糖/五分糖/七分糖/全糖）、冰度（去冰/少冰/正常冰）、加料（珍珠/椰果/布丁）
- **商品分类** - 支持按分类查看商品

### 3.3 订单系统

- **下单流程**：
  1. 选择商品
  2. 选择规格（动态显示该商品的规格选项）
  3. 选择收货地址
  4. 填写备注
  5. 提交订单

- **订单状态**：
  - 待处理（0）- 用户刚下单
  - 已处理（1）- 管理员处理完成

- **订单列表**：
  - 用户：查看自己的订单
  - 管理员：查看所有订单

- **订单处理** - 管理员可以将订单标记为"已处理"

### 3.4 通知系统

- **邮件通知** - 用户下单后，自动发送邮件通知管理员
- 邮件内容包含：商品信息、规格、收货地址、备注、下单时间
- 后续可接入微信服务号推送

### 3.5 地址管理

- **添加地址** - 收货人、手机号、详细地址
- **编辑地址** - 修改地址信息
- **删除地址** - 删除不需要的地址
- **默认地址** - 设置默认收货地址，下单时自动选中

## 四、数据模型设计

### 4.1 用户表 (user)

```sql
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(20) DEFAULT 'USER',  -- ADMIN / USER
    `nickname` VARCHAR(50),
    `email` VARCHAR(100),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 4.2 分类表 (category)

```sql
CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `sort` INT DEFAULT 0
);
```

### 4.3 商品表 (product)

```sql
CREATE TABLE `product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `image_url` VARCHAR(500),
    `price` DECIMAL(10,2) NOT NULL,
    `category_id` BIGINT,
    `status` INT DEFAULT 1,  -- 0: 下架, 1: 上架
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 4.4 商品规格表 (product_spec)

```sql
CREATE TABLE `product_spec` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `product_id` BIGINT NOT NULL,
    `spec_name` VARCHAR(50) NOT NULL,  -- 如：甜度、冰度
    `spec_values` JSON  -- 如：["无糖","三分糖","五分糖","七分糖","全糖"]
);
```

### 4.5 地址表 (address)

```sql
CREATE TABLE `address` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `is_default` INT DEFAULT 0  -- 0: 否, 1: 是
);
```

### 4.6 订单表 (orders)

```sql
CREATE TABLE `orders` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `specs` JSON,  -- 如：{"甜度":"正常冰","加料":"珍珠"}
    `address` VARCHAR(500),
    `remark` VARCHAR(500),
    `status` INT DEFAULT 0,  -- 0: 待处理, 1: 已处理
    `price` DECIMAL(10,2),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `handled_at` DATETIME
);
```

## 五、非功能性需求

### 5.1 安全性

- 密码使用 BCrypt 加密存储
- 接口使用 JWT Token 鉴权
- 敏感操作需要管理员权限

### 5.2 可用性

- 界面简洁直观
- 操作有即时反馈（成功/失败提示）
- 粉色浪漫主题风格

### 5.3 可扩展性

- 模块化设计，便于功能扩展
- 预留邮件通知接口

## 六、初始数据

### 6.1 默认账号

- 用户名：`admin`
- 密码：`admin123`
- 角色：`ADMIN`

### 6.2 默认分类

- 奶茶
- 咖啡
- 甜品
- 其他

## 七、待确认问题

| 问题 | 说明 |
|------|------|
| 商品规格存储 | 采用 JSON 存储规格名称和选项值 |
| 邮箱配置 | 需要提供 163 邮箱 SMTP 用户名和密码 |
| 初始账号 | 已预置管理员账号，普通用户需自行注册 |
| 页面风格 | 简洁粉色浪漫风格 |
