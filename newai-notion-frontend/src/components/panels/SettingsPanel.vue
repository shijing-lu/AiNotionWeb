<template>
  <div class="settings-panel">
    <!-- 面板头部 -->
    <div class="panel-header">
      <div class="header-left">
        <el-icon class="panel-icon"><Setting /></el-icon>
        <h3 class="panel-title">快速设置</h3>
      </div>
      <div class="header-right">
        <el-button text @click="closePanel">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>
    
    <!-- 设置内容 -->
    <div class="settings-content">
      <!-- 主题设置 -->
      <div class="setting-section">
        <div class="section-header">
          <el-icon class="section-icon"><Sunny /></el-icon>
          <h4 class="section-title">主题</h4>
        </div>
        <div class="theme-options">
          <div 
            v-for="theme in themes" 
            :key="theme.value"
            class="theme-option"
            :class="{ active: currentTheme === theme.value }"
            @click="changeTheme(theme.value)"
          >
            <div class="theme-preview" :class="`theme-${theme.value}`">
              <div class="preview-header"></div>
              <div class="preview-content">
                <div class="preview-sidebar"></div>
                <div class="preview-main"></div>
              </div>
            </div>
            <span class="theme-name">{{ theme.name }}</span>
          </div>
        </div>
      </div>
      
      <!-- 语言设置 -->
      <div class="setting-section">
        <div class="section-header">
          <el-icon class="section-icon"><Link /></el-icon>
          <h4 class="section-title">语言</h4>
        </div>
        <el-select 
          v-model="currentLanguage" 
          class="language-select"
          @change="changeLanguage"
        >
          <el-option 
            v-for="lang in languages" 
            :key="lang.value"
            :label="lang.name"
            :value="lang.value"
          >
            <div class="language-option">
              <span class="language-flag">{{ lang.flag }}</span>
              <span class="language-name">{{ lang.name }}</span>
            </div>
          </el-option>
        </el-select>
      </div>
      
      <!-- 快速切换 -->
      <div class="setting-section">
        <div class="section-header">
          <el-icon class="section-icon"><Switch /></el-icon>
          <h4 class="section-title">快速切换</h4>
        </div>
        <div class="quick-toggles">
          <div class="toggle-item">
            <div class="toggle-info">
              <el-icon class="toggle-icon"><Bell /></el-icon>
              <span class="toggle-label">通知</span>
            </div>
            <el-switch 
              v-model="settings.notifications" 
              @change="toggleNotifications"
            />
          </div>
          
          <div class="toggle-item">
            <div class="toggle-info">
              <el-icon class="toggle-icon"><View /></el-icon>
              <span class="toggle-label">专注模式</span>
            </div>
            <el-switch 
              v-model="settings.focusMode" 
              @change="toggleFocusMode"
            />
          </div>
          
          <div class="toggle-item">
            <div class="toggle-info">
              <el-icon class="toggle-icon"><DocumentCopy /></el-icon>
              <span class="toggle-label">自动保存</span>
            </div>
            <el-switch 
              v-model="settings.autoSave" 
              @change="toggleAutoSave"
            />
          </div>
          
          <div class="toggle-item">
            <div class="toggle-info">
              <el-icon class="toggle-icon"><MagicStick /></el-icon>
              <span class="toggle-label">AI 助手</span>
            </div>
            <el-switch 
              v-model="settings.aiAssistant" 
              @change="toggleAIAssistant"
            />
          </div>
        </div>
      </div>
      
      <!-- 编辑器设置 -->
      <div class="setting-section">
        <div class="section-header">
          <el-icon class="section-icon"><Edit /></el-icon>
          <h4 class="section-title">编辑器</h4>
        </div>
        <div class="editor-settings">
          <div class="setting-item">
            <label class="setting-label">字体大小</label>
            <el-slider 
              v-model="settings.editor.fontSize" 
              :min="12" 
              :max="24" 
              :step="1"
              show-input
              input-size="small"
              @change="updateEditorSettings"
            />
          </div>
          
          <div class="setting-item">
            <label class="setting-label">行高</label>
            <el-slider 
              v-model="settings.editor.lineHeight" 
              :min="1.2" 
              :max="2.0" 
              :step="0.1"
              show-input
              input-size="small"
              @change="updateEditorSettings"
            />
          </div>
          
          <div class="setting-item">
            <div class="setting-row">
              <span class="setting-label">显示行号</span>
              <el-switch 
                v-model="settings.editor.showLineNumbers" 
                size="small"
                @change="updateEditorSettings"
              />
            </div>
          </div>
          
          <div class="setting-item">
            <div class="setting-row">
              <span class="setting-label">代码高亮</span>
              <el-switch 
                v-model="settings.editor.syntaxHighlight" 
                size="small"
                @change="updateEditorSettings"
              />
            </div>
          </div>
        </div>
      </div>
      
      <!-- 快捷操作 -->
      <div class="setting-section">
        <div class="section-header">
          <el-icon class="section-icon"><Lightning /></el-icon>
          <h4 class="section-title">快捷操作</h4>
        </div>
        <div class="quick-actions">
          <el-button 
            class="action-button" 
            @click="openFullSettings"
          >
            <el-icon><Setting /></el-icon>
            完整设置
          </el-button>
          
          <el-button 
            class="action-button" 
            @click="exportSettings"
          >
            <el-icon><Download /></el-icon>
            导出设置
          </el-button>
          
          <el-button 
            class="action-button" 
            @click="resetSettings"
          >
            <el-icon><RefreshLeft /></el-icon>
            重置设置
          </el-button>
          
          <el-button 
            class="action-button" 
            @click="showKeyboardShortcuts"
          >
            <el-icon><Key /></el-icon>
            快捷键
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 快捷键对话框 -->
    <el-dialog 
      v-model="shortcutsDialogVisible" 
      title="键盘快捷键" 
      width="500px"
    >
      <div class="shortcuts-list">
        <div 
          v-for="shortcut in keyboardShortcuts" 
          :key="shortcut.action"
          class="shortcut-item"
        >
          <span class="shortcut-action">{{ shortcut.action }}</span>
          <div class="shortcut-keys">
            <kbd 
              v-for="key in shortcut.keys" 
              :key="key"
              class="shortcut-key"
            >
              {{ key }}
            </kbd>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting,
  Close,
  Sunny,
  Link,
  Switch,
  Bell,
  View,
  DocumentCopy,
  MagicStick,
  Edit,
  Lightning,
  Download,
  RefreshLeft,
  Key
} from '@element-plus/icons-vue'

