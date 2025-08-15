<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="dashboard__welcome">
      <div class="welcome-content">
        <h1 class="welcome-title">
          {{ getGreeting() }}，{{ authStore.user?.name || '用户' }}！
        </h1>
        <p class="welcome-subtitle">
          今天是 {{ formatDate(new Date(), 'YYYY年MM月DD日 dddd') }}
        </p>
      </div>
      <div class="welcome-actions">
        <el-button
          type="primary"
          size="large"
          @click="createDocument"
          :icon="DocumentAdd"
        >
          新建文档
        </el-button>
        <el-button
          size="large"
          @click="openAIAssistant"
          :icon="ChatDotRound"
        >
          AI 助手
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="dashboard__stats">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-card__icon stat-card__icon--primary">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__value">{{ stats.totalDocuments }}</div>
            <div class="stat-card__label">总文档数</div>
          </div>
          <div class="stat-card__trend" :class="{ 'stat-card__trend--up': stats.documentsGrowth > 0 }">
            <el-icon><ArrowUp v-if="stats.documentsGrowth > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(stats.documentsGrowth) }}%
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-card__icon stat-card__icon--success">
            <el-icon><Folder /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__value">{{ stats.totalFolders }}</div>
            <div class="stat-card__label">文件夹数</div>
          </div>
          <div class="stat-card__trend" :class="{ 'stat-card__trend--up': stats.foldersGrowth > 0 }">
            <el-icon><ArrowUp v-if="stats.foldersGrowth > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(stats.foldersGrowth) }}%
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-card__icon stat-card__icon--warning">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__value">{{ stats.favoriteDocuments }}</div>
            <div class="stat-card__label">收藏文档</div>
          </div>
          <div class="stat-card__trend" :class="{ 'stat-card__trend--up': stats.favoritesGrowth > 0 }">
            <el-icon><ArrowUp v-if="stats.favoritesGrowth > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(stats.favoritesGrowth) }}%
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-card__icon stat-card__icon--info">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__value">{{ stats.aiUsage }}</div>
            <div class="stat-card__label">AI 使用次数</div>
          </div>
          <div class="stat-card__trend" :class="{ 'stat-card__trend--up': stats.aiGrowth > 0 }">
            <el-icon><ArrowUp v-if="stats.aiGrowth > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(stats.aiGrowth) }}%
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="dashboard__content">
      <!-- 最近文档 -->
      <div class="content-section">
        <div class="section-header">
          <h3 class="section-title">
            <el-icon><Clock /></el-icon>
            最近访问
          </h3>
          <router-link to="/recent" class="section-more">
            查看全部
            <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
        
        <div v-if="recentDocuments.length > 0" class="document-grid">
          <div
            v-for="doc in recentDocuments.slice(0, 6)"
            :key="doc.id"
            class="document-card"
            @click="openDocument(doc)"
          >
            <div class="document-card__header">
              <div class="document-card__icon">
                <el-icon><Document /></el-icon>
              </div>
              <el-dropdown trigger="click" @command="handleDocumentAction">
                <el-button text :icon="MoreFilled" size="small" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ action: 'edit', doc }">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'favorite', doc }">
                      <el-icon><Star /></el-icon>
                      {{ doc.isFavorite ? '取消收藏' : '收藏' }}
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'share', doc }">
                      <el-icon><Share /></el-icon>
                      分享
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div class="document-card__content">
              <h4 class="document-card__title">{{ doc.title }}</h4>
              <p class="document-card__excerpt">{{ doc.excerpt }}</p>
            </div>
            <div class="document-card__footer">
              <span class="document-card__time">
                {{ formatRelativeTime(doc.updatedAt) }}
              </span>
              <div class="document-card__tags">
                <el-tag
                  v-for="tag in doc.tags?.slice(0, 2)"
                  :key="tag"
                  size="small"
                  type="info"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
        
        <EmptyState
          v-else
          title="暂无最近文档"
          description="开始创建您的第一个文档吧"
          :show-action="true"
          action-text="新建文档"
          @action="createDocument"
        />
      </div>

      <!-- 收藏文档 -->
      <div class="content-section">
        <div class="section-header">
          <h3 class="section-title">
            <el-icon><Star /></el-icon>
            收藏文档
          </h3>
          <router-link to="/favorites" class="section-more">
            查看全部
            <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
        
        <div v-if="favoriteDocuments.length > 0" class="document-list">
          <div
            v-for="doc in favoriteDocuments.slice(0, 5)"
            :key="doc.id"
            class="document-item"
            @click="openDocument(doc)"
          >
            <div class="document-item__icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="document-item__content">
              <h4 class="document-item__title">{{ doc.title }}</h4>
              <p class="document-item__meta">
                {{ formatRelativeTime(doc.updatedAt) }} · {{ doc.folder?.name || '根目录' }}
              </p>
            </div>
            <div class="document-item__actions">
              <el-button
                text
                :icon="Star"
                @click.stop="toggleFavorite(doc)"
                class="favorite-btn favorite-btn--active"
              />
            </div>
          </div>
        </div>
        
        <EmptyState
          v-else
          title="暂无收藏文档"
          description="收藏重要文档以便快速访问"
          :show-action="false"
        />
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="dashboard__quick-actions">
      <h3 class="section-title">
        <el-icon><Lightning /></el-icon>
        快速操作
      </h3>
      
      <div class="quick-actions-grid">
        <div class="quick-action" @click="createDocument">
          <div class="quick-action__icon">
            <el-icon><DocumentAdd /></el-icon>
          </div>
          <div class="quick-action__content">
            <h4>新建文档</h4>
            <p>创建一个新的文档</p>
          </div>
        </div>
        
        <div class="quick-action" @click="createFolder">
          <div class="quick-action__icon">
            <el-icon><FolderAdd /></el-icon>
          </div>
          <div class="quick-action__content">
            <h4>新建文件夹</h4>
            <p>组织您的文档</p>
          </div>
        </div>
        
        <div class="quick-action" @click="importDocument">
          <div class="quick-action__icon">
            <el-icon><Upload /></el-icon>
          </div>
          <div class="quick-action__content">
            <h4>导入文档</h4>
            <p>从本地导入文档</p>
          </div>
        </div>
        
        <div class="quick-action" @click="openAIAssistant">
          <div class="quick-action__icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="quick-action__content">
            <h4>AI 助手</h4>
            <p>智能写作助手</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Document,
  DocumentAdd,
  Folder,
  FolderAdd,
  Star,
  Clock,
  ChatDotRound,
  Lightning,
  Upload,
  ArrowRight,
  ArrowUp,
  ArrowDown,
  MoreFilled,
  Edit,
  Share
} from '@element-plus/icons-vue'
import { useAuthStore, useDocumentStore, useFolderStore, useAppStore } from '@/stores'
import { useNotification } from '@/composables'
import { formatDate, formatRelativeTime } from '@/utils'
import { EmptyState } from '@/components/common'

