<template>
  <div class="article-detail-view">
    <LoadingSpinner v-if="loading" />
    
    <div v-else-if="article" class="article-container">
      <!-- 文章头部 -->
      <div class="article-header">
        <div class="header-content">
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ name: 'Articles' }">文章管理</el-breadcrumb-item>
              <el-breadcrumb-item>{{ article.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="article-actions">
            <el-button @click="toggleFavorite">
              <el-icon><Star :class="{ 'favorited': article.isFavorited }" /></el-icon>
              {{ article.isFavorited ? '已收藏' : '收藏' }}
            </el-button>
            
            <el-button @click="shareArticle">
              <el-icon><Share /></el-icon>
              分享
            </el-button>
            
            <el-button type="primary" @click="editArticle">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            
            <el-dropdown @command="handleAction">
              <el-button>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="duplicate">
                    <el-icon><DocumentCopy /></el-icon>
                    复制文章
                  </el-dropdown-item>
                  <el-dropdown-item command="export">
                    <el-icon><Download /></el-icon>
                    导出文章
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="archive"
                    v-if="article.status !== 'ARCHIVED'"
                  >
                    <el-icon><FolderOpened /></el-icon>
                    归档文章
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="publish"
                    v-if="article.status === 'DRAFT'"
                  >
                    <el-icon><Upload /></el-icon>
                    发布文章
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除文章
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
      
      <!-- 文章内容 -->
      <div class="article-content">
        <div class="content-wrapper">
          <!-- 文章标题和元信息 -->
          <div class="article-meta-section">
            <h1 class="article-title">{{ article.title }}</h1>
            
            <div class="article-meta">
              <div class="meta-left">
                <div class="author-info">
                  <el-avatar :size="32" :src="authorAvatar">
                    {{ authorInitials }}
                  </el-avatar>
                  <div class="author-details">
                    <span class="author-name">{{ article.authorName || '未知作者' }}</span>
                    <span class="publish-time">{{ formatDateTime(article.updatedAt) }}</span>
                  </div>
                </div>
                
                <div class="article-stats">
                  <span class="stat-item">
                    <el-icon><View /></el-icon>
                    {{ article.viewCount || 0 }} 阅读
                  </span>
                  <span class="stat-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ article.commentCount || 0 }} 评论
                  </span>
                  <span class="stat-item">
                    <el-icon><Clock /></el-icon>
                    {{ article.readingTime || 0 }} 分钟阅读
                  </span>
                </div>
              </div>
              
              <div class="meta-right">
                <div class="article-badges">
                  <el-tag :type="getStatusType(article.status)">
                    {{ getStatusText(article.status) }}
                  </el-tag>
                  <el-tag :type="getVisibilityType(article.visibility)">
                    {{ getVisibilityText(article.visibility) }}
                  </el-tag>
                  <el-tag v-if="article.isPinned" type="warning">
                    <el-icon><Top /></el-icon>
                    置顶
                  </el-tag>
                </div>
              </div>
            </div>
            
            <!-- 文章摘要 -->
            <div v-if="article.summary" class="article-summary">
              <p>{{ article.summary }}</p>
            </div>
            
            <!-- 文章标签 -->
            <div v-if="article.tags && article.tags.length > 0" class="article-tags">
              <el-tag
                v-for="tag in article.tags"
                :key="tag"
                type="info"
                effect="plain"
                @click="searchByTag(tag)"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
          
          <!-- 文章正文 -->
          <div class="article-body">
            <div 
              class="content-html"
              v-html="article.content"
            ></div>
          </div>
          
          <!-- 文章底部信息 -->
          <div class="article-footer">
            <div class="footer-meta">
              <div class="creation-info">
                <p>创建时间：{{ formatDateTime(article.createdAt) }}</p>
                <p>最后更新：{{ formatDateTime(article.updatedAt) }}</p>
                <p v-if="article.category">分类：{{ article.category }}</p>
                <p>字数：{{ article.wordCount || 0 }} 字</p>
              </div>
            </div>
            
            <!-- 相关文章推荐 -->
            <div v-if="relatedArticles.length > 0" class="related-articles">
              <h3>相关文章</h3>
              <div class="related-list">
                <div
                  v-for="related in relatedArticles"
                  :key="related.id"
                  class="related-item"
                  @click="viewRelatedArticle(related.id)"
                >
                  <h4>{{ related.title }}</h4>
                  <p>{{ related.summary || '暂无摘要' }}</p>
                  <div class="related-meta">
                    <span>{{ formatRelativeTime(related.updatedAt) }}</span>
                    <span>{{ related.viewCount || 0 }} 阅读</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 侧边栏 -->
        <div class="article-sidebar">
          <!-- 目录 -->
          <div class="toc-section" v-if="tocItems.length > 0">
            <h3>目录</h3>
            <div class="toc-list">
              <div
                v-for="item in tocItems"
                :key="item.id"
                :class="['toc-item', `toc-level-${item.level}`]"
                @click="scrollToHeading(item.id)"
              >
                {{ item.text }}
              </div>
            </div>
          </div>
          
          <!-- 文章信息 -->
          <div class="info-section">
            <h3>文章信息</h3>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">状态：</span>
                <el-tag :type="getStatusType(article.status)" size="small">
                  {{ getStatusText(article.status) }}
                </el-tag>
              </div>
              <div class="info-item">
                <span class="info-label">可见性：</span>
                <el-tag :type="getVisibilityType(article.visibility)" size="small">
                  {{ getVisibilityText(article.visibility) }}
                </el-tag>
              </div>
              <div class="info-item" v-if="article.category">
                <span class="info-label">分类：</span>
                <span>{{ article.category }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">字数：</span>
                <span>{{ article.wordCount || 0 }} 字</span>
              </div>
              <div class="info-item">
                <span class="info-label">阅读时间：</span>
                <span>约 {{ article.readingTime || 0 }} 分钟</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 分享对话框 -->
    <el-dialog v-model="shareDialogVisible" title="分享文章" width="500px">
      <div class="share-content">
        <div class="share-url">
          <el-input
            v-model="shareUrl"
            readonly
            placeholder="文章链接"
          >
            <template #append>
              <el-button @click="copyShareUrl">
                <el-icon><DocumentCopy /></el-icon>
                复制
              </el-button>
            </template>
          </el-input>
        </div>
        
        <div class="share-qr">
          <div class="qr-placeholder">
            <!-- 这里可以集成二维码生成库 -->
            <p>二维码</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Star, Share, Edit, MoreFilled, DocumentCopy, Download,
  FolderOpened, Upload, Delete, View, ChatDotRound, Clock,
  Top
} from '@element-plus/icons-vue'
import { LoadingSpinner } from '@/components/common'
import { formatDateTime, formatRelativeTime, getAvatarColor, getNameInitials } from '@/utils'
import { articleApi } from '@/api'

/**
 * 文章接口定义
 */
interface Article {
  id: string
  title: string
  summary?: string
  content: string
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  visibility: 'PUBLIC' | 'PRIVATE' | 'PROTECTED'
  tags: string[]
  category?: string
  viewCount: number
  commentCount: number
  wordCount: number
  readingTime: number
  isFavorited: boolean
  isPinned: boolean
  createdAt: string
  updatedAt: string
  authorId: string
  authorName?: string
}

/**
 * 目录项接口
 */
interface TocItem {
  id: string
  text: string
  level: number
}

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(true)
const article = ref<Article | null>(null)
const relatedArticles = ref<Article[]>([])
const tocItems = ref<TocItem[]>([])
const shareDialogVisible = ref(false)
const shareUrl = ref('')

// 计算属性
const authorAvatar = computed(() => {
  // 这里可以根据作者信息生成头像URL
  return ''
})

const authorInitials = computed(() => {
  return getNameInitials(article.value?.authorName || '未知')
})

/**
 * 获取状态类型
 */
const getStatusType = (status: string) => {
  switch (status) {
    case 'PUBLISHED': return 'success'
    case 'DRAFT': return 'warning'
    case 'ARCHIVED': return 'info'
    default: return 'default'
  }
}

/**
 * 获取状态文本
 */
const getStatusText = (status: string) => {
  switch (status) {
    case 'PUBLISHED': return '已发布'
    case 'DRAFT': return '草稿'
    case 'ARCHIVED': return '已归档'
    default: return '未知'
  }
}

/**
 * 获取可见性类型
 */
const getVisibilityType = (visibility: string) => {
  switch (visibility) {
    case 'PUBLIC': return 'success'
    case 'PRIVATE': return 'danger'
    case 'PROTECTED': return 'warning'
    default: return 'default'
  }
}

/**
 * 获取可见性文本
 */
const getVisibilityText = (visibility: string) => {
  switch (visibility) {
    case 'PUBLIC': return '公开'
    case 'PRIVATE': return '私有'
    case 'PROTECTED': return '受保护'
    default: return '未知'
  }
}

/**
 * 加载文章详情
 */
const loadArticle = async () => {
  try {
    loading.value = true
    const articleId = route.params.id as string
    
    const response = await articleApi.getArticle(articleId)
    article.value = response.data
    
    // 生成目录
    await nextTick()
    generateToc()
    
    // 加载相关文章
    loadRelatedArticles()
    
    // 增加阅读量
    articleApi.incrementViewCount(articleId).catch(console.error)
    
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载文章失败')
    router.push('/app/articles')
  } finally {
    loading.value = false
  }
}

/**
 * 生成目录
 */
const generateToc = () => {
  const contentEl = document.querySelector('.content-html')
  if (!contentEl) return
  
  const headings = contentEl.querySelectorAll('h1, h2, h3, h4, h5, h6')
  const items: TocItem[] = []
  
  headings.forEach((heading, index) => {
    const id = `heading-${index}`
    heading.id = id
    
    items.push({
      id,
      text: heading.textContent || '',
      level: parseInt(heading.tagName.charAt(1))
    })
  })
  
  tocItems.value = items
}

/**
 * 滚动到指定标题
 */
const scrollToHeading = (id: string) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

/**
 * 加载相关文章
 */
const loadRelatedArticles = async () => {
  try {
    if (!article.value) return
    
    const response = await articleApi.getRelatedArticles(article.value.id)
    relatedArticles.value = response.data
  } catch (error) {
    console.error('加载相关文章失败:', error)
  }
}

/**
 * 切换收藏状态
 */
const toggleFavorite = async () => {
  try {
    if (!article.value) return
    
    if (article.value.isFavorited) {
      await articleApi.unfavoriteArticle(article.value.id)
      article.value.isFavorited = false
      ElMessage.success('已取消收藏')
    } else {
      await articleApi.favoriteArticle(article.value.id)
      article.value.isFavorited = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

/**
 * 分享文章
 */
const shareArticle = () => {
  shareUrl.value = window.location.href
  shareDialogVisible.value = true
}

/**
 * 复制分享链接
 */
const copyShareUrl = async () => {
  try {
    await navigator.clipboard.writeText(shareUrl.value)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

/**
 * 编辑文章
 */
const editArticle = () => {
  if (article.value) {
    router.push(`/app/articles/${article.value.id}/edit`)
  }
}

/**
 * 按标签搜索
 */
const searchByTag = (tag: string) => {
  router.push({
    name: 'Articles',
    query: { tag }
  })
}

/**
 * 查看相关文章
 */
const viewRelatedArticle = (id: string) => {
  router.push(`/app/articles/${id}`)
}

/**
 * 处理操作
 */
const handleAction = async (command: string) => {
  if (!article.value) return
  
  try {
    switch (command) {
      case 'duplicate':
        await articleApi.duplicateArticle(article.value.id)
        ElMessage.success('文章复制成功')
        break
        
      case 'export':
        // 导出文章逻辑
        ElMessage.info('导出功能开发中')
        break
        
      case 'archive':
        await ElMessageBox.confirm('确定要归档这篇文章吗？', '确认归档', {
          type: 'warning'
        })
        await articleApi.archiveArticle(article.value.id)
        ElMessage.success('文章归档成功')
        article.value.status = 'ARCHIVED'
        break
        
      case 'publish':
        await ElMessageBox.confirm('确定要发布这篇文章吗？', '确认发布', {
          type: 'warning'
        })
        await articleApi.publishArticle(article.value.id)
        ElMessage.success('文章发布成功')
        article.value.status = 'PUBLISHED'
        break
        
      case 'delete':
        await ElMessageBox.confirm('确定要删除这篇文章吗？删除后无法恢复。', '确认删除', {
          type: 'warning'
        })
        await articleApi.deleteArticle(article.value.id)
        ElMessage.success('文章删除成功')
        router.push('/app/articles')
        break
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadArticle()
})
</script>

<style scoped>
.article-detail-view {
  min-height: 100vh;
  background-color: var(--el-bg-color);
}

/* 文章头部 */
.article-header {
  background-color: var(--el-bg-color-page);
  border-bottom: 1px solid var(--el-border-color-light);
  padding: var(--spacing-lg) 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-xl);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.breadcrumb {
  flex: 1;
}

.article-actions {
  display: flex;
  gap: var(--spacing-sm);
}

/* 文章内容 */
.article-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-xl);
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: var(--spacing-xl);
}

.content-wrapper {
  min-width: 0;
}

/* 文章元信息 */
.article-meta-section {
  margin-bottom: var(--spacing-xl);
}

.article-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--el-text-color-primary);
  margin: 0 0 var(--spacing-lg) 0;
  line-height: 1.3;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-lg);
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.author-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.publish-time {
  font-size: 0.9rem;
  color: var(--el-text-color-regular);
}

.article-stats {
  display: flex;
  gap: var(--spacing-lg);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.9rem;
  color: var(--el-text-color-regular);
}

.article-badges {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.article-summary {
  background-color: var(--el-fill-color-lighter);
  padding: var(--spacing-lg);
  border-radius: var(--border-radius-base);
  margin-bottom: var(--spacing-lg);
}

.article-summary p {
  margin: 0;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  font-style: italic;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.article-tags .el-tag {
  cursor: pointer;
}

.article-tags .el-tag:hover {
  opacity: 0.8;
}

/* 文章正文 */
.article-body {
  margin-bottom: var(--spacing-xl);
}

.content-html {
  line-height: 1.8;
  color: var(--el-text-color-primary);
  font-size: 1rem;
}

.content-html h1,
.content-html h2,
.content-html h3,
.content-html h4,
.content-html h5,
.content-html h6 {
  margin: 2em 0 1em 0;
  font-weight: 600;
  line-height: 1.4;
}

.content-html h1 { font-size: 1.8rem; }
.content-html h2 { font-size: 1.5rem; }
.content-html h3 { font-size: 1.3rem; }
.content-html h4 { font-size: 1.1rem; }
.content-html h5 { font-size: 1rem; }
.content-html h6 { font-size: 0.9rem; }

.content-html p {
  margin: 1em 0;
}

.content-html img {
  max-width: 100%;
  height: auto;
  border-radius: var(--border-radius-base);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.content-html blockquote {
  border-left: 4px solid var(--el-color-primary);
  padding-left: var(--spacing-lg);
  margin: 1.5em 0;
  background-color: var(--el-fill-color-lighter);
  padding: var(--spacing-lg);
  border-radius: var(--border-radius-base);
}

.content-html code {
  background-color: var(--el-fill-color-light);
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.9em;
}

.content-html pre {
  background-color: var(--el-fill-color-darker);
  padding: var(--spacing-lg);
  border-radius: var(--border-radius-base);
  overflow-x: auto;
  margin: 1.5em 0;
}

.content-html pre code {
  background: none;
  padding: 0;
  color: var(--el-text-color-primary);
}

/* 文章底部 */
.article-footer {
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: var(--spacing-xl);
}

.creation-info {
  margin-bottom: var(--spacing-xl);
}

.creation-info p {
  margin: var(--spacing-xs) 0;
  color: var(--el-text-color-regular);
  font-size: 0.9rem;
}

.related-articles h3 {
  margin: 0 0 var(--spacing-lg) 0;
  color: var(--el-text-color-primary);
}

.related-list {
  display: grid;
  gap: var(--spacing-md);
}

.related-item {
  padding: var(--spacing-lg);
  background-color: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  cursor: pointer;
  transition: all 0.2s;
}

.related-item:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.related-item h4 {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--el-text-color-primary);
  font-size: 1rem;
}

.related-item p {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--el-text-color-regular);
  font-size: 0.9rem;
  line-height: 1.5;
}

.related-meta {
  display: flex;
  gap: var(--spacing-md);
  font-size: 0.8rem;
  color: var(--el-text-color-placeholder);
}

/* 侧边栏 */
.article-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.toc-section,
.info-section {
  background-color: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
}

.toc-section h3,
.info-section h3 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: 1rem;
  color: var(--el-text-color-primary);
}

.toc-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.toc-item {
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-small);
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9rem;
  color: var(--el-text-color-regular);
}

.toc-item:hover {
  background-color: var(--el-fill-color-light);
  color: var(--el-color-primary);
}

.toc-level-1 { padding-left: var(--spacing-sm); }
.toc-level-2 { padding-left: var(--spacing-md); }
.toc-level-3 { padding-left: var(--spacing-lg); }
.toc-level-4 { padding-left: calc(var(--spacing-lg) + var(--spacing-sm)); }
.toc-level-5 { padding-left: calc(var(--spacing-lg) + var(--spacing-md)); }
.toc-level-6 { padding-left: calc(var(--spacing-lg) + var(--spacing-lg)); }

.info-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
}

.info-label {
  color: var(--el-text-color-regular);
  font-weight: 500;
}

/* 收藏状态 */
.favorited {
  color: var(--el-color-warning);
}

/* 分享对话框 */
.share-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.share-url {
  width: 100%;
}

.share-qr {
  display: flex;
  justify-content: center;
}

.qr-placeholder {
  width: 200px;
  height: 200px;
  border: 2px dashed var(--el-border-color);
  border-radius: var(--border-radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-placeholder);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .article-content {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }
  
  .article-sidebar {
    order: -1;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: stretch;
  }
  
  .article-actions {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .article-content {
    padding: var(--spacing-lg);
  }
  
  .article-title {
    font-size: 1.5rem;
  }
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .article-stats {
    flex-wrap: wrap;
  }
}
</style>