// 事件定义
const emit = defineEmits<{
  close: []
}>()

// 路由
const router = useRouter()

// 响应式数据
const currentTheme = ref('light')
const currentLanguage = ref('zh-CN')
const shortcutsDialogVisible = ref(false)

// 设置数据
const settings = ref({
  notifications: true,
  focusMode: false,
  autoSave: true,
  aiAssistant: true,
  editor: {
    fontSize: 14,
    lineHeight: 1.6,
    showLineNumbers: true,
    syntaxHighlight: true
  }
})

// 主题选项
const themes = [
  { value: 'light', name: '浅色' },
  { value: 'dark', name: '深色' },
  { value: 'auto', name: '自动' }
]

// 语言选项
const languages = [
  { value: 'zh-CN', name: '简体中文', flag: '🇨🇳' },
  { value: 'en-US', name: 'English', flag: '🇺🇸' },
  { value: 'zh-TW', name: '繁體中文', flag: '🇹🇼' },
  { value: 'ja-JP', name: '日本語', flag: '🇯🇵' },
  { value: 'ko-KR', name: '한국어', flag: '🇰🇷' }
]

// 快捷键列表
const keyboardShortcuts = [
  { action: '新建文档', keys: ['Ctrl', 'N'] },
  { action: '保存文档', keys: ['Ctrl', 'S'] },
  { action: '搜索', keys: ['Ctrl', 'K'] },
  { action: '命令面板', keys: ['Ctrl', 'Shift', 'P'] },
  { action: '切换侧边栏', keys: ['Ctrl', 'B'] },
  { action: '专注模式', keys: ['F11'] },
  { action: '撤销', keys: ['Ctrl', 'Z'] },
  { action: '重做', keys: ['Ctrl', 'Y'] },
  { action: '复制', keys: ['Ctrl', 'C'] },
  { action: '粘贴', keys: ['Ctrl', 'V'] },
  { action: '全选', keys: ['Ctrl', 'A'] },
  { action: '查找', keys: ['Ctrl', 'F'] },
  { action: '替换', keys: ['Ctrl', 'H'] }
]

/**
 * 切换主题
 */
const changeTheme = (theme: string) => {
  currentTheme.value = theme
  
  // 应用主题
  if (theme === 'dark') {
    document.documentElement.classList.add('dark')
  } else if (theme === 'light') {
    document.documentElement.classList.remove('dark')
  } else {
    // 自动模式 - 根据系统偏好
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    if (prefersDark) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }
  
  // 保存到本地存储
  localStorage.setItem('theme', theme)
  ElMessage.success(`主题已切换为: ${themes.find(t => t.value === theme)?.name}`)
}

