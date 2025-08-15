<template>
  <div class="article-list-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon class="title-icon"><Edit /></el-icon>
            文章管理
          </h1>
          <p class="page-description">管理您的所有文章内容</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="createArticle">
            <el-icon><Plus /></el-icon>
            新建文章
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filters-section">
      <div class="filters-content">
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索文章标题、内容或标签..."
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <div class="filter-controls">
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
          
          <el-select v-model="visibilityFilter" placeholder="可见性" clearable style="width: 120px">
            <el-option label="公开" value="PUBLIC" />
            <el-option label="私有" value="PRIVATE" />
            <el-option label="受保护" value="PROTECTED" />
          </el-select>
          
          <el-select v-model="sortBy" style="width: 140px">
            <el-option label="最新更新" value="updatedAt" />
            <el-option label="创建时间" value="createdAt" />
            <el-option label="标题" value="title" />
            <el-option label="阅读量" value="viewCount" />
          </el-select>
          
          <el-button-group>
            <el-button 
              :type="viewMode === 'grid' ? 'primary' : 'default'"
              @click="viewMode = 'grid'"
            >
              <el-icon><Grid /></el-icon>
            </el-button>
            <el-button 
              :type="viewMode === 'list' ? 'primary' : 'default'"
              @click="viewMode = 'list'"
            >
              <el-icon><List /></el-icon>
            </el-button>
          </el-button-group>
        </div>
      </div>
    </div>

    <!-- 文章列表 -->
    <div class="articles-section">
      <div class="articles-content">
        <LoadingSpinner v-if="loading" />
        
        <EmptyState
          v-else-if="!loading && filteredArticles.length === 0"
          icon="Document"
          title="暂无文章"
          description="开始创建您的第一篇文章吧"
        >
          <el-button type="primary" @click="createArticle">
            <el-icon><Plus /></el-icon>
            新建文章
          </el-button>
        </EmptyState>
        
        <!-- 网格视图 -->
        <div v-else-if="viewMode === 'grid'" class="articles-grid">
          <div
            v-for="article in filteredArticles"
            :key="article.id"
            class="article-card"
            @click="viewArticle(article.id)"
          >
            <div class="card-header">
              <h3 class="article-title">{{ article.title }}</h3>
              <el-dropdown @command="handleArticleAction">
                <el-button text>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ action: 'edit', id: article.id }">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'duplicate', id: article.id }">
                      <el-icon><DocumentCopy /></el-icon>
                      复制
                    </el-dropdown-item>
                    <el-dropdown-item 
                      :command="{ action: 'favorite', id: article.id }"
                      :disabled="article.isFavorited"
                    >
                      <el-icon><Star /></el-icon>
                      {{ article.isFavorited ? '已收藏' : '收藏' }}
                    </el-dropdown-item>
                    <el-dropdown-item 
                      :command="{ action: 'archive', id: article.id }"
                      v-if="article.status !== 'ARCHIVED'"
                    >
                      <el-icon><FolderOpened /></el-icon>
                      归档
                    </el-dropdown-item>
                    <el-dropdown-item 
                      :command="{ action: 'delete', id: article.id }"
                      divided
                    >
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            
            <div class="card-content">
              <p class="article-summary">{{ article.summary || '暂无摘要' }}</p>
              
              <div class="article-tags" v-if="article.tags && article.tags.length > 0">
                <el-tag
                  v-for="tag in article.tags.slice(0, 3)"
                  :key="tag"
                  size="small"
                  type="info"
                >
                  {{ tag }}
                </el-tag>
                <span v-if="article.tags.length > 3" class="more-tags">
                  +{{ article.tags.length - 3 }}
                </span>
              </div>
            </div>
            
            <div class="card-footer">
              <div class="article-meta">
                <el-tag :type="getStatusType(article.status)" size="small">
                  {{ getStatusText(article.status) }}
                </el-tag>
                <el-tag :type="getVisibilityType(article.visibility)" size="small">
                  {{ getVisibilityText(article.visibility) }}
                </el-tag>
              </div>
              
              <div class="article-stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ article.viewCount || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ article.commentCount || 0 }}
                </span>
              </div>
              
              <div class="article-time">
                {{ formatRelativeTime(article.updatedAt) }}
              </div>
            </div>
          </div>
        </div>
        
        <!-- 列表视图 -->
        <div v-else class="articles-list">
          <el-table :data="filteredArticles" style="width: 100%">
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="{ row }">
                <div class="title-cell" @click="viewArticle(row.id)">
                  <span class="article-title-link">{{ row.title }}</span>
                  <div class="article-summary-small">{{ row.summary || '暂无摘要' }}</div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="visibility" label="可见性" width="100">
              <template #default="{ row }">
                <el-tag :type="getVisibilityType(row.visibility)" size="small">
                  {{ getVisibilityText(row.visibility) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="tags" label="标签" width="200">
              <template #default="{ row }">
                <div class="tags-cell" v-if="row.tags && row.tags.length > 0">
                  <el-tag
                    v-for="tag in row.tags.slice(0, 2)"
                    :key="tag"
                    size="small"
                    type="info"
                  >
                    {{ tag }}
                  </el-tag>
                  <span v-if="row.tags.length > 2" class="more-tags">
                    +{{ row.tags.length - 2 }}
                  </span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="统计" width="120">
              <template #default="{ row }">
                <div class="stats-cell">
                  <span class="stat-item">
                    <el-icon><View /></el-icon>
                    {{ row.viewCount || 0 }}
                  </span>
                  <span class="stat-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ row.commentCount || 0 }}
                  </span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="updatedAt" label="更新时间" width="150">
              <template #default="{ row }">
                {{ formatRelativeTime(row.updatedAt) }}
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button-group>
                  <el-button size="small" @click="editArticle(row.id)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-dropdown @command="handleArticleAction">
                    <el-button size="small">
                      <el-icon><MoreFilled /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item :command="{ action: 'duplicate', id: row.id }">
                          <el-icon><DocumentCopy /></el-icon>
                          复制
                        </el-dropdown-item>
                        <el-dropdown-item 
                          :command="{ action: 'favorite', id: row.id }"
                          :disabled="row.isFavorited"
                        >
                          <el-icon><Star /></el-icon>
                          {{ row.isFavorited ? '已收藏' : '收藏' }}
                        </el-dropdown-item>
                        <el-dropdown-item 
                          :command="{ action: 'archive', id: row.id }"
                          v-if="row.status !== 'ARCHIVED'"
                        >
                          <el-icon><FolderOpened /></el-icon>
                          归档
                        </el-dropdown-item>
                        <el-dropdown-item 
                          :command="{ action: 'delete', id: row.id }"
                          divided
                        >
                          <el-icon><Delete /></el-icon>
                          删除
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </el-button-group>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-section" v-if="!loading && filteredArticles.length > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit, Plus, Search, Grid, List, MoreFilled, DocumentCopy,
  Star, FolderOpened, Delete, View, ChatDotRound
} from '@element-plus/icons-vue'
import { LoadingSpinner, EmptyState } from '@/components/common'
import { formatRelativeTime } from '@/utils'
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
  isFavorited: boolean
  isPinned: boolean
  createdAt: string
  updatedAt: string
  authorId: string
}

