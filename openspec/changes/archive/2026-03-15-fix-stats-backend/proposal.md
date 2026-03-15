# 修复统计数据后端问题

## 为什么

数据概览统计功能仍然无法正确显示数量；订单查询API未返回总条数，导致分页统计失败。

## 变更内容

1. **修复订单API返回总条数** - 后端OrderController需要返回分页总条数
2. **修复统计数据获取** - 确保前端正确解析API响应

## 功能 (Capabilities)

### 修改功能

- `order-api`: 订单API返回分页总条数

## 影响

- **后端**: OrderController.java
- **前端**: Admin.vue, MobileAdmin.vue
