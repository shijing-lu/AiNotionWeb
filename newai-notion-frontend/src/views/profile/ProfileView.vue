<template>
  <div class="profile-view">
    <!-- 页面头部 -->
    <div class="profile-header">
      <div class="header-content">
        <div class="avatar-section">
          <el-avatar 
            :size="120" 
            :src="userProfile.avatar" 
            class="profile-avatar"
          >
            <el-icon><User /></el-icon>
          </el-avatar>
          <el-button 
            type="primary" 
            size="small" 
            class="change-avatar-btn"
            @click="changeAvatar"
          >
            <el-icon><Camera /></el-icon>
            更换头像
          </el-button>
        </div>
        
        <div class="profile-info">
          <h1 class="profile-name">{{ userProfile.name }}</h1>
          <p class="profile-title">{{ userProfile.title }}</p>
          <p class="profile-bio">{{ userProfile.bio }}</p>
          
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-number">{{ userProfile.stats.documents }}</span>
              <span class="stat-label">文档</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ userProfile.stats.followers }}</span>
              <span class="stat-label">关注者</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ userProfile.stats.following }}</span>
              <span class="stat-label">关注中</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ userProfile.stats.likes }}</span>
              <span class="stat-label">获赞</span>
            </div>
          </div>
          
          <div class="profile-actions">
            <el-button type="primary" @click="editProfile">
              <el-icon><Edit /></el-icon>
              编辑资料
            </el-button>
            <el-button @click="shareProfile">
              <el-icon><Share /></el-icon>
              分享
            </el-button>
            <el-button @click="exportData">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 标签页内容 -->
    <div class="profile-content">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- 我的文档 -->
        <el-tab-pane label="我的文档" name="documents">
          <div class="documents-section">
            <div class="section-header">
              <h3>我的文档</h3>
              <div class="header-actions">
                <el-select 
                  v-model="documentsFilter" 
                  placeholder="筛选类型"
                  size="small"
                  style="width: 120px"
                >
                  <el-option label="全部" value="all" />
                  <el-option label="最近编辑" value="recent" />
                  <el-option label="我创建的" value="created" />
                  <el-option label="我协作的" value="collaborated" />
                </el-select>
                
                <el-select 
                  v-model="documentsSortBy" 
                  placeholder="排序方式"
                  size="small"
                  style="width: 120px"
                >
                  <el-option label="最新修改" value="updated" />
                  <el-option label="创建时间" value="created" />
                  <el-option label="标题" value="title" />
                  <el-option label="大小" value="size" />
                </el-select>
              </div>
            </div>
            
            <div v-if="loading.documents" class="loading-container">
              <LoadingSpinner message="加载文档中..." />
            </div>
            
            <div v-else-if="filteredDocuments.length === 0" class="empty-container">
              <EmptyState 
                icon="Document"
                title="暂无文档"
                description="开始创建你的第一个文档吧"
                action-text="新建文档"
                @action="createDocument"
              />
            </div>
            
            <div v-else class="documents-grid">
              <div 
                v-for="doc in filteredDocuments" 
                :key="doc.id"
                class="document-card"
                @click="openDocument(doc)"
              >
                <div class="document-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="document-info">
                  <h4 class="document-title">{{ doc.title }}</h4>
                  <p class="document-description">{{ doc.description }}</p>
                  <div class="document-meta">
                    <span class="document-date">
                      {{ formatRelativeTime(doc.updatedAt) }}
                    </span>
                    <span class="document-size">{{ formatFileSize(doc.size) }}</span>
                  </div>
                </div>
                <div class="document-actions">
                  <el-dropdown trigger="click" @click.stop>
                    <el-button text>
                      <el-icon><MoreFilled /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item @click="editDocument(doc)">
                          <el-icon><Edit /></el-icon>
                          编辑
                        </el-dropdown-item>
                        <el-dropdown-item @click="shareDocument(doc)">
                          <el-icon><Share /></el-icon>
                          分享
                        </el-dropdown-item>
                        <el-dropdown-item @click="duplicateDocument(doc)">
                          <el-icon><CopyDocument /></el-icon>
                          复制
                        </el-dropdown-item>
                        <el-dropdown-item divided @click="deleteDocument(doc)">
                          <el-icon><Delete /></el-icon>
                          删除
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 活动记录 -->
        <el-tab-pane label="活动记录" name="activity">
          <div class="activity-section">
            <div class="section-header">
              <h3>最近活动</h3>
              <el-button size="small" @click="clearActivity">
                清空记录
              </el-button>
            </div>
            
            <div v-if="loading.activity" class="loading-container">
              <LoadingSpinner message="加载活动记录中..." />
            </div>
            
            <div v-else-if="activities.length === 0" class="empty-container">
              <EmptyState 
                icon="Clock"
                title="暂无活动记录"
                description="你的操作记录将显示在这里"
              />
            </div>
            
            <div v-else class="activity-timeline">
              <div 
                v-for="activity in activities" 
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-icon">
                  <el-icon :class="`activity-${activity.type}`">
                    <component :is="getActivityIcon(activity.type)" />
                  </el-icon>
                </div>
                <div class="activity-content">
                  <div class="activity-header">
                    <span class="activity-action">{{ activity.action }}</span>
                    <span class="activity-time">
                      {{ formatRelativeTime(activity.createdAt) }}
                    </span>
                  </div>
                  <p class="activity-description">{{ activity.description }}</p>
                  <div v-if="activity.target" class="activity-target">
                    <el-link 
                      :href="activity.target.url" 
                      type="primary"
                      :underline="false"
                    >
                      {{ activity.target.title }}
                    </el-link>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 收藏夹 -->
        <el-tab-pane label="收藏夹" name="favorites">
          <div class="favorites-section">
            <div class="section-header">
              <h3>我的收藏</h3>
              <el-button size="small" @click="clearFavorites">
                清空收藏
              </el-button>
            </div>
            
            <div v-if="loading.favorites" class="loading-container">
              <LoadingSpinner message="加载收藏中..." />
            </div>
            
            <div v-else-if="favorites.length === 0" class="empty-container">
              <EmptyState 
                icon="Star"
                title="暂无收藏"
                description="收藏的内容将显示在这里"
              />
            </div>
            
            <div v-else class="favorites-grid">
              <div 
                v-for="item in favorites" 
                :key="item.id"
                class="favorite-card"
                @click="openFavorite(item)"
              >
                <div class="favorite-icon">
                  <el-icon>
                    <component :is="getFavoriteIcon(item.type)" />
                  </el-icon>
                </div>
                <div class="favorite-info">
                  <h4 class="favorite-title">{{ item.title }}</h4>
                  <p class="favorite-description">{{ item.description }}</p>
                  <div class="favorite-meta">
                    <span class="favorite-type">{{ item.type }}</span>
                    <span class="favorite-date">
                      {{ formatRelativeTime(item.createdAt) }}
                    </span>
                  </div>
                </div>
                <div class="favorite-actions">
                  <el-button 
                    text 
                    @click.stop="removeFavorite(item)"
                  >
                    <el-icon><StarFilled /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 设置 -->
        <el-tab-pane label="设置" name="settings">
          <div class="settings-section">
            <div class="section-header">
              <h3>个人设置</h3>
            </div>
            
            <div class="settings-form">
              <el-form 
                ref="settingsFormRef" 
                :model="settingsForm" 
                :rules="settingsRules"
                label-width="120px"
              >
                <el-form-item label="显示名称" prop="displayName">
                  <el-input 
                    v-model="settingsForm.displayName" 
                    placeholder="请输入显示名称"
                  />
                </el-form-item>
                
                <el-form-item label="个人简介" prop="bio">
                  <el-input 
                    v-model="settingsForm.bio" 
                    type="textarea" 
                    :rows="3"
                    placeholder="介绍一下自己吧"
                    maxlength="200"
                    show-word-limit
                  />
                </el-form-item>
                
                <el-form-item label="职位/标题" prop="title">
                  <el-input 
                    v-model="settingsForm.title" 
                    placeholder="请输入职位或标题"
                  />
                </el-form-item>
                
                <el-form-item label="联系邮箱" prop="email">
                  <el-input 
                    v-model="settingsForm.email" 
                    placeholder="请输入邮箱地址"
                  />
                </el-form-item>
                
                <el-form-item label="个人网站" prop="website">
                  <el-input 
                    v-model="settingsForm.website" 
                    placeholder="请输入个人网站地址"
                  />
                </el-form-item>
                
                <el-form-item label="隐私设置">
                  <div class="privacy-settings">
                    <div class="privacy-item">
                      <el-checkbox v-model="settingsForm.privacy.showEmail">
                        公开邮箱地址
                      </el-checkbox>
                    </div>
                    <div class="privacy-item">
                      <el-checkbox v-model="settingsForm.privacy.showActivity">
                        公开活动记录
                      </el-checkbox>
                    </div>
                    <div class="privacy-item">
                      <el-checkbox v-model="settingsForm.privacy.allowFollow">
                        允许他人关注
                      </el-checkbox>
                    </div>
                  </div>
                </el-form-item>
                
                <el-form-item>
                  <el-button 
                    type="primary" 
                    @click="saveSettings"
                    :loading="saving"
                  >
                    保存设置
                  </el-button>
                  <el-button @click="resetSettings">
                    重置
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  User,
  Camera,
  Edit,
  Share,
  Download,
  Document,
  MoreFilled,
  CopyDocument,
  Delete,
  Clock,
  Star,
  StarFilled,
  Plus,
  Folder,
  Link
} from '@element-plus/icons-vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { formatRelativeTime, formatFileSize } from '@/utils/common'

