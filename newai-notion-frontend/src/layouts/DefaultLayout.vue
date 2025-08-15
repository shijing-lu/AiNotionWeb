<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside 
      class="sidebar" 
      :class="{ 'sidebar--collapsed': appStore.sidebarCollapsed }"
    >
      <div class="sidebar__header">
        <div class="logo">
          <img src="/logo.svg" alt="AI智能笔记" class="logo__image" />
          <span v-show="!appStore?.sidebarCollapsed" class="logo__text">
            AI智能笔记
          </span>
        </div>
        <el-button
          type="text"
          class="sidebar__toggle"
          @click="appStore.toggleSidebar()"
        >
          <el-icon>
            <Fold v-if="!appStore?.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
        </el-button>
      </div>
      
      <nav class="sidebar__nav">
        <el-menu
          :default-active="$route.path"
          :collapse="appStore.sidebarCollapsed"
          :collapse-transition="false"
          router
          class="sidebar__menu"
        >
          <el-menu-item index="/app/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>仪表板</span>
          </el-menu-item>
          
          <el-menu-item index="/documents">
            <el-icon><Document /></el-icon>
            <span>文档</span>
          </el-menu-item>
          
          <el-menu-item index="/folders">
            <el-icon><Folder /></el-icon>
            <span>文件夹</span>
          </el-menu-item>
          
          <el-sub-menu index="/ai">
            <template #title>
              <el-icon><MagicStick /></el-icon>
              <span>AI助手</span>
            </template>
            <el-menu-item index="/ai/search">
              <el-icon><Search /></el-icon>
              <span>AI搜索</span>
            </el-menu-item>
            <el-menu-item index="/ai/summarize">
              <el-icon><Reading /></el-icon>
              <span>AI总结</span>
            </el-menu-item>
            <el-menu-item index="/ai/translate">
              <el-icon><Switch /></el-icon>
              <span>AI翻译</span>
            </el-menu-item>
            <el-menu-item index="/ai/check">
              <el-icon><CircleCheck /></el-icon>
              <span>AI检查</span>
            </el-menu-item>
            <el-menu-item index="/ai/chat">
              <el-icon><ChatDotRound /></el-icon>
              <span>AI对话</span>
            </el-menu-item>
            <el-menu-item index="/ai/writing">
              <el-icon><Edit /></el-icon>
              <span>AI写作</span>
            </el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/search">
            <el-icon><Search /></el-icon>
            <span>搜索</span>
          </el-menu-item>
          
          <el-menu-item index="/favorites">
            <el-icon><Star /></el-icon>
            <span>收藏</span>
          </el-menu-item>
          
          <el-menu-item index="/recent">
            <el-icon><Clock /></el-icon>
            <span>最近访问</span>
          </el-menu-item>
          
          <el-menu-item index="/trash">
            <el-icon><Delete /></el-icon>
            <span>回收站</span>
          </el-menu-item>
        </el-menu>
      </nav>
      
      <div class="sidebar__footer">
        <el-menu
          :collapse="appStore.sidebarCollapsed"
          :collapse-transition="false"
          class="sidebar__menu"
        >
          <el-menu-item index="/settings">
            <el-icon><Setting /></el-icon>
            <span>设置</span>
          </el-menu-item>
        </el-menu>
      </div>
    </aside>
    
    <!-- 主内容区域 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <header class="header">
        <div class="header__left">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item
              v-for="item in breadcrumbs"
              :key="item.path"
              :to="item.path"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header__center">
          <el-input
            v-model="searchQuery"
            placeholder="搜索文档、文件夹..."
            class="search-input"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <div class="header__right">
          <!-- 通知 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <el-button type="text" @click="showNotifications">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>
          
          <!-- 主题切换 -->
          <el-button type="text" @click="toggleTheme">
            <el-icon>
              <Sunny v-if="appStore.theme === 'dark'" />
              <Moon v-else />
            </el-icon>
          </el-button>
          
          <!-- 用户菜单 -->
          <el-dropdown @command="handleUserCommand">
            <div class="user-avatar">
              <el-avatar
                :size="32"
                :src="authStore.user?.avatar"
                :style="{ backgroundColor: getAvatarColor(authStore.user?.username || '') }"
              >
                {{ getNameInitials(authStore.user?.username || '') }}
              </el-avatar>
              <span v-show="!appStore.sidebarCollapsed" class="username">
                {{ authStore.user?.username }}
              </span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
    
    <!-- AI助手面板 -->
    <AIPanel v-if="appStore.showAIPanel" />
    
    <!-- 搜索面板 -->
    <SearchPanel v-if="appStore.showSearchPanel" />
    
    <!-- 设置面板 -->
    <SettingsPanel v-if="appStore.showSettingsPanel" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Fold, Expand, Odometer, Document, Folder, MagicStick, Search,
  Reading, Switch, CircleCheck, ChatDotRound, Edit, Star, Clock,
  Delete, Setting, Bell, Sunny, Moon, User, SwitchButton
} from '@element-plus/icons-vue'
import { useAuthStore, useAppStore } from '@/stores'
import { getAvatarColor, getNameInitials } from '@/utils'
import { useConfirm, useNotification } from '@/composables'
import AIPanel from '@/components/panels/AIPanel.vue'
import SearchPanel from '@/components/panels/SearchPanel.vue'
import SettingsPanel from '@/components/panels/SettingsPanel.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()
const { confirm } = useConfirm()
const { success } = useNotification()

