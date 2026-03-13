<template>
  <div class="mobile-page">
    <!-- 顶部区域 -->
    <div class="mobile-header">
      <div class="header-left" @click="router.push('/m')">
        <span class="back-icon">←</span>
      </div>
      <div class="header-title">收货地址</div>
      <div class="header-right" @click="openDialog()">
        <span class="add-icon">+</span>
      </div>
    </div>

    <!-- 地址列表 -->
    <div class="address-list" v-loading="loading">
      <div
        v-for="addr in addresses"
        :key="addr.id"
        class="address-card"
      >
        <div class="address-content">
          <div class="address-header">
            <span class="name">{{ addr.name }}</span>
            <span class="phone">{{ addr.phone }}</span>
            <span class="tag" v-if="addr.isDefault === 1">默认</span>
          </div>
          <div class="address-text">{{ addr.address }}</div>
        </div>
        <div class="address-actions">
          <span class="action-btn edit" @click="openDialog(addr)">编辑</span>
          <span
            class="action-btn default"
            v-if="addr.isDefault !== 1"
            @click="setDefault(addr.id)"
          >
            设为默认
          </span>
          <span class="action-btn delete" @click="deleteAddress(addr.id)">删除</span>
        </div>
      </div>

      <el-empty v-if="!loading && addresses.length === 0" description="暂无收货地址" />

      <!-- 底部占位 -->
      <div class="bottom-space"></div>
    </div>

    <!-- 添加/编辑地址弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑地址' : '添加地址'"
      width="90%"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="收货人" prop="name">
          <el-input v-model="form.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input
            v-model="form.address"
            type="textarea"
            :rows="2"
            placeholder="请输入详细收货地址"
          />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
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
      <div class="tab-item active">
        <div class="tab-icon-wrap">
          <span class="tab-icon">🚚</span>
        </div>
        <span>配送</span>
      </div>
      <div class="tab-item" @click="router.push('/m/profile')">
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
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const router = useRouter()

const loading = ref(false)
const addresses = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  name: '',
  phone: '',
  address: '',
  isDefault: 0
})

const rules = {
  name: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const fetchAddresses = async () => {
  loading.value = true
  try {
    const res = await api.get('/addresses')
    addresses.value = res.data
  } catch (error) {
    ElMessage.error('获取地址失败')
  } finally {
    loading.value = false
  }
}

const openDialog = (addr = null) => {
  editingId.value = addr?.id || null
  if (addr) {
    form.name = addr.name
    form.phone = addr.phone
    form.address = addr.address
    form.isDefault = addr.isDefault
  } else {
    form.name = ''
    form.phone = ''
    form.address = ''
    form.isDefault = 0
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingId.value) {
      await api.put(`/addresses/${editingId.value}`, form)
      ElMessage.success('更新成功')
    } else {
      await api.post('/addresses', form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchAddresses()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const setDefault = async (id) => {
  try {
    await api.put(`/addresses/${id}`, { isDefault: 1 })
    ElMessage.success('设置成功')
    fetchAddresses()
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

const deleteAddress = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      type: 'warning'
    })
    await api.delete(`/addresses/${id}`)
    ElMessage.success('删除成功')
    fetchAddresses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchAddresses()
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

.back-icon, .add-icon {
  color: white;
  font-size: 24px;
  cursor: pointer;
}

.header-title {
  color: white;
  font-size: 18px;
  font-weight: bold;
}

/* 地址列表 */
.address-list {
  padding: 12px;
}

.address-card {
  background: white;
  border-radius: 12px;
  margin-bottom: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.address-content {
  padding: 16px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.phone {
  font-size: 14px;
  color: #666;
}

.tag {
  font-size: 11px;
  background: #667eea;
  color: white;
  padding: 2px 8px;
  border-radius: 8px;
}

.address-text {
  font-size: 13px;
  color: #999;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 16px;
  padding: 12px 16px;
  border-top: 1px solid #f5f5f5;
}

.action-btn {
  font-size: 13px;
  cursor: pointer;
}

.action-btn.edit {
  color: #667eea;
}

.action-btn.default {
  color: #4caf50;
}

.action-btn.delete {
  color: #ff4d4f;
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