// 路由
const router = useRouter()

// 响应式数据
const activeTab = ref('documents')
const documentsFilter = ref('all')
const documentsSortBy = ref('updated')
const saving = ref(false)
const settingsFormRef = ref<FormInstance>()

// 加载状态
const loading = ref({
  documents: false,
  activity: false,
  favorites: false
})

// 用户资料
const userProfile = ref({
  id: '1',
  name: '张三',
  title: '产品经理',
  bio: '热爱创新，专注于用户体验设计和产品策略。',
  avatar: '',
  email: 'zhangsan@example.com',
  website: 'https://zhangsan.dev',
  stats: {
    documents: 42,
    followers: 128,
    following: 89,
    likes: 256
  }
})

// 文档列表
const documents = ref([
  {
    id: '1',
    title: '产品需求文档',
    description: '新功能的详细需求说明和设计方案',
    size: 1024 * 1024 * 2.5, // 2.5MB
    updatedAt: new Date(Date.now() - 1000 * 60 * 30), // 30分钟前
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 3), // 3天前
    type: 'created'
  },
  {
    id: '2',
    title: '会议纪要',
    description: '周例会讨论内容和行动项',
    size: 1024 * 512, // 512KB
    updatedAt: new Date(Date.now() - 1000 * 60 * 60 * 2), // 2小时前
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24), // 1天前
    type: 'collaborated'
  },
  {
    id: '3',
    title: '项目计划',
    description: 'Q4季度项目规划和里程碑',
    size: 1024 * 1024 * 1.2, // 1.2MB
    updatedAt: new Date(Date.now() - 1000 * 60 * 60 * 24), // 1天前
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 7), // 1周前
    type: 'created'
  }
])