const router = useRouter()

// 响应式数据
const loading = ref(true)
const articles = ref<Article[]>([])
const searchQuery = ref('')
const statusFilter = ref('')
const visibilityFilter = ref('')
const sortBy = ref('updatedAt')
const viewMode = ref<'grid' | 'list'>('grid')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

/**
 * 过滤后的文章列表
 */
const filteredArticles = computed(() => {
  let result = articles.value
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(article => 
      article.title.toLowerCase().includes(query) ||
      article.summary?.toLowerCase().includes(query) ||
      article.tags.some(tag => tag.toLowerCase().includes(query))
    )
  }
  
  // 状态过滤
  if (statusFilter.value) {
    result = result.filter(article => article.status === statusFilter.value)
  }
  
  // 可见性过滤
  if (visibilityFilter.value) {
    result = result.filter(article => article.visibility === visibilityFilter.value)
  }
  
  // 排序
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'title':
        return a.title.localeCompare(b.title)
      case 'createdAt':
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      case 'viewCount':
        return (b.viewCount || 0) - (a.viewCount || 0)
      case 'updatedAt':
      default:
        return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
    }
  })
  
  return result
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
 * 加载文章列表
 */
const loadArticles = async () => {
  try {
    loading.value = true
    const response = await articleApi.getArticles({
      page: currentPage.value - 1,
      size: pageSize.value,
      status: statusFilter.value || undefined,
      visibility: visibilityFilter.value || undefined,
      search: searchQuery.value || undefined,
      sortBy: sortBy.value
    })
    
    articles.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('加载文章列表失败:', error)
    ElMessage.error('加载文章列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索处理
 */
const handleSearch = () => {
  currentPage.value = 1
  loadArticles()
}

/**
 * 创建文章
 */
const createArticle = () => {
  router.push('/app/articles/create')
}

/**
 * 查看文章
 */
const viewArticle = (id: string) => {
  router.push(`/app/articles/${id}`)
}

/**
 * 编辑文章
 */
const editArticle = (id: string) => {
  router.push(`/app/articles/${id}/edit`)
}

/**
 * 处理文章操作
 */
const handleArticleAction = async (command: { action: string; id: string }) => {
  const { action, id } = command
  
  try {
    switch (action) {
      case 'edit':
        editArticle(id)
        break
        
      case 'duplicate':
        await articleApi.duplicateArticle(id)
        ElMessage.success('文章复制成功')
        loadArticles()
        break
        
      case 'favorite':
        await articleApi.favoriteArticle(id)
        ElMessage.success('文章收藏成功')
        loadArticles()
        break
        
      case 'archive':
        await ElMessageBox.confirm('确定要归档这篇文章吗？', '确认归档', {
          type: 'warning'
        })
        await articleApi.archiveArticle(id)
        ElMessage.success('文章归档成功')
        loadArticles()
        break
        
      case 'delete':
        await ElMessageBox.confirm('确定要删除这篇文章吗？删除后无法恢复。', '确认删除', {
          type: 'warning'
        })
        await articleApi.deleteArticle(id)
        ElMessage.success('文章删除成功')
        loadArticles()
        break
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

/**
 * 分页大小变化
 */
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadArticles()
}

/**
 * 当前页变化
 */
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadArticles()
}

// 监听筛选条件变化
watch([statusFilter, visibilityFilter, sortBy], () => {
  currentPage.value = 1
  loadArticles()
})

// 组件挂载时加载数据
onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
.article-list-view {
  min-height: 100vh;
  background-color: var(--el-bg-color);
}

/* 页面头部 */
.page-header {
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

.page-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin: 0 0 var(--spacing-xs) 0;
  font-size: 1.5rem;
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

/* 筛选区域 */
.filters-section {
  background-color: var(--el-bg-color-page);
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: var(--spacing-md) 0;
}

.filters-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-xl);
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.search-bar {
  flex: 1;
  max-width: 400px;
}

.filter-controls {
  display: flex;
  gap: var(--spacing-sm);
  align-items: center;
}

/* 文章区域 */
.articles-section {
  padding: var(--spacing-lg) 0;
}

.articles-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-xl);
}

/* 网格视图 */
.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--spacing-lg);
}

