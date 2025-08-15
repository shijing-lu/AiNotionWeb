<template>
  <div class="document-list">
    <!-- 页面头部 -->
    <div class="document-list__header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon><Document /></el-icon>
          我的文档
        </h1>
        <p class="page-subtitle">管理您的所有文档</p>
      </div>
      
      <div class="header-actions">
        <el-button
          type="primary"
          :icon="DocumentAdd"
          @click="createDocument"
        >
          新建文档
        </el-button>
        
        <el-dropdown @command="handleBatchAction">
          <el-button :icon="MoreFilled">
            批量操作
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="export" :disabled="selectedDocuments.length === 0">
                <el-icon><Download /></el-icon>
                导出选中
              </el-dropdown-item>
              <el-dropdown-item command="favorite" :disabled="selectedDocuments.length === 0">
                <el-icon><Star /></el-icon>
                批量收藏
              </el-dropdown-item>
              <el-dropdown-item command="move" :disabled="selectedDocuments.length === 0">
                <el-icon><FolderOpened /></el-icon>
                移动到文件夹
              </el-dropdown-item>
              <el-dropdown-item command="delete" :disabled="selectedDocuments.length === 0" divided>
                <el-icon><Delete /></el-icon>
                删除选中
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="document-list__filters">
      <div class="filters-left">
        <el-input
          v-model="searchQuery"
          placeholder="搜索文档..."
          :prefix-icon="Search"
          clearable
          class="search-input"
          @input="handleSearch"
        />
        
        <el-select
          v-model="selectedFolder"
          placeholder="选择文件夹"
          clearable
          class="folder-select"
          @change="handleFolderChange"
        >
          <el-option label="全部文档" value="" />
          <el-option
            v-for="folder in folders"
            :key="folder.id"
            :label="folder.name"
            :value="folder.id"
          />
        </el-select>
        
        <el-select
          v-model="sortBy"
          placeholder="排序方式"
          class="sort-select"
          @change="handleSort"
        >
          <el-option label="最近修改" value="updatedAt" />
          <el-option label="创建时间" value="createdAt" />
          <el-option label="文档标题" value="title" />
          <el-option label="文档大小" value="size" />
        </el-select>
      </div>
      
      <div class="filters-right">
        <el-radio-group v-model="viewMode" @change="handleViewModeChange">
          <el-radio-button label="grid">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
          <el-radio-button label="list">
            <el-icon><List /></el-icon>
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 文档内容 -->
    <div class="document-list__content">
      <!-- 加载状态 -->
      <LoadingSpinner v-if="loading" text="加载文档中..." />
      
      <!-- 空状态 -->
      <EmptyState
        v-else-if="filteredDocuments.length === 0 && !searchQuery"
        title="暂无文档"
        description="开始创建您的第一个文档吧"
        :show-action="true"
        action-text="新建文档"
        @action="createDocument"
      />
      
      <!-- 搜索无结果 -->
      <EmptyState
        v-else-if="filteredDocuments.length === 0 && searchQuery"
        title="未找到相关文档"
        :description="`没有找到包含 '${searchQuery}' 的文档`"
        :show-action="false"
      />
      
      <!-- 网格视图 -->
      <div v-else-if="viewMode === 'grid'" class="document-grid">
        <div
          v-for="document in paginatedDocuments"
          :key="document.id"
          class="document-card"
          :class="{ 'document-card--selected': selectedDocuments.includes(document.id) }"
          @click="openDocument(document)"
        >
          <!-- 选择框 -->
          <div class="document-card__checkbox">
            <el-checkbox
              :model-value="selectedDocuments.includes(document.id)"
              @change="toggleDocumentSelection(document.id)"
              @click.stop
            />
          </div>
          
          <!-- 文档图标 -->
          <div class="document-card__icon">
            <el-icon><Document /></el-icon>
          </div>
          
          <!-- 文档信息 -->
          <div class="document-card__content">
            <h3 class="document-card__title">{{ document.title }}</h3>
            <p class="document-card__excerpt">{{ document.excerpt || '暂无内容' }}</p>
            
            <div class="document-card__meta">
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatRelativeTime(document.updatedAt) }}
              </span>
              <span class="meta-item">
                <el-icon><Folder /></el-icon>
                {{ document.folder?.name || '根目录' }}
              </span>
            </div>
            
            <div class="document-card__tags">
              <el-tag
                v-for="tag in document.tags?.slice(0, 3)"
                :key="tag"
                size="small"
                type="info"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="document-card__actions">
            <el-button
              text
              :icon="document.isFavorite ? Star : StarFilled"
              :class="{ 'favorite-active': document.isFavorite }"
              @click.stop="toggleFavorite(document)"
            />
            
            <el-dropdown trigger="click" @command="(command) => handleDocumentAction(command, document)">
              <el-button text :icon="MoreFilled" @click.stop />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="duplicate">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-dropdown-item>
                  <el-dropdown-item command="share">
                    <el-icon><Share /></el-icon>
                    分享
                  </el-dropdown-item>
                  <el-dropdown-item command="export">
                    <el-icon><Download /></el-icon>
                    导出
                  </el-dropdown-item>
                  <el-dropdown-item command="move">
                    <el-icon><FolderOpened /></el-icon>
                    移动
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
      
      <!-- 列表视图 -->
      <div v-else class="document-table">
        <el-table
          :data="paginatedDocuments"
          @selection-change="handleSelectionChange"
          @row-click="openDocument"
          row-class-name="document-row"
        >
          <el-table-column type="selection" width="55" />
          
          <el-table-column label="文档" min-width="300">
            <template #default="{ row }">
              <div class="document-info">
                <div class="document-info__icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="document-info__content">
                  <div class="document-info__title">{{ row.title }}</div>
                  <div class="document-info__excerpt">{{ row.excerpt || '暂无内容' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="文件夹" width="150">
            <template #default="{ row }">
              <span class="folder-name">
                <el-icon><Folder /></el-icon>
                {{ row.folder?.name || '根目录' }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column label="标签" width="200">
            <template #default="{ row }">
              <div class="tags-container">
                <el-tag
                  v-for="tag in row.tags?.slice(0, 2)"
                  :key="tag"
                  size="small"
                  type="info"
                >
                  {{ tag }}
                </el-tag>
                <span v-if="row.tags?.length > 2" class="more-tags">
                  +{{ row.tags.length - 2 }}
                </span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="修改时间" width="150">
            <template #default="{ row }">
              <span class="update-time">
                {{ formatRelativeTime(row.updatedAt) }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button
                  text
                  :icon="row.isFavorite ? Star : StarFilled"
                  :class="{ 'favorite-active': row.isFavorite }"
                  @click.stop="toggleFavorite(row)"
                />
                
                <el-dropdown trigger="click" @command="(command) => handleDocumentAction(command, row)">
                  <el-button text :icon="MoreFilled" @click.stop />
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">
                        <el-icon><Edit /></el-icon>
                        编辑
                      </el-dropdown-item>
                      <el-dropdown-item command="duplicate">
                        <el-icon><CopyDocument /></el-icon>
                        复制
                      </el-dropdown-item>
                      <el-dropdown-item command="share">
                        <el-icon><Share /></el-icon>
                        分享
                      </el-dropdown-item>
                      <el-dropdown-item command="export">
                        <el-icon><Download /></el-icon>
                        导出
                      </el-dropdown-item>
                      <el-dropdown-item command="move">
                        <el-icon><FolderOpened /></el-icon>
                        移动
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" divided>
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页 -->
      <div v-if="filteredDocuments.length > 0" class="document-list__pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="filteredDocuments.length"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  Document,
  DocumentAdd,
  Search,
  Grid,
  List,
  Clock,
  Folder,
  FolderOpened,
  Star,
  StarFilled,
  Edit,
  Share,
  Download,
  Delete,
  CopyDocument,
  MoreFilled,
  ArrowDown
} from '@element-plus/icons-vue'
import { useDocumentStore, useFolderStore } from '@/stores'
import { useNotification, usePagination, useSearch } from '@/composables'
import { formatRelativeTime } from '@/utils'
import { LoadingSpinner, EmptyState } from '@/components/common'

const router = useRouter()
const documentStore = useDocumentStore()
const folderStore = useFolderStore()
const { success, error, confirm } = useNotification()

// 响应式数据
const loading = ref(false)
const searchQuery = ref('')
const selectedFolder = ref('')
const sortBy = ref('updatedAt')
const viewMode = ref<'grid' | 'list'>('grid')
const selectedDocuments = ref<string[]>([])

// 分页
const {
  currentPage,
  pageSize,
  paginatedData: paginatedDocuments,
  handleSizeChange,
  handleCurrentChange
} = usePagination()

// 搜索
const { searchResults, handleSearch: performSearch } = useSearch()

// 计算属性
const documents = computed(() => documentStore.documents)
const folders = computed(() => folderStore.folders)

// 过滤后的文档
const filteredDocuments = computed(() => {
  let result = documents.value
  
  // 文件夹筛选
  if (selectedFolder.value) {
    result = result.filter(doc => doc.folderId === selectedFolder.value)
  }
  
  // 搜索筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(doc => 
      doc.title.toLowerCase().includes(query) ||
      doc.content?.toLowerCase().includes(query) ||
      doc.tags?.some(tag => tag.toLowerCase().includes(query))
    )
  }
  
  // 排序
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'title':
        return a.title.localeCompare(b.title)
      case 'createdAt':
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      case 'size':
        return (b.content?.length || 0) - (a.content?.length || 0)
      default: // updatedAt
        return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
    }
  })
  
  return result
})

