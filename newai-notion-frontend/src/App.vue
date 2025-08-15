<template>
  <div id="app" :class="appClasses">
    <!-- 全局加载遮罩 -->
    <div v-if="appStore.isLoading" class="global-loading">
      <LoadingSpinner size="large" text="加载中..." />
    </div>
    
    <!-- 路由视图 -->
    <router-view v-slot="{ Component, route }">
      <transition
        :name="getTransitionName(route)"
        mode="out-in"
        appear
      >
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>
    
    <!-- 全局通知容器 -->
    <div class="notification-container">
      <transition-group name="notification" tag="div">
        <div
          v-for="notification in appStore.notifications"
          :key="notification.id"
          class="notification-item"
          :class="`notification-item--${notification.type}`"
        >
          <div class="notification-content">
            <div class="notification-icon">
              <el-icon v-if="notification.type === 'success'"><SuccessFilled /></el-icon>
              <el-icon v-else-if="notification.type === 'warning'"><WarningFilled /></el-icon>
              <el-icon v-else-if="notification.type === 'error'"><CircleCloseFilled /></el-icon>
              <el-icon v-else><InfoFilled /></el-icon>
            </div>
            <div class="notification-text">
              <div class="notification-title">{{ notification.title }}</div>
              <div v-if="notification.message" class="notification-message">
                {{ notification.message }}
              </div>
            </div>
          </div>
          <el-button
            text
            :icon="Close"
            class="notification-close"
            @click="appStore.removeNotification(notification.id)"
          />
        </div>
      </transition-group>
    </div>
    
    <!-- 网络状态提示 -->
    <transition name="slide-up">
      <div v-if="!appStore.isOnline" class="offline-banner">
        <el-icon><Connection /></el-icon>
        <span>网络连接已断开，请检查您的网络设置</span>
      </div>
    </transition>
    
    <!-- 全局确认对话框 -->
    <ConfirmDialog
      v-model="confirmDialog.visible"
      :title="confirmDialog.title"
      :message="confirmDialog.message"
      :type="confirmDialog.type"
      :loading="confirmDialog.loading"
      @confirm="handleConfirm"
      @cancel="handleCancel"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  SuccessFilled,
  WarningFilled,
  CircleCloseFilled,
  InfoFilled,
  Close,
  Connection
} from '@element-plus/icons-vue'
import { useAppStore, useAuthStore } from '@/stores'
import { LoadingSpinner, ConfirmDialog } from '@/components/common'

const route = useRoute()
const appStore = useAppStore()
const authStore = useAuthStore()

// 确认对话框状态
const confirmDialog = ref({
  visible: false,
  title: '',
  message: '',
  type: 'warning' as 'warning' | 'error' | 'info' | 'success',
  loading: false,
  resolve: null as ((value: boolean) => void) | null
})

// 应用样式类
const appClasses = computed(() => {
  return {
    'app--dark': appStore.theme === 'dark',
    'app--mobile': appStore.isMobile,
    'app--loading': appStore.isLoading
  }
})

/**
 * 获取路由过渡动画名称
 */
const getTransitionName = (route: any) => {
  // 根据路由层级和类型决定过渡动画
  if (route.meta?.transition) {
    return route.meta.transition
  }
  
  // 认证页面使用淡入淡出
  if (route.path.startsWith('/auth') || route.path.startsWith('/login') || route.path.startsWith('/register')) {
    return 'fade'
  }
  
  // 其他页面使用滑动
  return 'slide'
}

/**
 * 处理确认对话框确认
 */
const handleConfirm = () => {
  if (confirmDialog.value.resolve) {
    confirmDialog.value.resolve(true)
  }
  confirmDialog.value.visible = false
}

/**
 * 处理确认对话框取消
 */
const handleCancel = () => {
  if (confirmDialog.value.resolve) {
    confirmDialog.value.resolve(false)
  }
  confirmDialog.value.visible = false
}

/**
 * 全局确认函数
 */
const globalConfirm = (title: string, message: string, type = 'warning') => {
  return new Promise<boolean>((resolve) => {
    confirmDialog.value = {
      visible: true,
      title,
      message,
      type: type as any,
      loading: false,
      resolve
    }
  })
}