// 搜索
const searchQuery = ref('')

// 通知数量
const unreadCount = computed(() => {
  // TODO: 从store获取未读通知数量
  return 0
})

// 面包屑导航
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    title: item.meta?.title as string,
    path: item.path
  }))
})

// 搜索处理
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({
      name: 'Search',
      query: { q: searchQuery.value }
    })
  }
}

// 显示通知
const showNotifications = () => {
  // TODO: 显示通知面板
  console.log('显示通知')
}

// 主题切换
const toggleTheme = () => {
  appStore.setTheme(appStore.theme === 'light' ? 'dark' : 'light')
}

// 用户菜单处理
const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/settings/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      const confirmed = await confirm('确定要退出登录吗？')
      if (confirmed) {
        await authStore.logout()
        success('已退出登录')
        router.push('/login')
      }
      break
  }
}

// 监听路由变化，清空搜索
watch(route, () => {
  if (route.name !== 'Search') {
    searchQuery.value = ''
  }
})
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  background-color: var(--el-bg-color);
}

/* 侧边栏样式 */
.sidebar {
  width: 240px;
  background-color: var(--el-bg-color-page);
  border-right: 1px solid var(--el-border-color-light);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  z-index: 1000;
}

.sidebar--collapsed {
  width: 64px;
}

.sidebar__header {
  height: 60px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo__image {
  width: 32px;
  height: 32px;
}

.logo__text {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.sidebar__toggle {
  padding: 8px;
}

.sidebar__nav {
  flex: 1;
  overflow-y: auto;
}

.sidebar__menu {
  border: none;
  background-color: transparent;
}

.sidebar__footer {
  border-top: 1px solid var(--el-border-color-lighter);
}

/* 主内容区域样式 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶部导航栏样式 */
.header {
  height: 60px;
  padding: 0 24px;
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.header__left {
  flex: 0 0 auto;
}

.header__center {
  flex: 1;
  max-width: 400px;
}

.header__right {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.breadcrumb {
  font-size: 14px;
}

.search-input {
  width: 100%;
}

.notification-badge {
  display: flex;
  align-items: center;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-avatar:hover {
  background-color: var(--el-fill-color-light);
}

.username {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

/* 页面内容样式 */
.content {
  flex: 1;
  overflow: auto;
  background-color: var(--el-bg-color);
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 1001;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .sidebar--collapsed {
    transform: translateX(0);
    width: 240px;
  }
  
  .header__center {
    display: none;
  }
  
  .header {
    padding: 0 16px;
  }
}

@media (max-width: 480px) {
  .header__left {
    display: none;
  }
  
  .username {
    display: none;
  }
}
</style>