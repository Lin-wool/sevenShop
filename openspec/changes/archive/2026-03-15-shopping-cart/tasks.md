## 1. 数据库结构修改

- [x] 1.1 新增 order_items 表（id, order_id, product_id, specs, price, quantity）
- [x] 1.2 修改 orders 表增加 total_price 字段
- [x] 1.3 修改 orders 表增加 cancel_reason 字段

## 2. 后端 - 订单模块改造

- [x] 2.1 创建 OrderItem 实体类
- [x] 2.2 创建 OrderItemMapper
- [x] 2.3 修改 Order 实体（添加 totalPrice, cancelReason，调整 status 注释）
- [x] 2.4 创建批量下单接口 OrderController POST /orders/batch
- [x] 2.5 实现 OrderService.createBatchOrder() 方法
- [x] 2.6 创建取消订单接口 OrderController PUT /orders/{id}/cancel
- [x] 2.7 实现 OrderService.cancelOrder() 方法（status=0 才可取消）
- [x] 2.8 修改订单查询返回逻辑，包含 orderItems

## 3. 后端 - 购物车 API（可选）

- [ ] 3.1 创建 Cart 实体类（可选，用于数据同步）
- [ ] 3.2 创建 CartController（可选）
- [ ] 3.3 实现购物车 CRUD（可选）

## 4. 前端 - 购物车功能

- [x] 4.1 创建购物车 Store/Composable（LocalStorage 操作）
- [x] 4.2 添加商品到购物车方法
- [x] 4.3 获取购物车商品列表方法
- [x] 4.4 更新购物车商品数量方法
- [x] 4.5 删除购物车商品方法
- [x] 4.6 清空购物车方法

## 5. 前端 - 页面开发

- [x] 5.1 创建购物车页面 Cart.vue（PC端）
- [x] 5.2 创建购物车页面 MobileCart.vue（移动端）
- [x] 5.3 修改商品详情页 Products.vue（添加加入购物车按钮）
- [x] 5.4 修改商品详情页 Mobile.vue（添加加入购物车按钮）
- [x] 5.5 修改订单确认流程，支持多商品

## 6. 前端 - 订单取消功能

- [x] 6.1 修改订单列表页，增加取消按钮
- [x] 6.2 创建取消订单弹窗组件
- [x] 6.3 调用取消订单 API
- [x] 6.4 更新订单状态显示

## 7. 测试与联调

- [ ] 7.1 后端单元测试（OrderService, OrderItem）
- [ ] 7.2 前端功能测试（购物车、订单创建、订单取消）
- [ ] 7.3 前后端联调测试
