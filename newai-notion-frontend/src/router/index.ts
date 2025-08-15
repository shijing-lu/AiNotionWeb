import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores'
import { DefaultLayout, AuthLayout, MinimalLayout } from '@/layouts'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/app/dashboard'
  },
   {
      path: '/test-api',
      name: 'TestApi',
      component: () => import('@/views/TestApiView.vue')
    },
    {
      path: '/api-test',
      name: 'ApiTest',
      component: () => import('../views/ApiTestView.vue'),
      meta: {
        title: 'API测试',
        requiresAuth: false
      }
    },
  {
    path: '/auth',
    component: AuthLayout,
    redirect: '/auth/login',
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/views/auth/LoginView.vue'),
        meta: {
          title: '登录',
          requiresAuth: false,
          hideForAuth: true
        }
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@/views/auth/RegisterView.vue'),
        meta: {
          title: '注册',
          requiresAuth: false,
          hideForAuth: true
        }
      },
      {
        path: 'forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/views/auth/ForgotPasswordView.vue'),
        meta: {
          title: '忘记密码',
          requiresAuth: false,
          hideForAuth: true
        }
      },
      {
        path: 'reset-password',
        name: 'ResetPassword',
        component: () => import('@/views/auth/ResetPasswordView.vue'),
        meta: {
          title: '重置密码',
          requiresAuth: false,
          hideForAuth: true
        }
      }
    ]
  },
  {
    path: '/app',
    component: DefaultLayout,
    redirect: '/app/dashboard',
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: {
          title: '仪表板',
          icon: 'Dashboard'
        }
      },
      {
        path: 'documents',
        name: 'Documents',
        component: () => import('@/views/documents/DocumentListView.vue'),
        meta: {
          title: '文档',
          icon: 'Document'
        }
      },
      {
        path: 'documents/:id',
        name: 'DocumentDetail',
        component: () => import('../views/documents/DocumentDetailView.vue'),
        meta: {
          title: '文档详情',
          hideInMenu: true
        }
      },
      {
        path: 'documents/:id/edit',
        name: 'DocumentEdit',
        component: () => import('../views/documents/DocumentEditView.vue'),
        meta: {
          title: '编辑文档',
          hideInMenu: true
        }
      },
      {
        path: 'articles',
        name: 'Articles',
        component: () => import('../views/articles/ArticleListView.vue'),
        meta: {
          title: '文章管理',
          icon: 'Edit'
        }
      },
      {
        path: 'articles/create',
        name: 'ArticleCreate',
        component: () => import('../views/articles/ArticleCreateView.vue'),
        meta: {
          title: '创建文章',
          hideInMenu: true
        }
      },
      {
        path: 'articles/:id',
        name: 'ArticleDetail',
        component: () => import('../views/articles/ArticleDetailView.vue'),
        meta: {
          title: '文章详情',
          hideInMenu: true
        }
      },
      {
        path: 'articles/:id/edit',
        name: 'ArticleEdit',
        component: () => import('../views/articles/ArticleEditView.vue'),
        meta: {
          title: '编辑文章',
          hideInMenu: true
        }
      },
      {
        path: 'folders',
        name: 'Folders',
        component: () => import('../views/folders/FolderListView.vue'),
        meta: {
          title: '文件夹',
          icon: 'Folder'
        }
      },
      {
        path: 'folders/:id',
        name: 'FolderDetail',
        component: () => import('../views/folders/FolderDetailView.vue'),
        meta: {
          title: '文件夹详情',
          hideInMenu: true
        }
      },
      {
        path: 'ai-assistant',
        name: 'AIAssistant',
        component: () => import('../views/ai/AIAssistantView.vue'),
        meta: {
          title: 'AI助手',
          icon: 'Robot'
        }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('../views/search/SearchView.vue'),
        meta: {
          title: '搜索',
          icon: 'Search'
        }
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('../views/favorites/FavoritesView.vue'),
        meta: {
          title: '收藏',
          icon: 'Star'
        }
      },
      {
        path: 'recent',
        name: 'Recent',
        component: () => import('../views/recent/RecentView.vue'),
        meta: {
          title: '最近访问',
          icon: 'Clock'
        }
      },
      {
        path: 'trash',
        name: 'Trash',
        component: () => import('../views/trash/TrashView.vue'),
        meta: {
          title: '回收站',
          icon: 'Delete'
        }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/settings/SettingsView.vue'),
        meta: {
          title: '设置',
          icon: 'Setting'
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/ProfileView.vue'),
        meta: {
          title: '个人资料',
          hideInMenu: true
        }
      }
    ]
  },
  {
    path: '/error',
    component: MinimalLayout,
    children: [
      {
        path: '404',
        name: 'NotFound',
        component: () => import('../views/error/NotFoundView.vue'),
        meta: {
          title: '页面未找到'
        }
      },
      {
        path: '403',
        name: 'Forbidden',
        component: () => import('../views/error/ForbiddenView.vue'),
        meta: {
          title: '访问被拒绝'
        }
      },
      {
        path: '500',
        name: 'ServerError',
        component: () => import('../views/error/ServerErrorView.vue'),
        meta: {
          title: '服务器错误'
        }
      }
    ]
  },
  {
    path: '/maintenance',
    name: 'Maintenance',
    component: () => import('../views/maintenance/MaintenanceView.vue'),
    meta: {
      title: '系统维护',
      layout: 'minimal'
    }
  },
  // 重定向旧路由
  {
    path: '/dashboard',
    redirect: '/app/dashboard'
  },
  {
    path: '/documents',
    redirect: '/app/documents'
  },
  {
    path: '/login',
    redirect: '/auth/login'
  },
  {
    path: '/register',
    redirect: '/auth/register'
  },
  // 404 页面 - 必须放在最后
  {
    path: '/:pathMatch(.*)*',
    redirect: '/error/404'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 如果有保存的位置，返回到该位置
    if (savedPosition) {
      return savedPosition
    }
    // 如果有锚点，滚动到锚点
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth'
      }
    }
    // 否则滚动到顶部
    return { top: 0 }
  }
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - NewAI Notion`
  } else {
    document.title = 'NewAI Notion'
  }
  
  // 检查认证状态
  if (!authStore.initialized) {
    await authStore.initializeAuth()
  }
  
  const isAuthenticated = authStore.isAuthenticated
  const requiresAuth = to.meta.requiresAuth !== false
  const hideForAuth = to.meta.hideForAuth === true
  
  // 如果页面需要认证但用户未登录
  if (requiresAuth && !isAuthenticated) {
    next({
      name: 'Login',
      query: { redirect: to.fullPath }
    })
    return
  }
  
  // 如果用户已登录但访问认证页面
  if (isAuthenticated && hideForAuth) {
    next({ name: 'Dashboard' })
    return
  }
  
  next()
})

// 全局后置钩子
router.afterEach((to, from) => {
  // 可以在这里添加页面访问统计等逻辑
  console.log(`Navigation: ${from.path} -> ${to.path}`)
})

// 路由错误处理
router.onError((error) => {
  console.error('Router error:', error)
  // 可以在这里添加错误上报逻辑
})

export default router

// 导出路由相关类型和工具函数
export type { RouteRecordRaw }

/**
 * 获取面包屑导航
 */
export const getBreadcrumbs = (route: any) => {
  const breadcrumbs = []
  const matched = route.matched.filter((item: any) => item.meta && item.meta.title)
  
  for (const match of matched) {
    breadcrumbs.push({
      title: match.meta.title,
      path: match.path,
      name: match.name
    })
  }
  
  return breadcrumbs
}

/**
 * 获取菜单项
 */
export const getMenuItems = () => {
  const menuRoutes = routes.find(route => route.path === '/app')?.children || []
  
  return menuRoutes
    .filter(route => !route.meta?.hideInMenu)
    .map(route => ({
      name: route.name,
      path: route.path,
      title: route.meta?.title,
      icon: route.meta?.icon
    }))
}