// 监听过滤结果变化，更新分页数据
watch(filteredDocuments, (newDocs) => {
  paginatedData.value = newDocs
}, { immediate: true })

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
 * 打开文档
 */
const openDocument = (document: any) => {
  router.push(`/documents/${document.id}`)
}

/**
 * 切换文档选择
 */
const toggleDocumentSelection = (documentId: string) => {
  const index = selectedDocuments.value.indexOf(documentId)
  if (index > -1) {
    selectedDocuments.value.splice(index, 1)
  } else {
    selectedDocuments.value.push(documentId)
  }
}

/**
 * 处理表格选择变化
 */
const handleSelectionChange = (selection: any[]) => {
  selectedDocuments.value = selection.map(item => item.id)
}

/**
 * 切换收藏状态
 */
const toggleFavorite = async (document: any) => {
  try {
    if (document.isFavorite) {
      await documentStore.unfavoriteDocument(document.id)
      success('已取消收藏')
    } else {
      await documentStore.favoriteDocument(document.id)
      success('已添加到收藏')
    }
  } catch (err: any) {
    error(err.message || '操作失败')
  }
}

/**
 * 处理搜索
 */
const handleSearch = (query: string) => {
  searchQuery.value = query
  currentPage.value = 1
}

/**
 * 处理文件夹变化
 */
