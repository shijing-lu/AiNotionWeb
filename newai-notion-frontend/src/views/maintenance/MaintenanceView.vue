<template>
  <div class="maintenance-view">
    <!-- 维护状态展示 -->
    <div class="maintenance-container">
      <div class="maintenance-content">
        <!-- 维护图标 -->
        <div class="maintenance-icon">
          <el-icon class="icon-large">
            <Tools />
          </el-icon>
        </div>
        
        <!-- 维护信息 -->
        <div class="maintenance-info">
          <h1 class="maintenance-title">系统维护中</h1>
          <p class="maintenance-description">
            我们正在对系统进行升级维护，以提供更好的服务体验。
            <br>
            预计维护时间：{{ formatMaintenanceTime() }}
          </p>
          
          <!-- 维护进度 -->
          <div class="maintenance-progress">
            <div class="progress-info">
              <span class="progress-label">维护进度</span>
              <span class="progress-percentage">{{ maintenanceProgress }}%</span>
            </div>
            <el-progress 
              :percentage="maintenanceProgress" 
              :stroke-width="8"
              :show-text="false"
              class="progress-bar"
            />
          </div>
          
          <!-- 维护详情 -->
          <div class="maintenance-details">
            <h3>本次维护内容：</h3>
            <ul class="maintenance-list">
              <li 
                v-for="item in maintenanceItems" 
                :key="item.id"
                class="maintenance-item"
                :class="{ completed: item.completed }"
              >
                <el-icon class="item-icon">
                  <component :is="item.completed ? 'Check' : 'Clock'" />
                </el-icon>
                <span class="item-text">{{ item.text }}</span>
                <span v-if="item.completed" class="item-status">已完成</span>
                <span v-else class="item-status">进行中</span>
              </li>
            </ul>
          </div>
          
          <!-- 预计完成时间 -->
          <div class="maintenance-eta">
            <el-icon class="eta-icon"><Clock /></el-icon>
            <span class="eta-text">
              预计完成时间：{{ estimatedCompletionTime }}
            </span>
          </div>
          
          <!-- 联系信息 -->
          <div class="maintenance-contact">
            <h3>需要帮助？</h3>
            <div class="contact-methods">
              <div class="contact-item">
                <el-icon><Message /></el-icon>
                <span>邮箱：support@newai-notion.com</span>
              </div>
              <div class="contact-item">
                <el-icon><Phone /></el-icon>
                <span>电话：400-123-4567</span>
              </div>
              <div class="contact-item">
                <el-icon><ChatDotRound /></el-icon>
                <span>在线客服：周一至周五 9:00-18:00</span>
              </div>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="maintenance-actions">
            <el-button 
              type="primary" 
              size="large"
              @click="refreshStatus"
              :loading="refreshing"
            >
              <el-icon><Refresh /></el-icon>
              刷新状态
            </el-button>
            
            <el-button 
              size="large"
              @click="subscribeNotification"
            >
              <el-icon><Bell /></el-icon>
              订阅通知
            </el-button>
            
            <el-button 
              size="large"
              @click="viewStatusPage"
            >
              <el-icon><Link /></el-icon>
              状态页面
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 维护公告 -->
      <div class="maintenance-announcements">
        <h3 class="announcements-title">
          <el-icon><Bell /></el-icon>
          维护公告
        </h3>
        
        <div class="announcements-list">
          <div 
            v-for="announcement in announcements" 
            :key="announcement.id"
            class="announcement-item"
          >
            <div class="announcement-header">
              <span class="announcement-time">
                {{ formatRelativeTime(announcement.createdAt) }}
              </span>
              <el-tag 
                :type="getAnnouncementType(announcement.type)"
                size="small"
              >
                {{ announcement.type }}
              </el-tag>
            </div>
            <h4 class="announcement-title">{{ announcement.title }}</h4>
            <p class="announcement-content">{{ announcement.content }}</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 订阅通知对话框 -->
    <el-dialog 
      v-model="notificationDialogVisible" 
      title="订阅维护通知" 
      width="400px"
    >
      <el-form 
        ref="notificationFormRef" 
        :model="notificationForm" 
        :rules="notificationRules"
        label-width="80px"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="notificationForm.email" 
            placeholder="请输入邮箱地址"
          />
        </el-form-item>
        
        <el-form-item label="通知类型">
          <el-checkbox-group v-model="notificationForm.types">
            <el-checkbox label="start">维护开始</el-checkbox>
            <el-checkbox label="progress">进度更新</el-checkbox>
            <el-checkbox label="complete">维护完成</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="notificationDialogVisible = false">
          取消
        </el-button>
        <el-button 
          type="primary" 
          @click="submitNotificationSubscription"
          :loading="subscribing"
        >
          订阅
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import {
  Tools,
  Check,
  Clock,
  Message,
  Phone,
  ChatDotRound,
  Refresh,
  Bell,
  Link
} from '@element-plus/icons-vue'
import { formatRelativeTime } from '@/utils/common'

// 响应式数据
const maintenanceProgress = ref(65)
const refreshing = ref(false)
const notificationDialogVisible = ref(false)
const subscribing = ref(false)
const notificationFormRef = ref<FormInstance>()

