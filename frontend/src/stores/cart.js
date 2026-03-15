import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const CART_KEY = 'shopping_cart'

export const useCartStore = defineStore('cart', () => {
  // 从 LocalStorage 加载购物车数据
  const loadCart = () => {
    const cartStr = localStorage.getItem(CART_KEY)
    if (!cartStr) return []
    try {
      return JSON.parse(cartStr)
    } catch {
      return []
    }
  }

  const items = ref(loadCart())

  // 保存到 LocalStorage
  const saveCart = () => {
    localStorage.setItem(CART_KEY, JSON.stringify(items.value))
  }

  // 购物车商品总数
  const totalCount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  // 购物车总金额
  const totalPrice = computed(() => {
    return items.value.reduce((sum, item) => {
      const price = item.price || 0
      return sum + (price * item.quantity)
    }, 0)
  })

  // 添加商品到购物车
  function addItem(product, specs, quantity = 1) {
    // 检查是否已存在相同商品和规格的项
    const existingIndex = items.value.findIndex(item => {
      if (item.productId !== product.id) return false
      return JSON.stringify(item.specs) === JSON.stringify(specs)
    })

    if (existingIndex >= 0) {
      // 已存在，增加数量
      items.value[existingIndex].quantity += quantity
    } else {
      // 不存在，新增
      items.value.push({
        id: Date.now(), // 临时ID
        productId: product.id,
        productName: product.name,
        productImage: product.imageUrl,
        price: product.price,
        specs: specs || {},
        quantity: quantity,
        addedAt: new Date().toISOString()
      })
    }

    saveCart()
  }

  // 更新商品数量
  function updateQuantity(itemId, quantity) {
    const item = items.value.find(i => i.id === itemId)
    if (item) {
      if (quantity <= 0) {
        // 数量为0或负数时删除
        removeItem(itemId)
      } else {
        item.quantity = quantity
        saveCart()
      }
    }
  }

  // 删除商品
  function removeItem(itemId) {
    const index = items.value.findIndex(i => i.id === itemId)
    if (index >= 0) {
      items.value.splice(index, 1)
      saveCart()
    }
  }

  // 清空购物车
  function clearCart() {
    items.value = []
    saveCart()
  }

  // 获取购物车商品列表
  function getItems() {
    return items.value
  }

  // 获取选中的商品（用于下单）
  function getSelectedItems(selectedIds) {
    return items.value.filter(item => selectedIds.includes(item.id))
  }

  return {
    items,
    totalCount,
    totalPrice,
    addItem,
    updateQuantity,
    removeItem,
    clearCart,
    getItems,
    getSelectedItems
  }
})
