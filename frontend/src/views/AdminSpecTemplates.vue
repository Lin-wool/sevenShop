<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>商品规格模板</span>
        <el-button type="primary" @click="openTemplateDialog()">
          添加模板
        </el-button>
      </div>
    </template>

    <el-collapse v-model="activeCollapse" v-loading="loading">
      <el-collapse-item
        v-for="template in groupedTemplates"
        :key="template.name"
        :name="template.name"
      >
        <template #title>
          <div class="template-header">
            <span class="template-name">{{ template.name }}</span>
            <span class="template-count">({{ template.specs.length }} 个规格)</span>
            <div class="template-actions" @click.stop>
              <el-button type="primary" size="small" link @click="openTemplateDialog(template)">
                编辑
              </el-button>
              <el-button type="danger" size="small" link @click="deleteTemplate(template.name)">
                删除
              </el-button>
            </div>
          </div>
        </template>

        <div class="spec-list">
          <div v-for="(spec, index) in template.specs" :key="index" class="spec-item">
            <div class="spec-info">
              <span class="spec-name">{{ spec.specName }}:</span>
              <div class="spec-values">
                <el-tag
                  v-for="(value, vIndex) in spec.specValues.split(',')"
                  :key="vIndex"
                  class="spec-value-tag"
                  type="info"
                >
                  {{ value.trim() }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-collapse-item>

      <el-empty v-if="!loading && groupedTemplates.length === 0" description="暂无规格模板" />
    </el-collapse>
  </el-card>

  <!-- 添加/编辑模板对话框 -->
  <el-dialog
    v-model="dialogVisible"
    :title="editingTemplate ? '编辑模板' : '添加模板'"
    width="600px"
  >
    <el-form :model="templateForm" label-width="100px">
      <el-form-item label="模板名称" required>
        <el-input v-model="templateForm.name" placeholder="如：奶茶、咖啡、甜品" :disabled="!!editingTemplate" />
      </el-form-item>

      <el-divider content-position="left">规格列表</el-divider>

      <div v-for="(spec, index) in templateForm.specs" :key="index" class="spec-edit-item">
        <el-row :gutter="10">
          <el-col :span="8">
            <el-input v-model="spec.specName" placeholder="规格名称（如：甜度）" />
          </el-col>
          <el-col :span="14">
            <el-input v-model="spec.specValues" placeholder="用逗号分隔（如：不另外加糖,五分糖,七分糖）" />
          </el-col>
          <el-col :span="2">
            <el-button type="danger" link @click="removeSpec(index)">
              删除
            </el-button>
          </el-col>
        </el-row>
      </div>

      <el-button type="primary" link @click="addSpec">
        + 添加规格
      </el-button>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const loading = ref(false)
const templates = ref([]) // 平铺数据
const activeCollapse = ref([])
const dialogVisible = ref(false)
const editingTemplate = ref(null)
const submitting = ref(false)

// 分组后的模板数据
const groupedTemplates = computed(() => {
  const groups = {}
  templates.value.forEach(t => {
    if (!groups[t.name]) {
      groups[t.name] = {
        name: t.name,
        specs: []
      }
    }
    groups[t.name].specs.push({
      specName: t.specName,
      specValues: t.specValues
    })
  })
  return Object.values(groups)
})

const templateForm = reactive({
  name: '',
  specs: [
    { specName: '', specValues: '' }
  ]
})

const fetchTemplates = async () => {
  loading.value = true
  try {
    const res = await api.get('/spec-templates/all')
    templates.value = res || []
  } catch (error) {
    ElMessage.error('获取规格模板失败')
  } finally {
    loading.value = false
  }
}

const openTemplateDialog = (template = null) => {
  editingTemplate.value = template
  if (template) {
    templateForm.name = template.name
    templateForm.specs = template.specs.map(s => ({
      specName: s.specName,
      specValues: s.specValues
    }))
  } else {
    templateForm.name = ''
    templateForm.specs = [{ specName: '', specValues: '' }]
  }
  dialogVisible.value = true
}

const addSpec = () => {
  templateForm.specs.push({ specName: '', specValues: '' })
}

const removeSpec = (index) => {
  templateForm.specs.splice(index, 1)
}

const handleSubmit = async () => {
  if (!templateForm.name.trim()) {
    ElMessage.warning('请输入模板名称')
    return
  }

  const validSpecs = templateForm.specs.filter(s => s.specName.trim() && s.specValues.trim())
  if (validSpecs.length === 0) {
    ElMessage.warning('请至少添加一个规格')
    return
  }

  submitting.value = true
  try {
    // 如果是编辑模式，先删除原有规格，再添加新规格
    if (editingTemplate.value) {
      const toDelete = templates.value.filter(t => t.name === editingTemplate.value.name)
      for (const spec of toDelete) {
        await api.delete(`/spec-templates/${spec.id}`)
      }
    }

    // 添加新规格
    for (const spec of validSpecs) {
      await api.post('/spec-templates', {
        name: templateForm.name,
        specName: spec.specName,
        specValues: spec.specValues,
        sort: 0
      })
    }

    ElMessage.success(editingTemplate.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchTemplates()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const deleteTemplate = async (name) => {
  try {
    await ElMessageBox.confirm(`确定要删除模板"${name}"及其所有规格吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    loading.value = true
    const toDelete = templates.value.filter(t => t.name === name)
    for (const spec of toDelete) {
      await api.delete(`/spec-templates/${spec.id}`)
    }
    ElMessage.success('删除成功')
    fetchTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  } finally {
    loading.value = false
  }
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

.template-header {
  display: flex;
  align-items: center;
  width: 100%;
}

.template-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.template-count {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}

.template-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.spec-list {
  padding: 10px 0;
}

.spec-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.spec-item:last-child {
  border-bottom: none;
}

.spec-info {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.spec-name {
  font-weight: 500;
  min-width: 60px;
}

.spec-values {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.spec-value-tag {
  margin-right: 0;
}

.spec-edit-item {
  margin-bottom: 10px;
}
</style>
