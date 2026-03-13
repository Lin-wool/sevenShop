<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="title">💕 情侣商城</h1>
      <p class="subtitle">许愿清单 - 帮她实现愿望</p>

      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>

        <div class="links">
          <el-link type="primary" @click="router.push('/register')">
            还没有账号？立即注册
          </el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getHomePath } from '../utils/device'
import api from '../api'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await api.post('/auth/login', form)
    userStore.setToken(res.data.token)
    userStore.setUser(res.data.user)
    ElMessage.success('登录成功')
    router.push(getHomePath())
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
}

.login-card {
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  width: 400px;
  text-align: center;
}

.title {
  font-size: 28px;
  color: #ff6b6b;
  margin-bottom: 8px;
}

.subtitle {
  color: #999;
  margin-bottom: 32px;
  font-size: 14px;
}

.login-form {
  margin-top: 24px;
}

.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
  border: none;
}

.login-btn:hover {
  opacity: 0.9;
}

.links {
  margin-top: 16px;
}
</style>
