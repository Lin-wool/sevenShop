## 上下文

### 背景
- 移动端已有 MobileTabbar.vue 组件
- MobileCart.vue 已使用该组件
- 其他页面（Mobile.vue、MobileOrders.vue、MobileAddresses.vue、MobileProfile.vue、MobileAdmin.vue）仍使用内联代码

### 约束
- 保持现有页面结构和功能不变
- 确保导航栏样式完全一致

## 目标 / 非目标

**目标：**
- 所有移动端页面使用统一的 MobileTabbar 组件
- 移除重复的内联导航栏代码

**非目标：**
- 不修改页面业务逻辑

## 决策

**方案**：统一引用 MobileTabbar 组件

- 已有的 MobileTabbar.vue 组件设计良好，支持：
  - 商城、订单、购物车、配送、我的 五个tab
  - 购物车数量badge显示
  - 当前路由高亮

## 风险 / 权衡

- 风险：样式微调可能需要适配 → 风险较低，组件已存在
