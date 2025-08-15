<template>
  <div class="search-view">
    <!-- 搜索头部 -->
    <div class="search-header">
      <div class="search-container">
        <el-input
          v-model="searchQuery"
          size="large"
          placeholder="搜索文档、文件夹..."
          class="search-input"
          @input="handleSearch"
          @keydown.enter="performSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #suffix>
            <el-button 
              v-if="searchQuery" 
              text 
              @click="clearSearch"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </template>
        </el-input>
        
        <el-button 
          type="primary" 
          size="large" 
          @click="performSearch"
          :loading="isSearching"
        >
          搜索
        </el-button>
      </div>
      
      <!-- 搜索筛选 -->
      <div class="search-filters">
        <el-radio-group v-model="searchType" @change="handleTypeChange">
          <el-radio-button value="all">全部</el-radio-button>
          <el-radio-button value="documents">文档</el-radio-button>
          <el-radio-button value="folders">文件夹</el-radio-button>
        </el-radio-group>
        
        <el-select 
          v-model="sortBy" 
          placeholder="排序方式"
          style="width: 120px"
          @change="handleSortChange"
        >
          <el-option label="相关性" value="relevance" />
          <el-option label="修改时间" value="updatedAt" />
          <el-option label="创建时间" value="createdAt" />
          <el-option label="名称" value="name" />
        </el-select>
        
        <el-button 
          text 
          @click="showAdvancedSearch = !showAdvancedSearch"
        >
          <el-icon><Setting /></el-icon>
          高级搜索
        </el-button>
      </div>
      
      <!-- 高级搜索 -->
      <el-collapse-transition>
        <div v-show="showAdvancedSearch" class="advanced-search">
          <el-row :gutter="16">
            <el-col :span="6">
              <el-select v-model="advancedFilters.author" placeholder="作者" clearable>
                <el-option 
                  v-for="author in authors" 
                  :key="author.id" 
                  :label="author.name" 
                  :value="author.id" 
                />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-date-picker
                v-model="advancedFilters.dateRange"
                type="daterange"
                placeholder="选择日期范围"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-col>
            <el-col :span="6">
              <el-select v-model="advancedFilters.tags" placeholder="标签" multiple clearable>
                <el-option 
                  v-for="tag in tags" 
                  :key="tag.id" 
                  :label="tag.name" 
                  :value="tag.id" 
                />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-input 
                v-model="advancedFilters.fileType" 
                placeholder="文件类型"
                clearable
              />
            </el-col>
          </el-row>
        </div>
      </el-collapse-transition>
    </div>
    
    <!-- 搜索结果 -->
    <div class="search-content">
      <!-- 搜索统计 -->
      <div v-if="hasSearched" class="search-stats">
        <span class="stats-text">
          找到 <strong>{{ totalResults }}</strong> 个结果
          <span v-if="searchQuery">关于 "{{ searchQuery }}"</span>
          <span class="search-time">({{ searchTime }}ms)</span>
        </span>
        
        <div class="view-controls">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button value="list">
              <el-icon><List /></el-icon>
            </el-radio-button>
            <el-radio-button value="grid">
              <el-icon><Grid /></el-icon>
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <LoadingSpinner v-if="isSearching" />
      
      <!-- 搜索结果列表 -->
      <div v-else-if="searchResults.length > 0" class="results-container">
        <!-- 列表视图 -->
        <div v-if="viewMode === 'list'" class="results-list">
          <div 
            v-for="item in searchResults" 
            :key="item.id"
            class="result-item"
            @click="openItem(item)"
          >
            <div class="item-icon">
              <el-icon v-if="item.type === 'document'" class="document-icon">
                <Document />
              </el-icon>
              <el-icon v-else class="folder-icon">
                <Folder />
              </el-icon>
            </div>
            
            <div class="item-content">
              <div class="item-header">
                <h3 class="item-title" v-html="highlightText(item.title)"></h3>
                <div class="item-meta">
                  <span class="item-type">{{ item.type === 'document' ? '文档' : '文件夹' }}</span>
                  <span class="item-date">{{ formatRelativeTime(item.updatedAt) }}</span>
                </div>
              </div>
              
              <div class="item-path">
                <el-breadcrumb separator="/">
                  <el-breadcrumb-item 
                    v-for="(path, index) in item.breadcrumb" 
                    :key="index"
                  >
                    {{ path }}
                  </el-breadcrumb-item>
                </el-breadcrumb>
              </div>
              
              <div v-if="item.snippet" class="item-snippet" v-html="highlightText(item.snippet)"></div>
              
              <div v-if="item.tags?.length" class="item-tags">
                <el-tag 
                  v-for="tag in item.tags" 
                  :key="tag.id" 
                  size="small" 
                  type="info"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
            </div>
            
            <div class="item-actions">
              <el-dropdown trigger="click">
                <el-button text>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click.stop="openItem(item)">
                      <el-icon><View /></el-icon>
                      打开
                    </el-dropdown-item>
                    <el-dropdown-item @click.stop="editItem(item)">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item @click.stop="shareItem(item)">
                      <el-icon><Share /></el-icon>
                      分享
                    </el-dropdown-item>
                    <el-dropdown-item @click.stop="toggleFavorite(item)">
                      <el-icon><Star /></el-icon>
                      {{ item.isFavorite ? '取消收藏' : '收藏' }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
        
        <!-- 网格视图 -->
        <div v-else class="results-grid">
          <div 
            v-for="item in searchResults" 
            :key="item.id"
            class="grid-item"
            @click="openItem(item)"
          >
            <div class="grid-item-icon">
              <el-icon v-if="item.type === 'document'" class="document-icon">
                <Document />
              </el-icon>
              <el-icon v-else class="folder-icon">
                <Folder />
              </el-icon>
            </div>
            
            <div class="grid-item-content">
              <h4 class="grid-item-title" v-html="highlightText(item.title)"></h4>
              <p class="grid-item-snippet" v-html="highlightText(item.snippet || '')"></p>
              <div class="grid-item-meta">
                <span class="grid-item-type">{{ item.type === 'document' ? '文档' : '文件夹' }}</span>
                <span class="grid-item-date">{{ formatRelativeTime(item.updatedAt) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 分页 -->
        <div v-if="totalResults > pageSize" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="totalResults"
            layout="prev, pager, next, jumper, total"
            @current-change="handlePageChange"
          />
        </div>
      </div>
      
      <!-- 空状态 -->
      <EmptyState 
        v-else-if="hasSearched && !isSearching"
        icon="Search"
        :title="searchQuery ? '未找到相关结果' : '请输入搜索关键词'"
        :description="searchQuery ? `没有找到包含 '${searchQuery}' 的内容` : '输入关键词开始搜索文档和文件夹'"
      >
        <template #actions>
          <el-button @click="clearSearch">清除搜索</el-button>
        </template>
      </EmptyState>
      
      <!-- 默认状态 -->
      <div v-else class="search-suggestions">
        <h3>搜索建议</h3>
        <div class="suggestions-grid">
          <div class="suggestion-item" @click="searchQuery = '会议记录'">
            <el-icon><Document /></el-icon>
            <span>会议记录</span>
          </div>
          <div class="suggestion-item" @click="searchQuery = '项目计划'">
            <el-icon><Document /></el-icon>
            <span>项目计划</span>
          </div>
          <div class="suggestion-item" @click="searchQuery = '设计文档'">
            <el-icon><Document /></el-icon>
            <span>设计文档</span>
          </div>
          <div class="suggestion-item" @click="searchQuery = '技术文档'">
            <el-icon><Document /></el-icon>
            <span>技术文档</span>
          </div>
        </div>
        
        <h3>最近搜索</h3>
        <div class="recent-searches">
          <el-tag 
            v-for="(query, index) in recentSearches" 
            :key="index"
            closable
            @click="searchQuery = query"
            @close="removeRecentSearch(index)"
          >
            {{ query }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  Close,
  Setting,
  List,
  Grid,
  Document,
  Folder,
  MoreFilled,
  View,
  Edit,
  Share,
  Star
} from '@element-plus/icons-vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { formatRelativeTime } from '@/utils/date'
import { debounce } from '@/utils/common'

// 接口定义
interface SearchResult {
  id: string
  title: string
  type: 'document' | 'folder'
  snippet?: string
  breadcrumb: string[]
  tags?: Array<{ id: string; name: string }>
  author: { id: string; name: string }
  createdAt: Date
  updatedAt: Date
  isFavorite: boolean
}

interface Author {
  id: string
  name: string
}

interface Tag {
  id: string
  name: string
}

// 路由
const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const searchType = ref('all')
const sortBy = ref('relevance')
const viewMode = ref('list')
const showAdvancedSearch = ref(false)
const isSearching = ref(false)
const hasSearched = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const searchTime = ref(0)

// 高级搜索筛选
const advancedFilters = ref({
  author: '',
  dateRange: [],
  tags: [],
  fileType: ''
})

// 搜索结果
const searchResults = ref<SearchResult[]>([])
const totalResults = ref(0)
const authors = ref<Author[]>([])
const tags = ref<Tag[]>([])
const recentSearches = ref<string[]>([])

/**
 * 执行搜索
 */
const performSearch = async () => {
  if (!searchQuery.value.trim()) return
  
  isSearching.value = true
  hasSearched.value = true
  const startTime = Date.now()
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500 + Math.random() * 1000))
    
    // 模拟搜索结果
    const mockResults = generateMockResults(searchQuery.value)
    searchResults.value = mockResults
    totalResults.value = mockResults.length
    searchTime.value = Date.now() - startTime
    
    // 添加到最近搜索
    addToRecentSearches(searchQuery.value)
    
  } catch (error) {
    ElMessage.error('搜索失败，请重试')
  } finally {
    isSearching.value = false
  }
}

