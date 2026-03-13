<template>
  <div class="mobile-page">
    <!-- 顶部区域 -->
    <div class="mobile-header">
      <div class="header-left" @click="router.push('/m')">
        <span class="back-icon">←</span>
      </div>
      <div class="header-title">管理中心</div>
      <div class="header-right"></div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <div class="menu-item" @click="currentTab = 'products'">
        <span class="menu-icon">🛍️</span>
        <span class="menu-text">商品管理</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="currentTab = 'orders'">
        <span class="menu-icon">📦</span>
        <span class="menu-text">订单管理</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-section" v-if="currentTab === 'products'">
      <div class="section-header">
        <span class="section-title">商品列表</span>
        <el-button type="primary" size="small" round @click="openProductDialog()">
          + 新增商品
        </el-button>
      </div>
      <div class="product-list" v-loading="loading">
        <div v-for="product in products" :key="product.id" class="product-item">
          <img :src="product.imageUrl || 'https://via.placeholder.com/60'" class="product-img" />
          <div class="product-info">
            <div class="product-name">{{ product.name }}</div>
            <div class="product-price">¥{{ product.price }}</div>
          </div>
          <div class="product-actions">
            <el-button type="primary" size="small" link @click="openProductDialog(product)">编辑</el-button>
            <el-button type="danger" size="small" link @click="deleteProduct(product.id)">删除</el-button>
          </div>
        </div>
        <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-section" v-if="currentTab === 'orders'">
      <div class="section-header">
        <span class="section-title">订单列表</span>
      </div>
      <div class="status-tabs">
        <div
          class="status-tab"
          :class="{ active: orderStatus === null }"
          @click="changeOrderStatus(null)"
        >
          全部
        </div>
        <div
          class="status-tab"
          :class="{ active: orderStatus === 0 }"
          @click="changeOrderStatus(0)"
        >
          待处理
        </div>
        <div
          class="status-tab"
          :class="{ active: orderStatus === 1 }"
          @click="changeOrderStatus(1)"
        >
          已处理
        </div>
      </div>
      <div class="order-list" v-loading="orderLoading">
        <div v-for="order in orders" :key="order.id" class="order-item">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.id }}</span>
            <span class="order-status" :class="order.status === 0 ? 'pending' : 'processed'">
              {{ order.status === 0 ? '待处理' : '已处理' }}
            </span>
          </div>
          <div class="order-product">
            <img :src="order.productImage || 'https://via.placeholder.com/50'" class="order-product-img" />
            <div class="order-product-info">
              <div class="order-product-name">{{ order.productName }}</div>
              <div class="order-product-row">
                <span class="order-product-price">¥{{ order.price }}</span>
                <span class="order-product-quantity">x{{ order.quantity || 1 }}</span>
              </div>
            </div>
          </div>
          <div class="order-actions">
            <el-button
              v-if="order.status === 0"
              type="success"
              size="small"
              link
              @click="handleOrder(order.id)"
            >
              处理订单
            </el-button>
          </div>
        </div>
        <el-empty v-if="!orderLoading && orders.length === 0" description="暂无订单" />
      </div>
    </div>

    <!-- 底部占位 -->
    <div class="bottom-space"></div>

    <!-- 商品编辑弹窗 -->
    <el-dialog
      v-model="productDialogVisible"
      :title="editingProduct ? '编辑商品' : '新增商品'"
      width="90%"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="productForm" :rules="productRules" label-width="70px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="productForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品图片" prop="imageUrl">
          <el-input v-model="productForm.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitProduct">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 底部导航栏 -->
    <div class="mobile-tabbar">
      <div class="tab-item" @click="router.push('/m')">
        <div class="tab-icon-wrap">
          <span class="tab-icon">🏪</span>
        </div>
        <span>商城</span>
      </div>
      <div class="tab-item" @click="router.push('/m/orders')">
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
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import api from '../api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const products = ref([])
const categories = ref([])
const productDialogVisible = ref(false)
const editingProduct = ref(null)
const submitting = ref(false)
const formRef = ref()

// 订单管理
const currentTab = ref('products')
const orders = ref([])
const orderLoading = ref(false)
const orderStatus = ref(null)

const productForm = reactive({
  name: '',
  categoryId: null,
  price: 0,
  imageUrl: '',
  description: ''
})

const productRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await api.get('/products', { params: { page: 1, size: 50 } })
    products.value = res.data.records
  } catch (error) {
    ElMessage.error('获取商品失败')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await api.get('/categories')
    categories.value = res.data
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchOrders = async () => {
  orderLoading.value = true
  try {
    const res = await api.get('/orders', {
      params: {
        page: 1,
        size: 50,
        status: orderStatus.value
      }
    })
    orders.value = res.data.records
  } catch (error) {
    ElMessage.error('获取订单失败')
  } finally {
    orderLoading.value = false
  }
}

const changeOrderStatus = (status) => {
  orderStatus.value = status
  fetchOrders()
}

const handleOrder = async (orderId) => {
  try {
    await api.put(`/orders/${orderId}/handle`)
    ElMessage.success('处理成功')
    fetchOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '处理失败')
  }
}

const openProductDialog = (product = null) => {
  editingProduct.value = product
  if (product) {
    productForm.name = product.name
    productForm.categoryId = product.categoryId
    productForm.price = product.price
    productForm.imageUrl = product.imageUrl || ''
    productForm.description = product.description || ''
  } else {
    productForm.name = ''
    productForm.categoryId = null
    productForm.price = 0
    productForm.imageUrl = ''
    productForm.description = ''
  }
  productDialogVisible.value = true
}

const submitProduct = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingProduct.value) {
      await api.put(`/products/${editingProduct.value.id}`, productForm)
      ElMessage.success('更新成功')
    } else {
      await api.post('/products', productForm)
      ElMessage.success('添加成功')
    }
    productDialogVisible.value = false
    fetchProducts()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const deleteProduct = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', { type: 'warning' })
    await api.delete(`/products/${id}`)
    ElMessage.success('删除成功')
    fetchProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
  fetchOrders()
})

watch(currentTab, (newTab) => {
  if (newTab === 'orders') {
    fetchOrders()
  }
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
  font-size: 24px;
  cursor: pointer;
}

.header-title {
  color: white;
  font-size: 18px;
  font-weight: bold;
}

/* 功能菜单 */
.menu-section {
  margin: 12px;
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 20px;
  margin-right: 12px;
}

.menu-text {
  flex: 1;
  font-size: 15px;
  color: #333;
}

.menu-arrow {
  color: #ccc;
  font-size: 18px;
}

/* 商品列表 */
.product-section {
  margin: 12px;
  background: white;
  border-radius: 12px;
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: bold;
  color: #333;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #fafafa;
  border-radius: 8px;
}

.product-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.product-price {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: bold;
}

.product-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 订单列表 */
.order-section {
  margin: 12px;
  background: white;
  border-radius: 12px;
  padding: 16px;
}

.status-tabs {
  display: flex;
  gap: 8px;
  margin: 12px 0;
}

.status-tab {
  flex: 1;
  text-align: center;
  padding: 8px;
  border-radius: 16px;
  font-size: 13px;
  color: #666;
  background: #f5f5f5;
  cursor: pointer;
}

.status-tab.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-item {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.order-no {
  font-size: 12px;
  color: #999;
}

.order-status {
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 8px;
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
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.order-product-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
}

.order-product-info {
  flex: 1;
}

.order-product-name {
  font-size: 13px;
  color: #333;
  margin-bottom: 4px;
}

.order-product-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-product-price {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: bold;
}

.order-product-quantity {
  font-size: 13px;
  color: #666;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
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
