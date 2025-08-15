<template>
  <div class="settings-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon class="title-icon"><Setting /></el-icon>
            设置
          </h1>
          <p class="page-description">管理您的账户设置和应用偏好</p>
        </div>
      </div>
    </div>

    <!-- 设置内容 -->
    <div class="settings-content">
      <!-- 设置侧边栏 -->
      <div class="settings-sidebar">
        <el-menu
          v-model="activeSection"
          class="settings-menu"
          @select="handleSectionChange"
        >
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-menu-item>
          <el-menu-item index="account">
            <el-icon><Lock /></el-icon>
            <span>账户安全</span>
          </el-menu-item>
          <el-menu-item index="preferences">
            <el-icon><Tools /></el-icon>
            <span>应用偏好</span>
          </el-menu-item>
          <el-menu-item index="notifications">
            <el-icon><Bell /></el-icon>
            <span>通知设置</span>
          </el-menu-item>
          <el-menu-item index="storage">
            <el-icon><FolderOpened /></el-icon>
            <span>存储管理</span>
          </el-menu-item>
          <el-menu-item index="export">
            <el-icon><Download /></el-icon>
            <span>数据导出</span>
          </el-menu-item>
          <el-menu-item index="about">
            <el-icon><InfoFilled /></el-icon>
            <span>关于</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 设置主体 -->
      <div class="settings-main">
        <!-- 个人资料 -->
        <div v-if="activeSection === 'profile'" class="settings-section">
          <div class="section-header">
            <h2 class="section-title">个人资料</h2>
            <p class="section-description">管理您的个人信息和头像</p>
          </div>

          <el-card class="settings-card">
            <div class="profile-section">
              <!-- 头像设置 -->
              <div class="avatar-section">
                <div class="avatar-container">
                  <el-avatar :size="80" :src="userProfile.avatar">
                    <el-icon><User /></el-icon>
                  </el-avatar>
                  <el-button 
                    class="avatar-upload-btn" 
                    size="small" 
                    @click="handleAvatarUpload"
                  >
                    <el-icon><Camera /></el-icon>
                    更换头像
                  </el-button>
                </div>
              </div>

              <!-- 基本信息 -->
              <div class="profile-form">
                <el-form 
                  ref="profileFormRef" 
                  :model="userProfile" 
                  :rules="profileRules" 
                  label-width="100px"
                >
                  <el-form-item label="用户名" prop="username">
                    <el-input v-model="userProfile.username" placeholder="请输入用户名" />
                  </el-form-item>
                  
                  <el-form-item label="邮箱" prop="email">
                    <el-input v-model="userProfile.email" placeholder="请输入邮箱" />
                  </el-form-item>
                  
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="userProfile.phone" placeholder="请输入手机号" />
                  </el-form-item>
                  
                  <el-form-item label="个人简介">
                    <el-input 
                      v-model="userProfile.bio" 
                      type="textarea" 
                      :rows="3" 
                      placeholder="介绍一下自己吧"
                    />
                  </el-form-item>
                  
                  <el-form-item>
                    <el-button type="primary" @click="saveProfile">保存更改</el-button>
                    <el-button @click="resetProfile">重置</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 账户安全 -->
        <div v-else-if="activeSection === 'account'" class="settings-section">
          <div class="section-header">
            <h2 class="section-title">账户安全</h2>
            <p class="section-description">管理您的密码和安全设置</p>
          </div>

          <el-card class="settings-card">
            <div class="security-section">
              <!-- 修改密码 -->
              <div class="security-item">
                <h3>修改密码</h3>
                <el-form 
                  ref="passwordFormRef" 
                  :model="passwordForm" 
                  :rules="passwordRules" 
                  label-width="120px"
                >
                  <el-form-item label="当前密码" prop="currentPassword">
                    <el-input 
                      v-model="passwordForm.currentPassword" 
                      type="password" 
                      show-password 
                      placeholder="请输入当前密码"
                    />
                  </el-form-item>
                  
                  <el-form-item label="新密码" prop="newPassword">
                    <el-input 
                      v-model="passwordForm.newPassword" 
                      type="password" 
                      show-password 
                      placeholder="请输入新密码"
                    />
                  </el-form-item>
                  
                  <el-form-item label="确认新密码" prop="confirmPassword">
                    <el-input 
                      v-model="passwordForm.confirmPassword" 
                      type="password" 
                      show-password 
                      placeholder="请再次输入新密码"
                    />
                  </el-form-item>
                  
                  <el-form-item>
                    <el-button type="primary" @click="changePassword">修改密码</el-button>
                  </el-form-item>
                </el-form>
              </div>

              <!-- 两步验证 -->
              <el-divider />
              <div class="security-item">
                <div class="security-item-header">
                  <div>
                    <h3>两步验证</h3>
                    <p class="item-description">为您的账户添加额外的安全保护</p>
                  </div>
                  <el-switch 
                    v-model="securitySettings.twoFactorEnabled" 
                    @change="toggleTwoFactor"
                  />
                </div>
              </div>

              <!-- 登录设备 -->
              <el-divider />
              <div class="security-item">
                <h3>登录设备</h3>
                <div class="device-list">
                  <div 
                    v-for="device in loginDevices" 
                    :key="device.id" 
                    class="device-item"
                  >
                    <div class="device-info">
                      <div class="device-name">{{ device.name }}</div>
                      <div class="device-details">
                        {{ device.location }} • {{ formatRelativeTime(device.lastActive) }}
                      </div>
                    </div>
                    <el-button 
                      v-if="!device.current" 
                      size="small" 
                      type="danger" 
                      text 
                      @click="revokeDevice(device.id)"
                    >
                      移除
                    </el-button>
                    <el-tag v-else size="small" type="success">当前设备</el-tag>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 应用偏好 -->
        <div v-else-if="activeSection === 'preferences'" class="settings-section">
          <div class="section-header">
            <h2 class="section-title">应用偏好</h2>
            <p class="section-description">自定义您的应用体验</p>
          </div>

          <el-card class="settings-card">
            <div class="preferences-section">
              <!-- 主题设置 -->
              <div class="preference-item">
                <div class="preference-header">
                  <h3>主题</h3>
                  <p class="item-description">选择您喜欢的界面主题</p>
                </div>
                <el-radio-group v-model="preferences.theme" @change="changeTheme">
                  <el-radio value="light">浅色主题</el-radio>
                  <el-radio value="dark">深色主题</el-radio>
                  <el-radio value="auto">跟随系统</el-radio>
                </el-radio-group>
              </div>

              <el-divider />

              <!-- 语言设置 -->
              <div class="preference-item">
                <div class="preference-header">
                  <h3>语言</h3>
                  <p class="item-description">选择界面显示语言</p>
                </div>
                <el-select v-model="preferences.language" @change="changeLanguage">
                  <el-option label="简体中文" value="zh-CN" />
                  <el-option label="English" value="en-US" />
                  <el-option label="繁體中文" value="zh-TW" />
                </el-select>
              </div>

              <el-divider />

              <!-- 编辑器设置 -->
              <div class="preference-item">
                <div class="preference-header">
                  <h3>编辑器</h3>
                  <p class="item-description">自定义编辑器行为</p>
                </div>
                <div class="editor-preferences">
                  <el-form label-width="120px">
                    <el-form-item label="字体大小">
                      <el-slider 
                        v-model="preferences.editor.fontSize" 
                        :min="12" 
                        :max="24" 
                        show-input
                      />
                    </el-form-item>
                    
                    <el-form-item label="自动保存">
                      <el-switch v-model="preferences.editor.autoSave" />
                    </el-form-item>
                    
                    <el-form-item label="显示行号">
                      <el-switch v-model="preferences.editor.showLineNumbers" />
                    </el-form-item>
                    
                    <el-form-item label="代码高亮">
                      <el-switch v-model="preferences.editor.syntaxHighlight" />
                    </el-form-item>
                  </el-form>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 通知设置 -->
        <div v-else-if="activeSection === 'notifications'" class="settings-section">
          <div class="section-header">
            <h2 class="section-title">通知设置</h2>
            <p class="section-description">管理您接收通知的方式</p>
          </div>

          <el-card class="settings-card">
            <div class="notifications-section">
              <div 
                v-for="(setting, key) in notificationSettings" 
                :key="key" 
                class="notification-item"
              >
                <div class="notification-info">
                  <h3>{{ setting.title }}</h3>
                  <p class="item-description">{{ setting.description }}</p>
                </div>
                <div class="notification-controls">
                  <el-switch 
                    v-model="setting.email" 
                    inactive-text="邮箱" 
                    @change="saveNotificationSettings"
                  />
                  <el-switch 
                    v-model="setting.push" 
                    inactive-text="推送" 
                    @change="saveNotificationSettings"
                  />
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 存储管理 -->
        <div v-else-if="activeSection === 'storage'" class="settings-section">
          <div class="section-header">
            <h2 class="section-title">存储管理</h2>
            <p class="section-description">查看和管理您的存储空间使用情况</p>
          </div>

          <el-card class="settings-card">
            <div class="storage-section">
              <!-- 存储概览 -->
              <div class="storage-overview">
                <div class="storage-stats">
                  <div class="storage-used">
                    <h3>{{ formatFileSize(storageInfo.used) }}</h3>
                    <p>已使用</p>
                  </div>
                  <div class="storage-total">
                    <h3>{{ formatFileSize(storageInfo.total) }}</h3>
                    <p>总容量</p>
                  </div>
                </div>
                <div class="storage-progress">
                  <el-progress 
                    :percentage="storagePercentage" 
                    :color="getStorageColor(storagePercentage)"
                  />
                </div>
              </div>

              <el-divider />

              <!-- 存储详情 -->
              <div class="storage-details">
                <h3>存储详情</h3>
                <div class="storage-breakdown">
                  <div 
                    v-for="item in storageBreakdown" 
                    :key="item.type" 
                    class="storage-item"
                  >
                    <div class="storage-item-info">
                      <div class="storage-item-icon">
                        <el-icon><component :is="item.icon" /></el-icon>
                      </div>
                      <div class="storage-item-details">
                        <div class="storage-item-name">{{ item.name }}</div>
                        <div class="storage-item-size">{{ formatFileSize(item.size) }}</div>
                      </div>
                    </div>
                    <el-button size="small" text @click="cleanupStorage(item.type)">
                      清理
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 数据导出 -->
        <div v-else-if="activeSection === 'export'" class="settings-section">
          <div class="section-header">
            <h2 class="section-title">数据导出</h2>
            <p class="section-description">导出您的数据以备份或迁移</p>
          </div>

          <el-card class="settings-card">
            <div class="export-section">
              <div class="export-options">
                <div 
                  v-for="option in exportOptions" 
                  :key="option.type" 
                  class="export-option"
                >
                  <div class="export-option-info">
                    <h3>{{ option.title }}</h3>
                    <p class="item-description">{{ option.description }}</p>
                  </div>
                  <el-button 
                    type="primary" 
                    :loading="option.loading" 
                    @click="exportData(option.type)"
                  >
                    导出
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 关于 -->
        <div v-else-if="activeSection === 'about'" class="settings-section">
          <div class="section-header">
            <h2 class="section-title">关于</h2>
            <p class="section-description">应用信息和版本详情</p>
          </div>

          <el-card class="settings-card">
            <div class="about-section">
              <div class="app-info">
                <div class="app-logo">
                  <img src="/logo.svg" alt="Logo" class="logo-image" />
                </div>
                <div class="app-details">
                  <h2>NewAI Notion</h2>
                  <p class="app-version">版本 {{ appInfo.version }}</p>
                  <p class="app-description">{{ appInfo.description }}</p>
                </div>
              </div>

              <el-divider />

              <div class="app-links">
                <el-button text @click="openLink('https://github.com')">
                  <el-icon><Link /></el-icon>
                  GitHub
                </el-button>
                <el-button text @click="openLink('https://docs.example.com')">
                  <el-icon><Document /></el-icon>
                  文档
                </el-button>
                <el-button text @click="openLink('mailto:support@example.com')">
                  <el-icon><Message /></el-icon>
                  联系我们
                </el-button>
              </div>

              <el-divider />

              <div class="system-info">
                <h3>系统信息</h3>
                <div class="info-grid">
                  <div class="info-item">
                    <span class="info-label">浏览器:</span>
                    <span class="info-value">{{ systemInfo.browser }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">操作系统:</span>
                    <span class="info-value">{{ systemInfo.os }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">屏幕分辨率:</span>
                    <span class="info-value">{{ systemInfo.resolution }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  Setting,
  User,
  Lock,
  Tools,
  Bell,
  FolderOpened,
  Download,
  InfoFilled,
  Camera,
  Link,
  Document,
  Message
} from '@element-plus/icons-vue'
import { formatRelativeTime } from '@/utils/date'
import { formatFileSize } from '@/utils/common'

// 当前激活的设置部分
const activeSection = ref('profile')

// 表单引用
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 用户资料
const userProfile = ref({
  username: 'John Doe',
  email: 'john@example.com',
  phone: '+86 138 0013 8000',
  bio: '这是一个简短的个人介绍',
  avatar: ''
})

// 密码表单
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 安全设置
const securitySettings = ref({
  twoFactorEnabled: false
})

// 应用偏好
const preferences = ref({
  theme: 'light',
  language: 'zh-CN',
  editor: {
    fontSize: 14,
    autoSave: true,
    showLineNumbers: true,
    syntaxHighlight: true
  }
})

// 通知设置
const notificationSettings = ref({
  comments: {
    title: '评论通知',
    description: '当有人评论您的文档时通知您',
    email: true,
    push: true
  },
  mentions: {
    title: '提及通知',
    description: '当有人在文档中@您时通知您',
    email: true,
    push: true
  },
  shares: {
    title: '分享通知',
    description: '当有人与您分享文档时通知您',
    email: true,
    push: false
  },
  updates: {
    title: '更新通知',
    description: '应用更新和新功能通知',
    email: false,
    push: true
  }
})

// 存储信息
const storageInfo = ref({
  used: 2.5 * 1024 * 1024 * 1024, // 2.5GB
  total: 10 * 1024 * 1024 * 1024  // 10GB
})

// 存储详情
const storageBreakdown = ref([
  {
    type: 'documents',
    name: '文档',
    size: 1.2 * 1024 * 1024 * 1024,
    icon: 'Document'
  },
  {
    type: 'images',
    name: '图片',
    size: 800 * 1024 * 1024,
    icon: 'Picture'
  },
  {
    type: 'attachments',
    name: '附件',
    size: 500 * 1024 * 1024,
    icon: 'Paperclip'
  }
])

// 导出选项
const exportOptions = ref([
  {
    type: 'all',
    title: '完整数据导出',
    description: '导出所有文档、文件夹和设置',
    loading: false
  },
  {
    type: 'documents',
    title: '文档导出',
    description: '仅导出文档内容',
    loading: false
  },
  {
    type: 'settings',
    title: '设置导出',
    description: '导出应用设置和偏好',
    loading: false
  }
])

// 登录设备
const loginDevices = ref([
  {
    id: '1',
    name: 'Windows PC - Chrome',
    location: '北京, 中国',
    lastActive: new Date(),
    current: true
  },
  {
    id: '2',
    name: 'iPhone 15 Pro - Safari',
    location: '上海, 中国',
    lastActive: new Date('2024-01-19'),
    current: false
  }
])

// 应用信息
const appInfo = ref({
  version: '1.0.0',
  description: '基于AI的智能笔记应用，让您的想法更有条理'
})

// 系统信息
const systemInfo = ref({
  browser: '',
  os: '',
  resolution: ''
})

// 表单验证规则
const profileRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const passwordRules: FormRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算属性
const storagePercentage = computed(() => {
  return Math.round((storageInfo.value.used / storageInfo.value.total) * 100)
})

// 方法
const handleSectionChange = (section: string) => {
  activeSection.value = section
}

const handleAvatarUpload = () => {
  // 模拟头像上传
  ElMessage.success('头像上传功能开发中')
}

const saveProfile = async () => {
  if (!profileFormRef.value) return
  
  try {
    await profileFormRef.value.validate()
    // 模拟保存
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('个人资料保存成功')
  } catch {
    ElMessage.error('请检查输入信息')
  }
}

const resetProfile = () => {
  profileFormRef.value?.resetFields()
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    // 模拟修改密码
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('密码修改成功')
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch {
    ElMessage.error('请检查输入信息')
  }
}

const toggleTwoFactor = (enabled: boolean) => {
  if (enabled) {
    ElMessage.info('两步验证功能开发中')
  } else {
    ElMessage.success('两步验证已关闭')
  }
}

const revokeDevice = async (deviceId: string) => {
  try {
    await ElMessageBox.confirm('确定要移除此设备吗？', '确认操作', {
      type: 'warning'
    })
    
    // 模拟移除设备
    const index = loginDevices.value.findIndex(d => d.id === deviceId)
    if (index > -1) {
      loginDevices.value.splice(index, 1)
      ElMessage.success('设备已移除')
    }
  } catch {
    // 用户取消
  }
}

const changeTheme = (theme: string) => {
  ElMessage.success(`主题已切换为: ${theme}`)
  // 这里可以实现主题切换逻辑
}

const changeLanguage = (language: string) => {
  ElMessage.success(`语言已切换为: ${language}`)
  // 这里可以实现语言切换逻辑
}

const saveNotificationSettings = () => {
  ElMessage.success('通知设置已保存')
}

const getStorageColor = (percentage: number) => {
  if (percentage < 60) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}

const cleanupStorage = (type: string) => {
  ElMessage.info(`${type} 清理功能开发中`)
}

const exportData = async (type: string) => {
  const option = exportOptions.value.find(o => o.type === type)
  if (!option) return
  
  option.loading = true
  try {
    // 模拟导出
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('数据导出成功')
  } catch {
    ElMessage.error('数据导出失败')
  } finally {
    option.loading = false
  }
}

const openLink = (url: string) => {
  window.open(url, '_blank')
}

const getSystemInfo = () => {
  const userAgent = navigator.userAgent
  
  // 检测浏览器
  let browser = 'Unknown'
  if (userAgent.includes('Chrome')) browser = 'Chrome'
  else if (userAgent.includes('Firefox')) browser = 'Firefox'
  else if (userAgent.includes('Safari')) browser = 'Safari'
  else if (userAgent.includes('Edge')) browser = 'Edge'
  
  // 检测操作系统
  let os = 'Unknown'
  if (userAgent.includes('Windows')) os = 'Windows'
  else if (userAgent.includes('Mac')) os = 'macOS'
  else if (userAgent.includes('Linux')) os = 'Linux'
  else if (userAgent.includes('Android')) os = 'Android'
  else if (userAgent.includes('iOS')) os = 'iOS'
  
  // 获取屏幕分辨率
  const resolution = `${screen.width} × ${screen.height}`
  
  systemInfo.value = {
    browser,
    os,
    resolution
  }
}

// 组件挂载
onMounted(() => {
  getSystemInfo()
})
</script>

<style scoped>
.settings-view {
  min-height: 100vh;
  background: var(--el-bg-color-page);
}

.page-header {
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: var(--spacing-lg) 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.page-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin: 0;
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.title-icon {
  color: var(--el-color-primary);
}

.page-description {
  margin: 0;
  color: var(--el-text-color-regular);
}

.settings-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-lg);
  display: flex;
  gap: var(--spacing-lg);
}

.settings-sidebar {
  width: 240px;
  flex-shrink: 0;
}

.settings-menu {
  border: none;
  background: var(--el-bg-color);
  border-radius: var(--border-radius-base);
  overflow: hidden;
}

.settings-main {
  flex: 1;
  min-width: 0;
}

.settings-section {
  width: 100%;
}

.section-header {
  margin-bottom: var(--spacing-lg);
}

.section-title {
  margin: 0 0 var(--spacing-sm) 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.section-description {
  margin: 0;
  color: var(--el-text-color-regular);
}

.settings-card {
  margin-bottom: var(--spacing-lg);
}

/* 个人资料样式 */
.profile-section {
  display: flex;
  gap: var(--spacing-xl);
}

.avatar-section {
  flex-shrink: 0;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-md);
}

.avatar-upload-btn {
  width: 100%;
}

.profile-form {
  flex: 1;
  max-width: 500px;
}

/* 安全设置样式 */
.security-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.security-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.security-item h3 {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.security-item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.item-description {
  margin: var(--spacing-xs) 0 0 0;
  color: var(--el-text-color-regular);
  font-size: 0.875rem;
}

.device-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.device-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  background: var(--el-fill-color-lighter);
  border-radius: var(--border-radius-base);
}

.device-info {
  flex: 1;
}

.device-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: var(--spacing-xs);
}

