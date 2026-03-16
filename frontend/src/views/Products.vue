<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>商品列表</span>
      </div>
    </template>

    <div class="category-filter">
      <el-radio-group v-model="categoryId" @change="fetchProducts">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button
          v-for="cat in categories"
          :key="cat.id"
          :value="cat.id"
        >
          {{ cat.name }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col
        v-for="product in products"
        :key="product.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
      >
        <el-card class="product-card" shadow="hover">
          <div class="product-image" @click="openOrderDialog(product)">
            <img
              :src="product.imageUrl || 'https://via.placeholder.com/200'"
              :alt="product.name"
            />
          </div>
          <div class="product-info">
            <h3>{{ product.name }}</h3>
            <p class="category">{{ product.categoryName }}</p>
            <p class="price">¥{{ product.price }}</p>
            <div class="product-actions">
              <el-button size="small" @click="addToCart(product)">加入购物车</el-button>
              <el-button size="small" type="primary" @click="openOrderDialog(product)">立即下单</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
  </el-card>

  <!-- 下单对话框 -->
  <el-dialog
    v-model="orderDialogVisible"
    title="提交订单"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form v-if="selectedProduct" :model="orderForm" label-width="80px">
      <el-form-item label="商品">
        <div class="order-product-info">
          <img :src="selectedProduct.imageUrl || 'https://via.placeholder.com/60'" class="product-thumb" />
          <div>
            <div>{{ selectedProduct.name }}</div>
            <div class="price">¥{{ selectedProduct.price }}</div>
          </div>
        </div>
      </el-form-item>

      <el-form-item
        v-for="spec in selectedProduct.specs"
        :key="spec.id"
        :label="spec.specName"
      >
        <el-radio-group v-model="orderForm.specs[spec.specName]">
          <el-radio
            v-for="value in spec.specValues"
            :key="value"
            :value="value"
          >
            {{ value }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="收货地址">
        <el-select v-model="orderForm.addressId" placeholder="请选择收货地址" style="width: 100%">
          <el-option
            v-for="addr in addresses"
            :key="addr.id"
            :label="`${addr.name} - ${addr.phone} - ${addr.address}`"
            :value="addr.id"
          />
        </el-select>
        <el-link type="primary" @click="router.push('/addresses')" style="margin-top: 8px">
          管理收货地址
        </el-link>
      </el-form-item>

      <el-form-item label="数量">
        <el-input-number v-model="orderForm.quantity" :min="1" :max="99" />
      </el-form-item>

      <el-form-item label="备注">
        <el-input
          v-model="orderForm.remark"
          type="textarea"
          :rows="2"
          placeholder="有什么想说的..."
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="orderDialogVisible = false">取消</el-button>
      <el-button @click="addToCartFromDialog">加入购物车</el-button>
      <el-button type="primary" :loading="submitting" @click="submitOrder">
        提交订单
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const cartStore = useCartStore()

const loading = ref(false)
const products = ref([])
const categories = ref([])
const categoryId = ref(null)

const orderDialogVisible = ref(false)
const selectedProduct = ref(null)
const submitting = ref(false)
const addresses = ref([])

const orderForm = reactive({
  specs: {},
  addressId: null,
  remark: '',
  quantity: 1
})

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await api.get('/products', {
      params: { page: 1, size: 50, categoryId: categoryId.value }
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
      router.push('/addresses')
      return
    }
  }

  try {
    const res = await api.get(`/products/${product.id}`)
    selectedProduct.value = res
    orderForm.specs = {}
    orderForm.remark = ''

    // 设置默认规格
    selectedProduct.value.specs?.forEach(spec => {
      if (spec.specValues && spec.specValues.length > 0) {
        orderForm.specs[spec.specName] = spec.specValues[0]
      }
    })

    orderForm.quantity = 1

    orderDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取商品详情失败')
  }
}

// 加入购物车
const addToCart = async (product) => {
  try {
    const res = await api.get(`/products/${product.id}`)
    const fullProduct = res

    // 构建规格
    const specs = {}
    fullProduct.specs?.forEach(spec => {
      if (spec.specValues && spec.specValues.length > 0) {
        specs[spec.specName] = spec.specValues[0]
      }
    })

    cartStore.addItem(fullProduct, specs, 1)
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error('加入购物车失败')
  }
}

// 加入购物车（从对话框）
const addToCartFromDialog = () => {
  if (!selectedProduct.value) return
  cartStore.addItem(selectedProduct.value, orderForm.specs, orderForm.quantity || 1)
  orderDialogVisible.value = false
  ElMessage.success('已加入购物车')
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
    ElMessage.success('订单提交成功！管理员会收到通知~')
    orderDialogVisible.value = false
    router.push('/orders')
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}

.category-filter {
  margin-bottom: 24px;
}

.category-filter :deep(.el-radio-button__inner) {
  border-radius: var(--radius-lg) !important;
  padding: 8px 16px;
  border: 1px solid var(--color-border);
}

.category-filter :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.product-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border-light);
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
  border-color: var(--color-primary-light);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  position: relative;
}

.product-image::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  background: linear-gradient(to top, rgba(0,0,0,0.02), transparent);
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-info {
  padding: 16px;
}

.product-info h3 {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-info .category {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin: 0 0 10px;
  display: inline-block;
  background: var(--color-bg);
  padding: 2px 10px;
  border-radius: var(--radius-sm);
}

.product-info .price {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-danger);
  margin: 0;
}

.product-info .price::before {
  content: '¥';
  font-size: 14px;
  font-weight: 500;
}

.product-actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.product-actions .el-button {
  flex: 1;
  border-radius: var(--radius-md);
  font-weight: 500;
}

.product-actions .el-button--primary {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.order-product-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: var(--color-bg);
  border-radius: var(--radius-md);
}

.product-thumb {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: var(--radius-md);
}

.order-product-info .price {
  color: var(--color-danger);
  font-weight: 700;
  font-size: 16px;
}

@media (max-width: 768px) {
  .category-filter {
    overflow-x: auto;
    padding-bottom: 8px;
  }

  .category-filter :deep(.el-radio-group) {
    flex-wrap: nowrap;
  }

  .product-image {
    height: 160px;
  }

  .product-info {
    padding: 12px;
  }

  .product-actions {
    flex-direction: column;
  }
}
</style>
