<template>
  <div class="mobile-page">
    <!-- 顶部区域 -->
    <div class="mobile-header">
      <div class="header-top">
        <div class="user-greeting">
          <span class="avatar">👤</span>
          <div class="greeting-text">
            <span class="hello">你好，</span>
            <span class="username">{{ userStore.user?.nickname || userStore.user?.username || '小主' }}</span>
          </div>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <span class="search-icon">🔍</span>
        <input
          type="text"
          placeholder="搜索心仪的商品..."
          v-model="searchKeyword"
          @keyup.enter="doSearch"
          @input="handleSearch"
        />
        <span class="search-btn" @click="doSearch">搜索</span>
      </div>
    </div>

    <!-- 分类导航 -->
    <div class="category-nav">
      <div class="category-scroll">
        <div
          class="category-item"
          :class="{ active: categoryId === null }"
          @click="selectCategory(null)"
        >
          <span class="cat-icon">🏠</span>
          <span>全部</span>
        </div>
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="category-item"
          :class="{ active: categoryId === cat.id }"
          @click="selectCategory(cat.id)"
        >
          <span class="cat-icon">{{ getCategoryIcon(cat.name) }}</span>
          <span>{{ cat.name }}</span>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-list" v-loading="loading">
      <div class="product-grid">
        <div
          v-for="product in products"
          :key="product.id"
          class="product-item"
          @click="openOrderDialog(product)"
        >
          <div class="product-image">
            <img :src="product.imageUrl || 'https://via.placeholder.com/200'" :alt="product.name" />
            <div class="product-tag" v-if="product.categoryName">{{ product.categoryName }}</div>
          </div>
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <div class="price-row">
              <span class="price">
                <span class="yuan">¥</span>{{ product.price }}
              </span>
              <span class="buy-btn">立即购买</span>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && products.length === 0" description="暂无商品，看看其他吧~" />
    </div>

    <!-- 底部导航栏 -->
    <div class="mobile-tabbar">
      <div class="tab-item active" @click="router.push('/m')">
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

    <!-- 下单抽屉 -->
    <el-drawer
      v-model="orderDrawerVisible"
      title="确认订单"
      direction="btt"
      size="85%"
      :show-close="true"
      :z-index="2000"
    >
      <div class="order-drawer" v-if="selectedProduct">
        <!-- 商品信息 -->
        <div class="order-product">
          <img :src="selectedProduct.imageUrl || 'https://via.placeholder.com/100'" class="product-img" />
          <div class="product-detail">
            <h3>{{ selectedProduct.name }}</h3>
            <p class="category-tag">{{ selectedProduct.categoryName }}</p>
            <p class="price">¥{{ selectedProduct.price }}</p>
          </div>
        </div>

        <!-- 规格选择 -->
        <div class="spec-section" v-if="selectedProduct.specs?.length">
          <div class="section-title">选择规格</div>
          <div v-for="spec in selectedProduct.specs" :key="spec.id" class="spec-group">
            <span class="spec-name">{{ spec.specName }}</span>
            <div class="spec-values">
              <span
                v-for="value in spec.specValues"
                :key="value"
                class="spec-value"
                :class="{ active: orderForm.specs[spec.specName] === value }"
                @click="orderForm.specs[spec.specName] = value"
              >
                {{ value }}
              </span>
            </div>
          </div>
        </div>

        <!-- 收货地址 -->
        <div class="address-section">
          <div class="section-title">
            <span>配送地址</span>
            <span class="add-btn" @click="router.push('/m/addresses')">+ 新建</span>
          </div>
          <div v-if="addresses.length > 0" class="address-list">
            <div
              v-for="addr in addresses"
              :key="addr.id"
              class="address-item"
              :class="{ active: orderForm.addressId === addr.id }"
              @click="orderForm.addressId = addr.id"
            >
              <div class="address-icon">📍</div>
              <div class="address-content">
                <div class="address-header">
                  <span class="name">{{ addr.name }}</span>
                  <span class="phone">{{ addr.phone }}</span>
                  <span class="tag" v-if="addr.isDefault === 1">默认</span>
                </div>
                <div class="address-text">{{ addr.address }}</div>
              </div>
              <div class="check-icon" v-if="orderForm.addressId === addr.id">✓</div>
            </div>
          </div>
          <div v-else class="no-address">
            <span>暂无收货地址，</span>
            <span class="link" @click="router.push('/m/addresses')">点击添加</span>
          </div>
        </div>

        <!-- 数量选择 -->
        <div class="quantity-section">
          <div class="section-title">购买数量</div>
          <div class="quantity-selector">
            <span class="qty-btn" @click="orderForm.quantity > 1 && orderForm.quantity--">-</span>
            <span class="qty-value">{{ orderForm.quantity }}</span>
            <span class="qty-btn" @click="orderForm.quantity < 99 && orderForm.quantity++">+</span>
          </div>
        </div>

        <!-- 备注 -->
        <div class="remark-section">
          <div class="section-title">订单备注</div>
          <textarea v-model="orderForm.remark" placeholder="选填，请输入您的特殊要求..."></textarea>
        </div>

        <!-- 提交按钮 -->
        <div class="submit-bar">
          <div class="total-price">
            <span class="label">实付：</span>
            <span class="price">¥{{ (selectedProduct.price * orderForm.quantity).toFixed(2) }}</span>
          </div>
          <el-button type="primary" size="large" :loading="submitting" @click="submitOrder" class="submit-btn">
            提交订单
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import api from '../api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const products = ref([])
const categories = ref([])
const categoryId = ref(null)
const searchKeyword = ref('')

