<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>用户管理</span>
      </div>
    </template>

    <el-table :data="users" v-loading="loading">
      <el-table-column label="用户" min-width="150">
        <template #default="{ row }">
          <div class="user-cell">
            <div class="user-avatar">{{ row.nickname?.charAt(0) || row.username?.charAt(0) || '?' }}</div>
            <div>
              <div class="user-name">{{ row.nickname || row.username }}</div>
              <div class="user-email">{{ row.email }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 1"
            type="danger"
            size="small"
            @click="toggleUserStatus(row)"
          >
            禁用
          </el-button>
          <el-button
            v-else
            type="success"
            size="small"
            @click="toggleUserStatus(row)"
          >
            启用
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="fetchUsers"
      @current-change="fetchUsers"
      style="margin-top: 20px; justify-content: center;"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const loading = ref(false)
const users = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await api.get('/users', {
      params: {
        page: page.value,
        size: size.value
      }
    })
    users.value = res.data?.records || res.records || []
    total.value = res.data?.total || res.total || 0
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const toggleUserStatus = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', { type: 'warning' })
    await api.put(`/users/${user.id}/status`, { status: newStatus })
    ElMessage.success(`${action}成功`)
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.user-email {
  font-size: 12px;
  color: #999;
}
</style>
