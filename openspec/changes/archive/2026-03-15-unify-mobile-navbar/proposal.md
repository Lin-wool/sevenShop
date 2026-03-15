## 为什么

移动端各页面（商品页、订单页、我的页面、地址页等）的底部导航栏实现方式不一致，有些页面使用内联代码，有些使用组件。这导致：
- 代码重复，维护困难
- 样式不一致
- 购物车数量badge等功能无法统一管理

## 变更内容

1. **统一使用 MobileTabbar 组件** - 所有移动端页面都引用统一的底部导航栏组件
2. **组件增强** - 确保组件支持所有页面的导航需求
3. **移除重复代码** - 删除各页面内联的导航栏 HTML 和 CSS

## 功能 (Capabilities)

### 新增功能
- **unify-navbar**: 统一移动端导航栏 - 将所有页面的底部导航栏抽象为统一组件

### 修改功能
- （无）

## 影响

- **前端**: 修改 Mobile.vue、MobileOrders.vue、MobileAddresses.vue、MobileProfile.vue、MobileAdmin.vue