const orderDrawerVisible = ref(false)
const selectedProduct = ref(null)
const submitting = ref(false)
const addresses = ref([])

const orderForm = reactive({
  specs: {},
  addressId: null,
  remark: '',
  quantity: 1
})

const categoryIcons = {
  '情侣': '💕',
  '礼物': '🎁',
  '首饰': '💍',
  '鲜花': '💐',
  '玩具': '🧸',
  '服装': '👔',
  '美食': '🍫',
  '其他': '📦'
}

const getCategoryIcon = (name) => {
  for (const key in categoryIcons) {
    if (name.includes(key)) return categoryIcons[key]
  }
  return '🎁'
}

let searchTimer = null

const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    fetchProducts()
  }, 300)
}

const doSearch = () => {
  clearTimeout(searchTimer)
  fetchProducts()
}

const selectCategory = (id) => {
  categoryId.value = id
  fetchProducts()
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await api.get('/products', {
      params: {
        page: 1,
        size: 50,
        categoryId: categoryId.value,
        keyword: searchKeyword.value
      }
    })
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

const fetchAddresses = async () => {
  try {
    const res = await api.get('/addresses')
    addresses.value = res.data
    if (addresses.value.length > 0) {
      const defaultAddr = addresses.value.find(a => a.isDefault === 1)
      orderForm.addressId = defaultAddr?.id || addresses.value[0].id
    }
  } catch (error) {
    console.error('获取地址失败:', error)
  }
}

const openOrderDialog = async (product) => {
  if (addresses.value.length === 0) {
    ElMessage.warning('请先添加收货地址')
    router.push('/m/addresses')
    return
  }

  try {
    const res = await api.get(`/products/${product.id}`)
    selectedProduct.value = res.data
    orderForm.specs = {}
    orderForm.remark = ''

    selectedProduct.value.specs?.forEach(spec => {
      if (spec.specValues && spec.specValues.length > 0) {
        orderForm.specs[spec.specName] = spec.specValues[0]
      }
    })

    orderForm.quantity = 1

    orderDrawerVisible.value = true
  } catch (error) {
    ElMessage.error('获取商品详情失败')
  }
}

const submitOrder = async () => {
  if (!orderForm.addressId) {
    ElMessage.warning('请选择收货地址')
    return
  }

  submitting.value = true
  try {
    await api.post('/orders', {
      productId: selectedProduct.value.id,
      specs: orderForm.specs,
      addressId: orderForm.addressId,
      remark: orderForm.remark,
      price: selectedProduct.value.price,
      quantity: orderForm.quantity
    })
    ElMessage.success('订单提交成功！')
    orderDrawerVisible.value = false
    router.push('/m/orders')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '提交订单失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
  fetchAddresses()
})
</script>

<style scoped>
.mobile-page {
  min-height: 100vh;
  background: #f8f9fa;
  padding-bottom: 70px;
}

/* 顶部区域 */
.mobile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 16px 20px;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.user-greeting {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  background: rgba(255,255,255,0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.greeting-text {
  display: flex;
  flex-direction: column;
}

.hello {
  font-size: 12px;
  opacity: 0.8;
  color: white;
}

.username {
  font-size: 18px;
  font-weight: bold;
  color: white;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 24px;
  padding: 12px 18px;
  gap: 10px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.search-icon {
  font-size: 16px;
  opacity: 0.5;
}

.search-bar input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  outline: none;
  color: #333;
}

.search-bar input::placeholder {
  color: #999;
}

.search-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 13px;
  white-space: nowrap;
  cursor: pointer;
}

/* 分类导航 */
.category-nav {
  background: white;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #f0f0f0;
}

.category-scroll {
  display: flex;
  overflow-x: auto;
  padding: 12px;
  gap: 8px;
  scrollbar-width: none;
}

.category-scroll::-webkit-scrollbar {
  display: none;
}

.category-item {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
  background: #f8f9fa;
}

.category-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.cat-icon {
  font-size: 20px;
}

.category-item span:last-child {
  font-size: 12px;
}

/* 商品列表 */
.product-list {
  padding: 12px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.product-item {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: all 0.2s;
}

.product-item:active {
  transform: scale(0.98);
}

.product-image {
  position: relative;
  width: 100%;
  height: 150px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(102, 126, 234, 0.9);
  color: white;
  font-size: 10px;
  padding: 4px 8px;
  border-radius: 10px;
}

.product-info {
  padding: 12px;
}

.product-info h3 {
  margin: 0 0 10px;
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-row .price {
  font-size: 18px;
  font-weight: bold;
  color: #ff4d4f;
}

.price-row .yuan {
  font-size: 12px;
}

.buy-btn {
  font-size: 11px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 6px 12px;
  border-radius: 12px;
  white-space: nowrap;
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

/* 下单抽屉 */
:deep(.el-drawer) {
  border-radius: 20px 20px 0 0;
}

:deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 16px;
  font-weight: bold;
}

.order-drawer {
  padding: 0 0 100px;
}

.order-product {
  display: flex;
  gap: 14px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  margin: 0 20px 20px;
  border-radius: 16px;
}

.product-img {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: 12px;
}

.product-detail h3 {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.category-tag {
  display: inline-block;
  font-size: 11px;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 3px 10px;
  border-radius: 10px;
  margin: 0 0 8px;
}

.product-detail .price {
  font-size: 22px;
  font-weight: bold;
  color: #ff4d4f;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  padding: 0 20px;
  margin-bottom: 14px;
}

.add-btn {
  font-size: 13px;
  color: #667eea;
  font-weight: normal;
}

.spec-section {
  padding-bottom: 16px;
}

.spec-group {
  padding: 0 20px 14px;
}

.spec-name {
  font-size: 13px;
  color: #666;
  display: block;
  margin-bottom: 10px;
}

.spec-values {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.spec-value {
  padding: 8px 20px;
  background: #f5f7fa;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.spec-value.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-color: transparent;
}

.address-section {
  padding: 16px 0;
  background: #fafafa;
}

.address-list {
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: white;
  border-radius: 12px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.address-item.active {
  border-color: #667eea;
  background: #f8f9ff;
}

.address-icon {
  font-size: 20px;
  opacity: 0.6;
}

.address-content {
  flex: 1;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.address-header .name {
  font-weight: 600;
  font-size: 14px;
}

.address-header .phone {
  color: #666;
  font-size: 13px;
}

.address-header .tag {
  background: #667eea;
  color: white;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 8px;
}

.address-text {
  font-size: 12px;
  color: #999;
}

.check-icon {
  width: 20px;
  height: 20px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.no-address {
  padding: 0 20px;
  font-size: 13px;
  color: #999;
}

.no-address .link {
  color: #667eea;
}

.quantity-section {
  padding: 16px 0 20px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  margin: 0 20px;
  gap: 0;
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid #eee;
}

.qty-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  font-size: 20px;
  cursor: pointer;
  user-select: none;
}

.qty-btn:active {
  background: #e8eaf0;
}

.qty-value {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
}

.remark-section {
  padding: 16px 0 20px;
}

.remark-section textarea {
  display: block;
  width: calc(100% - 40px);
  height: 70px;
  margin: 0 20px;
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 12px;
  font-size: 14px;
  resize: none;
  outline: none;
  background: #fafafa;
}

.remark-section textarea:focus {
  border-color: #667eea;
  background: white;
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.08);
}

.total-price .label {
  font-size: 13px;
  color: #666;
}

.total-price .price {
  font-size: 24px;
  font-weight: bold;
  color: #ff4d4f;
}

.submit-btn {
  padding: 14px 40px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 24px;
  font-size: 15px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
}
</style>
