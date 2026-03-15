<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>订单管理</span>
      </div>
    </template>

    <el-tabs v-model="statusFilter" @tab-change="fetchOrders">
      <el-tab-pane label="全部" :name="null" />
      <el-tab-pane label="待处理" :name="0" />
      <el-tab-pane label="已处理" :name="1" />
    </el-tabs>

    <el-table :data="orders" v-loading="loading">
      <el-table-column label="商品" min-width="180">
        <template #default="{ row }">
          <div class="product-cell">
            <img :src="row.productImage || 'https://via.placeholder.com/60'" class="product-image" />
            <div>
              <div class="product-name">{{ row.productName }}</div>
              <div class="product-specs" v-if="row.specs">
                    <span v-for="(value, key) in row.specs" :key="key">
                      {{ key }}: {{ value }}
                    </span>
              </div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="用户" width="120">
        <template #default="{ row }">
          <div>{{ row.userNickname }}</div>
          <div class="user-email">{{ row.userEmail }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="80">
        <template #default="{ row }">
          <span>x{{ row.quantity || 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="总价" width="100">
        <template #default="{ row }">
          <span class="price">¥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="address" label="收货地址" min-width="150" />
      <el-table-column label="备注" min-width="120">
        <template #default="{ row }">
          {{ row.remark || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 0"
            type="primary"
            size="small"
            @click="handleOrder(row.id)"
          >
            处理
          </el-button>
          <span v-else-if="row.status === 1" class="handled-time">
            {{ formatTime(row.handledAt) }}
          </span>
          <span v-else-if="row.status === -1" class="cancel-info">
            已取消
          </span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      class="pagination"
      :current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="fetchOrders"
    />

    <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const loading = ref(false)
const orders = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusFilter = ref(null)

const fetchOrders = async (newPage = 1) => {
  loading.value = true
  try {
    const res = await api.get('/orders', {
      params: {
        page: newPage,
        size: size.value,
        status: statusFilter.value
      }
    })
    orders.value = res.records
    total.value = res.total
    page.value = newPage
  } catch (error) {
    ElMessage.error('获取订单失败')
  } finally {
    loading.value = false
  }
}

const handleOrder = async (id) => {
  try {
    await api.put(`/orders/${id}/handle`)
    ElMessage.success('处理成功')
    fetchOrders()
  } catch (error) {
    ElMessage.error('处理失败')
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'success',
    '-1': 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    0: '待处理',
    1: '已处理',
    '-1': '已取消'
  }
  return texts[status] || '未知'
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 8px;
}

.product-name {
  font-weight: 500;
}

.product-specs {
  font-size: 12px;
  color: #999;
}

.product-specs span {
  display: inline-block;
  background: #f0f0f0;
  padding: 1px 6px;
  border-radius: 4px;
  margin-right: 4px;
}

.user-email {
  font-size: 12px;
  color: #999;
}

.price {
  color: #ff6b6b;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}

.handled-time {
  font-size: 12px;
  color: #999;
}

.cancel-info {
  font-size: 12px;
  color: #999;
}
</style>
