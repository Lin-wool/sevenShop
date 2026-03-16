<template>
  <div class="mobile-cart">
    <!-- 顶部返回栏 -->
    <div class="cart-header">
      <span class="back-btn" @click="router.push('/m')">←</span>
      <span>购物车</span>
      <span v-if="cartItems.length > 0" class="clear-btn" @click="handleClearCart">
        <el-button type="danger" size="small" round>清空购物车</el-button>
      </span>
    </div>

    <div v-if="cartItems.length === 0" class="empty-cart">
      <div class="empty-text">购物车是空的</div>
      <el-button type="primary" @click="router.push('/m')">去购物</el-button>
    </div>

    <div v-else class="cart-content">
      <div
        v-for="item in cartItems"
        :key="item.id"
        class="cart-item"
        :class="{ selected: selectedItems.includes(item.id) }"
        @click="toggleSelect(item.id)"
      >
        <div class="item-checkbox">
          <el-checkbox :model-value="selectedItems.includes(item.id)" />
        </div>
        <img
          :src="item.productImage || 'https://via.placeholder.com/80'"
          class="item-image"
        />
        <div class="item-info">
          <div class="item-name">{{ item.productName }}</div>
          <div class="item-specs" v-if="item.specs && Object.keys(item.specs).length > 0">
            <span v-for="(value, key) in item.specs" :key="key">
              {{ key }}: {{ value }}
            </span>
          </div>
          <div class="item-bottom">
            <span class="item-price">¥{{ item.price }}</span>
            <div class="quantity-control">
              <span class="qty-btn" @click.stop="decreaseQty(item)">-</span>
              <input
                type="number"
                class="qty-input"
                v-model="item.quantity"
                @change="handleQuantityChange(item)"
                min="1"
                max="99"
              />
              <span class="qty-btn" @click.stop="increaseQty(item)">+</span>
            </div>
          </div>
        </div>
        <div class="item-delete" @click.stop="handleRemoveItem(item.id)">
          <el-button type="danger" size="small" plain round>删除</el-button>
        </div>
      </div>
    </div>

    <div v-if="cartItems.length > 0" class="cart-footer">
      <div class="footer-left" @click="toggleSelectAll">
        <el-checkbox :model-value="isAllSelected" />
        <span>全选</span>
      </div>
      <div class="footer-center">
        合计: <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
      </div>
      <div
        class="footer-right"
        :class="{ disabled: selectedItems.length === 0 }"
        @click="handleCheckout"
      >
        结算{{ selectedItems.length > 0 ? `(${selectedItems.length})` : '' }}
      </div>
    </div>

    <!-- 结算弹窗 -->
    <el-dialog v-model="checkoutDialogVisible" title="确认订单" width="90%">
      <el-form :model="checkoutForm" label-width="80px">
        <el-form-item label="收货地址" required>
          <el-select v-model="checkoutForm.addressId" placeholder="请选择收货地址" style="width: 100%">
            <el-option
              v-for="addr in addresses"
              :key="addr.id"
              :label="`${addr.name} - ${addr.phone} - ${addr.address}`"
              :value="addr.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="checkoutForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注（可选）"
          />
        </el-form-item>
        <el-form-item label="商品清单">
          <div class="checkout-items">
            <div v-for="item in selectedCartItems" :key="item.id" class="checkout-item">
              <span>{{ item.productName }} x {{ item.quantity }}</span>
              <span class="price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="订单总价">
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="checkoutDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="submitting">
          提交订单
        </el-button>
      </template>
    </el-dialog>

    <!-- 底部导航栏 -->
    <MobileTabbar />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import MobileTabbar from '../components/MobileTabbar.vue'

const router = useRouter()
const cartStore = useCartStore()

const cartItems = ref([])
const selectedItems = ref([])
const checkoutDialogVisible = ref(false)
const submitting = ref(false)
const addresses = ref([])

const checkoutForm = ref({
  addressId: null,
  remark: ''
})

// 加载购物车数据
const loadCart = () => {
  cartItems.value = cartStore.getItems()
}

// 是否全选
const isAllSelected = computed(() => {
  return cartItems.value.length > 0 && selectedItems.value.length === cartItems.value.length
})

// 选中的商品列表
const selectedCartItems = computed(() => {
  return cartItems.value.filter(item => selectedItems.value.includes(item.id))
})

// 计算总价
const totalPrice = computed(() => {
  return selectedCartItems.value.reduce((sum, item) => {
    return sum + (item.price * item.quantity)
  }, 0)
})

// 切换选择
const toggleSelect = (itemId) => {
  const index = selectedItems.value.indexOf(itemId)
  if (index >= 0) {
    selectedItems.value.splice(index, 1)
  } else {
    selectedItems.value.push(itemId)
  }
}

// 全选/取消全选
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedItems.value = []
  } else {
    selectedItems.value = cartItems.value.map(item => item.id)
  }
}

