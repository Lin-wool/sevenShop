<template>
  <el-config-provider :locale="zhCn">
    <router-view />
    <!-- Page Agent AI 助手面板 -->
    <PageAgentPanel />
  </el-config-provider>
</template>

<script setup>
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import PageAgentPanel from './components/PageAgentPanel.vue'
</script>

<style>
/* 引入 Google Fonts - Noto Sans SC for Chinese */
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;600;700&display=swap');

:root {
  /* 商城品牌色 */
  --color-primary: #3B82F6;
  --color-primary-light: #60A5FA;
  --color-primary-dark: #2563EB;
  --color-cta: #F97316;
  --color-cta-hover: #EA580C;
  --color-success: #10B981;
  --color-warning: #F59E0B;
  --color-danger: #EF4444;

  /* 背景色 */
  --color-bg: #F8FAFC;
  --color-bg-card: #FFFFFF;
  --color-bg-dark: #1E293B;

  /* 文字色 */
  --color-text: #1E293B;
  --color-text-secondary: #64748B;
  --color-text-muted: #94A3B8;
  --color-text-light: #FFFFFF;

  /* 边框色 */
  --color-border: #E2E8F0;
  --color-border-light: #F1F5F9;

  /* 圆角 */
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
  --radius-xl: 24px;

  /* 阴影 */
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1);
  --shadow-card: 0 2px 8px rgba(0, 0, 0, 0.06);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: var(--color-bg);
  color: var(--color-text);
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  min-height: 100vh;
}

/* Element Plus 主题定制 */
.el-button--primary {
  --el-button-bg-color: var(--color-primary);
  --el-button-border-color: var(--color-primary);
  --el-button-hover-bg-color: var(--color-primary-dark);
  --el-button-hover-border-color: var(--color-primary-dark);
  --el-button-active-bg-color: var(--color-primary-dark);
  --el-button-active-border-color: var(--color-primary-dark);
}

.el-button--warning {
  --el-button-bg-color: var(--color-cta);
  --el-button-border-color: var(--color-cta);
  --el-button-hover-bg-color: var(--color-cta-hover);
  --el-button-hover-border-color: var(--color-cta-hover);
}

.el-button--danger {
  --el-button-bg-color: var(--color-danger);
  --el-button-border-color: var(--color-danger);
}

/* 卡片样式统一 */
.el-card {
  --el-card-border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-card);
}

/* 输入框样式统一 */
.el-input__wrapper {
  border-radius: var(--radius-md);
}

.el-textarea__inner {
  border-radius: var(--radius-md);
}

/* 通用过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
