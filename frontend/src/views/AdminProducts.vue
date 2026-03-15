<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>商品管理</span>
        <el-button type="primary" @click="openDialog()">
          添加商品
        </el-button>
      </div>
    </template>

    <el-table :data="products" v-loading="loading">
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <div class="product-cell">
            <img :src="row.imageUrl || 'https://via.placeholder.com/60'" class="product-image" />
            <div>
              <div class="product-name">{{ row.name }}</div>
              <div class="product-category">{{ row.categoryName }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">
          <span class="price">¥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button type="primary" size="small" @click="openDialog(row)">
              编辑
            </el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" size="small" @click="deleteProduct(row.id)">
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      class="pagination"
      :current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="fetchProducts"
    />
  </el-card>

  <!-- 商品编辑对话框 -->
  <el-dialog
    v-model="dialogVisible"
    :title="editingId ? '编辑商品' : '添加商品'"
    width="600px"
    :close-on-click-modal="false"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入商品名称" />
      </el-form-item>
      <el-form-item label="商品分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="价格" prop="price">
        <el-input-number
          v-model="form.price"
          :min="0"
          :precision="2"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="图片URL">
        <el-input v-model="form.imageUrl" placeholder="请输入图片URL" />
      </el-form-item>

      <el-divider>商品规格</el-divider>

      <!-- 预设模板选择 -->
      <el-form-item label="预设模板">
        <el-select
          v-model="selectedTemplate"
          placeholder="选择预设模板（可选）"
          clearable
          style="width: 100%"
          @change="applyTemplate"
        >
          <el-option
            v-for="template in specTemplates"
            :key="template.name"
            :label="template.name"
            :value="template.name"
          />
        </el-select>
      </el-form-item>

      <div v-for="(spec, index) in form.specs" :key="index" class="spec-item">
          <el-form-item :label="'规格' + (index + 1)">
            <el-row :gutter="10">
              <el-col :span="10">
                <el-input v-model="spec.specName" placeholder="规格名称（如：甜度）" />
              </el-col>
              <el-col :span="12">
                <el-input
                  v-model="spec.specValuesText"
                  placeholder="用逗号分隔（如：不另外加糖,正常糖,七分糖）"
                  @blur="parseSpecValues(spec)"
                />
              </el-col>
              <el-col :span="2">
                <el-button type="danger" link @click="removeSpec(index)">
                  删除
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
      </div>

      <el-button type="primary" link @click="addSpec" style="margin-bottom: 20px">
        + 添加规格
      </el-button>

      <el-form-item label="状态">
        <el-switch
          v-model="form.status"
          :active-value="1"
          :inactive-value="0"
          active-text="上架"
          inactive-text="下架"
        />
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
const products = ref([])
const categories = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 预设规格模板
const specTemplates = ref([])
const selectedTemplate = ref('')

const dialogVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  name: '',
  categoryId: null,
  price: 0,
  imageUrl: '',
  status: 1,
  specs: []
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const fetchProducts = async (newPage = 1) => {
  loading.value = true
  try {
    const res = await api.get('/products', {
      params: { page: newPage, size: size.value }
    })
    products.value = res.records
    total.value = res.total
    page.value = newPage
  } catch (error) {
    ElMessage.error('获取商品失败')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await api.get('/categories')
    categories.value = res
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 获取预设规格模板
const fetchSpecTemplates = async () => {
  try {
    const res = await api.get('/spec-templates')
    specTemplates.value = res
  } catch (error) {
    console.error('获取预设规格失败:', error)
  }
}

// 应用预设模板
const applyTemplate = (templateName) => {
  if (!templateName) {
    selectedTemplate.value = ''
    return
  }

  const template = specTemplates.value.find(t => t.name === templateName)
  if (template && template.specs) {
    form.specs = template.specs.map(s => ({
      specName: s.specName,
      specValues: s.specValues.split(',').map(v => v.trim()),
      specValuesText: s.specValues
    }))
  }
}

const openDialog = async (product = null) => {
  editingId.value = product?.id || null
  if (product) {
    form.name = product.name
    form.categoryId = product.categoryId
    form.price = product.price
    form.imageUrl = product.imageUrl
    form.status = product.status

    // 获取规格
    try {
      const res = await api.get(`/products/${product.id}`)
      form.specs = (res.specs || []).map(s => ({
        specName: s.specName,
        specValues: s.specValues,
        specValuesText: s.specValues?.join(',') || ''
      }))
    } catch (error) {
      form.specs = []
    }
  } else {
    form.name = ''
    form.categoryId = null
    form.price = 0
    form.imageUrl = ''
    form.status = 1
    form.specs = []
    selectedTemplate.value = ''  // 清空预设模板选择
  }
  dialogVisible.value = true
}

const addSpec = () => {
  form.specs.push({
    specName: '',
    specValues: [],
    specValuesText: ''
  })
}

const removeSpec = (index) => {
  form.specs.splice(index, 1)
}

const parseSpecValues = (spec) => {
  spec.specValues = spec.specValuesText.split(',').map(v => v.trim()).filter(v => v)
}

const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 处理规格
  const specs = form.specs
    .filter(s => s.specName && s.specValues?.length > 0)
    .map(s => ({
      specName: s.specName,
      specValues: s.specValues
    }))

  submitting.value = true
  try {
    const data = {
      name: form.name,
      categoryId: form.categoryId,
      price: form.price,
      imageUrl: form.imageUrl,
      status: form.status,
      specs
    }

    if (editingId.value) {
      await api.put(`/products/${editingId.value}`, data)
      ElMessage.success('更新成功')
    } else {
      await api.post('/products', data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchProducts()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (product) => {
  try {
    await api.put(`/products/${product.id}`, {
      status: product.status === 1 ? 0 : 1
    })
    ElMessage.success(product.status === 1 ? '已下架' : '已上架')
    fetchProducts()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteProduct = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      type: 'warning'
    })
    await api.delete(`/products/${id}`)
    ElMessage.success('删除成功')
    fetchProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
  fetchSpecTemplates()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 8px;
}

.product-name {
  font-weight: 500;
}

.product-category {
  font-size: 12px;
  color: #999;
}

.price {
  color: #ff6b6b;
  font-weight: bold;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}

.spec-item {
  margin-bottom: 10px;
}
</style>
