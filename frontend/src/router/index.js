import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/m',
    component: () => import('../views/MobileLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '',
    children: [
      {
        path: '',
        name: 'MobileHome',
        component: () => import('../views/Mobile.vue')
      },
      {
        path: 'orders',
        name: 'MobileOrders',
        component: () => import('../views/MobileOrders.vue')
      },
      {
        path: 'addresses',
        name: 'MobileAddresses',
        component: () => import('../views/MobileAddresses.vue')
      },
      {
        path: 'profile',
        name: 'MobileProfile',
        component: () => import('../views/MobileProfile.vue')
      },
      {
        path: 'admin',
        name: 'MobileAdmin',
        component: () => import('../views/MobileAdmin.vue'),
        meta: { requiresAdmin: true }
      }
    ]
  },
  {
    path: '/',
    component: () => import('../views/UserLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '',
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue')
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('../views/Products.vue')
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('../views/Orders.vue')
      },
      {
        path: 'addresses',
        name: 'Addresses',
        component: () => import('../views/Addresses.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('../views/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    redirect: '',
    children: [
      {
        path: '',
        name: 'Admin',
        component: () => import('../views/Admin.vue')
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('../views/AdminProducts.vue')
      },
      {
        path: 'spec-templates',
        name: 'AdminSpecTemplates',
        component: () => import('../views/AdminSpecTemplates.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/AdminOrders.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 检测是否为移动设备
const isMobile = () => {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')

  // 如果是首页且是移动设备，跳转到手机端首页
  if (to.path === '/' && isMobile()) {
    next('/m')
    return
  }

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.requiresAdmin && userStore.user?.role !== 'ADMIN') {
    // 移动端管理员页面需要管理员权限
    if (to.path.startsWith('/m/admin')) {
      next('/m')
    } else {
      next('/')
    }
  } else {
    next()
  }
})

export default router
