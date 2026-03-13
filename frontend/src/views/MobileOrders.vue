<template>
  <div class="mobile-page">
    <!-- 顶部区域 -->
    <div class="mobile-header">
      <div class="header-left" @click="router.push('/m')">
        <span class="back-icon">←</span>
      </div>
      <div class="header-title">我的订单</div>
      <div class="header-right"></div>
    </div>

    <!-- 状态筛选 -->
    <div class="status-tabs">
      <div
        class="status-tab"
        :class="{ active: status === null }"
        @click="changeStatus(null)"
      >
        全部
      </div>
      <div
        class="status-tab"
        :class="{ active: status === 0 }"
        @click="changeStatus(0)"
      >
        待处理
      </div>
      <div
        class="status-tab"
        :class="{ active: status === 1 }"
        @click="changeStatus(1)"
      >
        已处理
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-loading="loading">
      <div
        v-for="order in orders"
        :key="order.id"
        class="order-card"
      >
        <div class="order-header">
          <span class="order-no">订单号：{{ order.id }}</span>
          <span class="order-status" :class="order.status === 0 ? 'pending' : 'processed'">
            {{ order.status === 0 ? '待处理' : '已处理' }}
          </span>
        </div>
        <div class="order-product">
          <img :src="order.productImage || 'https://via.placeholder.com/80'" class="product-img" />
          <div class="product-info">
            <h3>{{ order.productName }}</h3>
            <p class="specs" v-if="order.specs">{{ formatSpecs(order.specs) }}</p>
            <p class="price-row">
              <span class="price">¥{{ order.price }}</span>
              <span class="quantity">x{{ order.quantity || 1 }}</span>
            </p>
          </div>
        </div>
        <div class="order-footer">
          <div class="address-info">
            <span class="label">配送至：</span>
            <span>{{ order.address }}</span>
          </div>
          <div class="order-time">{{ formatTime(order.createdAt) }}</div>
        </div>
      </div>

      <el-empty v-if="!loading && orders.length === 0" description="暂无订单，快去下单吧~" />

      <!-- 底部占位 -->
      <div class="bottom-space"></div>
    </div>

    <!-- 底部导航栏 -->
    <div class="mobile-tabbar">
      <div class="tab-item" @click="router.push('/m')">
        <div class="tab-icon-wrap">
          <span class="tab-icon">🏪</span>
        </div>
        <span>商城</span>
      </div>
      <div class="tab-item active">
        <div class="tab-icon-wrap">
          <span class="tab-icon">📋</span>
        </div>
        <span>订单</span>
      </div>
      <div class="tab-item" @click="router.push('/m/addresses')">
        <div class="tab-icon-wrap">
          <span class="tab-icon">🚚</span>
        </div>
        <span>配送</span>
      </div>
      <div class="tab-item" @click="router.push('/m/profile')">
        <div class="tab-icon-wrap">
          <span class="tab-icon">👤</span>
        </div>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const orders = ref([])
const status = ref(null)

// 组件挂载时加载数据
onMounted(() => {
  fetchOrders()
})

// 监听路由变化，刷新数据
watch(() => route.fullPath, () => {
  fetchOrders()
})

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await api.get('/orders/my', {
      params: {
        page: 1,
        size: 50,
        status: status.value
      }
    })
    orders.value = res.data.records
  } catch (error) {
    ElMessage.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

const changeStatus = (s) => {
  status.value = s
  fetchOrders()
}

const formatSpecs = (specs) => {
  if (!specs) return ''
  if (typeof specs === 'string') {
    try {
      specs = JSON.parse(specs)
    } catch {
      return specs
    }
  }
  return Object.entries(specs)
    .map(([key, value]) => `${key}: ${value}`)
    .join(', ')
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.mobile-page {
  min-height: 100vh;
  background: #f8f9fa;
}

/* 顶部区域 */
.mobile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left, .header-right {
  width: 40px;
}

.back-icon {
  color: white;
  font-size: 20px;
  cursor: pointer;
}

.header-title {
  color: white;
  font-size: 18px;
  font-weight: bold;
}

/* 状态筛选 */
.status-tabs {
  display: flex;
  background: white;
  padding: 12px 16px;
  gap: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.status-tab {
  flex: 1;
  text-align: center;
  padding: 10px;
  border-radius: 20px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.status-tab.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

/* 订单列表 */
.order-list {
  padding: 12px;
}

.order-card {
  background: white;
  border-radius: 12px;
  margin-bottom: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-bottom: 1px solid #f5f5f5;
}

.order-no {
  font-size: 12px;
  color: #999;
}

.order-status {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 10px;
}

.order-status.pending {
  color: #ff9800;
  background: #fff3e0;
}

.order-status.processed {
  color: #4caf50;
  background: #e8f5e9;
}

.order-product {
  display: flex;
  gap: 12px;
  padding: 14px;
}

.product-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.product-info {
  flex: 1;
}

.product-info h3 {
  margin: 0 0 6px;
  font-size: 14px;
  color: #333;
}

.product-info .specs {
  font-size: 12px;
  color: #999;
  margin: 0 0 6px;
}

.product-info .price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0;
}

.product-info .price {
  font-size: 16px;
  font-weight: bold;
  color: #ff4d4f;
}

.product-info .quantity {
  font-size: 14px;
  color: #666;
}

.order-footer {
  padding: 12px 14px;
  background: #fafafa;
}

.address-info {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.address-info .label {
  color: #999;
}

.order-time {
  font-size: 11px;
  color: #bbb;
}

.bottom-space {
  height: 70px;
}

/* 底部导航栏 */
.mobile-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  justify-content: space-around;
  padding: 8px 0 12px;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.06);
  z-index: 1000;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #999;
  cursor: pointer;
}

.tab-item.active {
  color: #667eea;
}

.tab-icon-wrap {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-icon {
  font-size: 22px;
}
</style>
