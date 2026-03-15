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
      <el-tab-pane label="已取消" :name="-1" />
    </el-tabs>

    <el-table :data="orders" v-loading="loading">
      <el-table-column label="订单号" width="80">
        <template #default="{ row }">
          <span>#{{ row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column label="商品" min-width="250">
        <template #default="{ row }">
          <!-- 多商品订单 -->
          <div v-if="row.items && row.items.length > 0" class="order-items">
            <div v-for="item in row.items" :key="item.id" class="product-cell">
              <img
                :src="item.productImage || 'https://via.placeholder.com/50'"
                class="product-image"
              />
              <div>
                <div class="product-name">{{ item.productName }}</div>
                <div class="product-specs" v-if="item.specs && Object.keys(item.specs).length > 0">
                  <span v-for="(value, key) in item.specs" :key="key">
                    {{ key }}: {{ value }}
                  </span>
                </div>
                <div class="quantity">x{{ item.quantity }}</div>
              </div>
            </div>
          </div>
          <!-- 兼容旧数据 -->
          <div v-else class="product-cell">
            <img
              :src="row.productImage || 'https://via.placeholder.com/50'"
              class="product-image"
            />
            <div>
              <div class="product-name">{{ row.productName }}</div>
              <div class="product-specs" v-if="row.specs">
                <span v-for="(value, key) in row.specs" :key="key">
                  {{ key }}: {{ value }}
                </span>
              </div>
              <div class="quantity">x{{ row.quantity || 1 }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="总价" width="100">
        <template #default="{ row }">
          <span class="price">¥{{ row.totalPrice || row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="address" label="收货地址" min-width="150" />
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
            type="danger"
            size="small"
            link
            @click="handleCancelOrder(row)"
          >
            取消订单
          </el-button>
          <span v-else-if="row.status === -1" class="cancel-reason">
            {{ row.cancelReason || '已取消' }}
          </span>
          <span v-else>-</span>
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

    <!-- 取消订单弹窗 -->
    <el-dialog v-model="cancelDialogVisible" title="取消订单" width="400px">
      <el-form>
        <el-form-item label="取消原因">
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入取消原因（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCancelOrder" :loading="canceling">
          确定
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const orders = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const activeTab = ref(null)

// 取消订单相关
const cancelDialogVisible = ref(false)
const cancelForm = ref({ reason: '' })
const canceling = ref(false)
const currentCancelOrder = ref(null)

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
    orders.value = res.records
    total.value = res.total
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

// 取消订单
const handleCancelOrder = (order) => {
  currentCancelOrder.value = order
  cancelForm.value.reason = ''
  cancelDialogVisible.value = true
}

const confirmCancelOrder = async () => {
  if (!currentCancelOrder.value) return

  canceling.value = true
  try {
    await api.put(`/orders/${currentCancelOrder.value.id}/cancel`, {
      cancelReason: cancelForm.value.reason
    })
    ElMessage.success('订单已取消')
    cancelDialogVisible.value = false
    fetchOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '取消失败')
  } finally {
    canceling.value = false
  }
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

.order-items {
  max-height: 150px;
  overflow-y: auto;
}

.quantity {
  font-size: 12px;
  color: #666;
}

.cancel-reason {
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
</style>
