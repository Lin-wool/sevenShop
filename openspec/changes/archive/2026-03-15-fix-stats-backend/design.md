# 技术设计文档

## 上下文

订单API返回的分页数据可能缺少total字段，导致前端统计功能失败。

## 目标 / 非目标

**目标：**
- 修复OrderController返回分页total字段

**非目标：**
- 不改变业务逻辑

## 决策

### 1. 订单API修复
- **选择**: 检查OrderController返回格式，确保返回IPage包含total
- **理由**: 前端依赖total字段进行统计
