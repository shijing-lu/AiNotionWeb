/**
 * 应用全局状态管理
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserSettings, Notification } from '@/types'

export const useAppStore = defineStore('app', () => {
  // 状态
  const sidebarCollapsed = ref(false)
  const theme = ref<'light' | 'dark' | 'auto'>('light')
  const language = ref<'zh-CN' | 'en-US'>('zh-CN')
  const loading = ref(false)
  const notifications = ref<Notification[]>([])
  const isOnline = ref(navigator.onLine)
  const windowSize = ref({
    width: window.innerWidth,
    height: window.innerHeight
  })
  
  // 布局相关
  const showAIPanel = ref(false)
  const showSearchPanel = ref(false)
  const showSettingsPanel = ref(false)
  const activeTab = ref('documents')
  
  // 编辑器相关
  const editorMode = ref<'edit' | 'preview' | 'split'>('edit')
  const showToolbar = ref(true)
  const showOutline = ref(false)
  const fontSize = ref(14)
  const lineHeight = ref(1.6)
  
  // 用户设置
  const userSettings = ref<UserSettings>({
    theme: 'light',
    language: 'zh-CN',
    fontSize: 14,
    autoSave: true,
    aiEnabled: true,
    notifications: {
      email: true,
      push: true,
      mentions: true,
      comments: true
    }
  })

  // 计算属性
  const isMobile = computed(() => windowSize.value.width < 768)
  const isTablet = computed(() => windowSize.value.width >= 768 && windowSize.value.width < 1024)
  const isDesktop = computed(() => windowSize.value.width >= 1024)
  const unreadNotificationCount = computed(() => {
    return notifications.value.filter(n => !n.id.includes('read')).length
  })
  const isDarkMode = computed(() => {
    if (theme.value === 'auto') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    return theme.value === 'dark'
  })

  // 切换侧边栏
  const toggleSidebar = (): void => {
    sidebarCollapsed.value = !sidebarCollapsed.value
    localStorage.setItem('sidebar_collapsed', sidebarCollapsed.value.toString())
  }

  // 设置侧边栏状态
  const setSidebarCollapsed = (collapsed: boolean): void => {
    sidebarCollapsed.value = collapsed
    localStorage.setItem('sidebar_collapsed', collapsed.toString())
  }

  // 切换主题
  const toggleTheme = (): void => {
    const themes: Array<'light' | 'dark' | 'auto'> = ['light', 'dark', 'auto']
    const currentIndex = themes.indexOf(theme.value)
    const nextIndex = (currentIndex + 1) % themes.length
    setTheme(themes[nextIndex])
  }

  // 设置主题
  const setTheme = (newTheme: 'light' | 'dark' | 'auto'): void => {
    theme.value = newTheme
    userSettings.value.theme = newTheme
    localStorage.setItem('theme', newTheme)
    
    // 应用主题到DOM
    applyTheme()
  }

  // 应用主题
  const applyTheme = (): void => {
    const root = document.documentElement
    
    if (isDarkMode.value) {
      root.classList.add('dark')
      root.classList.remove('light')
    } else {
      root.classList.add('light')
      root.classList.remove('dark')
    }
  }

  // 设置语言
  const setLanguage = (newLanguage: 'zh-CN' | 'en-US'): void => {
    language.value = newLanguage
    userSettings.value.language = newLanguage
    localStorage.setItem('language', newLanguage)
  }

  // 设置加载状态
  const setLoading = (loading: boolean): void => {
    loading.value = loading
  }

  // 显示/隐藏AI面板
  const toggleAIPanel = (): void => {
    showAIPanel.value = !showAIPanel.value
    if (showAIPanel.value) {
      showSearchPanel.value = false
      showSettingsPanel.value = false
    }
  }

  // 显示/隐藏搜索面板
  const toggleSearchPanel = (): void => {
    showSearchPanel.value = !showSearchPanel.value
    if (showSearchPanel.value) {
      showAIPanel.value = false
      showSettingsPanel.value = false
    }
  }

  // 显示/隐藏设置面板
  const toggleSettingsPanel = (): void => {
    showSettingsPanel.value = !showSettingsPanel.value
    if (showSettingsPanel.value) {
      showAIPanel.value = false
      showSearchPanel.value = false
    }
  }

  // 关闭所有面板
  const closeAllPanels = (): void => {
    showAIPanel.value = false
    showSearchPanel.value = false
    showSettingsPanel.value = false
  }

  // 设置活动标签
  const setActiveTab = (tab: string): void => {
    activeTab.value = tab
  }

  // 设置编辑器模式
  const setEditorMode = (mode: 'edit' | 'preview' | 'split'): void => {
    editorMode.value = mode
    localStorage.setItem('editor_mode', mode)
  }

  // 切换工具栏显示
  const toggleToolbar = (): void => {
    showToolbar.value = !showToolbar.value
    localStorage.setItem('show_toolbar', showToolbar.value.toString())
  }

  // 切换大纲显示
  const toggleOutline = (): void => {
    showOutline.value = !showOutline.value
    localStorage.setItem('show_outline', showOutline.value.toString())
  }

  // 设置字体大小
  const setFontSize = (size: number): void => {
    fontSize.value = Math.max(12, Math.min(24, size))
    userSettings.value.fontSize = fontSize.value
    localStorage.setItem('font_size', fontSize.value.toString())
  }

  // 增加字体大小
  const increaseFontSize = (): void => {
    setFontSize(fontSize.value + 1)
  }

  // 减少字体大小
  const decreaseFontSize = (): void => {
    setFontSize(fontSize.value - 1)
  }

  // 添加通知
  const addNotification = (notification: Omit<Notification, 'id' | 'createdAt'>): void => {
    const newNotification: Notification = {
      ...notification,
      id: `notification_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      createdAt: new Date().toISOString()
    }
    
    notifications.value.unshift(newNotification)
    
    // 自动移除通知
    if (notification.duration && notification.duration > 0) {
      setTimeout(() => {
        removeNotification(newNotification.id)
      }, notification.duration)
    }
  }

  // 移除通知
  const removeNotification = (id: string): void => {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index !== -1) {
      notifications.value.splice(index, 1)
    }
  }

  // 清空所有通知
  const clearAllNotifications = (): void => {
    notifications.value = []
  }

  // 标记通知为已读
  const markNotificationAsRead = (id: string): void => {
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.id = notification.id + '_read'
    }
  }

  // 更新窗口大小
  const updateWindowSize = (): void => {
    windowSize.value = {
      width: window.innerWidth,
      height: window.innerHeight
    }
  }

  // 设置在线状态
  const setOnlineStatus = (online: boolean): void => {
    isOnline.value = online
    
    if (online) {
      addNotification({
        type: 'success',
        title: '网络已连接',
        message: '网络连接已恢复',
        duration: 3000
      })
    } else {
      addNotification({
        type: 'warning',
        title: '网络已断开',
        message: '请检查网络连接',
        duration: 0
      })
    }
  }

  // 初始化应用设置
  const initializeApp = (): void => {
    // 恢复侧边栏状态
    const savedSidebarState = localStorage.getItem('sidebar_collapsed')
    if (savedSidebarState) {
      sidebarCollapsed.value = savedSidebarState === 'true'
    }
    
    // 恢复主题设置
    const savedTheme = localStorage.getItem('theme') as 'light' | 'dark' | 'auto'
    if (savedTheme) {
      theme.value = savedTheme
      userSettings.value.theme = savedTheme
    }
    
    // 恢复语言设置
    const savedLanguage = localStorage.getItem('language') as 'zh-CN' | 'en-US'
    if (savedLanguage) {
      language.value = savedLanguage
      userSettings.value.language = savedLanguage
    }
    
    // 恢复编辑器设置
    const savedEditorMode = localStorage.getItem('editor_mode') as 'edit' | 'preview' | 'split'
    if (savedEditorMode) {
      editorMode.value = savedEditorMode
    }
    
    const savedShowToolbar = localStorage.getItem('show_toolbar')
    if (savedShowToolbar) {
      showToolbar.value = savedShowToolbar === 'true'
    }
    
    const savedShowOutline = localStorage.getItem('show_outline')
    if (savedShowOutline) {
      showOutline.value = savedShowOutline === 'true'
    }
    
    const savedFontSize = localStorage.getItem('font_size')
    if (savedFontSize) {
      fontSize.value = parseInt(savedFontSize, 10)
      userSettings.value.fontSize = fontSize.value
    }
    
    // 应用主题
    applyTheme()
    
    // 监听窗口大小变化
    window.addEventListener('resize', updateWindowSize)
    
    // 监听在线状态变化
    window.addEventListener('online', () => setOnlineStatus(true))
    window.addEventListener('offline', () => setOnlineStatus(false))
    
    // 监听主题变化
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', () => {
      if (theme.value === 'auto') {
        applyTheme()
      }
    })
  }

  // 重置应用设置
  const resetAppSettings = (): void => {
    sidebarCollapsed.value = false
    theme.value = 'light'
    language.value = 'zh-CN'
    editorMode.value = 'edit'
    showToolbar.value = true
    showOutline.value = false
    fontSize.value = 14
    
    // 清除本地存储
    localStorage.removeItem('sidebar_collapsed')
    localStorage.removeItem('theme')
    localStorage.removeItem('language')
    localStorage.removeItem('editor_mode')
    localStorage.removeItem('show_toolbar')
    localStorage.removeItem('show_outline')
    localStorage.removeItem('font_size')
    
    // 重新应用设置
    applyTheme()
  }

  return {
    // 状态
    sidebarCollapsed,
    theme,
    language,
    loading,
    notifications,
    isOnline,
    windowSize,
    showAIPanel,
    showSearchPanel,
    showSettingsPanel,
    activeTab,
    editorMode,
    showToolbar,
    showOutline,
    fontSize,
    lineHeight,
    userSettings,
    
    // 计算属性
    isMobile,
    isTablet,
    isDesktop,
    unreadNotificationCount,
    isDarkMode,
    
    // 方法
    toggleSidebar,
    setSidebarCollapsed,
    toggleTheme,
    setTheme,
    applyTheme,
    setLanguage,
    setLoading,
    toggleAIPanel,
    toggleSearchPanel,
    toggleSettingsPanel,
    closeAllPanels,
    setActiveTab,
    setEditorMode,
    toggleToolbar,
    toggleOutline,
    setFontSize,
    increaseFontSize,
    decreaseFontSize,
    addNotification,
    removeNotification,
    clearAllNotifications,
    markNotificationAsRead,
    updateWindowSize,
    setOnlineStatus,
    initializeApp,
    resetAppSettings
  }
})