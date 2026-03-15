<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>购物车</span>
        <div>
          <el-button @click="router.push('/products')">继续购物</el-button>
          <el-button type="danger" @click="handleClearCart" :disabled="cartItems.length === 0">
            清空购物车
          </el-button>
        </div>
      </div>
    </template>

    <div v-if="cartItems.length === 0" class="empty-cart">
      <el-empty description="购物车是空的">
        <el-button type="primary" @click="router.push('/products')">去购物</el-button>
      </el-empty>
    </div>

    <div v-else>
      <el-table :data="cartItems" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />

        <el-table-column label="商品" min-width="250">
          <template #default="{ row }">
            <div class="product-cell">
              <img
                :src="row.productImage || 'https://via.placeholder.com/80'"
                class="product-image"
              />
              <div>
                <div class="product-name">{{ row.productName }}</div>
                <div class="product-specs" v-if="row.specs && Object.keys(row.specs).length > 0">
                  <span v-for="(value, key) in row.specs" :key="key" class="spec-tag">
                    {{ key }}: {{ value }}
                  </span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="单价" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>

        <el-table-column label="数量" width="150">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="99"
              size="small"
              @change="handleQuantityChange(row.id, row.quantity)"
            />
          </template>
        </el-table-column>

        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleRemoveItem(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-footer">
        <div class="selected-info">
          已选择 {{ selectedItems.length }} 件商品，总计：
          <span class="total-price">¥{{ totalSelectedPrice.toFixed(2) }}</span>
        </div>
        <div>
          <el-button
            type="primary"
            size="large"
            :disabled="selectedItems.length === 0"
            @click="handleCheckout"
          >
            结算
          </el-button>
        </div>
      </div>
    </div>

    <!-- 结算弹窗 -->
    <el-dialog v-model="checkoutDialogVisible" title="确认订单" width="500px">
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
            :rows="3"
            placeholder="请输入备注（可选）"
          />
        </el-form-item>
        <el-form-item label="商品清单">
          <div class="checkout-items">
            <div v-for="item in selectedItems" :key="item.id" class="checkout-item">
              <span>{{ item.productName }} x {{ item.quantity }}</span>
              <span class="price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="订单总价">
          <span class="total-price">¥{{ totalSelectedPrice.toFixed(2) }}</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="checkoutDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="submitting">
          提交订单
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

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

// 计算选中商品的总价
const totalSelectedPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => {
    return sum + (item.price * item.quantity)
  }, 0)
})

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

// 处理数量变化
const handleQuantityChange = (itemId, quantity) => {
  cartStore.updateQuantity(itemId, quantity)
}

// 处理删除商品
const handleRemoveItem = (itemId) => {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    cartStore.removeItem(itemId)
    loadCart()
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 处理清空购物车
const handleClearCart = () => {
  ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    cartStore.clearCart()
    loadCart()
    ElMessage.success('已清空')
  }).catch(() => {})
}

// 处理结算
const handleCheckout = async () => {
  // 获取用户地址
  try {
    const res = await api.get('/addresses')
    addresses.value = res.data || []
    if (addresses.value.length === 0) {
      ElMessage.warning('请先添加收货地址')
      router.push('/addresses')
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
    const items = selectedItems.value.map(item => ({
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
    selectedItems.value.forEach(item => {
      cartStore.removeItem(item.id)
    })

    checkoutDialogVisible.value = false
    loadCart()
    ElMessage.success('订单提交成功')
    router.push('/orders')
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-cart {
  padding: 50px 0;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.product-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.product-specs {
  font-size: 12px;
  color: #999;
}

.spec-tag {
  display: inline-block;
  margin-right: 8px;
  padding: 2px 6px;
  background: #f5f5f5;
  border-radius: 4px;
}

.price {
  color: #e6a23c;
  font-weight: 500;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.selected-info {
  font-size: 14px;
}

.total-price {
  font-size: 20px;
  color: #e6a23c;
  font-weight: bold;
}

.checkout-items {
  max-height: 200px;
  overflow-y: auto;
}

.checkout-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}
</style>
