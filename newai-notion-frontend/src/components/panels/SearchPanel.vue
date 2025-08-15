<template>
  <div class="search-panel">
    <!-- 面板头部 -->
    <div class="panel-header">
      <div class="header-left">
        <el-icon class="panel-icon"><Search /></el-icon>
        <h3 class="panel-title">快速搜索</h3>
      </div>
      <div class="header-right">
        <el-button text @click="closePanel">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>
    
    <!-- 搜索输入 -->
    <div class="search-input-container">
      <el-input
        v-model="searchQuery"
        placeholder="搜索文档、文件夹..."
        @input="handleSearch"
        @keydown.enter="performSearch"
        @keydown.esc="closePanel"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #suffix>
          <el-button 
            v-if="searchQuery" 
            text 
            size="small"
            @click="clearSearch"
          >
            <el-icon><Close /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>
    
    <!-- 搜索筛选 -->
    <div class="search-filters">
      <el-radio-group v-model="searchType" size="small" @change="handleTypeChange">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="documents">文档</el-radio-button>
        <el-radio-button value="folders">文件夹</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 搜索结果 -->
    <div class="search-results">
      <!-- 加载状态 -->
      <div v-if="isSearching" class="loading-container">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>搜索中...</span>
      </div>
      
      <!-- 搜索结果列表 -->
      <div v-else-if="searchResults.length > 0" class="results-list">
        <div class="results-header">
          <span class="results-count">找到 {{ searchResults.length }} 个结果</span>
        </div>
        
        <div 
          v-for="(item, index) in searchResults" 
          :key="item.id"
          class="result-item"
          :class="{ active: selectedIndex === index }"
          @click="selectItem(item)"
          @mouseenter="selectedIndex = index"
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
            <div class="item-title" v-html="highlightText(item.title)"></div>
            <div class="item-path">{{ item.path }}</div>
            <div v-if="item.snippet" class="item-snippet" v-html="highlightText(item.snippet)"></div>
          </div>
          
          <div class="item-meta">
            <span class="item-date">{{ formatRelativeTime(item.updatedAt) }}</span>
          </div>
        </div>
        
        <div class="results-footer">
          <el-button text size="small" @click="viewAllResults">
            查看全部结果
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="hasSearched && !isSearching" class="empty-state">
        <el-icon class="empty-icon"><Search /></el-icon>
        <div class="empty-text">
          <div v-if="searchQuery">未找到相关结果</div>
          <div v-else>请输入搜索关键词</div>
        </div>
      </div>
      
      <!-- 默认状态 - 最近搜索和建议 -->
      <div v-else class="default-state">
        <!-- 最近搜索 -->
        <div v-if="recentSearches.length > 0" class="recent-section">
          <h4 class="section-title">最近搜索</h4>
          <div class="recent-list">
            <div 
              v-for="(query, index) in recentSearches" 
              :key="index"
              class="recent-item"
              @click="searchQuery = query; performSearch()"
            >
              <el-icon><Clock /></el-icon>
              <span>{{ query }}</span>
              <el-button 
                text 
                size="small" 
                @click.stop="removeRecentSearch(index)"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 快速访问 -->
        <div class="quick-access-section">
          <h4 class="section-title">快速访问</h4>
          <div class="quick-list">
            <div 
              v-for="item in quickAccessItems" 
              :key="item.id"
              class="quick-item"
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
                <div class="item-title">{{ item.title }}</div>
                <div class="item-meta">{{ formatRelativeTime(item.updatedAt) }}</div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 搜索建议 -->
        <div class="suggestions-section">
          <h4 class="section-title">搜索建议</h4>
          <div class="suggestions-list">
            <div 
              v-for="suggestion in searchSuggestions" 
              :key="suggestion"
              class="suggestion-item"
              @click="searchQuery = suggestion; performSearch()"
            >
              <el-icon><Search /></el-icon>
              <span>{{ suggestion }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  Close,
  Loading,
  Document,
  Folder,
  ArrowRight,
  Clock
} from '@element-plus/icons-vue'
import { formatRelativeTime } from '@/utils/date'
import { debounce } from '@/utils/common'

// 接口定义
interface SearchResult {
  id: string
  title: string
  type: 'document' | 'folder'
  path: string
  snippet?: string
  updatedAt: Date
}

// 事件定义
const emit = defineEmits<{
  close: []
}>()

// 路由
const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const searchType = ref('all')
const isSearching = ref(false)
const hasSearched = ref(false)
const searchResults = ref<SearchResult[]>([])
const selectedIndex = ref(-1)
const recentSearches = ref<string[]>([])
const quickAccessItems = ref<SearchResult[]>([])

// 搜索建议
const searchSuggestions = [
  '会议记录',
  '项目计划',
  '设计文档',
  '技术文档',
  '产品需求',
  '用户手册'
]

/**
 * 执行搜索
 */
const performSearch = async () => {
  if (!searchQuery.value.trim()) return
  
  isSearching.value = true
  hasSearched.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 300 + Math.random() * 500))
    
    // 模拟搜索结果
    const mockResults = generateMockResults(searchQuery.value)
    searchResults.value = mockResults
    selectedIndex.value = mockResults.length > 0 ? 0 : -1
    
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
  } else {
    searchResults.value = []
    hasSearched.value = false
    selectedIndex.value = -1
  }
}, 200)

/**
 * 生成模拟搜索结果
 */
