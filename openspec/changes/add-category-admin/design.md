## 上下文

项目是一个前后端分离的电商系统（sevenShop），使用 Vue3 + Element Plus 管理后台。后端已有分类管理的 REST API，但前端缺少管理界面。

## 目标 / 非目标

**目标：**
- 创建分类管理页面，支持 CRUD 操作
- 在后台管理左侧菜单添加分类管理入口

**非目标：**
- 不修改后端 API
- 不添加分类层级功能（暂只支持平铺分类）

## 决策

### 1. 页面设计
参考现有的 AdminProducts.vue 和 AdminUsers.vue 的布局风格，使用 Element Plus 表格 + Dialog 弹窗实现。

### 2. API 调用
直接复用现有的 `/api/categories` 接口，无需额外封装。

## 风险 / 权衡

- 无重大风险，实现相对简单