/**
 * 处理搜索输入（防抖）
 */
const handleSearch = debounce(() => {
  if (searchQuery.value.trim()) {
    performSearch()
  }
}, 300)

/**
 * 生成模拟搜索结果
 */
const generateMockResults = (query: string): SearchResult[] => {
  const mockData = [
    {
      id: '1',
      title: `包含"${query}"的文档1`,
      type: 'document' as const,
      snippet: `这是一个包含${query}关键词的文档摘要...`,
      breadcrumb: ['根目录', '项目文档'],
      tags: [{ id: '1', name: '重要' }],
      author: { id: '1', name: '张三' },
      createdAt: new Date('2024-01-15'),
      updatedAt: new Date('2024-01-20'),
      isFavorite: false
    },
    {
      id: '2',
      title: `${query}相关文件夹`,
      type: 'folder' as const,
      snippet: `包含多个${query}相关文档的文件夹`,
      breadcrumb: ['根目录'],
      author: { id: '2', name: '李四' },
      createdAt: new Date('2024-01-10'),
      updatedAt: new Date('2024-01-18'),
      isFavorite: true
    }
  ]
  
  return mockData.slice(0, Math.floor(Math.random() * 10) + 1)
}

/**
 * 高亮搜索关键词
 */
const highlightText = (text: string): string => {
  if (!searchQuery.value || !text) return text
  
  const regex = new RegExp(`(${searchQuery.value})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

/**
 * 清除搜索
 */
const clearSearch = () => {
  searchQuery.value = ''
  searchResults.value = []
  hasSearched.value = false
  totalResults.value = 0
}

/**
 * 处理类型变化
 */
const handleTypeChange = () => {
  if (hasSearched.value) {
    performSearch()
  }
}

/**
 * 处理排序变化
 */
const handleSortChange = () => {
  if (hasSearched.value) {
    performSearch()
  }
}

/**
 * 处理分页变化
 */
const handlePageChange = (page: number) => {
  currentPage.value = page
  performSearch()
}

/**
 * 打开项目
 */
const openItem = (item: SearchResult) => {
  if (item.type === 'document') {
    router.push(`/documents/${item.id}`)
  } else {
    router.push(`/folders/${item.id}`)
  }
}

/**
 * 编辑项目
 */
const editItem = (item: SearchResult) => {
  if (item.type === 'document') {
    router.push(`/documents/${item.id}/edit`)
  } else {
    router.push(`/folders/${item.id}/edit`)
  }
}

/**
 * 分享项目
 */
const shareItem = (item: SearchResult) => {
  ElMessage.success('分享链接已复制到剪贴板')
}

/**
 * 切换收藏状态
 */
const toggleFavorite = (item: SearchResult) => {
  item.isFavorite = !item.isFavorite
  ElMessage.success(item.isFavorite ? '已添加到收藏' : '已取消收藏')
}

/**
 * 添加到最近搜索
 */
const addToRecentSearches = (query: string) => {
  const index = recentSearches.value.indexOf(query)
  if (index > -1) {
    recentSearches.value.splice(index, 1)
  }
  recentSearches.value.unshift(query)
  if (recentSearches.value.length > 10) {
    recentSearches.value.pop()
  }
  
  // 保存到本地存储
  localStorage.setItem('recentSearches', JSON.stringify(recentSearches.value))
}

/**
 * 移除最近搜索
 */
const removeRecentSearch = (index: number) => {
  recentSearches.value.splice(index, 1)
  localStorage.setItem('recentSearches', JSON.stringify(recentSearches.value))
}

/**
 * 加载数据
 */
const loadData = async () => {
  // 加载作者列表
  authors.value = [
    { id: '1', name: '张三' },
    { id: '2', name: '李四' },
    { id: '3', name: '王五' }
  ]
  
  // 加载标签列表
  tags.value = [
    { id: '1', name: '重要' },
    { id: '2', name: '紧急' },
    { id: '3', name: '项目' },
    { id: '4', name: '会议' }
  ]
  
  // 加载最近搜索
  const saved = localStorage.getItem('recentSearches')
  if (saved) {
    recentSearches.value = JSON.parse(saved)
  }
}

// 组件挂载
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.search-view {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color-page);
}

.search-header {
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  padding: var(--spacing-xl);
}

.search-container {
  display: flex;
  gap: var(--spacing-md);
  max-width: 800px;
  margin: 0 auto var(--spacing-lg) auto;
}

.search-input {
  flex: 1;
}

.search-filters {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.advanced-search {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--spacing-lg);
  background: var(--el-fill-color-lighter);
  border-radius: var(--border-radius-base);
}

.search-content {
  flex: 1;
  padding: var(--spacing-xl);
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.search-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.stats-text {
  color: var(--el-text-color-regular);
}

.search-time {
  color: var(--el-text-color-placeholder);
  font-size: 0.875rem;
}

.results-container {
  min-height: 400px;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.result-item {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  cursor: pointer;
  transition: all 0.2s;
}

.result-item:hover {
  border-color: var(--el-color-primary);
  box-shadow: var(--el-box-shadow-light);
}

.item-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--border-radius-base);
  background: var(--el-fill-color-light);
}

.document-icon {
  color: var(--el-color-primary);
}

.folder-icon {
  color: var(--el-color-warning);
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-xs);
}

.item-title {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  gap: var(--spacing-sm);
  font-size: 0.875rem;
  color: var(--el-text-color-placeholder);
}

.item-path {
  margin-bottom: var(--spacing-xs);
  font-size: 0.875rem;
}

.item-snippet {
  margin-bottom: var(--spacing-sm);
  color: var(--el-text-color-regular);
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-tags {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.item-actions {
  flex-shrink: 0;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-lg);
}

.grid-item {
  padding: var(--spacing-lg);
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  cursor: pointer;
  transition: all 0.2s;
}

.grid-item:hover {
  border-color: var(--el-color-primary);
  box-shadow: var(--el-box-shadow-light);
}

.grid-item-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--border-radius-base);
  background: var(--el-fill-color-light);
  margin-bottom: var(--spacing-md);
}

.grid-item-title {
  margin: 0 0 var(--spacing-sm) 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.grid-item-snippet {
  margin: 0 0 var(--spacing-md) 0;
  color: var(--el-text-color-regular);
  font-size: 0.875rem;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.grid-item-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: var(--el-text-color-placeholder);
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-xl);
}

.search-suggestions {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}

.search-suggestions h3 {
  margin: var(--spacing-xl) 0 var(--spacing-lg) 0;
  color: var(--el-text-color-primary);
}

.suggestions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
}

.suggestion-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg);
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  cursor: pointer;
  transition: all 0.2s;
}

.suggestion-item:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.recent-searches {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  justify-content: center;
}

.recent-searches .el-tag {
  cursor: pointer;
}

/* 高亮样式 */
:deep(mark) {
  background: var(--el-color-warning-light-7);
  color: var(--el-color-warning-dark-2);
  padding: 0 2px;
  border-radius: 2px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-container {
    flex-direction: column;
  }
  
  .search-filters {
    flex-direction: column;
    gap: var(--spacing-md);
  }
  
  .search-stats {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }
  
  .results-grid {
    grid-template-columns: 1fr;
  }
  
  .suggestions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>