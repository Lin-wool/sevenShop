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
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.welcome-card {
  margin-bottom: 24px;
  border-radius: var(--radius-xl);
  background: linear-gradient(135deg, var(--color-primary) 0%, #6366F1 100%);
  border: none;
  overflow: hidden;
  position: relative;
}

.welcome-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.welcome-card :deep(.el-card__body) {
  padding: 48px 32px;
  position: relative;
  z-index: 1;
}

.welcome {
  text-align: center;
  padding: 20px;
}

.welcome h2 {
  color: #fff;
  margin-bottom: 12px;
  font-size: 28px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.welcome p {
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 28px;
  font-size: 16px;
}

.quick-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.quick-actions .el-button {
  padding: 12px 28px;
  font-size: 15px;
  border-radius: var(--radius-lg);
  font-weight: 500;
  transition: all 0.3s ease;
}

.quick-actions .el-button--primary {
  background: #fff;
  color: var(--color-primary);
  border: none;
}

.quick-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}

.quick-actions .el-button:not(.el-button--primary) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.quick-actions .el-button:not(.el-button--primary):hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.stats-row {
  margin-top: 8px;
}

.stats-row :deep(.el-col) {
  padding: 0 8px;
}

.stat-card {
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  padding: 20px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid var(--color-border-light);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  margin-left: 16px;
  flex: 1;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

@media (max-width: 768px) {
  .home-content {
    padding: 16px;
  }

  .welcome-card :deep(.el-card__body) {
    padding: 32px 20px;
  }

  .welcome h2 {
    font-size: 22px;
  }

  .welcome p {
    font-size: 14px;
  }

  .quick-actions {
    flex-direction: column;
    gap: 12px;
  }

  .quick-actions .el-button {
    width: 100%;
  }

  .stats-row :deep(.el-col) {
    margin-bottom: 12px;
  }
}
</style>