.article-card {
  background-color: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
  cursor: pointer;
  transition: all 0.2s;
}

.article-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-md);
}

.article-title {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
  flex: 1;
  margin-right: var(--spacing-sm);
}

.card-content {
  margin-bottom: var(--spacing-md);
}

.article-summary {
  margin: 0 0 var(--spacing-md) 0;
  color: var(--el-text-color-regular);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  align-items: center;
}

.more-tags {
  font-size: 0.8rem;
  color: var(--el-text-color-placeholder);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.article-meta {
  display: flex;
  gap: var(--spacing-xs);
}

.article-stats {
  display: flex;
  gap: var(--spacing-md);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 0.9rem;
  color: var(--el-text-color-regular);
}

.article-time {
  font-size: 0.9rem;
  color: var(--el-text-color-placeholder);
}

/* 列表视图 */
.title-cell {
  cursor: pointer;
}

.article-title-link {
  color: var(--el-color-primary);
  font-weight: 500;
}

.article-title-link:hover {
  text-decoration: underline;
}

.article-summary-small {
  font-size: 0.9rem;
  color: var(--el-text-color-regular);
  margin-top: var(--spacing-xs);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tags-cell {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  align-items: center;
}

.stats-cell {
  display: flex;
  gap: var(--spacing-md);
}

/* 分页 */
.pagination-section {
  margin-top: var(--spacing-xl);
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: stretch;
  }
  
  .filters-content {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: stretch;
  }
  
  .filter-controls {
    flex-wrap: wrap;
  }
  
  .articles-grid {
    grid-template-columns: 1fr;
  }
  
  .card-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>