// 活动记录
const activities = ref([
  {
    id: '1',
    type: 'create',
    action: '创建了文档',
    description: '新建了产品需求文档',
    createdAt: new Date(Date.now() - 1000 * 60 * 30),
    target: {
      title: '产品需求文档',
      url: '/documents/1'
    }
  },
  {
    id: '2',
    type: 'edit',
    action: '编辑了文档',
    description: '更新了会议纪要的内容',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2),
    target: {
      title: '会议纪要',
      url: '/documents/2'
    }
  },
  {
    id: '3',
    type: 'share',
    action: '分享了文档',
    description: '将项目计划分享给团队成员',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24),
    target: {
      title: '项目计划',
      url: '/documents/3'
    }
  }
])

// 收藏列表
const favorites = ref([
  {
    id: '1',
    type: 'document',
    title: '设计规范',
    description: 'UI/UX设计规范和组件库',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 2)
  },
  {
    id: '2',
    type: 'template',
    title: '项目模板',
    description: '标准项目管理模板',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 5)
  },
  {
    id: '3',
    type: 'link',
    title: '参考资料',
    description: '行业最佳实践案例',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 7)
  }
])

// 设置表单
const settingsForm = ref({
  displayName: userProfile.value.name,
  bio: userProfile.value.bio,
  title: userProfile.value.title,
  email: userProfile.value.email,
  website: userProfile.value.website,
  privacy: {
    showEmail: false,
    showActivity: true,
    allowFollow: true
  }
})

