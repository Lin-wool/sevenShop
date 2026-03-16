## 1. 创建配置文件

- [x] 1.1 创建 `application-local.yml` 本地开发配置
- [x] 1.2 创建 `application-prod.yml` 生产环境配置
- [x] 1.3 修改 `application.yml` 为共享基础配置

## 2. 配置测试

- [ ] 2.1 使用本地 profile 启动应用验证本地配置
  - 命令: `./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local`
- [ ] 2.2 配置生产环境变量测试生产配置
  - 命令: `./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod`
  - 需要设置环境变量: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
