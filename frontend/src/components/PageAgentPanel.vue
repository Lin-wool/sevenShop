<template>
  <div class="page-agent-panel">
    <!-- 悬浮按钮 -->
    <div
      v-if="!isOpen"
      class="agent-float-btn"
      @click="openPanel"
      title="AI 助手"
    >
      <el-icon :size="24"><MagicStick /></el-icon>
    </div>

    <!-- 控制面板 -->
    <transition name="slide">
      <div v-if="isOpen" class="agent-panel">
        <div class="panel-header">
          <div class="header-title">
            <el-icon><MagicStick /></el-icon>
            <span>AI 助手</span>
          </div>
          <div class="header-actions">
            <el-button
              type="danger"
              size="small"
              text
              @click="stopExecution"
              v-if="isExecuting"
            >
              停止
            </el-button>
            <el-button
              size="small"
              text
              @click="closePanel"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>

        <div class="panel-body" ref="messageContainer">
          <!-- 欢迎消息 -->
          <div v-if="messages.length === 0" class="welcome-message">
            <p>你好！我是 AI 助手，可以通过自然语言帮你操作页面。</p>
            <p>例如：</p>
            <ul>
              <li>"点击登录按钮"</li>
              <li>"填写用户名和密码"</li>
              <li>"搜索商品"</li>
              <li>"添加到购物车"</li>
            </ul>
          </div>

          <!-- 消息列表 -->
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message"
            :class="msg.type"
          >
            <div class="message-content">
              <div class="message-text">{{ msg.content }}</div>
              <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
            </div>
          </div>

          <!-- 加载状态 -->
          <div v-if="isExecuting" class="executing-indicator">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>AI 正在执行...</span>
          </div>
        </div>

        <div class="panel-footer">
          <el-input
            v-model="inputText"
            placeholder="输入指令，如：点击登录按钮"
            :disabled="isExecuting"
            @keyup.enter="submitInstruction"
          >
            <template #append>
              <el-button
                :icon="Promotion"
                @click="submitInstruction"
                :loading="isExecuting"
                :disabled="!inputText.trim()"
              />
            </template>
          </el-input>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { MagicStick, Close, Promotion, Loading } from '@element-plus/icons-vue'
import { getPageAgent, destroyPageAgent } from '../plugins/pageAgent'

const isOpen = ref(false)
const inputText = ref('')
const messages = ref([])
const isExecuting = ref(false)
const messageContainer = ref(null)

const openPanel = () => {
  isOpen.value = true
}

const closePanel = () => {
  isOpen.value = false
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  }
}

const submitInstruction = async () => {
  if (!inputText.value.trim() || isExecuting.value) return

  const instruction = inputText.value.trim()
  inputText.value = ''

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: instruction,
    timestamp: Date.now()
  })

  await scrollToBottom()

  // 添加系统消息占位
  const systemMsgIndex = messages.value.length
  messages.value.push({
    type: 'system',
    content: '',
    timestamp: Date.now()
  })

  isExecuting.value = true

  try {
    // 每次获取最新的实例（自动重建失效的实例）
    const pageAgent = getPageAgent()

    // 执行指令
    const result = await pageAgent.execute(instruction)

    // 更新系统消息
    messages.value[systemMsgIndex].content = result.success
      ? '执行成功'
      : `执行失败: ${result.error || '未知错误'}`

    if (result.error) {
      console.error('执行错误:', result.error)
    }
  } catch (error) {
    // 如果实例失效，销毁并重建
    if (error.message && error.message.includes('disposed')) {
      destroyPageAgent()
      messages.value[systemMsgIndex].content = '实例已重建，请重试'
    } else {
      messages.value[systemMsgIndex].content = `执行出错: ${error.message}`
    }
    console.error('Page Agent 执行错误:', error)
  } finally {
    isExecuting.value = false
    await scrollToBottom()
  }
}

const stopExecution = () => {
  const pageAgent = getPageAgent()
  if (pageAgent && pageAgent.stop) {
    pageAgent.stop()
  }
  isExecuting.value = false
  messages.value.push({
    type: 'system',
    content: '已停止执行',
    timestamp: Date.now()
  })
}

onMounted(() => {
  // 初始化时检查一次
  try {
    getPageAgent()
  } catch (e) {
    console.warn('Page Agent 初始化失败:', e)
  }
})
</script>

<style scoped>
.page-agent-panel {
  position: fixed;
  z-index: 9999;
}

.agent-float-btn {
  position: fixed;
  bottom: 80px;
  right: 20px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: transform 0.3s, box-shadow 0.3s;
}

.agent-float-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.5);
}

.agent-panel {
  position: fixed;
  bottom: 80px;
  right: 20px;
  width: 380px;
  height: 500px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-actions .el-button {
  color: white;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.welcome-message {
  text-align: center;
  color: #666;
  padding: 20px;
}

.welcome-message ul {
  text-align: left;
  margin-top: 12px;
  padding-left: 20px;
}

.welcome-message li {
  margin: 8px 0;
  color: #667eea;
}

.message {
  display: flex;
  flex-direction: column;
}

.message.user {
  align-items: flex-end;
}

.message.system {
  align-items: flex-start;
}

.message-content {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 12px;
  word-break: break-word;
}

.message.user .message-content {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 4px;
}

.message.system .message-content {
  background: #f5f5f5;
  color: #333;
  border-bottom-left-radius: 4px;
}

.message-time {
  font-size: 10px;
  color: #999;
  margin-top: 4px;
}

.executing-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667eea;
  padding: 8px;
}

.panel-footer {
  padding: 12px 16px;
  border-top: 1px solid #eee;
}

.panel-footer :deep(.el-input-group__append) {
  background: #667eea;
  border-color: #667eea;
}

.panel-footer :deep(.el-input-group__append .el-button) {
  color: white;
}

/* 过渡动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