/**
 * 切换语言
 */
const changeLanguage = (language: string) => {
  currentLanguage.value = language
  localStorage.setItem('language', language)
  
  const langName = languages.find(l => l.value === language)?.name
  ElMessage.success(`语言已切换为: ${langName}`)
  
  // 这里可以集成 i18n 进行实际的语言切换
}

/**
 * 切换通知
 */
const toggleNotifications = (enabled: boolean) => {
  if (enabled) {
    // 请求通知权限
    if ('Notification' in window) {
      Notification.requestPermission().then(permission => {
        if (permission === 'granted') {
          ElMessage.success('通知已开启')
        } else {
          settings.value.notifications = false
          ElMessage.warning('通知权限被拒绝')
        }
      })
    } else {
      ElMessage.warning('浏览器不支持通知功能')
      settings.value.notifications = false
    }
  } else {
    ElMessage.info('通知已关闭')
  }
  
  saveSettings()
}

/**
 * 切换专注模式
 */
const toggleFocusMode = (enabled: boolean) => {
  if (enabled) {
    document.body.classList.add('focus-mode')
    ElMessage.success('专注模式已开启')
  } else {
    document.body.classList.remove('focus-mode')
    ElMessage.info('专注模式已关闭')
  }
  
  saveSettings()
}

/**
 * 切换自动保存
 */
const toggleAutoSave = (enabled: boolean) => {
  ElMessage.success(enabled ? '自动保存已开启' : '自动保存已关闭')
  saveSettings()
}

/**
 * 切换AI助手
 */
const toggleAIAssistant = (enabled: boolean) => {
  ElMessage.success(enabled ? 'AI助手已开启' : 'AI助手已关闭')
  saveSettings()
}

/**
 * 更新编辑器设置
 */
const updateEditorSettings = () => {
  // 应用编辑器设置到实际编辑器
  const editorElement = document.querySelector('.editor')
  if (editorElement) {
    const { fontSize, lineHeight } = settings.value.editor
    editorElement.style.fontSize = `${fontSize}px`
    editorElement.style.lineHeight = String(lineHeight)
  }
  
  saveSettings()
  ElMessage.success('编辑器设置已更新')
}

/**
 * 保存设置
 */
const saveSettings = () => {
  localStorage.setItem('appSettings', JSON.stringify(settings.value))
}

/**
 * 加载设置
 */
const loadSettings = () => {
  // 加载主题
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme) {
    currentTheme.value = savedTheme
    changeTheme(savedTheme)
  }
  
  // 加载语言
  const savedLanguage = localStorage.getItem('language')
  if (savedLanguage) {
    currentLanguage.value = savedLanguage
  }
  
  // 加载应用设置
  const savedSettings = localStorage.getItem('appSettings')
  if (savedSettings) {
    try {
      const parsed = JSON.parse(savedSettings)
      settings.value = { ...settings.value, ...parsed }
    } catch (error) {
      console.warn('Failed to parse saved settings:', error)
    }
  }
}

/**
 * 打开完整设置页面
 */
const openFullSettings = () => {
  router.push('/settings')
  closePanel()
}

/**
 * 导出设置
 */
const exportSettings = () => {
  const settingsData = {
    theme: currentTheme.value,
    language: currentLanguage.value,
    settings: settings.value,
    exportTime: new Date().toISOString()
  }
  
  const dataStr = JSON.stringify(settingsData, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(dataBlob)
  
  const link = document.createElement('a')
  link.href = url
  link.download = `settings-${new Date().toISOString().split('T')[0]}.json`
  link.click()
  
  URL.revokeObjectURL(url)
  ElMessage.success('设置已导出')
}

/**
 * 重置设置
 */
const resetSettings = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重置所有设置吗？此操作不可撤销。',
      '重置设置',
      {
        type: 'warning',
        confirmButtonText: '确定重置',
        cancelButtonText: '取消'
      }
    )
    
    // 重置设置
    currentTheme.value = 'light'
    currentLanguage.value = 'zh-CN'
    settings.value = {
      notifications: true,
      focusMode: false,
      autoSave: true,
      aiAssistant: true,
      editor: {
        fontSize: 14,
        lineHeight: 1.6,
        showLineNumbers: true,
        syntaxHighlight: true
      }
    }
    
    // 清除本地存储
    localStorage.removeItem('theme')
    localStorage.removeItem('language')
    localStorage.removeItem('appSettings')
    
    // 应用默认主题
    changeTheme('light')
    
    ElMessage.success('设置已重置为默认值')
  } catch {
    // 用户取消
  }
}

/**
 * 显示快捷键
 */