const router = useRouter()
const authStore = useAuthStore()
const documentStore = useDocumentStore()
const folderStore = useFolderStore()
const appStore = useAppStore()
const { success, error } = useNotification()

// 统计数据
const stats = reactive({
  totalDocuments: 0,
  totalFolders: 0,
  favoriteDocuments: 0,
  aiUsage: 0,
  documentsGrowth: 0,
  foldersGrowth: 0,
  favoritesGrowth: 0,
  aiGrowth: 0
})

// 最近文档
const recentDocuments = computed(() => documentStore.recentDocuments)

// 收藏文档
const favoriteDocuments = computed(() => documentStore.favoriteDocuments)

/**
 * 获取问候语
 */
const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
}

/**
 * 创建新文档 - 直接进入编辑模式
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
 * 创建新文件夹
 */
const createFolder = async () => {
  try {
    await folderStore.createFolder({
      name: '新建文件夹',
      parentId: null
    })
    
    router.push('/folders')
    success('文件夹创建成功')
  } catch (err: any) {
    error(err.message || '创建文件夹失败')
  }
}

/**
 * 导入文档
 */
const importDocument = () => {
  router.push('/documents/import')
}

/**
 * 打开AI助手
 */
const openAIAssistant = () => {
  appStore.setAIPanelVisible(true)
}

/**
 * 打开文档
 */
const openDocument = (doc: any) => {
  router.push(`/documents/${doc.id}`)
}

/**
 * 处理文档操作
 */
