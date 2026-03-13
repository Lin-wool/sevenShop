<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="200px">
        <div class="logo">
          <span class="logo-icon">💕</span>
          <span class="logo-text">情侣商城</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          class="admin-menu"
        >
          <el-menu-item index="/admin">
            <el-icon><DataAnalysis /></el-icon>
            <span>概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/spec-templates">
            <el-icon><Document /></el-icon>
            <span>规格模板</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <span>返回商城</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-title">{{ pageTitle }}</div>
          <div class="user-info">
            <span>管理员：{{ userStore.user?.nickname || userStore.user?.username }}</span>
            <el-button type="danger" link @click="handleLogout">
              退出
            </el-button>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { DataAnalysis, Goods, Document, List, House } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  const titles = {
    '/admin': '管理概览',
    '/admin/products': '商品管理',
    '/admin/spec-templates': '规格模板',
    '/admin/orders': '订单管理'
  }
  return titles[route.path] || '管理后台'
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: #f5f5f5;
}

.el-container {
  min-height: 100vh;
}

.el-aside {
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b6b;
}

.admin-menu {
  border-right: none;
}

.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.main-content {
  padding: 20px;
  background: #f5f5f5;
}
</style>