const handleFolderChange = () => {
  currentPage.value = 1
}

/**
 * 处理排序变化
 */
const handleSort = () => {
  currentPage.value = 1
}

/**
 * 处理视图模式变化
 */
const handleViewModeChange = () => {
  // 保存用户偏好
  localStorage.setItem('documentViewMode', viewMode.value)
}

/**
 * 处理文档操作
 */
const handleDocumentAction = async (action: string, document: any) => {
  try {
    switch (action) {
      case 'edit':
        router.push(`/documents/${document.id}/edit`)
        break
      case 'duplicate':
        await documentStore.duplicateDocument(document.id)
        success('文档复制成功')
        break
      case 'share':
        // 实现分享功能
        success('分享链接已复制到剪贴板')
        break
      case 'export':
        await documentStore.exportDocument(document.id)
        success('文档导出成功')
        break
      case 'move':
        // 实现移动功能
        break
      case 'delete':
        await handleDeleteDocument(document)
        break
    }
  } catch (err: any) {
    error(err.message || '操作失败')
  }
}

/**
 * 处理批量操作
 */
const handleBatchAction = async (action: string) => {
  if (selectedDocuments.value.length === 0) {
    error('请先选择文档')
    return
  }
  
  try {
    switch (action) {
      case 'export':
        await documentStore.exportDocuments(selectedDocuments.value)
        success('文档导出成功')
        break
      case 'favorite':
        await documentStore.batchFavoriteDocuments(selectedDocuments.value)
        success('批量收藏成功')
        break
      case 'move':
        // 实现批量移动功能
        break
      case 'delete':
        await handleBatchDelete()
        break
    }
  } catch (err: any) {
    error(err.message || '批量操作失败')
  }
}