// 维护项目列表
const maintenanceItems = ref([
  {
    id: 1,
    text: '数据库优化升级',
    completed: true
  },
  {
    id: 2,
    text: '服务器性能调优',
    completed: true
  },
  {
    id: 3,
    text: 'AI 模型更新',
    completed: false
  },
  {
    id: 4,
    text: '安全补丁安装',
    completed: false
  },
  {
    id: 5,
    text: '系统功能测试',
    completed: false
  }
])

// 维护公告
const announcements = ref([
  {
    id: 1,
    type: '维护通知',
    title: '系统维护开始',
    content: '我们已开始对系统进行计划内维护，预计持续2小时。',
    createdAt: new Date(Date.now() - 1000 * 60 * 30) // 30分钟前
  },
  {
    id: 2,
    type: '进度更新',
    title: '维护进度更新',
    content: '数据库优化已完成，正在进行服务器性能调优。',
    createdAt: new Date(Date.now() - 1000 * 60 * 15) // 15分钟前
  },
  {
    id: 3,
    type: '重要提醒',
    title: '数据安全保障',
    content: '维护期间您的数据完全安全，我们已做好完整备份。',
    createdAt: new Date(Date.now() - 1000 * 60 * 45) // 45分钟前
  }
])

// 通知订阅表单
const notificationForm = ref({
  email: '',
  types: ['complete']
})

// 表单验证规则
const notificationRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 预计完成时间
const estimatedCompletionTime = ref('2024-01-15 20:00')

// 定时器
let progressTimer: NodeJS.Timeout | null = null

/**
 * 格式化维护时间
 */
const formatMaintenanceTime = (): string => {
  const startTime = '18:00'
  const endTime = '20:00'
  return `${startTime} - ${endTime}`
}

/**
 * 获取公告类型样式
 */
const getAnnouncementType = (type: string): string => {
  const typeMap: Record<string, string> = {
    '维护通知': 'info',
    '进度更新': 'success',
    '重要提醒': 'warning',
    '紧急通知': 'danger'
  }
  return typeMap[type] || 'info'
}

/**
 * 刷新维护状态
 */
const refreshStatus = async (): Promise<void> => {
  refreshing.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 模拟进度更新
    const newProgress = Math.min(maintenanceProgress.value + Math.floor(Math.random() * 10), 100)
    maintenanceProgress.value = newProgress
    
    // 更新维护项目状态
    if (newProgress >= 80) {
      maintenanceItems.value[2].completed = true
    }
    if (newProgress >= 90) {
      maintenanceItems.value[3].completed = true
    }
    if (newProgress >= 100) {
      maintenanceItems.value[4].completed = true
      ElMessage.success('系统维护已完成！')
      // 可以在这里重定向到主页
      setTimeout(() => {
        window.location.href = '/'
      }, 2000)
    } else {
      ElMessage.success('状态已更新')
    }
  } catch (error) {
    ElMessage.error('刷新失败，请稍后重试')
  } finally {
    refreshing.value = false
  }
}

/**
 * 订阅通知
 */
const subscribeNotification = (): void => {
  notificationDialogVisible.value = true
}

/**
 * 提交通知订阅
 */
const submitNotificationSubscription = async (): Promise<void> => {
  if (!notificationFormRef.value) return
  
  try {
    await notificationFormRef.value.validate()
    
    subscribing.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('订阅成功！我们会及时通知您维护进展')
    notificationDialogVisible.value = false
    
    // 重置表单
    notificationForm.value = {
      email: '',
      types: ['complete']
    }
  } catch {
    ElMessage.error('请检查输入信息')
  } finally {
    subscribing.value = false
  }
}

/**
 * 查看状态页面
 */
const viewStatusPage = (): void => {
  // 打开状态页面
  window.open('https://status.newai-notion.com', '_blank')
}

/**
 * 模拟进度更新
 */
const simulateProgress = (): void => {
  progressTimer = setInterval(() => {
    if (maintenanceProgress.value < 100) {
      // 随机增加1-3%的进度
      const increment = Math.floor(Math.random() * 3) + 1
      maintenanceProgress.value = Math.min(maintenanceProgress.value + increment, 100)
      
      // 根据进度更新维护项目状态
      if (maintenanceProgress.value >= 75 && !maintenanceItems.value[2].completed) {
        maintenanceItems.value[2].completed = true
        addProgressAnnouncement('AI 模型更新完成')
      }
      if (maintenanceProgress.value >= 85 && !maintenanceItems.value[3].completed) {
        maintenanceItems.value[3].completed = true
        addProgressAnnouncement('安全补丁安装完成')
      }
      if (maintenanceProgress.value >= 95 && !maintenanceItems.value[4].completed) {
        maintenanceItems.value[4].completed = true
        addProgressAnnouncement('系统功能测试完成')
      }
      
      // 维护完成
      if (maintenanceProgress.value >= 100) {
        addProgressAnnouncement('系统维护全部完成，即将恢复服务')
        clearInterval(progressTimer!)
        
        // 3秒后重定向到主页
        setTimeout(() => {
          ElMessage.success('维护完成，正在跳转到主页...')
          window.location.href = '/'
        }, 3000)
      }
    }
  }, 10000) // 每10秒更新一次
}

