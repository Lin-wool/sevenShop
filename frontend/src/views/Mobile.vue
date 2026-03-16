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
              <div class="action-btns">
                <span class="cart-btn-small" @click="addToCartFromList(product, $event)">🛒</span>
                <span class="buy-btn" @click.stop="openOrderDialog(product)">购买</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && products.length === 0" description="暂无商品，看看其他吧~" />
    </div>

    <!-- 底部导航栏 -->
    <MobileTabbar />

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
            <input
              type="number"
              class="qty-input"
              v-model="orderForm.quantity"
              @change="validateOrderQuantity"
              min="1"
              max="99"
            />
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
          <div class="button-group">
            <el-button size="large" @click="addToCart" class="cart-btn">
              加入购物车
            </el-button>
            <el-button type="primary" size="large" :loading="submitting" @click="submitOrder" class="submit-btn">
              提交订单
            </el-button>
          </div>
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
import { useCartStore } from '../stores/cart'
import api from '../api'
import MobileTabbar from '../components/MobileTabbar.vue'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

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

// 验证订单数量
const validateOrderQuantity = () => {
  let value = parseInt(orderForm.quantity)
  if (isNaN(value) || value < 1) {
    orderForm.quantity = 1
  } else if (value > 99) {
    orderForm.quantity = 99
  }
}

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
    products.value = res.records
  } catch (error) {
    ElMessage.error('获取商品失败')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await api.get('/categories')
    categories.value = res
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchAddresses = async () => {
  try {
    const res = await api.get('/addresses')
    addresses.value = res
    if (addresses.value.length > 0) {
      const defaultAddr = addresses.value.find(a => a.isDefault === 1)
      orderForm.addressId = defaultAddr?.id || addresses.value[0].id
    }
  } catch (error) {
    console.error('获取地址失败:', error)
  }
}

const openOrderDialog = async (product) => {
  // 确保地址已加载
  if (addresses.value.length === 0) {
    await fetchAddresses()
    if (addresses.value.length === 0) {
      ElMessage.warning('请先添加收货地址')
      router.push('/m/addresses')
      return
    }
  }

  try {
    const res = await api.get(`/products/${product.id}`)
    selectedProduct.value = res
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

// 加入购物车
const addToCart = () => {
  if (!selectedProduct.value) return
  cartStore.addItem(selectedProduct.value, orderForm.specs, orderForm.quantity || 1)
  orderDrawerVisible.value = false
  ElMessage.success('已加入购物车')
}

// 加入购物车（从商品列表）
const addToCartFromList = (product, event) => {
  event.stopPropagation()
  cartStore.addItem(product, {}, 1)
  ElMessage.success('已加入购物车')
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
  background: var(--color-bg);
  padding-bottom: 70px;
}

/* 顶部区域 */
.mobile-header {
  background: linear-gradient(135deg, var(--color-primary) 0%, #6366F1 100%);
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
  opacity: 0.9;
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
  color: var(--color-text);
}

.search-bar input::placeholder {
  color: var(--color-text-secondary);
}

.search-btn {
  background: linear-gradient(135deg, var(--color-primary), #6366F1);
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
  border-bottom: 1px solid var(--color-border-light);
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
  background: var(--color-bg);
}

.category-item.active {
  background: linear-gradient(135deg, var(--color-primary), #6366F1);
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
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-card);
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
  transition: transform 0.4s ease;
}

.product-item:hover .product-image img {
  transform: scale(1.05);
}

.product-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: var(--color-primary);
  color: white;
  font-size: 10px;
  padding: 4px 10px;
  border-radius: 12px;
}

.product-info {
  padding: 12px;
}

.product-info h3 {
  margin: 0 0 10px;
  font-size: 14px;
  color: var(--color-text);
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
  color: var(--color-danger);
}

.price-row .yuan {
  font-size: 12px;
}

.buy-btn {
  font-size: 11px;
  background: linear-gradient(135deg, var(--color-primary), #6366F1);
  color: white;
  padding: 6px 14px;
  border-radius: 14px;
  white-space: nowrap;
}

.action-btns {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cart-btn-small {
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
}

/* 下单抽屉 */
:deep(.el-drawer) {
  border-radius: 20px 20px 0 0;
}

:deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border-light);
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
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  margin: 0 20px 20px;
  border-radius: var(--radius-lg);
}

.product-img {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: var(--radius-md);
}

.product-detail h3 {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.category-tag {
  display: inline-block;
  font-size: 11px;
  color: var(--color-primary);
  background: rgba(59, 130, 246, 0.1);
  padding: 3px 10px;
  border-radius: 12px;
  margin: 0 0 8px;
}

.product-detail .price {
  font-size: 22px;
  font-weight: bold;
  color: var(--color-danger);
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  padding: 0 20px;
  margin-bottom: 14px;
}

.add-btn {
  font-size: 13px;
  color: var(--color-primary);
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
  color: var(--color-text-secondary);
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
  background: var(--color-bg);
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.spec-value.active {
  background: linear-gradient(135deg, var(--color-primary), #6366F1);
  color: white;
  border-color: transparent;
}

.address-section {
  padding: 16px 0;
  background: var(--color-bg);
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
  border-radius: var(--radius-md);
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.address-item.active {
  border-color: var(--color-primary);
  background: #f0f9ff;
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
  color: var(--color-text-secondary);
  font-size: 13px;
}

.address-header .tag {
  background: var(--color-primary);
  color: white;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 8px;
}

.address-text {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.check-icon {
  width: 20px;
  height: 20px;
  background: var(--color-primary);
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
  color: var(--color-text-secondary);
}

.no-address .link {
  color: var(--color-primary);
}

.quantity-section {
  padding: 16px 0 20px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  margin: 0 20px;
  gap: 0;
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border);
}

.qty-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  font-size: 20px;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
}

.qty-btn:active {
  background: var(--color-primary);
  color: white;
}

.qty-value {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
}

.qty-input {
  flex: 1;
  width: 40px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  border: none;
  background: transparent;
}

.qty-input:focus {
  outline: none;
}

.remark-section {
  padding: 16px 0 20px;
}

.remark-section textarea {
  display: block;
  width: calc(100% - 40px);
  height: 70px;
  margin: 0 20px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 12px;
  font-size: 14px;
  resize: none;
  outline: none;
  background: var(--color-bg);
}

.remark-section textarea:focus {
  border-color: var(--color-primary);
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
  color: var(--color-text-secondary);
}

.total-price .price {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-danger);
}

.submit-btn {
  padding: 14px 30px;
  background: linear-gradient(135deg, var(--color-primary), #6366F1);
  border: none;
  border-radius: var(--radius-xl);
  font-size: 15px;
}

.button-group {
  display: flex;
  gap: 10px;
}

.cart-btn {
  padding: 14px 20px;
  border-radius: var(--radius-xl);
  font-size: 15px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--color-primary), #6366F1);
  border: none;
}
</style>
