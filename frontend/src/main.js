import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import pageAgent from './plugins/pageAgent'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus)
app.use(router)
app.use(createPinia())
// 注册 Page Agent 插件 (使用 GLM)
app.use(pageAgent, {
  model: 'glm-4.5-air',
  baseURL: 'https://open.bigmodel.cn/api/paas/v4',
  language: 'zh-CN',
})
app.mount('#app')
