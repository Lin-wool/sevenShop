# SevenShop 部署避坑指南

## 项目结构
- 前端: `frontend/` (Vue 3 + Vite)
- 后端: `backend/` (Spring Boot 3 + MyBatis-Plus)
- 部署: 前端 Netlify + 后端 Railway + MySQL

---

## 1. 前端部署 (Netlify)

### 问题1: 找不到 package.json
**错误**: `Could not read package.json`
**原因**: 前端在 `frontend` 子目录，Netlify 默认在根目录查找
**解决**: 在 Netlify 后台配置:
```
Base directory: frontend
Build command: npm run build
Publish directory: dist
```

### 问题2: SPA 路由 404
**错误**: `Page not found`
**原因**: Vue Router 使用 History 模式，直接访问子路径返回 404
**解决**: 创建 `frontend/public/_redirects`:
```
/*    /index.html   200
```

### 问题3: API 路径不对
**原因**: 后端路径是 `/api/auth`，前端没加 `/api` 前缀
**解决**: 创建 `frontend/.env.production`:
```
VITE_API_BASE_URL=https://your-backend-url/api
```

---

## 2. 后端部署 (Railway)

### 问题1: 找不到 start.sh
**错误**: `Script start.sh not found`
**原因**: Railway 无法识别多模块项目结构
**解决**: 创建 `Railway.toml`:
```toml
[build]
builder = "docker"
startCommand = "java -jar app.jar"
```

### 问题2: Java 命令找不到
**错误**: `java: command not found`
**原因**: Railway 默认环境没有 Java
**解决**: 使用 Dockerfile 多阶段构建，包含 Java 运行时

### 问题3: 数据库连接失败
**错误**: `Communications link failure`
**原因**: 环境变量没有正确传递到 Docker 容器
**解决**:
1. Railway 后台配置环境变量 (不是项目级别，是服务级别):
   - `DB_HOST`: MySQL 主机地址
   - `DB_PORT`: 端口
   - `DB_USER`: 用户名
   - `DB_PASSWORD`: 密码
   - `DB_NAME`: 数据库名

2. Spring Boot 配置使用环境变量:
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:-localhost}:${DB_PORT:-3306}/${DB_NAME:-sevenshop}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: ${DB_USER:-root}
    password: ${DB_PASSWORD:-root123}
```

### 问题4: NullPointerException
**错误**: `Map.of()` 放入 null 值导致 NPE
**原因**: `e.getMessage()` 可能返回 null
**解决**: 总是检查 null:
```java
} catch (Exception e) {
    return ResponseEntity.badRequest().body(Map.of("message", e.getMessage() != null ? e.getMessage() : "登录失败"));
}
```

---

## 3. 关键配置文件

### Dockerfile
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend/pom.xml backend/
COPY backend/src backend/src
RUN mvn -f backend/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/backend/target/seven-shop-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Railway.toml
```toml
[build]
builder = "docker"
startCommand = "java -jar app.jar"
```

### frontend/.env.production
```
VITE_API_BASE_URL=https://sevenshop-production.up.railway.app/api
```

### frontend/.env.development (本地开发用)
```
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 4. 部署检查清单

- [ ] 本地测试通过 (连接 Railway 数据库)
- [ ] Railway 环境变量配置正确 (在服务级别，不是项目级别)
- [ ] MySQL 服务状态为 Active
- [ ] 前端 `_redirects` 文件已创建
- [ ] 前端 `.env.production` 配置了正确的 API 地址
- [ ] 前端 Netlify 配置了正确的 base directory
