<template>
  <div class="admin-overview">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #ff6b6b;">
            <el-icon size="32"><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.productCount }}</div>
            <div class="stat-label">商品数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #ffb347;">
            <el-icon size="32"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingOrderCount }}</div>
            <div class="stat-label">待处理订单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #4ecdc4;">
            <el-icon size="32"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.handledOrderCount }}</div>
            <div class="stat-label">已完成订单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #45b7d1;">
            <el-icon size="32"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.userCount }}</div>
            <div class="stat-label">用户数量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="recent-orders">
      <template #header>
        <div class="card-header">
          <span>最新订单</span>
          <el-button type="primary" link @click="router.push('/admin/orders')">
            查看全部
          </el-button>
        </div>
      </template>

      <el-table :data="recentOrders" v-loading="loading">
        <el-table-column label="商品" min-width="150">
          <template #default="{ row }">
            {{ row.productName }}
          </template>
        </el-table-column>
        <el-table-column label="用户" width="120">
          <template #default="{ row }">
            {{ row.userNickname }}
          </template>
        </el-table-column>
        <el-table-column label="规格" min-width="120">
          <template #default="{ row }">
            <span v-if="row.specs">
              <span v-for="(value, key) in row.specs" :key="key" class="spec-tag">
                {{ key }}: {{ value }}
              </span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'">
              {{ row.status === 0 ? '待处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              @click="handleOrder(row.id)"
            >
              处理
            </el-button>
            <span v-else>已完成</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Goods, Document, CircleCheck, User } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const loading = ref(false)

const stats = ref({
  productCount: 0,
  pendingOrderCount: 0,
  handledOrderCount: 0,
  userCount: 0
})

const recentOrders = ref([])

const fetchStats = async () => {
  try {
    const [productsRes, ordersRes, allOrdersRes] = await Promise.all([
      api.get('/products', { params: { page: 1, size: 1 } }),
      api.get('/orders', { params: { page: 1, size: 1, status: 0 } }),
      api.get('/orders', { params: { page: 1, size: 100 } })
    ])

    const allOrders = allOrdersRes.data.records || []
    const handledCount = allOrders.filter(o => o.status === 1).length

    stats.value = {
      productCount: productsRes.data.total || 0,
      pendingOrderCount: ordersRes.data.total || 0,
      handledOrderCount: handledCount,
      userCount: 1
    }
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

const fetchRecentOrders = async () => {
  loading.value = true
  try {
    const res = await api.get('/orders', {
      params: { page: 1, size: 5 }
    })
    recentOrders.value = res.data.records
  } catch (error) {
    console.error('获取订单失败:', error)
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const handleOrder = async (id) => {
  try {
    await api.put(`/orders/${id}/handle`)
    ElMessage.success('处理成功')
    fetchRecentOrders()
    fetchStats()
  } catch (error) {
    ElMessage.error('处理失败')
  }
}

onMounted(() => {
  fetchStats()
  fetchRecentOrders()
})
</script>

<style scoped>
.admin-overview {
  width: 100%;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  margin-bottom: 20px;
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

.recent-orders {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.spec-tag {
  display: inline-block;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
  margin-right: 4px;
  font-size: 12px;
}
</style>
