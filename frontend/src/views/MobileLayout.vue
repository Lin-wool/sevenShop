<template>
  <div class="mobile-layout">
    <!-- 路由内容区域 -->
    <router-view v-slot="{ Component }">
      <keep-alive>
        <component :is="Component" />
      </keep-alive>
    </router-view>
  </div>
</template>

<script setup>
</script>

<style scoped>
.mobile-layout {
  min-height: 100vh;
  background: #f8f9fa;
}
</style>
