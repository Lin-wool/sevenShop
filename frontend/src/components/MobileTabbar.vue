<template>
  <div class="mobile-tabbar">
    <div
      v-for="tab in tabs"
      :key="tab.path"
      class="tab-item"
      :class="{ active: currentPath === tab.path }"
      @click="router.push(tab.path)"
    >
      <div class="tab-icon-wrap">
        <span class="tab-icon">{{ tab.icon }}</span>
        <span v-if="tab.showBadge && cartCount > 0" class="cart-badge">{{ cartCount }}</span>
      </div>
      <span>{{ tab.label }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()

const currentPath = computed(() => route.path)

const cartCount = computed(() => cartStore.totalCount)

const tabs = [
  { path: '/m', label: '商城', icon: '🏪' },
  { path: '/m/orders', label: '订单', icon: '📋' },
  { path: '/m/cart', label: '购物车', icon: '🛒', showBadge: true },
  { path: '/m/addresses', label: '配送', icon: '🚚' },
  { path: '/m/profile', label: '我的', icon: '👤' }
]
</script>

<style scoped>
.mobile-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 50px;
  background: white;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
  z-index: 1000;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  cursor: pointer;
  position: relative;
}

.tab-item.active {
  color: #667eea;
}

.tab-icon-wrap {
  position: relative;
  font-size: 20px;
}

.tab-item span:last-child {
  font-size: 11px;
  margin-top: 2px;
}

.cart-badge {
  position: absolute;
  top: -5px;
  right: -8px;
  background: #ff4d4f;
  color: white;
  font-size: 10px;
  padding: 2px 5px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
}
</style>