/**
 * 键盘事件处理
 */
const handleKeydown = (event: KeyboardEvent) => {
  const { ctrlKey, metaKey, shiftKey, key } = event
  const isCtrl = ctrlKey || metaKey
  
  // 阻止某些默认行为
  if (isCtrl) {
    switch (key.toLowerCase()) {
      case '\\':
        event.preventDefault()
        appStore.toggleSidebar()
        break
      case 'p':
        if (shiftKey) {
          event.preventDefault()
          appStore.setAIPanelVisible(true)
        }
        break
    }
  }
}

/**
 * 监听网络状态变化
 */
const handleOnline = () => {
  appStore.setOnlineStatus(true)
}

const handleOffline = () => {
  appStore.setOnlineStatus(false)
}

/**
 * 监听窗口大小变化
 */
const handleResize = () => {
  appStore.updateWindowSize({
    width: window.innerWidth,
    height: window.innerHeight
  })
}

/**
 * 组件挂载时的初始化
 */
onMounted(async () => {
  // 初始化应用
  await appStore.initializeApp()
  
  // 初始化认证状态
  await authStore.initializeAuth()
  
  // 添加事件监听器
  document.addEventListener('keydown', handleKeydown)
  window.addEventListener('online', handleOnline)
  window.addEventListener('offline', handleOffline)
  window.addEventListener('resize', handleResize)
  
  // 初始化窗口大小
  handleResize()
  
  // 设置初始网络状态
  appStore.setOnlineStatus(navigator.onLine)
  
  // 暴露全局确认函数
  ;(window as any).$confirm = globalConfirm
})

/**
 * 组件卸载时清理
 */
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('online', handleOnline)
  window.removeEventListener('offline', handleOffline)
  window.removeEventListener('resize', handleResize)
})
</script>

<style>
/* 全局样式重置 */
* {
  box-sizing: border-box;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100vh;
  overflow: hidden;
  position: relative;
}

/* 全局加载遮罩 */
.global-loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app--dark .global-loading {
  background: rgba(0, 0, 0, 0.9);
}

/* 路由过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

/* 通知样式 */
.notification-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 3000;
  max-width: 400px;
}

.notification-item {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  margin-bottom: 12px;
  padding: 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  border-left: 4px solid var(--el-color-info);
}

.notification-item--success {
  border-left-color: var(--el-color-success);
}

.notification-item--warning {
  border-left-color: var(--el-color-warning);
}

.notification-item--error {
  border-left-color: var(--el-color-danger);
}

.notification-content {
  flex: 1;
  display: flex;
  gap: 8px;
}

.notification-icon {
  font-size: 18px;
  margin-top: 2px;
}

.notification-item--success .notification-icon {
  color: var(--el-color-success);
}

.notification-item--warning .notification-icon {
  color: var(--el-color-warning);
}

.notification-item--error .notification-icon {
  color: var(--el-color-danger);
}

.notification-item--info .notification-icon {
  color: var(--el-color-info);
}

.notification-title {
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.notification-message {
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.4;
}

.notification-close {
  padding: 4px;
  margin: -4px;
}

.notification-enter-active,
.notification-leave-active {
  transition: all 0.3s ease;
}

.notification-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.notification-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

/* 离线横幅 */
.offline-banner {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--el-color-warning);
  color: white;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
  z-index: 2000;
  font-weight: 500;
}

/* 暗色主题适配 */
.app--dark {
  color-scheme: dark;
}

.app--dark .notification-item {
  background: var(--el-bg-color-page);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

/* 移动端适配 */
.app--mobile .notification-container {
  left: 20px;
  right: 20px;
  max-width: none;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: var(--el-fill-color-lighter);
}

::-webkit-scrollbar-thumb {
  background: var(--el-border-color-dark);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color-darker);
}

.app--dark ::-webkit-scrollbar-track {
  background: var(--el-fill-color-dark);
}

.app--dark ::-webkit-scrollbar-thumb {
  background: var(--el-border-color-light);
}

.app--dark ::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color);
}
</style>
