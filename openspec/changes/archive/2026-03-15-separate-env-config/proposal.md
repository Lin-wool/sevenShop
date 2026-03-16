## 为什么

当前 application.yml 中数据库连接配置是硬编码的本地配置。当部署到 Railway 等生产环境时，无法连接数据库，因为生产环境使用不同的数据库连接信息。需要区分本地开发和部署环境的配置文件。

## 变更内容

- 创建 `application-local.yml` 本地开发配置文件
- 创建 `application-prod.yml` 生产环境配置文件
- 修改 `application.yml` 作为默认/共享配置
- 配置 Spring Profiles 以根据环境自动切换

## 功能 (Capabilities)

### 新增功能

- **env-config-separation**: 环境配置分离 - 创建本地和生产环境的数据库配置分离方案

### 修改功能

- （无）

## 影响

- `backend/src/main/resources/application.yml` - 修改为共享配置
- `backend/src/main/resources/application-local.yml` - 新增本地配置
- `backend/src/main/resources/application-prod.yml` - 新增生产配置