/**
 * 添加进度公告
 */
const addProgressAnnouncement = (content: string): void => {
  const newAnnouncement = {
    id: Date.now(),
    type: '进度更新',
    title: '维护进度更新',
    content,
    createdAt: new Date()
  }
  
  announcements.value.unshift(newAnnouncement)
  
  // 只保留最新的5条公告
  if (announcements.value.length > 5) {
    announcements.value = announcements.value.slice(0, 5)
  }
}

// 组件挂载
onMounted(() => {
  // 开始模拟进度更新
  simulateProgress()
  
  // 设置页面标题
  document.title = '系统维护中 - NewAI Notion'
})

// 组件卸载
onUnmounted(() => {
  if (progressTimer) {
    clearInterval(progressTimer)
  }
})
</script>

<style scoped>
.maintenance-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
}

.maintenance-container {
  max-width: 800px;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.maintenance-content {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: var(--border-radius-large);
  padding: var(--spacing-xxl);
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.maintenance-icon {
  margin-bottom: var(--spacing-xl);
}

.icon-large {
  font-size: 4rem;
  color: var(--el-color-primary);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.maintenance-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.maintenance-title {
  margin: 0;
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--el-text-color-primary);
  margin-bottom: var(--spacing-md);
}

.maintenance-description {
  font-size: 1.125rem;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  margin: 0;
}

.maintenance-progress {
  text-align: left;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.progress-label {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.progress-percentage {
  font-weight: 700;
  color: var(--el-color-primary);
  font-size: 1.125rem;
}

.progress-bar {
  margin-bottom: var(--spacing-md);
}

.maintenance-details {
  text-align: left;
}

.maintenance-details h3 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.maintenance-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.maintenance-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  background: var(--el-fill-color-lighter);
  border-radius: var(--border-radius-base);
  transition: all 0.3s;
}

.maintenance-item.completed {
  background: var(--el-color-success-light-9);
  border: 1px solid var(--el-color-success-light-7);
}

.item-icon {
  color: var(--el-text-color-secondary);
}

.maintenance-item.completed .item-icon {
  color: var(--el-color-success);
}

.item-text {
  flex: 1;
  color: var(--el-text-color-primary);
}

.maintenance-item.completed .item-text {
  color: var(--el-color-success);
}

.item-status {
  font-size: 0.875rem;
  color: var(--el-text-color-secondary);
}

.maintenance-item.completed .item-status {
  color: var(--el-color-success);
  font-weight: 600;
}

.maintenance-eta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  background: var(--el-color-info-light-9);
  border-radius: var(--border-radius-base);
  border: 1px solid var(--el-color-info-light-7);
}

.eta-icon {
  color: var(--el-color-info);
}

.eta-text {
  font-weight: 600;
  color: var(--el-color-info);
}

.maintenance-contact {
  text-align: left;
}

.maintenance-contact h3 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.contact-methods {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.contact-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  background: var(--el-fill-color-lighter);
  border-radius: var(--border-radius-base);
}

.contact-item .el-icon {
  color: var(--el-color-primary);
}

.maintenance-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
  flex-wrap: wrap;
}

.maintenance-announcements {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: var(--border-radius-large);
  padding: var(--spacing-xl);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.announcements-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin: 0 0 var(--spacing-lg) 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.announcements-title .el-icon {
  color: var(--el-color-warning);
}

.announcements-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.announcement-item {
  padding: var(--spacing-lg);
  background: var(--el-fill-color-lighter);
  border-radius: var(--border-radius-base);
  border-left: 4px solid var(--el-color-primary);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.announcement-time {
  font-size: 0.875rem;
  color: var(--el-text-color-secondary);
}

.announcement-title {
  margin: 0 0 var(--spacing-sm) 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.announcement-content {
  margin: 0;
  color: var(--el-text-color-regular);
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .maintenance-view {
    padding: var(--spacing-md);
  }
  
  .maintenance-content {
    padding: var(--spacing-xl);
  }
  
  .maintenance-title {
    font-size: 2rem;
  }
  
  .maintenance-description {
    font-size: 1rem;
  }
  
  .maintenance-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .contact-methods {
    gap: var(--spacing-xs);
  }
  
  .contact-item {
    flex-direction: column;
    align-items: flex-start;
    text-align: left;
  }
}

@media (max-width: 480px) {
  .maintenance-title {
    font-size: 1.75rem;
  }
  
  .icon-large {
    font-size: 3rem;
  }
  
  .progress-info {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-xs);
  }
}

/* 深色主题适配 */
:global(.dark) .maintenance-content,
:global(.dark) .maintenance-announcements {
  background: rgba(0, 0, 0, 0.8);
  color: var(--el-text-color-primary);
}

:global(.dark) .maintenance-item,
:global(.dark) .contact-item,
:global(.dark) .announcement-item {
  background: var(--el-fill-color-dark);
}

:global(.dark) .maintenance-item.completed {
  background: rgba(103, 194, 58, 0.2);
}
</style>