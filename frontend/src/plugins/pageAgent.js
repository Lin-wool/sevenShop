// src/plugins/pageAgent.js
import { PageAgent } from 'page-agent'

let agentInstance = null
let currentConfig = null

// 默认配置 - 使用 GLM (智谱AI)
const defaultConfig = {
  model: 'glm-4.5-air',
  baseURL: 'https://open.bigmodel.cn/api/paas/v4',
  // 填入你的 GLM API Key
  apiKey: 'ac050a69a6b34a35bcf0dc68e23b7968.O0oibo586Icq1hM7',
  language: 'zh-CN',
}

/**
 * 重新创建 Page Agent 实例
 */
function createAgent(config) {
  if (agentInstance) {
    try {
      // 尝试销毁旧实例
      if (typeof agentInstance.destroy === 'function') {
        agentInstance.destroy()
      }
    } catch (e) {
      // 忽略销毁错误
    }
  }
  agentInstance = new PageAgent(config)
  return agentInstance
}

/**
 * 初始化 Page Agent
 * @param {Object} config - 配置选项
 * @param {string} config.model - 使用的模型
 * @param {string} config.baseURL - API地址
 * @param {string} config.apiKey - API密钥
 * @param {string} config.language - 语言设置
 */
export function initPageAgent(config = {}) {
  currentConfig = {
    ...defaultConfig,
    ...config,
  }

  if (agentInstance) {
    return agentInstance
  }

  agentInstance = new PageAgent(currentConfig)
  return agentInstance
}

/**
 * 确保 Page Agent 实例可用，必要时重建
 */
function ensureAgent() {
  if (!agentInstance || !currentConfig) {
    return initPageAgent()
  }

  try {
    // 测试实例是否可用
    if (typeof agentInstance.execute !== 'function') {
      return createAgent(currentConfig)
    }
  } catch (e) {
    // 实例已失效，重新创建
    return createAgent(currentConfig)
  }

  return agentInstance
}

/**
 * 获取 Page Agent 实例
 */
export function getPageAgent() {
  return ensureAgent()
}

/**
 * 执行自然语言指令
 * @param {string} instruction - 自然语言指令
 */
export async function executeInstruction(instruction) {
  const agent = ensureAgent()
  return await agent.execute(instruction)
}

/**
 * 销毁 Page Agent 实例
 */
export function destroyPageAgent() {
  if (agentInstance) {
    try {
      if (typeof agentInstance.destroy === 'function') {
        agentInstance.destroy()
      }
    } catch (e) {
      // 忽略错误
    }
    agentInstance = null
  }
}

export default {
  install(app, config = {}) {
    const agent = initPageAgent(config)
    app.config.globalProperties.$pageAgent = agent
    app.provide('pageAgent', agent)
  },
}
