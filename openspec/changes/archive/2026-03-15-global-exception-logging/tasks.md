## 1. 创建全局异常处理器

- [x] 1.1 创建 GlobalExceptionHandler 类，使用 @ControllerAdvice 注解
- [x] 1.2 实现 handleException 方法捕获通用 Exception
- [x] 1.3 实现 handleBusinessException 方法捕获业务异常
- [x] 1.4 实现 handleValidationException 方法捕获参数验证异常

## 2. 实现格式化日志输出

- [x] 2.1 使用 SLF4J 进行日志记录
- [x] 2.2 在异常处理方法中记录请求方法、路径、参数
- [x] 2.3 在异常处理方法中记录异常类型、消息、堆栈
- [x] 2.4 创建统一的日志格式化工具类（已集成在 GlobalExceptionHandler 中）

## 3. 实现友好的错误响应

- [x] 3.1 创建统一的错误响应类 ApiResponse
- [x] 3.2 实现错误码和错误消息的返回格式
- [x] 3.3 确保异常处理不影响正常业务流程

## 4. 测试验证

- [ ] 4.1 测试订单列表接口异常时的日志输出
- [ ] 4.2 验证日志格式是否清晰明了
- [ ] 4.3 验证前端收到的错误响应是否友好
