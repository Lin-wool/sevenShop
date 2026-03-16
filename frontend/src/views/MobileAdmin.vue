<template>
  <div class="mobile-page">
    <!-- 顶部区域 -->
    <div class="mobile-header">
      <div class="header-left" @click="router.push('/m')">
        <span class="back-icon">←</span>
      </div>
      <div class="header-title">管理中心</div>
      <div class="header-right"></div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <div class="menu-item" @click="currentTab = 'overview'">
        <span class="menu-icon">📊</span>
        <span class="menu-text">数据概览</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="currentTab = 'products'">
        <span class="menu-icon">🛍️</span>
        <span class="menu-text">商品管理</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="currentTab = 'categories'">
        <span class="menu-icon">📁</span>
        <span class="menu-text">分类管理</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="currentTab = 'orders'">
        <span class="menu-icon">📦</span>
        <span class="menu-text">订单管理</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="currentTab = 'specs'">
        <span class="menu-icon">📝</span>
        <span class="menu-text">规格模板</span>
        <span class="menu-arrow">›</span>
      </div>
      <div class="menu-item" @click="currentTab = 'users'">
        <span class="menu-icon">👥</span>
        <span class="menu-text">用户管理</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>

    <!-- 数据概览 -->
    <div class="overview-section" v-if="currentTab === 'overview'">
      <div class="stats-grid">
        <div class="stat-card" @click="goToProducts" style="cursor: pointer;">
          <div class="stat-value">{{ stats.productCount }}</div>
          <div class="stat-label">商品数量</div>
        </div>
        <div class="stat-card" @click="goToPendingOrders" style="cursor: pointer;">
          <div class="stat-value">{{ stats.pendingOrderCount }}</div>
          <div class="stat-label">待处理订单</div>
        </div>
        <div class="stat-card" @click="goToHandledOrders" style="cursor: pointer;">
          <div class="stat-value">{{ stats.handledOrderCount }}</div>
          <div class="stat-label">已完成订单</div>
        </div>
        <div class="stat-card" @click="goToUsers" style="cursor: pointer;">
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">用户数量</div>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="product-section" v-if="currentTab === 'products'">
      <div class="section-header">
        <span class="section-title">商品列表</span>
        <el-button type="primary" size="small" round @click="openProductDialog()">
          + 新增商品
        </el-button>
      </div>
      <div class="product-list" v-loading="loading">
        <div v-for="product in products" :key="product.id" class="product-item">
          <img :src="product.imageUrl || 'https://via.placeholder.com/60'" class="product-img" />
          <div class="product-info">
            <div class="product-name">{{ product.name }}</div>
            <div class="product-price">¥{{ product.price }}</div>
          </div>
          <div class="product-actions">
            <el-button type="primary" size="small" round @click="openProductDialog(product)">编辑</el-button>
            <el-button type="danger" size="small" round @click="deleteProduct(product.id)">删除</el-button>
          </div>
        </div>
        <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
      </div>
    </div>

    <!-- 分类管理 -->
    <div class="spec-section" v-if="currentTab === 'categories'">
      <div class="section-header">
        <span class="section-title">分类管理</span>
        <el-button type="primary" size="small" round @click="openCategoryDialog()">
          + 添加分类
        </el-button>
      </div>
      <div class="spec-list" v-loading="categoryLoading">
        <div v-for="cat in categoryList" :key="cat.id" class="spec-item">
          <div class="spec-info">
            <div class="spec-name">{{ cat.name }}</div>
            <div class="spec-detail">排序: {{ cat.sort }}</div>
          </div>
          <div class="spec-actions">
            <el-button type="primary" size="small" round @click="openCategoryDialog(cat)">编辑</el-button>
            <el-button type="danger" size="small" round @click="deleteCategory(cat.id)">删除</el-button>
          </div>
        </div>
        <el-empty v-if="!categoryLoading && categoryList.length === 0" description="暂无分类" />
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-section" v-if="currentTab === 'orders'">
      <div class="section-header">
        <span class="section-title">订单列表</span>
      </div>
      <div class="status-tabs">
        <div
          class="status-tab"
          :class="{ active: orderStatus === null }"
          @click="changeOrderStatus(null)"
        >
          全部
        </div>
        <div
          class="status-tab"
          :class="{ active: orderStatus === 0 }"
          @click="changeOrderStatus(0)"
        >
          待处理
        </div>
        <div
          class="status-tab"
          :class="{ active: orderStatus === 1 }"
          @click="changeOrderStatus(1)"
        >
          已处理
        </div>
      </div>
      <div class="order-list" v-loading="orderLoading">
        <div v-for="order in orders" :key="order.id" class="order-item">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.id }}</span>
            <span class="order-status" :class="order.status === 0 ? 'pending' : 'processed'">
              {{ order.status === 0 ? '待处理' : '已处理' }}
            </span>
          </div>
          <div class="order-product">
            <img :src="order.productImage || 'https://via.placeholder.com/50'" class="order-product-img" />
            <div class="order-product-info">
              <div class="order-product-name">{{ order.productName }}</div>
              <div class="order-product-row">
                <span class="order-product-price">¥{{ order.price }}</span>
                <span class="order-product-quantity">x{{ order.quantity || 1 }}</span>
              </div>
            </div>
          </div>
          <div class="order-user" v-if="order.userNickname">
            <span class="user-label">下单人:</span>
            <span class="user-name">{{ order.userNickname }}</span>
          </div>
          <div class="order-actions">
            <el-button
              v-if="order.status === 0"
              type="success"
              size="small"
              round
              @click="handleOrder(order.id)"
            >
              处理订单
            </el-button>
          </div>
        </div>
        <el-empty v-if="!orderLoading && orders.length === 0" description="暂无订单" />
      </div>
    </div>

    <!-- 规格模板 -->
    <div class="spec-section" v-if="currentTab === 'specs'">
      <div class="section-header">
        <span class="section-title">规格模板</span>
        <el-button type="primary" size="small" round @click="openTemplateDialog()">
          + 添加模板
        </el-button>
      </div>
      <div class="spec-list" v-loading="specLoading">
        <el-collapse v-model="activeSpecCollapse">
          <el-collapse-item
            v-for="template in groupedSpecTemplates"
            :key="template.name"
            :name="template.name"
          >
            <template #title>
              <div class="template-header-mobile">
                <span class="template-name">{{ template.name }}</span>
                <span class="template-count">({{ template.specs.length }} 个规格)</span>
              </div>
            </template>
            <div class="template-specs">
              <div v-for="spec in template.specs" :key="spec.id" class="spec-item-mobile">
                <div class="spec-info">
                  <span class="spec-name">{{ spec.specName }}:</span>
                  <div class="spec-values">
                    <el-tag
                      v-for="(value, vIndex) in spec.specValues.split(',')"
                      :key="vIndex"
                      size="small"
                      type="info"
                    >
                      {{ value.trim() }}
                    </el-tag>
                  </div>
                </div>
              </div>
              <div class="template-actions-mobile">
                <el-button type="primary" size="small" round @click="openTemplateDialog(template)">编辑</el-button>
                <el-button type="danger" size="small" round @click="deleteTemplate(template.name)">删除</el-button>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
        <el-empty v-if="!specLoading && groupedSpecTemplates.length === 0" description="暂无规格模板" />
      </div>
    </div>

    <!-- 规格模板编辑弹窗 -->
    <el-dialog
      v-model="specDialogVisible"
      :title="editingSpecTemplate ? '编辑模板' : '添加模板'"
      width="90%"
    >
      <el-form :model="specForm" label-width="80px">
        <el-form-item label="模板名称" required>
          <el-input v-model="specForm.name" placeholder="如：奶茶、咖啡、甜品" :disabled="!!editingSpecTemplate" />
        </el-form-item>
        <el-divider content-position="left">规格列表</el-divider>
        <div v-for="(spec, index) in specForm.specs" :key="index" class="spec-edit-item-mobile">
          <el-row :gutter="8">
            <el-col :span="8">
              <el-input v-model="spec.specName" placeholder="规格名称" size="small" />
            </el-col>
            <el-col :span="14">
              <el-input v-model="spec.specValues" placeholder="用逗号分隔" size="small" />
            </el-col>
            <el-col :span="2">
              <el-button type="danger" link size="small" @click="removeSpecFromTemplate(index)">删除</el-button>
            </el-col>
          </el-row>
        </div>
        <el-button type="primary" link @click="addSpecToTemplate">+ 添加规格</el-button>
      </el-form>
      <template #footer>
        <el-button @click="specDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="specSubmitting" @click="submitTemplate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 用户管理 -->
    <div class="user-section" v-if="currentTab === 'users'">
      <div class="section-header">
        <span class="section-title">用户管理</span>
      </div>
      <div class="user-list" v-loading="userLoading">
        <div v-for="user in users" :key="user.id" class="user-item">
          <div class="user-avatar">{{ user.nickname?.charAt(0) || user.username?.charAt(0) || '?' }}</div>
          <div class="user-info">
            <div class="user-name">{{ user.nickname || user.username }}</div>
            <div class="user-email">{{ user.email }}</div>
          </div>
          <div class="user-status">
            <el-tag :type="user.status === 1 ? 'success' : 'danger'" size="small">
              {{ user.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </div>
          <div class="user-actions">
            <el-button
              v-if="user.status === 1"
              type="danger"
              size="small"
              round
              @click="toggleUserStatus(user)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              round
              @click="toggleUserStatus(user)"
            >
              启用
            </el-button>
          </div>
        </div>
        <el-empty v-if="!userLoading && users.length === 0" description="暂无用户" />
      </div>
    </div>

    <!-- 底部占位 -->
    <div class="bottom-space"></div>

    <!-- 商品编辑弹窗 -->
    <el-dialog
      v-model="productDialogVisible"
      :title="editingProduct ? '编辑商品' : '新增商品'"
      width="90%"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="productForm" :rules="productRules" label-width="70px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="productForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品图片" prop="imageUrl">
          <el-input v-model="productForm.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>

        <!-- 商品规格配置 -->
        <el-divider content-position="left">商品规格</el-divider>

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

        <!-- 规格列表 -->
        <div v-for="(spec, index) in productForm.specs" :key="index" class="spec-item-mobile">
          <el-form-item :label="'规格' + (index + 1)">
            <div class="spec-row">
              <el-input
                v-model="spec.specName"
                placeholder="规格名称（如：甜度）"
                style="flex: 1; margin-right: 8px;"
              />
              <el-input
                v-model="spec.specValuesText"
                placeholder="用逗号分隔"
                style="flex: 1; margin-right: 8px;"
                @blur="parseSpecValues(spec)"
              />
              <el-button type="danger" size="small" @click="removeSpec(index)">删</el-button>
            </div>
          </el-form-item>
        </div>

        <el-button type="primary" link @click="addSpec" style="margin-bottom: 15px;">
          + 添加规格
        </el-button>
      </el-form>

      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitProduct">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 分类管理弹窗 -->
    <el-dialog v-model="categoryDialogVisible" :title="editingCategory ? '编辑分类' : '新增分类'" width="90%">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="categorySubmitting" @click="submitCategory">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 底部导航栏 -->
    <MobileTabbar />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import api from '../api'
import MobileTabbar from '../components/MobileTabbar.vue'

const router = useRouter()
const userStore = useUserStore()

// 跳转到商品管理
const goToProducts = () => {
  currentTab.value = 'products'
}

// 跳转到待处理订单
const goToPendingOrders = () => {
  currentTab.value = 'orders'
  orderStatus.value = 0
  fetchOrders()
}

// 跳转到已完成订单
const goToHandledOrders = () => {
  currentTab.value = 'orders'
  orderStatus.value = 1
  fetchOrders()
}

// 跳转到用户管理
const goToUsers = () => {
  currentTab.value = 'users'
}

// 分类管理
const categoryList = ref([])
const categoryLoading = ref(false)
const categoryDialogVisible = ref(false)
const editingCategory = ref(null)
const categorySubmitting = ref(false)

const categoryForm = reactive({
  name: '',
  sort: 0
})

const fetchCategoryList = async () => {
  categoryLoading.value = true
  try {
    const res = await api.get('/categories')
    categoryList.value = res || []
  } catch (error) {
    ElMessage.error('获取分类失败')
  } finally {
    categoryLoading.value = false
  }
}

const openCategoryDialog = (cat = null) => {
  editingCategory.value = cat ? cat.id : null
  if (cat) {
    categoryForm.name = cat.name
    categoryForm.sort = cat.sort || 0
  } else {
    categoryForm.name = ''
    categoryForm.sort = 0
  }
  categoryDialogVisible.value = true
}

const submitCategory = async () => {
  if (!categoryForm.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  categorySubmitting.value = true
  try {
    if (editingCategory.value) {
      await api.put(`/categories/${editingCategory.value}`, {
        name: categoryForm.name,
        sort: categoryForm.sort
      })
      ElMessage.success('编辑成功')
    } else {
      await api.post('/categories', {
        name: categoryForm.name,
        sort: categoryForm.sort
      })
      ElMessage.success('添加成功')
    }
    categoryDialogVisible.value = false
    fetchCategoryList()
  } catch (error) {
    ElMessage.error(editingCategory.value ? '编辑失败' : '添加失败')
  } finally {
    categorySubmitting.value = false
  }
}

const deleteCategory = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', { type: 'warning' })
    await api.delete(`/categories/${id}`)
    ElMessage.success('删除成功')
    fetchCategoryList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const loading = ref(false)
const products = ref([])
const categories = ref([])
const productDialogVisible = ref(false)
const editingProduct = ref(null)
const submitting = ref(false)
const formRef = ref()

// 订单管理
const currentTab = ref('overview')
const orders = ref([])
const orderLoading = ref(false)
const orderStatus = ref(null)

// 数据统计
const stats = ref({
  productCount: 0,
  pendingOrderCount: 0,
  handledOrderCount: 0,
  userCount: 0
})

// 规格模板管理
const specLoading = ref(false)
const specTemplates = ref([]) // 平铺数据
const specDialogVisible = ref(false)
const editingSpecTemplate = ref(null) // 编辑的模板名称
const specSubmitting = ref(false)
const activeSpecCollapse = ref([]) // 展开的规格模板

// 分组后的规格模板
const groupedSpecTemplates = computed(() => {
  const groups = {}
  specTemplates.value.forEach(t => {
    if (!groups[t.name]) {
      groups[t.name] = {
        name: t.name,
        specs: []
      }
    }
    groups[t.name].specs.push({
      id: t.id,
      specName: t.specName,
      specValues: t.specValues
    })
  })
  return Object.values(groups)
})

// 商品规格相关
const selectedTemplate = ref('')

const specForm = reactive({
  name: '',
  specs: [
    { specName: '', specValues: '' }
  ]
})

const productForm = reactive({
  name: '',
  categoryId: null,
  price: 0,
  imageUrl: '',
  description: '',
  specs: []
})

const productRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await api.get('/products', { params: { page: 1, size: 50 } })
    products.value = res.records
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

const fetchOrders = async () => {
  orderLoading.value = true
  try {
    const res = await api.get('/orders', {
      params: {
        page: 1,
        size: 50,
        status: orderStatus.value
      }
    })
    orders.value = res.records
  } catch (error) {
    ElMessage.error('获取订单失败')
  } finally {
    orderLoading.value = false
  }
}

const changeOrderStatus = (status) => {
  orderStatus.value = status
  fetchOrders()
}

const fetchStats = async () => {
  try {
    // 分别调用每个API，这样一个失败不会影响其他
    let productCount = 0
    let pendingOrderCount = 0
    let handledOrderCount = 0
    let userCount = 0

    try {
      const productsRes = await api.get('/products', { params: { page: 1, size: 1 } })
      productCount = productsRes?.total || 0
    } catch (e) {
      console.error('获取商品数量失败:', e)
    }

    try {
      const ordersRes = await api.get('/orders', { params: { page: 1, size: 1, status: 0 } })
      pendingOrderCount = ordersRes?.total || 0
    } catch (e) {
      console.error('获取待处理订单失败:', e)
    }

    try {
      const allOrdersRes = await api.get('/orders', { params: { page: 1, size: 100 } })
      const allOrders = allOrdersRes?.records || []
      handledOrderCount = allOrders.filter(o => o.status === 1).length
    } catch (e) {
      console.error('获取所有订单失败:', e)
    }

    try {
      const usersRes = await api.get('/users', { params: { page: 1, size: 1 } })
      userCount = usersRes?.total || 0
    } catch (e) {
      console.error('获取用户数量失败:', e)
    }

    stats.value = {
      productCount,
      pendingOrderCount,
      handledOrderCount,
      userCount
    }
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

const fetchSpecTemplates = async () => {
  specLoading.value = true
  try {
    const res = await api.get('/spec-templates/all')
    specTemplates.value = res || []
  } catch (error) {
    console.error('获取规格模板失败:', error)
  } finally {
    specLoading.value = false
  }
}

const openTemplateDialog = (template = null) => {
  editingSpecTemplate.value = template
  if (template) {
    specForm.name = template.name
    specForm.specs = template.specs.map(s => ({
      specName: s.specName,
      specValues: s.specValues
    }))
  } else {
    specForm.name = ''
    specForm.specs = [{ specName: '', specValues: '' }]
  }
  specDialogVisible.value = true
}

const addSpecToTemplate = () => {
  specForm.specs.push({ specName: '', specValues: '' })
}

const removeSpecFromTemplate = (index) => {
  specForm.specs.splice(index, 1)
}

const submitTemplate = async () => {
  if (!specForm.name.trim()) {
    ElMessage.warning('请输入模板名称')
    return
  }

  const validSpecs = specForm.specs.filter(s => s.specName.trim() && s.specValues.trim())
  if (validSpecs.length === 0) {
    ElMessage.warning('请至少添加一个规格')
    return
  }

  specSubmitting.value = true
  try {
    // 如果是编辑模式，先删除原有规格，再添加新规格
    if (editingSpecTemplate.value) {
      const toDelete = specTemplates.value.filter(t => t.name === editingSpecTemplate.value.name)
      for (const spec of toDelete) {
        await api.delete(`/spec-templates/${spec.id}`)
      }
    }

    // 添加新规格
    for (const spec of validSpecs) {
      await api.post('/spec-templates', {
        name: specForm.name,
        specName: spec.specName,
        specValues: spec.specValues,
        sort: 0
      })
    }

    ElMessage.success(editingSpecTemplate.value ? '更新成功' : '创建成功')
    specDialogVisible.value = false
    fetchSpecTemplates()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    specSubmitting.value = false
  }
}

const deleteTemplate = async (name) => {
  try {
    await ElMessageBox.confirm(`确定要删除模板"${name}"及其所有规格吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    specLoading.value = true
    const toDelete = specTemplates.value.filter(t => t.name === name)
    for (const spec of toDelete) {
      await api.delete(`/spec-templates/${spec.id}`)
    }
    ElMessage.success('删除成功')
    fetchSpecTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  } finally {
    specLoading.value = false
  }
}

// 用户管理
const userLoading = ref(false)
const users = ref([])

const fetchUsers = async () => {
  userLoading.value = true
  try {
    const res = await api.get('/users')
    users.value = res.data?.records || res.records || res || []
  } catch (error) {
    console.error('获取用户失败:', error)
  } finally {
    userLoading.value = false
  }
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

const handleOrder = async (orderId) => {
  try {
    await api.put(`/orders/${orderId}/handle`)
    ElMessage.success('处理成功')
    fetchOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '处理失败')
  }
}

const openProductDialog = async (product = null) => {
  editingProduct.value = product
  // 先获取规格模板列表
  await fetchSpecTemplates()

  if (product) {
    productForm.name = product.name
    productForm.categoryId = product.categoryId
    productForm.price = product.price
    productForm.imageUrl = product.imageUrl || ''
    productForm.description = product.description || ''

    // 获取商品规格
    try {
      const res = await api.get(`/products/${product.id}`)
      productForm.specs = (res.specs || []).map(s => ({
        specName: s.specName,
        specValues: s.specValues,
        specValuesText: s.specValues?.join(',') || ''
      }))
    } catch (error) {
      productForm.specs = []
    }
  } else {
    productForm.name = ''
    productForm.categoryId = null
    productForm.price = 0
    productForm.imageUrl = ''
    productForm.description = ''
    productForm.specs = []
  }
  selectedTemplate.value = ''
  productDialogVisible.value = true
}

// 应用预设模板
const applyTemplate = (templateName) => {
  if (!templateName) {
    selectedTemplate.value = ''
    return
  }

  const template = specTemplates.value.find(t => t.name === templateName)
  if (template && template.specs) {
    productForm.specs = template.specs.map(s => ({
      specName: s.specName,
      specValues: s.specValues.split(',').map(v => v.trim()),
      specValuesText: s.specValues
    }))
  }
}

const addSpec = () => {
  productForm.specs.push({
    specName: '',
    specValues: [],
    specValuesText: ''
  })
}

const removeSpec = (index) => {
  productForm.specs.splice(index, 1)
}

const parseSpecValues = (spec) => {
  spec.specValues = spec.specValuesText.split(',').map(v => v.trim()).filter(v => v)
}

const submitProduct = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 处理规格数据
  const specs = productForm.specs
    .filter(s => s.specName && s.specValues?.length > 0)
    .map(s => ({
      specName: s.specName,
      specValues: s.specValues
    }))

  const data = {
    name: productForm.name,
    categoryId: productForm.categoryId,
    price: productForm.price,
    imageUrl: productForm.imageUrl,
    description: productForm.description,
    specs
  }

  submitting.value = true
  try {
    if (editingProduct.value) {
      await api.put(`/products/${editingProduct.value.id}`, data)
      ElMessage.success('更新成功')
    } else {
      await api.post('/products', data)
      ElMessage.success('添加成功')
    }
    productDialogVisible.value = false
    fetchProducts()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const deleteProduct = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', { type: 'warning' })
    await api.delete(`/products/${id}`)
    ElMessage.success('删除成功')
    fetchProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchStats()
  fetchProducts()
  fetchCategories()
  fetchOrders()
})

watch(currentTab, (newTab) => {
  if (newTab === 'orders') {
    fetchOrders()
  } else if (newTab === 'overview') {
    fetchStats()
  } else if (newTab === 'specs') {
    fetchSpecTemplates()
  } else if (newTab === 'users') {
    fetchUsers()
  } else if (newTab === 'categories') {
    fetchCategoryList()
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

/* 商品列表 */
.product-section {
  margin: 12px;
  background: white;
  border-radius: 12px;
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: bold;
  color: #333;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #fafafa;
  border-radius: 8px;
}

.product-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.product-price {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: bold;
}

.product-actions {
  display: flex;
  flex-direction: row;
  gap: 8px;
}

/* 订单列表 */
.order-section {
  margin: 12px;
  background: white;
  border-radius: 12px;
  padding: 16px;
}

.status-tabs {
  display: flex;
  gap: 8px;
  margin: 12px 0;
}

.status-tab {
  flex: 1;
  text-align: center;
  padding: 8px;
  border-radius: 16px;
  font-size: 13px;
  color: #666;
  background: #f5f5f5;
  cursor: pointer;
}

.status-tab.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-item {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.order-no {
  font-size: 12px;
  color: #999;
}

.order-status {
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 8px;
}

.order-status.pending {
  color: #ff9800;
  background: #fff3e0;
}

.order-status.processed {
  color: #4caf50;
  background: #e8f5e9;
}

.order-product {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.order-product-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
}

.order-product-info {
  flex: 1;
}

.order-product-name {
  font-size: 13px;
  color: #333;
  margin-bottom: 4px;
}

.order-product-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-product-price {
  font-size: 14px;
  color: #ff4d4f;
  font-weight: bold;
}

.order-product-quantity {
  font-size: 13px;
  color: #666;
}

.order-user {
  padding: 8px 12px;
  font-size: 12px;
  color: #666;
  border-top: 1px dashed #eee;
}

.user-label {
  margin-right: 4px;
}

.user-name {
  color: #333;
  font-weight: 500;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
}

/* 数据概览 */
.overview-section {
  margin: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:active {
  transform: scale(0.98);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

/* 规格模板 */
.spec-section {
  margin: 12px;
  background: white;
  border-radius: 12px;
  padding: 16px;
}

.spec-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.spec-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.spec-info {
  flex: 1;
}

.spec-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.spec-detail {
  font-size: 12px;
  color: #999;
}

.spec-actions {
  display: flex;
  gap: 8px;
}

/* 分组模板样式 */
.template-header-mobile {
  display: flex;
  align-items: center;
  width: 100%;
}

.template-header-mobile .template-name {
  font-size: 15px;
  font-weight: bold;
  color: #333;
}

.template-header-mobile .template-count {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}

.template-specs {
  padding: 8px 0;
}

.spec-item-mobile {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.spec-item-mobile:last-of-type {
  border-bottom: none;
}

.template-specs .spec-info {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.template-specs .spec-name {
  font-weight: 500;
  min-width: 50px;
  margin-bottom: 0;
}

.template-specs .spec-values {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.template-actions-mobile {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.spec-edit-item-mobile {
  margin-bottom: 8px;
}

/* 用户管理 */
.user-section {
  margin: 12px;
  background: white;
  border-radius: 12px;
  padding: 16px;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
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

.user-info {
  flex: 1;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
}

.user-email {
  font-size: 12px;
  color: #999;
}

.user-status {
  margin-right: 8px;
}

.user-actions {
  display: flex;
}

.bottom-space {
  height: 70px;
}

/* 移动端商品规格样式 */
.spec-item-mobile {
  margin-bottom: 0;
}

.spec-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.el-divider__text) {
  font-size: 14px;
  font-weight: bold;
}
</style>