// 表单验证规则
const settingsRules = {
  displayName: [
    { required: true, message: '请输入显示名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  website: [
    { type: 'url', message: '请输入正确的网址', trigger: 'blur' }
  ]
}

// 计算属性
const filteredDocuments = computed(() => {
  let filtered = documents.value
  
  // 按类型筛选
  if (documentsFilter.value !== 'all') {
    if (documentsFilter.value === 'recent') {
      const oneDayAgo = new Date(Date.now() - 1000 * 60 * 60 * 24)
      filtered = filtered.filter(doc => doc.updatedAt > oneDayAgo)
    } else {
      filtered = filtered.filter(doc => doc.type === documentsFilter.value)
    }
  }
  
  // 排序
  filtered.sort((a, b) => {
    switch (documentsSortBy.value) {
      case 'updated':
        return b.updatedAt.getTime() - a.updatedAt.getTime()
      case 'created':
        return b.createdAt.getTime() - a.createdAt.getTime()
      case 'title':
        return a.title.localeCompare(b.title)
      case 'size':
        return b.size - a.size
      default:
        return 0
    }
  })
  
  return filtered
})

/**
 * 获取活动图标
 */
const getActivityIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    create: Plus,
    edit: Edit,
    share: Share,
    delete: Delete,
    move: Folder
  }
  return iconMap[type] || Document
}

/**
 * 获取收藏图标
 */
const getFavoriteIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    document: Document,
    template: CopyDocument,
    link: Link,
    folder: Folder
  }
  return iconMap[type] || Star
}

/**
 * 更换头像
 */
const changeAvatar = () => {
  // 创建文件输入元素
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  
  input.onchange = (event) => {
    const file = (event.target as HTMLInputElement).files?.[0]
    if (file) {
      // 这里应该上传文件到服务器
      const reader = new FileReader()
      reader.onload = (e) => {
        userProfile.value.avatar = e.target?.result as string
        ElMessage.success('头像更新成功')
      }
      reader.readAsDataURL(file)
    }
  }
  
  input.click()
}

/**
 * 编辑资料
 */
const editProfile = () => {
  activeTab.value = 'settings'
}

/**
 * 分享资料
 */
const shareProfile = async () => {
  const profileUrl = `${window.location.origin}/profile/${userProfile.value.id}`
  
  try {
    await navigator.clipboard.writeText(profileUrl)
    ElMessage.success('个人资料链接已复制到剪贴板')
  } catch {
    ElMessage.warning('复制失败，请手动复制链接')
  }
}

/**
 * 导出数据
 */
