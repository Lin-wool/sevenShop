<template>
  <div class="mobile-page">
    <!-- 顶部区域 -->
    <div class="mobile-header">
      <div class="header-left" @click="router.push('/m')">
        <span class="back-icon">←</span>
      </div>
      <div class="header-title">个人中心</div>
      <div class="header-right"></div>
    </div>

    <!-- 用户信息 -->
    <div class="user-info-card">
      <div class="avatar-wrap">
        <div class="avatar">👤</div>
      </div>
      <div class="user-detail">
        <div class="username">{{ userStore.user?.nickname || userStore.user?.username || '用户' }}</div>
        <div class="email" v-if="userStore.user?.email">{{ userStore.user?.email }}</div>
        <div class="role-tag" v-if="userStore.user?.role === 'ADMIN'">管理员</div>
      </div>
      <div class="edit-btn" @click="openProfileDialog">
        <span>编辑</span>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <div class="menu-item" @click="router.push('/m/orders')">
        <span class="menu-icon">📋</span>
        <span class="menu-text">我的订单</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="router.push('/m/addresses')">
        <span class="menu-icon">📍</span>
        <span class="menu-text">收货地址</span>
        <span class="menu-arrow">›</span>
      </div>
      <!-- 管理员入口 -->
      <div class="menu-item" @click="router.push('/m/admin')" v-if="userStore.user?.role === 'ADMIN'">
        <span class="menu-icon">🔧</span>
        <span class="menu-text">管理中心</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="openEditDialog">
        <span class="menu-icon">⚙️</span>
        <span class="menu-text">修改密码</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-section">
      <el-button type="danger" size="large" round :loading="loggingOut" @click="handleLogout" class="logout-btn">
        退出登录
      </el-button>
    </div>

    <!-- 底部占位 -->
    <div class="bottom-space"></div>

    <!-- 编辑资料弹窗 -->
    <el-dialog
      v-model="profileDialogVisible"
      title="编辑资料"
      width="90%"
      :close-on-click-modal="false"
    >
      <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="70px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="profileSubmitting" @click="submitProfile">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="修改密码"
      width="90%"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="passwordForm" :rules="passwordRules" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPassword">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 底部导航栏 -->
    <div class="mobile-tabbar">
      <div class="tab-item" @click="router.push('/m')">
        <div class="tab-icon-wrap">
          <span class="tab-icon">🏪</span>
        </div>
        <span>商城</span>
      </div>
      <div class="tab-item" @click="router.push('/m/orders')">
        <div class="tab-icon-wrap">
          <span class="tab-icon">📋</span>
        </div>
        <span>订单</span>
      </div>
      <div class="tab-item" @click="router.push('/m/addresses')">
        <div class="tab-icon-wrap">
          <span class="tab-icon">🚚</span>
        </div>
        <span>配送</span>
      </div>
      <div class="tab-item active">
        <div class="tab-icon-wrap">
          <span class="tab-icon">👤</span>
        </div>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import api from '../api'

const router = useRouter()
const userStore = useUserStore()

const editDialogVisible = ref(false)
const profileDialogVisible = ref(false)
const submitting = ref(false)
const profileSubmitting = ref(false)
const loggingOut = ref(false)
const formRef = ref()
const profileFormRef = ref()

const profileForm = reactive({
  nickname: '',
  email: ''
})

const profileRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const openProfileDialog = () => {
  profileForm.nickname = userStore.user?.nickname || userStore.user?.username || ''
  profileForm.email = userStore.user?.email || ''
  profileDialogVisible.value = true
}

const submitProfile = async () => {
  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid) return

  profileSubmitting.value = true
  try {
    const res = await api.put('/auth/profile', {
      nickname: profileForm.nickname,
      email: profileForm.email
    })
    // 更新本地用户信息
    userStore.user = { ...userStore.user, ...res.data }
    ElMessage.success('保存成功')
    profileDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    profileSubmitting.value = false
  }
}

const openEditDialog = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  editDialogVisible.value = true
}

const submitPassword = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await api.put('/auth/password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    editDialogVisible.value = false
    userStore.logout()
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '修改失败')
  } finally {
    submitting.value = false
  }
}

const handleLogout = async () => {
  loggingOut.value = true
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  // 检查用户信息
  if (!userStore.user) {
    userStore.fetchUser()
  }
})
</script>

<style scoped>
.mobile-page {
  min-height: 100vh;
  background: #f8f9fa;
}

/* 顶部区域 */
.mobile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left, .header-right {
  width: 40px;
}

.back-icon {
  color: white;
  font-size: 24px;
  cursor: pointer;
}

.header-title {
  color: white;
  font-size: 18px;
  font-weight: bold;
}

/* 用户信息 */
.user-info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
}

.edit-btn {
  position: absolute;
  right: 16px;
  top: 16px;
  background: rgba(255,255,255,0.2);
  color: white;
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
}

.avatar-wrap {
  flex-shrink: 0;
}

.avatar {
  width: 70px;
  height: 70px;
  background: rgba(255,255,255,0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.user-detail {
  color: white;
}

.username {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 6px;
}

.email {
  font-size: 13px;
  opacity: 0.8;
  margin-bottom: 8px;
}

.role-tag {
  display: inline-block;
  background: rgba(255,255,255,0.2);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

/* 功能菜单 */
.menu-section {
  margin: 12px;
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 20px;
  margin-right: 12px;
}

.menu-text {
  flex: 1;
  font-size: 15px;
  color: #333;
}

.menu-arrow {
  color: #ccc;
  font-size: 18px;
}

/* 退出登录 */
.logout-section {
  padding: 20px 12px;
}

.logout-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

.bottom-space {
  height: 70px;
}

/* 底部导航栏 */
.mobile-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  justify-content: space-around;
  padding: 8px 0 12px;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.06);
  z-index: 1000;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #999;
  cursor: pointer;
}

.tab-item.active {
  color: #667eea;
}

.tab-icon-wrap {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-icon {
  font-size: 22px;
}
</style>
