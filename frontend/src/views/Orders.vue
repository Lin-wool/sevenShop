<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>我的订单</span>
        <el-button type="primary" @click="router.push('/products')">
          去下单
        </el-button>
      </div>
    </template>

    <el-tabs v-model="activeTab" @tab-change="fetchOrders">
      <el-tab-pane label="全部" :name="null" />
      <el-tab-pane label="待处理" :name="0" />
      <el-tab-pane label="已处理" :name="1" />
    </el-tabs>

    <el-table :data="orders" v-loading="loading">
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <div class="product-cell">
            <img
              :src="row.productImage || 'https://via.placeholder.com/60'"
              class="product-image"
            />
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
      <el-table-column label="备注" min-width="120">
        <template #default="{ row }">
          {{ row.remark || '-' }}
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
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '../api'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const orders = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const activeTab = ref(null)

// 组件挂载时加载数据
onMounted(() => {
  fetchOrders()
})

// 监听路由变化，刷新数据
watch(() => route.fullPath, () => {
  fetchOrders()
})

const fetchOrders = async (newPage = 1) => {
  loading.value = true
  try {
    const res = await api.get('/orders/my', {
      params: {
        page: newPage,
        size: size.value,
        status: activeTab.value
      }
    })
    orders.value = res.data.records
    total.value = res.data.total
    page.value = newPage
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
  margin-right: 8px;
}

.price {
  color: #ff6b6b;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
