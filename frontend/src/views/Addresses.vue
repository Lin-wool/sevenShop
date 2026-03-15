<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>收货地址</span>
        <el-button type="primary" @click="openDialog()">
          添加地址
        </el-button>
      </div>
    </template>

    <el-row :gutter="20" v-loading="loading">
      <el-col
        v-for="addr in addresses"
        :key="addr.id"
        :xs="24"
        :sm="12"
        :md="8"
      >
        <el-card class="address-card" shadow="hover">
          <div class="address-content">
            <div class="address-header">
              <span class="name">{{ addr.name }}</span>
              <el-tag v-if="addr.isDefault === 1" type="success" size="small">
                默认
              </el-tag>
            </div>
            <div class="phone">{{ addr.phone }}</div>
            <div class="address">{{ addr.address }}</div>
            <div class="actions">
              <el-button type="primary" link @click="openDialog(addr)">
                编辑
              </el-button>
              <el-button
                type="success"
                link
                @click="setDefault(addr.id)"
                v-if="addr.isDefault !== 1"
              >
                设为默认
              </el-button>
              <el-button type="danger" link @click="deleteAddress(addr.id)">
                删除
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && addresses.length === 0" description="暂无收货地址" />
  </el-card>

  <!-- 地址编辑对话框 -->
  <el-dialog
    v-model="dialogVisible"
    :title="editingId ? '编辑地址' : '添加地址'"
    width="500px"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
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
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

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
    addresses.value = res
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
  const valid = await formRef.value.validate().catch(() => false)
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.address-card {
  margin-bottom: 20px;
}

.address-content {
  position: relative;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.name {
  font-size: 16px;
  font-weight: bold;
}

.phone {
  color: #666;
  margin-bottom: 8px;
}

.address {
  color: #999;
  margin-bottom: 12px;
}

.actions {
  display: flex;
  gap: 8px;
  border-top: 1px solid #eee;
  padding-top: 12px;
}
</style>