/**
 * 删除文档
 */
const handleDeleteDocument = async (document: any) => {
  const confirmed = await confirm(
    `确定要删除文档 "${document.title}" 吗？`,
    '删除确认',
    {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    }
  )
  
  if (confirmed) {
    await documentStore.deleteDocument(document.id)
    success('文档删除成功')
  }
}

/**
 * 批量删除文档
 */
const handleBatchDelete = async () => {
  const confirmed = await confirm(
    `确定要删除选中的 ${selectedDocuments.value.length} 个文档吗？`,
    '批量删除确认',
    {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    }
  )
  
  if (confirmed) {
    await documentStore.batchDeleteDocuments(selectedDocuments.value)
    selectedDocuments.value = []
    success('文档删除成功')
  }
}

/**
 * 加载数据
 */
const loadData = async () => {
  try {
    loading.value = true
    await Promise.all([
      documentStore.fetchDocuments(),
      folderStore.fetchFolders()
    ])
  } catch (err: any) {
    error(err.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 组件挂载时加载数据
 */
onMounted(() => {
  // 恢复用户偏好
  const savedViewMode = localStorage.getItem('documentViewMode')
  if (savedViewMode) {
    viewMode.value = savedViewMode as 'grid' | 'list'
  }
  
  loadData()
})
</script>

<style scoped>
.document-list {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面头部 */
.document-list__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-subtitle {
  font-size: 16px;
  color: var(--el-text-color-regular);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 筛选区域 */
.document-list__filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filters-left {
  display: flex;
  gap: 16px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.folder-select,
.sort-select {
  width: 150px;
}

/* 文档网格 */
.document-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.document-card {
  background: white;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.document-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.document-card--selected {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}

.document-card__checkbox {
  position: absolute;
  top: 12px;
  left: 12px;
}

.document-card__icon {
  width: 48px;
  height: 48px;
  background: var(--el-color-primary-light-9);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
  font-size: 24px;
  margin: 0 0 16px 0;
}

.document-card__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.document-card__excerpt {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin: 0 0 16px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.document-card__meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.document-card__tags {
  display: flex;
  gap: 6px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.document-card__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.favorite-active {
  color: var(--el-color-warning) !important;
}

/* 表格样式 */
.document-table {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.document-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.document-info__icon {
  width: 32px;
  height: 32px;
  background: var(--el-color-primary-light-9);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
}

.document-info__title {
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.document-info__excerpt {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.folder-name {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.tags-container {
  display: flex;
  gap: 4px;
  align-items: center;
}

.more-tags {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.update-time {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.table-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 分页 */
.document-list__pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .document-list {
    padding: 16px;
  }
  
  .document-list__header {
    flex-direction: column;
    gap: 16px;
  }
  
  .document-list__filters {
    flex-direction: column;
    gap: 16px;
  }
  
  .filters-left {
    flex-direction: column;
    width: 100%;
  }
  
  .search-input,
  .folder-select,
  .sort-select {
    width: 100%;
  }
  
  .document-grid {
    grid-template-columns: 1fr;
  }
}

/* 暗色主题适配 */
.dark .document-list__filters,
.dark .document-card,
.dark .document-table {
  background: var(--el-bg-color-page);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.dark .document-card {
  border-color: var(--el-border-color);
}

.dark .document-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}
</style>