.device-details {
  font-size: 0.875rem;
  color: var(--el-text-color-regular);
}

/* 偏好设置样式 */
.preferences-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.preference-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.preference-header h3 {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.editor-preferences {
  max-width: 400px;
}

/* 通知设置样式 */
.notifications-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.notification-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: var(--spacing-md);
  background: var(--el-fill-color-lighter);
  border-radius: var(--border-radius-base);
}

.notification-info {
  flex: 1;
}

.notification-info h3 {
  margin: 0 0 var(--spacing-xs) 0;
  font-size: 1rem;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.notification-controls {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  align-items: flex-end;
}

/* 存储管理样式 */
.storage-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.storage-overview {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.storage-stats {
  display: flex;
  gap: var(--spacing-xl);
}

.storage-used,
.storage-total {
  text-align: center;
}

.storage-used h3,
.storage-total h3 {
  margin: 0 0 var(--spacing-xs) 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.storage-used p,
.storage-total p {
  margin: 0;
  color: var(--el-text-color-regular);
  font-size: 0.875rem;
}

.storage-details h3 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.storage-breakdown {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.storage-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  background: var(--el-fill-color-lighter);
  border-radius: var(--border-radius-base);
}

.storage-item-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.storage-item-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  border-radius: var(--border-radius-base);
}

.storage-item-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: var(--spacing-xs);
}

.storage-item-size {
  font-size: 0.875rem;
  color: var(--el-text-color-regular);
}

/* 导出设置样式 */
.export-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.export-options {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.export-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  background: var(--el-fill-color-lighter);
  border-radius: var(--border-radius-base);
}

.export-option-info {
  flex: 1;
}

.export-option-info h3 {
  margin: 0 0 var(--spacing-xs) 0;
  font-size: 1rem;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

/* 关于页面样式 */
.about-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.app-info {
  display: flex;
  gap: var(--spacing-lg);
  align-items: center;
}

.app-logo {
  flex-shrink: 0;
}

.logo-image {
  width: 64px;
  height: 64px;
}

.app-details h2 {
  margin: 0 0 var(--spacing-xs) 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.app-version {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--el-text-color-regular);
  font-size: 0.875rem;
}

.app-description {
  margin: 0;
  color: var(--el-text-color-regular);
}

.app-links {
  display: flex;
  gap: var(--spacing-md);
}

.system-info h3 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-md);
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--el-fill-color-lighter);
  border-radius: var(--border-radius-base);
}

.info-label {
  font-weight: 500;
  color: var(--el-text-color-regular);
}

.info-value {
  color: var(--el-text-color-primary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .settings-content {
    flex-direction: column;
    padding: var(--spacing-md);
  }
  
  .settings-sidebar {
    width: 100%;
  }
  
  .settings-menu {
    display: flex;
    overflow-x: auto;
  }
  
  .profile-section {
    flex-direction: column;
    align-items: center;
  }
  
  .app-info {
    flex-direction: column;
    text-align: center;
  }
  
  .storage-stats {
    justify-content: center;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>