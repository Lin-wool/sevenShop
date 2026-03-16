## 为什么

当前系统缺少商品分类的管理界面，管理员只能在数据库中管理分类，不便于运营人员使用。添加分类管理界面可以提升后台管理效率。

## 变更内容

1. 新增分类管理页面 (`AdminCategories.vue`)
2. 在后台管理左侧菜单添加"分类管理"入口
3. 分类管理功能：列表展示、添加分类、编辑分类、删除分类

## 功能 (Capabilities)

### 新增功能
- `category-admin`: 商品分类管理界面

### 修改功能
无需修改现有功能。

## 影响

### 后端
无需修改，后端已有完整的分类管理 API：
- `GET /api/categories` - 获取所有分类
- `POST /api/categories` - 创建分类
- `PUT /api/categories/{id}` - 更新分类
- `DELETE /api/categories/{id}` - 删除分类

### 前端
- 新增 `src/views/AdminCategories.vue` 页面
- 修改 `AdminLayout.vue` 添加菜单项
