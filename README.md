# 情侣商城系统 (Seven Shop)

一个"许愿清单"式的小商城系统，女朋友可以在商城里添加想要的商品（奶茶、外卖等），管理员帮她去电商平台下单。

## 技术栈

| 层级 | 技术选型 |
|------|----------|
| 后端 | Spring Boot 3.2 + MyBatis Plus 3.5 |
| 前端 | Vue 3 + Element Plus + Pinia |
| 数据库 | MySQL 8.0 |
| 邮件 | 163 邮箱 SMTP |
| 部署 | 可部署到 Railway / Render |

## 项目结构

```
sevenShop/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/sevenshop/
│   │   ├── config/            # 配置类 (JWT拦截器、Web配置)
│   │   ├── controller/         # 控制器 (Auth, Product, Order, Address, Category)
│   │   ├── dto/               # 数据传输对象
│   │   ├── entity/            # 实体类
│   │   ├── mapper/            # MyBatis Plus Mapper
│   │   ├── service/           # 业务逻辑层
│   │   └── util/              # 工具类 (JWT)
│   └── src/main/resources/
│       ├── application.yml    # 配置文件
│       └── schema.sql         # 数据库初始化脚本
│
└── frontend/                  # Vue 3 前端
    ├── src/
    │   ├── api/               # Axios 封装
    │   ├── router/            # Vue Router 配置
    │   ├── stores/            # Pinia 状态管理
    │   └── views/             # 页面组件
    │       ├── Login.vue      # 登录页
    │       ├── Register.vue   # 注册页
    │       ├── Home.vue       # 首页
    │       ├── Products.vue   # 商品列表（用户）
    │       ├── Orders.vue     # 订单列表（用户）
    │       ├── Addresses.vue  # 地址管理
    │       ├── Admin.vue      # 管理后台首页
    │       ├── AdminProducts.vue  # 商品管理
    │       └── AdminOrders.vue    # 订单管理
    └── package.json
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+

### 2. 数据库初始化

```bash
# 登录 MySQL 并执行初始化脚本
mysql -u root -p < backend/src/main/resources/schema.sql
```

### 3. 配置邮件通知（可选）

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  mail:
    username: your-163-email@163.com    # 你的 163 邮箱
    password: your-smtp-password         # SMTP 授权码（非登录密码）

app:
  admin-email: your-email@example.com   # 管理员接收通知的邮箱
```

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动。

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端将在 `http://localhost:5173` 启动。

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | (自行注册) | - |

## 功能说明

### 普通用户功能

1. **注册/登录** - 创建账号并登录系统
2. **浏览商品** - 查看商品列表，按分类筛选
3. **购物车** - 将商品加入购物车，支持数量修改、删除、清空
4. **下单** - 支持单商品直接下单或多商品批量下单
5. **查看订单** - 查看订单列表和状态
6. **取消订单** - 可取消未处理的订单
7. **管理地址** - 添加、编辑、删除收货地址，设置默认地址

### 管理员功能

1. **商品管理** - 添加、编辑、删除商品，设置商品规格（上架/下架）
2. **订单管理** - 查看所有订单，将订单标记为"已处理"
3. **邮件通知** - 用户下单时自动发送邮件通知管理员

## API 接口

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 登录 |
| POST | /api/auth/register | 注册 |
| GET | /api/auth/me | 获取当前用户信息 |

### 商品接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/products | 获取商品列表 |
| GET | /api/products/{id} | 获取商品详情 |
| POST | /api/products | 添加商品（管理员） |
| PUT | /api/products/{id} | 更新商品（管理员） |
| DELETE | /api/products/{id} | 删除商品（管理员） |

### 订单接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/orders | 创建订单（单商品） |
| POST | /api/orders/batch | 批量创建订单（多商品） |
| GET | /api/orders/my | 获取我的订单 |
| GET | /api/orders | 获取所有订单（管理员） |
| GET | /api/orders/{id} | 获取订单详情 |
| PUT | /api/orders/{id}/handle | 处理订单（管理员） |
| PUT | /api/orders/{id}/cancel | 取消订单 |

### 地址接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/addresses | 获取我的地址 |
| POST | /api/addresses | 添加地址 |
| PUT | /api/addresses/{id} | 更新地址 |
| DELETE | /api/addresses/{id} | 删除地址 |

### 分类接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/categories | 获取分类列表 |
| POST | /api/categories | 添加分类（管理员） |
| PUT | /api/categories/{id} | 更新分类（管理员） |
| DELETE | /api/categories/{id} | 删除分类（管理员） |

## 数据库表

### user - 用户表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| role | VARCHAR(20) | 角色：ADMIN / USER |
| nickname | VARCHAR(50) | 昵称 |
| email | VARCHAR(100) | 邮箱 |
| created_at | DATETIME | 创建时间 |

### category - 分类表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 分类名称 |
| sort | INT | 排序 |

### product - 商品表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 商品名称 |
| image_url | VARCHAR(500) | 图片URL |
| price | DECIMAL(10,2) | 价格 |
| category_id | BIGINT | 分类ID |
| status | INT | 状态：0下架 / 1上架 |
| created_at | DATETIME | 创建时间 |

### product_spec - 商品规格表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| product_id | BIGINT | 商品ID |
| spec_name | VARCHAR(50) | 规格名称（如：甜度） |
| spec_values | JSON | 规格选项数组 |

### address - 地址表
| 字段 |类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| name | VARCHAR(50) | 收货人 |
| phone | VARCHAR(20) | 电话 |
| address | VARCHAR(255) | 详细地址 |
| is_default | INT | 默认地址：0否 / 1是 |

### orders - 订单表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| product_id | BIGINT | 商品ID |
| specs | JSON | 选择的规格 |
| address | VARCHAR(500) | 收货地址 |
| remark | VARCHAR(500) | 备注 |
| status | INT | 状态：0待处理 / 1已处理 |
| price | DECIMAL(10,2) | 价格 |
| created_at | DATETIME | 创建时间 |
| handled_at | DATETIME | 处理时间 |

## 部署说明

### 打包后端

```bash
cd backend
mvn clean package -DskipTests
```

生成的 JAR 文件在 `backend/target/seven-shop-1.0.0.jar`

### 部署到 Railway/Render

1. 将打包后的 JAR 上传
2. 配置环境变量：
   - `DATABASE_URL`: MySQL 连接地址
   - `MAIL_USERNAME`: 163 邮箱
   - `MAIL_PASSWORD`: SMTP 授权码
   - `ADMIN_EMAIL`: 管理员邮箱