const exportData = () => {
  const data = {
    profile: userProfile.value,
    documents: documents.value,
    activities: activities.value,
    favorites: favorites.value,
    exportTime: new Date().toISOString()
  }
  
  const dataStr = JSON.stringify(data, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(dataBlob)
  
  const link = document.createElement('a')
  link.href = url
  link.download = `profile-data-${new Date().toISOString().split('T')[0]}.json`
  link.click()
  
  URL.revokeObjectURL(url)
  ElMessage.success('数据导出成功')
}

/**
 * 创建文档 - 直接进入编辑模式
 */
const createDocument = () => {
  router.push({
    name: 'ArticleEdit',
    params: { id: 'new' },
    query: {
      title: '无标题文档',
      visibility: 'PRIVATE'
    }
  })
}

/**
 * 打开文档
 */
const openDocument = (doc: any) => {
  router.push(`/editor/${doc.id}`)
}

/**
 * 编辑文档
 */
const editDocument = (doc: any) => {
  router.push(`/editor/${doc.id}`)
}

/**
 * 分享文档
 */
const shareDocument = async (doc: any) => {
  const shareUrl = `${window.location.origin}/documents/${doc.id}`
  
  try {
    await navigator.clipboard.writeText(shareUrl)
    ElMessage.success('文档链接已复制到剪贴板')
  } catch {
    ElMessage.warning('复制失败，请手动复制链接')
  }
}

/**
 * 复制文档
 */
const duplicateDocument = (doc: any) => {
  const newDoc = {
    ...doc,
    id: Date.now().toString(),
    title: `${doc.title} (副本)`,
    createdAt: new Date(),
    updatedAt: new Date()
  }
  
  documents.value.unshift(newDoc)
  ElMessage.success('文档复制成功')
}

/**
 * 删除文档
 */
const deleteDocument = async (doc: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档 "${doc.title}" 吗？此操作不可撤销。`,
      '删除文档',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    const index = documents.value.findIndex(d => d.id === doc.id)
    if (index > -1) {
      documents.value.splice(index, 1)
      ElMessage.success('文档删除成功')
    }
  } catch {
    // 用户取消
  }
}

/**
 * 清空活动记录
 */
const clearActivity = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有活动记录吗？',
      '清空记录',
      {
        type: 'warning',
        confirmButtonText: '确定清空',
        cancelButtonText: '取消'
      }
    )
    
    activities.value = []
    ElMessage.success('活动记录已清空')
  } catch {
    // 用户取消
  }
}

/**
 * 打开收藏
 */
const openFavorite = (item: any) => {
  if (item.type === 'document') {
    router.push(`/documents/${item.id}`)
  } else if (item.type === 'template') {
    router.push(`/templates/${item.id}`)
  } else if (item.type === 'link') {
    window.open(item.url, '_blank')
  }
}

/**
 * 取消收藏
 */
const removeFavorite = (item: any) => {
  const index = favorites.value.findIndex(f => f.id === item.id)
  if (index > -1) {
    favorites.value.splice(index, 1)
    ElMessage.success('已取消收藏')
  }
}

/**
 * 清空收藏
 */
const clearFavorites = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有收藏吗？',
      '清空收藏',
      {
        type: 'warning',
        confirmButtonText: '确定清空',
        cancelButtonText: '取消'
      }
    )
    
    favorites.value = []
    ElMessage.success('收藏已清空')
  } catch {
    // 用户取消
  }
}

/**
 * 保存设置
 */
const saveSettings = async () => {
  if (!settingsFormRef.value) return
  
  try {
    await settingsFormRef.value.validate()
    
    saving.value = true
    
    // 模拟保存延迟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新用户资料
    userProfile.value.name = settingsForm.value.displayName
    userProfile.value.bio = settingsForm.value.bio
    userProfile.value.title = settingsForm.value.title
    userProfile.value.email = settingsForm.value.email
    userProfile.value.website = settingsForm.value.website
    
    ElMessage.success('设置保存成功')
  } catch {
    ElMessage.error('请检查输入信息')
  } finally {
    saving.value = false
  }
}

/**
 * 重置设置
 */
const resetSettings = () => {
  settingsForm.value = {
    displayName: userProfile.value.name,
    bio: userProfile.value.bio,
    title: userProfile.value.title,
    email: userProfile.value.email,
    website: userProfile.value.website,
    privacy: {
      showEmail: false,
      showActivity: true,
      allowFollow: true
    }
  }
  
  ElMessage.info('设置已重置')
}

// 组件挂载
onMounted(() => {
  // 可以在这里加载用户数据
})
</script>

<style scoped>
.profile-view {
  min-height: 100vh;
  background: var(--el-bg-color-page);
}

.profile-header {
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: var(--spacing-xl) 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  display: flex;
  gap: var(--spacing-xl);
  align-items: flex-start;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-md);
}

.profile-avatar {
  border: 4px solid var(--el-color-primary-light-8);
}

.change-avatar-btn {
  font-size: 0.875rem;
}

.profile-info {
  flex: 1;
}

.profile-name {
  margin: 0 0 var(--spacing-sm) 0;
  font-size: 2rem;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.profile-title {
  margin: 0 0 var(--spacing-md) 0;
  font-size: 1.125rem;
  color: var(--el-color-primary);
  font-weight: 500;
}

.profile-bio {
  margin: 0 0 var(--spacing-lg) 0;
  font-size: 1rem;
  color: var(--el-text-color-regular);
  line-height: 1.6;
}

.profile-stats {
  display: flex;
  gap: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--el-text-color-regular);
}

.profile-actions {
  display: flex;
  gap: var(--spacing-md);
}

.profile-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.profile-tabs {
  background: var(--el-bg-color);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.section-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.header-actions {
  display: flex;
  gap: var(--spacing-md);
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

/* 文档网格样式 */
.documents-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--spacing-lg);
}

.document-card {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--el-fill-color-lighter);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: var(--border-radius-base);
  cursor: pointer;
  transition: all 0.2s;
}

.document-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: var(--el-box-shadow-light);
}

.document-icon {
  color: var(--el-color-primary);
  font-size: 1.5rem;
}

.document-info {
  flex: 1;
  min-width: 0;
}

.document-title {
  margin: 0 0 var(--spacing-xs) 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.document-description {
  margin: 0 0 var(--spacing-sm) 0;
  font-size: 0.875rem;
  color: var(--el-text-color-regular);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.document-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: var(--el-text-color-secondary);
}

.document-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.document-card:hover .document-actions {
  opacity: 1;
}

/* 活动时间线样式 */
.activity-timeline {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.activity-item {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--el-fill-color-lighter);
  border-radius: var(--border-radius-base);
  border-left: 4px solid var(--el-color-primary);
}

.activity-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xs);
}

.activity-action {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.activity-time {
  font-size: 0.875rem;
  color: var(--el-text-color-secondary);
}

.activity-description {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--el-text-color-regular);
  line-height: 1.4;
}

.activity-target {
  font-size: 0.875rem;
}

/* 收藏网格样式 */
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-lg);
}

.favorite-card {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--el-fill-color-lighter);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: var(--border-radius-base);
  cursor: pointer;
  transition: all 0.2s;
}

.favorite-card:hover {
  border-color: var(--el-color-warning);
  box-shadow: var(--el-box-shadow-light);
}

.favorite-icon {
  color: var(--el-color-warning);
  font-size: 1.25rem;
}

.favorite-info {
  flex: 1;
  min-width: 0;
}

.favorite-title {
  margin: 0 0 var(--spacing-xs) 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorite-description {
  margin: 0 0 var(--spacing-sm) 0;
  font-size: 0.875rem;
  color: var(--el-text-color-regular);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.favorite-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: var(--el-text-color-secondary);
}

.favorite-type {
  background: var(--el-color-warning-light-8);
  color: var(--el-color-warning);
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 0.75rem;
}

.favorite-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.favorite-card:hover .favorite-actions {
  opacity: 1;
}

/* 设置表单样式 */
.settings-form {
  max-width: 600px;
}

.privacy-settings {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.privacy-item {
  padding: var(--spacing-sm) 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: var(--spacing-lg);
  }
  
  .profile-stats {
    justify-content: center;
  }
  
  .profile-actions {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .documents-grid,
  .favorites-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>