const showKeyboardShortcuts = () => {
  shortcutsDialogVisible.value = true
}

/**
 * 关闭面板
 */
const closePanel = () => {
  emit('close')
}

// 组件挂载
onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.settings-panel {
  width: 350px;
  max-height: 80vh;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  box-shadow: var(--el-box-shadow);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-lighter);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.panel-icon {
  color: var(--el-color-primary);
}

.panel-title {
  margin: 0;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.settings-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-sm);
}

.setting-section {
  margin-bottom: var(--spacing-lg);
}

.section-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.section-icon {
  color: var(--el-color-primary);
  font-size: 1rem;
}

.section-title {
  margin: 0;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

/* 主题设置样式 */
.theme-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-sm);
}

.theme-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm);
  border: 2px solid transparent;
  border-radius: var(--border-radius-base);
  cursor: pointer;
  transition: all 0.2s;
}

.theme-option:hover {
  background: var(--el-fill-color-light);
}

.theme-option.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.theme-preview {
  width: 60px;
  height: 40px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-light);
}

.theme-light {
  background: #ffffff;
}

.theme-dark {
  background: #1a1a1a;
}

.theme-auto {
  background: linear-gradient(90deg, #ffffff 50%, #1a1a1a 50%);
}

.preview-header {
  height: 8px;
  background: var(--el-fill-color);
}

.preview-content {
  display: flex;
  height: 32px;
}

.preview-sidebar {
  width: 20px;
  background: var(--el-fill-color-light);
}

.preview-main {
  flex: 1;
  background: var(--el-fill-color-lighter);
}

.theme-name {
  font-size: 0.75rem;
  color: var(--el-text-color-regular);
  text-align: center;
}

/* 语言设置样式 */
.language-select {
  width: 100%;
}

.language-option {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.language-flag {
  font-size: 1.2em;
}

.language-name {
  flex: 1;
}

/* 快速切换样式 */
.quick-toggles {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.toggle-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm);
  background: var(--el-fill-color-lighter);
  border-radius: var(--border-radius-small);
}

.toggle-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.toggle-icon {
  color: var(--el-text-color-regular);
  font-size: 0.875rem;
}

.toggle-label {
  font-size: 0.875rem;
  color: var(--el-text-color-primary);
}

/* 编辑器设置样式 */
.editor-settings {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.setting-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.setting-label {
  font-size: 0.875rem;
  color: var(--el-text-color-regular);
  margin-bottom: var(--spacing-xs);
}

/* 快捷操作样式 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-sm);
}

.action-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-md) var(--spacing-sm);
  height: auto;
  font-size: 0.75rem;
  border: 1px solid var(--el-border-color-light);
}

.action-button:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

/* 快捷键对话框样式 */
.shortcuts-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  max-height: 400px;
  overflow-y: auto;
}

.shortcut-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm);
  background: var(--el-fill-color-lighter);
  border-radius: var(--border-radius-small);
}

.shortcut-action {
  font-size: 0.875rem;
  color: var(--el-text-color-primary);
}

.shortcut-keys {
  display: flex;
  gap: var(--spacing-xs);
}

.shortcut-key {
  padding: 2px 6px;
  background: var(--el-fill-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 3px;
  font-size: 0.75rem;
  font-family: monospace;
  color: var(--el-text-color-regular);
}

/* 滚动条样式 */
.settings-content::-webkit-scrollbar {
  width: 4px;
}

.settings-content::-webkit-scrollbar-track {
  background: transparent;
}

.settings-content::-webkit-scrollbar-thumb {
  background: var(--el-border-color-light);
  border-radius: 2px;
}

.settings-content::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color);
}

.shortcuts-list::-webkit-scrollbar {
  width: 4px;
}

.shortcuts-list::-webkit-scrollbar-track {
  background: transparent;
}

.shortcuts-list::-webkit-scrollbar-thumb {
  background: var(--el-border-color-light);
  border-radius: 2px;
}

.shortcuts-list::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color);
}

/* 专注模式样式 */
:global(.focus-mode) {
  .sidebar,
  .header-actions,
  .footer {
    display: none !important;
  }
}

/* 深色主题适配 */
:global(.dark) .theme-light .preview-header {
  background: #f0f0f0;
}

:global(.dark) .theme-light .preview-sidebar {
  background: #e0e0e0;
}

:global(.dark) .theme-light .preview-main {
  background: #f5f5f5;
}

:global(.dark) .theme-dark .preview-header {
  background: #333333;
}

:global(.dark) .theme-dark .preview-sidebar {
  background: #2a2a2a;
}

:global(.dark) .theme-dark .preview-main {
  background: #1e1e1e;
}
</style>