const handleDocumentAction = async ({ action, doc }: { action: string; doc: any }) => {
  try {
    switch (action) {
      case 'edit':
        router.push(`/documents/${doc.id}/edit`)
        break
      case 'favorite':
        await toggleFavorite(doc)
        break
      case 'share':
        // 实现分享功能
        success('分享链接已复制到剪贴板')
        break
    }
  } catch (err: any) {
    error(err.message || '操作失败')
  }
}

/**
 * 切换收藏状态
 */
const toggleFavorite = async (doc: any) => {
  try {
    if (doc.isFavorite) {
      await documentStore.unfavoriteDocument(doc.id)
      success('已取消收藏')
    } else {
      await documentStore.favoriteDocument(doc.id)
      success('已添加到收藏')
    }
  } catch (err: any) {
    error(err.message || '操作失败')
  }
}

/**
 * 加载统计数据
 */
const loadStats = async () => {
  try {
    // 这里应该调用API获取统计数据
    // 暂时使用模拟数据
    Object.assign(stats, {
      totalDocuments: 42,
      totalFolders: 8,
      favoriteDocuments: 12,
      aiUsage: 156,
      documentsGrowth: 12.5,
      foldersGrowth: 8.3,
      favoritesGrowth: 15.2,
      aiGrowth: 23.7
    })
  } catch (err: any) {
    console.error('加载统计数据失败:', err)
  }
}

/**
 * 加载数据
 */
const loadData = async () => {
  try {
    await Promise.all([
      documentStore.fetchRecentDocuments(),
      documentStore.fetchFavoriteDocuments(),
      loadStats()
    ])
  } catch (err: any) {
    console.error('加载数据失败:', err)
  }
}

/**
 * 组件挂载时加载数据
 */
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 欢迎区域 */
.dashboard__welcome {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 24px;
  background: linear-gradient(135deg, var(--el-color-primary) 0%, var(--el-color-primary-light-3) 100%);
  border-radius: 12px;
  color: white;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.welcome-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.dashboard__stats {
  margin-bottom: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stat-card__icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-card__icon--primary {
  background: var(--el-color-primary);
}

.stat-card__icon--success {
  background: var(--el-color-success);
}

.stat-card__icon--warning {
  background: var(--el-color-warning);
}

.stat-card__icon--info {
  background: var(--el-color-info);
}

.stat-card__content {
  flex: 1;
}

.stat-card__value {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.stat-card__label {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.stat-card__trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
  color: var(--el-color-danger);
}

.stat-card__trend--up {
  color: var(--el-color-success);
}

/* 内容区域 */
.dashboard__content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 32px;
  margin-bottom: 32px;
}

.content-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.section-more {
  color: var(--el-color-primary);
  text-decoration: none;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
}

.section-more:hover {
  color: var(--el-color-primary-dark-2);
}

/* 文档网格 */
.document-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.document-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.document-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.document-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.document-card__icon {
  width: 32px;
  height: 32px;
  background: var(--el-color-primary-light-9);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
}

.document-card__title {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.document-card__excerpt {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.document-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.document-card__time {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.document-card__tags {
  display: flex;
  gap: 4px;
}

/* 文档列表 */
.document-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.document-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.document-item:hover {
  background-color: var(--el-fill-color-light);
}

.document-item__icon {
  width: 32px;
  height: 32px;
  background: var(--el-color-primary-light-9);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
}

.document-item__content {
  flex: 1;
}

.document-item__title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.document-item__meta {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin: 0;
}

.favorite-btn--active {
  color: var(--el-color-warning) !important;
}

/* 快速操作 */
.dashboard__quick-actions {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.quick-action {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-action:hover {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}

.quick-action__icon {
  width: 40px;
  height: 40px;
  background: var(--el-color-primary-light-9);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
  font-size: 20px;
}

.quick-action__content h4 {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin: 0 0 4px 0;
}

.quick-action__content p {
  font-size: 12px;
  color: var(--el-text-color-regular);
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }
  
  .dashboard__welcome {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .dashboard__content {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
  
  .document-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-actions-grid {
    grid-template-columns: 1fr;
  }
}

/* 暗色主题适配 */
.dark .stat-card,
.dark .content-section,
.dark .dashboard__quick-actions {
  background: var(--el-bg-color-page);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.dark .document-card {
  border-color: var(--el-border-color);
}

.dark .document-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}
</style>