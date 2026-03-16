## 1. 安全性修复

- [x] 1.1 移除 OrderRequest.java 中的 price 字段，防止客户端篡改价格
- [x] 1.2 修改 OrderService.java，从商品表获取价格而非客户端传入
- [x] 1.3 为 OrderRequest.java 添加输入校验注解（@NotNull, @Min, @Max）
- [x] 1.4 为其他 DTO（RegisterRequest等）添加密码强度校验

## 2. 性能优化 - N+1 查询

- [x] 2.1 优化 OrderService.getUserOrders()：使用 JOIN 查询获取订单项
- [x] 2.2 优化 OrderService.getAllOrders()：使用 JOIN 查询获取订单项
- [x] 2.3 优化 ProductController.getProductList()：批量查询分类信息
- [x] 2.4 优化 OrderService.createBatchOrder()：先收集商品ID批量查询

## 3. 可维护性改进

- [x] 3.1 创建 OrderStatus 枚举类，替代 Magic Numbers
- [x] 3.2 创建 BusinessException 统一异常处理（已存在）
- [x] 3.3 为关键业务方法添加日志记录（如下单、支付）

## 4. CORS 配置优化

- [x] 4.1 修改 WebMvcConfig.java，使用配置文件管理 CORS 来源
- [x] 4.2 在 application.yml 中添加 allowedOrigins 配置项
