<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>商品规格模板</span>
        <el-button type="primary" @click="openDialog()">
          添加规格
        </el-button>
      </div>
    </template>

    <el-table :data="templates" v-loading="loading">
      <el-table-column prop="name" label="模板名称" width="120">
        <template #default="{ row }">
          <el-tag type="primary">{{ row.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="specName" label="规格名称" width="120" />
      <el-table-column prop="specValues" label="规格值" min-width="300">
        <template #default="{ row }">
          <el-tag
            v-for="(value, index) in row.specValues.split(',')"
            :key="index"
            class="spec-value-tag"
            type="info"
          >
            {{ value.trim() }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openDialog(row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <!-- 添加/编辑对话框 -->
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑规格模板' : '添加规格模板'"
    width="500px"
  >
    <el-form :model="form" label-width="100px">
      <el-form-item label="模板名称" required>
        <el-input v-model="form.name" placeholder="如：奶茶、咖啡、甜品" />
      </el-form-item>
      <el-form-item label="规格名称" required>
        <el-input v-model="form.specName" placeholder="如：甜度、温度、加料" />
      </el-form-item>
      <el-form-item label="规格值" required>
        <el-input
          v-model="form.specValues"
          type="textarea"
          :rows="3"
          placeholder="多个值用逗号分隔，如：不另外加糖,五分糖,七分糖,正常糖"
        />
        <div class="form-tip">多个值用逗号分隔</div>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" :max="999" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
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
const templates = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)

const form = reactive({
  id: null,
  name: '',
  specName: '',
  specValues: '',
  sort: 0
})

const fetchTemplates = async () => {
  loading.value = true
  try {
    const res = await api.get('/spec-templates/all')
    templates.value = res
  } catch (error) {
    ElMessage.error('获取规格模板失败')
  } finally {
    loading.value = false
  }
}

const openDialog = (row = null) => {
  if (row) {
    isEdit.value = true
    form.id = row.id
    form.name = row.name
    form.specName = row.specName
    form.specValues = row.specValues
    form.sort = row.sort
  } else {
    isEdit.value = false
    form.id = null
    form.name = ''
    form.specName = ''
    form.specValues = ''
    form.sort = 0
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入模板名称')
    return
  }
  if (!form.specName.trim()) {
    ElMessage.warning('请输入规格名称')
    return
  }
  if (!form.specValues.trim()) {
    ElMessage.warning('请输入规格值')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await api.put(`/spec-templates/${form.id}`, form)
      ElMessage.success('更新成功')
    } else {
      await api.post('/spec-templates', form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchTemplates()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这条规格模板吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await api.delete(`/spec-templates/${id}`)
      ElMessage.success('删除成功')
      fetchTemplates()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchTemplates()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.spec-value-tag {
  margin-right: 6px;
  margin-bottom: 4px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