const generateMockResults = (query: string): SearchResult[] => {
  const mockData = [
    {
      id: '1',
      title: `包含"${query}"的文档1`,
      type: 'document' as const,
      path: '根目录/项目文档',
      snippet: `这是一个包含${query}关键词的文档摘要...`,
      updatedAt: new Date('2024-01-20')
    },
    {
      id: '2',
      title: `${query}相关文件夹`,
      type: 'folder' as const,
      path: '根目录',
      updatedAt: new Date('2024-01-18')
    },
    {
      id: '3',
      title: `${query}项目计划`,
      type: 'document' as const,
      path: '根目录/计划文档',
      snippet: `关于${query}的详细计划和安排...`,
      updatedAt: new Date('2024-01-15')
    }
  ]
  
  return mockData.slice(0, Math.floor(Math.random() * 3) + 1)
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
  selectedIndex.value = -1
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
 * 选择项目
 */
const selectItem = (item: SearchResult) => {
  openItem(item)
  closePanel()
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
 * 查看全部结果
 */
const viewAllResults = () => {
  router.push({
    path: '/search',
    query: { q: searchQuery.value, type: searchType.value }
  })
  closePanel()
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
  if (recentSearches.value.length > 5) {
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
 * 关闭面板
 */
const closePanel = () => {
  emit('close')
}

/**
 * 键盘导航
 */
const handleKeydown = (event: KeyboardEvent) => {
  if (!searchResults.value.length) return
  
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      selectedIndex.value = Math.min(selectedIndex.value + 1, searchResults.value.length - 1)
      break
    case 'ArrowUp':
      event.preventDefault()
      selectedIndex.value = Math.max(selectedIndex.value - 1, 0)
      break
    case 'Enter':
      event.preventDefault()
      if (selectedIndex.value >= 0) {
        selectItem(searchResults.value[selectedIndex.value])
      }
      break
  }
}

/**
 * 加载数据
 */
const loadData = () => {
  // 加载最近搜索
  const saved = localStorage.getItem('recentSearches')
  if (saved) {
    recentSearches.value = JSON.parse(saved)
  }
  
  // 加载快速访问项目
  quickAccessItems.value = [
    {
      id: 'recent1',
      title: '最近的会议记录',
      type: 'document',
      path: '根目录/会议记录',
      updatedAt: new Date('2024-01-20')
    },
    {
      id: 'recent2',
      title: '项目文档',
      type: 'folder',
      path: '根目录',
      updatedAt: new Date('2024-01-19')
    },
    {
      id: 'recent3',
      title: '产品需求文档',
      type: 'document',
      path: '根目录/产品文档',
      updatedAt: new Date('2024-01-18')
    }
  ]
}

// 组件挂载
onMounted(() => {
  loadData()
  document.addEventListener('keydown', handleKeydown)
})

// 组件卸载
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.search-panel {
  width: 400px;
  max-height: 600px;
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

.search-input-container {
  padding: var(--spacing-md);
}

.search-filters {
  padding: 0 var(--spacing-md) var(--spacing-md);
}

.search-results {
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);
  color: var(--el-text-color-placeholder);
}

.loading-icon {
  font-size: 1.5rem;
  margin-bottom: var(--spacing-sm);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.results-list {
  padding: var(--spacing-sm);
}

.results-header {
  padding: var(--spacing-sm) var(--spacing-md);
  border-bottom: 1px solid var(--el-border-color-lighter);
  margin-bottom: var(--spacing-sm);
}

.results-count {
  font-size: 0.75rem;
  color: var(--el-text-color-placeholder);
}

.result-item {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius-small);
  cursor: pointer;
  transition: all 0.2s;
}

.result-item:hover,
.result-item.active {
  background: var(--el-fill-color-light);
}

.item-icon {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
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

.item-title {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: var(--spacing-xs);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-path {
  font-size: 0.75rem;
  color: var(--el-text-color-placeholder);
  margin-bottom: var(--spacing-xs);
}

.item-snippet {
  font-size: 0.75rem;
  color: var(--el-text-color-regular);
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-meta {
  flex-shrink: 0;
  text-align: right;
}

.item-date {
  font-size: 0.75rem;
  color: var(--el-text-color-placeholder);
}

.results-footer {
  padding: var(--spacing-sm) var(--spacing-md);
  border-top: 1px solid var(--el-border-color-lighter);
  text-align: center;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);
  color: var(--el-text-color-placeholder);
}

.empty-icon {
  font-size: 2rem;
  margin-bottom: var(--spacing-md);
}

.empty-text {
  text-align: center;
}

.default-state {
  padding: var(--spacing-sm);
}

.section-title {
  margin: var(--spacing-md) 0 var(--spacing-sm) 0;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--el-text-color-regular);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.recent-list,
.quick-list,
.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.recent-item,
.quick-item,
.suggestion-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  border-radius: var(--border-radius-small);
  cursor: pointer;
  transition: all 0.2s;
}

.recent-item:hover,
.quick-item:hover,
.suggestion-item:hover {
  background: var(--el-fill-color-light);
}

.recent-item {
  justify-content: space-between;
}

.recent-item span {
  flex: 1;
  font-size: 0.875rem;
  color: var(--el-text-color-regular);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quick-item .item-content {
  flex: 1;
  min-width: 0;
}

.quick-item .item-title {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: var(--spacing-xs);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quick-item .item-meta {
  font-size: 0.75rem;
  color: var(--el-text-color-placeholder);
}

.suggestion-item span {
  font-size: 0.875rem;
  color: var(--el-text-color-regular);
}

/* 高亮样式 */
:deep(mark) {
  background: var(--el-color-warning-light-7);
  color: var(--el-color-warning-dark-2);
  padding: 0 2px;
  border-radius: 2px;
}

/* 滚动条样式 */
.search-results::-webkit-scrollbar {
  width: 4px;
}

.search-results::-webkit-scrollbar-track {
  background: transparent;
}

.search-results::-webkit-scrollbar-thumb {
  background: var(--el-border-color-light);
  border-radius: 2px;
}

.search-results::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color);
}
</style>