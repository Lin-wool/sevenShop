<template>
  <div class="register-container">
    <div class="register-card">
      <h1 class="title">💕 加入情侣商城</h1>
      <p class="subtitle">注册成为会员，开始许愿吧</p>

      <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="昵称"
            :prefix-icon="Star"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="邮箱（用于接收通知）"
            :prefix-icon="Message"
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
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-btn"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>

        <div class="links">
          <el-link type="primary" @click="router.push('/login')">
            已有账号？立即登录
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
import { User, Lock, Star, Message } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await api.post('/auth/register', form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.register-card {
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

.register-form {
  margin-top: 24px;
}

.register-btn {
  width: 100%;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
  border: none;
}

.register-btn:hover {
  opacity: 0.9;
}

.links {
  margin-top: 16px;
}
</style>
