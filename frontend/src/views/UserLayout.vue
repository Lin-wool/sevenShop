<template>
  <div class="user-layout">
    <el-container>
      <el-aside width="200px">
        <div class="logo" @click="router.push('/')">
          <span class="logo-icon">💕</span>
          <span class="logo-text">情侣商城</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          class="user-menu"
        >
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/products">
            <el-icon><ShoppingBag /></el-icon>
            <span>商品列表</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><List /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="/addresses">
            <el-icon><Location /></el-icon>
            <span>收货地址</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/admin">
            <el-icon><Setting /></el-icon>
            <span>管理后台</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-title">{{ pageTitle }}</div>
          <div class="user-info">
            <span>欢迎，{{ userStore.user?.nickname || userStore.user?.username }}</span>
            <el-button type="danger" link @click="handleLogout">
              退出登录
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
import { House, ShoppingBag, List, Location, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const isAdmin = computed(() => userStore.user?.role === 'ADMIN')

const pageTitle = computed(() => {
  const titles = {
    '/': '首页',
    '/products': '商品列表',
    '/orders': '我的订单',
    '/addresses': '收货地址'
  }
  return titles[route.path] || '情侣商城'
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.user-layout {
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
  cursor: pointer;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b6b;
}

.user-menu {
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
