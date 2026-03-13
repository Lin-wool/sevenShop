<template>
  <div class="home-content">
    <el-card class="welcome-card">
      <div class="welcome">
        <h2>💝 {{ getGreeting() }}，{{ userStore.user?.nickname || userStore.user?.username }}~</h2>
        <p>在这里许下你的愿望，让TA帮你实现</p>
        <div class="quick-actions">
          <el-button type="primary" @click="router.push('/products')">
            <el-icon><ShoppingBag /></el-icon>
            去逛商城
          </el-button>
          <el-button @click="router.push('/orders')">
            <el-icon><List /></el-icon>
            查看订单
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #ff6b6b;">
            <el-icon size="32"><ShoppingBag /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.productCount }}</div>
            <div class="stat-label">商品数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #4ecdc4;">
            <el-icon size="32"><List /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.orderCount }}</div>
            <div class="stat-label">我的订单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #45b7d1;">
            <el-icon size="32"><Location /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.addressCount }}</div>
            <div class="stat-label">收货地址</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ShoppingBag, List, Location } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const userStore = useUserStore()

const stats = ref({
  productCount: 0,
  orderCount: 0,
  addressCount: 0
})

const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
}

const fetchStats = async () => {
  try {
    const [productsRes, ordersRes, addressesRes] = await Promise.all([
      api.get('/products', { params: { page: 1, size: 1 } }),
      api.get('/orders/my', { params: { page: 1, size: 1 } }),
      api.get('/addresses')
    ])
    stats.value = {
      productCount: productsRes.data.total || 0,
      orderCount: ordersRes.data.total || 0,
      addressCount: addressesRes.data.length || 0
    }
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.home-content {
  width: 100%;
}

.welcome-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.welcome {
  text-align: center;
  padding: 40px 20px;
}

.welcome h2 {
  color: #333;
  margin-bottom: 12px;
}

.welcome p {
  color: #999;
  margin-bottom: 24px;
}

.quick-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.stats-row {
  margin-top: 20px;
}

.stat-card {
  border-radius: 12px;
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  margin-left: 16px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
}
</style>
