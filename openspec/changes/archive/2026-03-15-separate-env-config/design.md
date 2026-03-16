## 上下文

当前 `application.yml` 中数据库配置是硬编码的本地连接信息：
- 数据库地址：`localhost:3306`
- 用户名：`root`
- 密码：`root123`

部署到 Railway 时，生产环境使用环境变量提供不同的数据库连接信息，当前配置无法适配。

## 目标 / 非目标

**目标：**
- 实现本地开发和生产环境配置分离
- 本地开发使用 `application-local.yml`
- 生产环境使用 `application-prod.yml` 或环境变量
- 不破坏现有的 CI/CD 部署流程

**非目标：**
- 不修改业务代码
- 不改变现有的数据库结构

## 决策

1. **使用 Spring Profiles 进行环境隔离**
   - 本地开发激活 `local` profile
   - 生产环境激活 `prod` profile

2. **配置优先级策略**
   - `application.yml` 保留共享配置（如端口、编码等）
   - `application-local.yml` 覆盖本地数据库配置
   - `application-prod.yml` 从环境变量读取数据库配置

3. **环境变量命名规范**
   - 使用 Railway 支持的标准环境变量格式：
     - `DATABASE_URL` 或分解为 `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`

## 风险 / 权衡

- **风险**：本地测试时忘记激活 profile 导致连接生产数据库
  - 缓解：本地默认使用 local profile，生产环境必须显式配置
- **风险**：密码等敏感信息明文存储在配置文件中
  - 缓解：生产环境使用环境变量，不存储在文件中