// 增加数量
const increaseQty = (item) => {
  cartStore.updateQuantity(item.id, item.quantity + 1)
  loadCart()
}

// 直接输入数量
const handleQuantityChange = (item) => {
  let value = parseInt(item.quantity)
  if (isNaN(value) || value < 1) {
    value = 1
    item.quantity = 1
  } else if (value > 99) {
    value = 99
    item.quantity = 99
  }
  cartStore.updateQuantity(item.id, value)
  loadCart()
}

// 验证数量
// 减少数量
const decreaseQty = (item) => {
  if (item.quantity > 1) {
    cartStore.updateQuantity(item.id, item.quantity - 1)
    loadCart()
  }
}

// 删除商品
const handleRemoveItem = (itemId) => {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    cartStore.removeItem(itemId)
    loadCart()
    selectedItems.value = selectedItems.value.filter(id => id !== itemId)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 清空购物车
const handleClearCart = () => {
  ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    cartStore.clearCart()
    loadCart()
    selectedItems.value = []
    ElMessage.success('已清空')
  }).catch(() => {})
}

// 结算
const handleCheckout = async () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }

  try {
    const res = await api.get('/addresses')
    addresses.value = res || []
    if (addresses.value.length === 0) {
      ElMessage.warning('请先添加收货地址')
      router.push('/m/addresses')
      return
    }
    checkoutForm.value.addressId = addresses.value.find(a => a.isDefault)?.id || addresses.value[0].id
    checkoutDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取地址失败')
  }
}

// 提交订单
const submitOrder = async () => {
  if (!checkoutForm.value.addressId) {
    ElMessage.warning('请选择收货地址')
    return
  }

  submitting.value = true
  try {
    const items = selectedCartItems.value.map(item => ({
      productId: item.productId,
      specs: item.specs,
      price: item.price,
      quantity: item.quantity
    }))

    await api.post('/orders/batch', {
      items,
      addressId: checkoutForm.value.addressId,
      remark: checkoutForm.value.remark
    })

    // 移除已下单的商品
    selectedItems.value.forEach(id => {
      cartStore.removeItem(id)
    })

    checkoutDialogVisible.value = false
    loadCart()
    selectedItems.value = []
    ElMessage.success('订单提交成功')
    router.push('/m/orders')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '下单失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.mobile-cart {
  min-height: 100vh;
  background: var(--color-bg);
  padding-bottom: 70px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fff;
  font-size: 17px;
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.back-btn {
  font-size: 22px;
  cursor: pointer;
  padding: 6px 12px;
  margin-left: -12px;
  color: var(--color-text);
}

.clear-btn {
  color: var(--color-danger);
  font-size: 14px;
}

.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
}

.empty-text {
  color: var(--color-text-secondary);
  margin-bottom: 24px;
  font-size: 15px;
}

.cart-content {
  padding: 12px;
}

.cart-item {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 14px;
  margin-bottom: 12px;
  box-shadow: var(--shadow-card);
  transition: all 0.2s ease;
}

.cart-item.selected {
  background: #f0f9ff;
  border: 1px solid var(--color-primary-light);
}

.item-checkbox {
  margin-right: 12px;
}

.item-image {
  width: 88px;
  height: 88px;
  object-fit: cover;
  border-radius: var(--radius-md);
  margin-right: 12px;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-specs {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 10px;
  background: var(--color-bg);
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  display: inline-block;
}

.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price {
  color: var(--color-danger);
  font-weight: 700;
  font-size: 17px;
}

.item-price::before {
  content: '¥';
  font-size: 13px;
  font-weight: 500;
}

.quantity-control {
  display: flex;
  align-items: center;
  background: var(--color-bg);
  border-radius: var(--radius-md);
  padding: 2px;
}

.qty-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: var(--radius-sm);
  font-size: 18px;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.2s ease;
}

.qty-btn:active {
  background: var(--color-primary);
  color: #fff;
}

.qty-input {
  width: 40px;
  height: 28px;
  text-align: center;
  border: none;
  background: transparent;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 500;
  margin: 0 4px;
}

.qty-input:focus {
  outline: none;
  background: #fff;
}

.item-delete {
  padding: 8px;
}

.delete-icon {
  font-size: 20px;
  color: #999;
}

.cart-footer {
  position: fixed;
  bottom: 50px;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  height: 60px;
  background: #fff;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.footer-left {
  display: flex;
  align-items: center;
  padding: 0 16px;
}

.footer-left span {
  margin-left: 8px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.footer-center {
  flex: 1;
  text-align: center;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.total-price {
  color: var(--color-danger);
  font-weight: 700;
  font-size: 20px;
}

.footer-right {
  width: 110px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.footer-right.disabled {
  background: #cbd5e1;
}

.checkout-items {
  max-height: 180px;
  overflow-y: auto;
}

.checkout-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.checkout-item .price {
  color: var(--color-danger);
  font-weight: 600;
}
</style>
