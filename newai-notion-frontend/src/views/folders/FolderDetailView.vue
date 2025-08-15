<template>
  <div class="folder-detail-view">
    <div class="folder-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/app/folders' }">文件夹</el-breadcrumb-item>
        <el-breadcrumb-item>{{ folder?.name || '文件夹详情' }}</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="folder-actions">
        <el-button @click="editFolder">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button @click="shareFolder">
          <el-icon><Share /></el-icon>
          分享
        </el-button>
        <el-button type="primary" @click="createDocument">
          <el-icon><DocumentAdd /></el-icon>
          新建文档
        </el-button>
      </div>
    </div>
    
    <div class="folder-info" v-if="folder">
      <div class="folder-meta">
        <div class="folder-icon">
          <el-icon size="32"><Folder /></el-icon>
        </div>
        <div class="folder-details">
          <h1 class="folder-name">{{ folder.name }}</h1>
          <p class="folder-description">{{ folder.description || '暂无描述' }}</p>
          <div class="folder-stats">
            <span>{{ folder.documentCount }} 个文档</span>
            <span>创建于 {{ formatDate(folder.createdAt) }}</span>
            <span>更新于 {{ formatDate(folder.updatedAt) }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="documents-section">
      <div class="section-header">
        <h2>文档列表</h2>
        <div class="section-actions">
          <el-input
            v-model="searchQuery"
            placeholder="搜索文档..."
            clearable
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select v-model="sortBy" placeholder="排序" class="sort-select">
            <el-option label="更新时间" value="updatedAt" />
            <el-option label="创建时间" value="createdAt" />
            <el-option label="标题" value="title" />
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
      
      <div class="documents-content">
        <LoadingSpinner v-if="loading" text="加载中..." />
        
        <div v-else-if="filteredDocuments.length > 0">
          <!-- 网格视图 -->
          <div v-if="viewMode === 'grid'" class="documents-grid">
            <div
              v-for="document in filteredDocuments"
              :key="document.id"
              class="document-card"
              @click="openDocument(document)"
            >
              <div class="document-icon">
                <el-icon size="24"><Document /></el-icon>
              </div>
              <div class="document-info">
                <h3 class="document-title">{{ document.title }}</h3>
                <p class="document-preview">{{ document.preview }}</p>
                <div class="document-meta">
                  <span>{{ formatDate(document.updatedAt) }}</span>
                </div>
              </div>
              <div class="document-actions" @click.stop>
                <el-dropdown trigger="click">
                  <el-button text>
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="editDocument(document)">
                        <el-icon><Edit /></el-icon>
                        编辑
                      </el-dropdown-item>
                      <el-dropdown-item @click="shareDocument(document)">
                        <el-icon><Share /></el-icon>
                        分享
                      </el-dropdown-item>
                      <el-dropdown-item @click="moveDocument(document)">
                        <el-icon><FolderOpened /></el-icon>
                        移动
                      </el-dropdown-item>
                      <el-dropdown-item divided @click="deleteDocument(document)">
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
          <el-table v-else :data="filteredDocuments" @row-click="openDocument">
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="{ row }">
                <div class="document-title-cell">
                  <el-icon><Document /></el-icon>
                  <span>{{ row.title }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="preview" label="预览" min-width="300" show-overflow-tooltip />
            <el-table-column prop="updatedAt" label="更新时间" width="150">
              <template #default="{ row }">
                {{ formatDate(row.updatedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-dropdown trigger="click">
                  <el-button text>
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="editDocument(row)">
                        <el-icon><Edit /></el-icon>
                        编辑
                      </el-dropdown-item>
                      <el-dropdown-item @click="shareDocument(row)">
                        <el-icon><Share /></el-icon>
                        分享
                      </el-dropdown-item>
                      <el-dropdown-item @click="moveDocument(row)">
                        <el-icon><FolderOpened /></el-icon>
                        移动
                      </el-dropdown-item>
                      <el-dropdown-item divided @click="deleteDocument(row)">
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <EmptyState
          v-else
          title="暂无文档"
          description="在此文件夹中创建您的第一个文档"
        >
          <el-button type="primary" @click="createDocument">
            <el-icon><DocumentAdd /></el-icon>
            新建文档
          </el-button>
        </EmptyState>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit,
  Share,
  DocumentAdd,
  Folder,
  Search,
  Grid,
  List,
  Document,
  MoreFilled,
  FolderOpened,
  Delete
} from '@element-plus/icons-vue'
import { LoadingSpinner, EmptyState } from '@/components/common'
import { formatRelativeTime } from '@/utils'

interface DocumentItem {
  id: string
  title: string
  preview: string
  createdAt: string
  updatedAt: string
}

interface FolderItem {
  id: string
  name: string
  description?: string
  documentCount: number
  createdAt: string
  updatedAt: string
}

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const searchQuery = ref('')
const sortBy = ref('updatedAt')
const viewMode = ref<'grid' | 'list'>('grid')
const folder = ref<FolderItem | null>(null)
const documents = ref<DocumentItem[]>([])

/**
 * 过滤和排序后的文档列表
 */
const filteredDocuments = computed(() => {
  let result = documents.value
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(doc => 
      doc.title.toLowerCase().includes(query) ||
      doc.preview.toLowerCase().includes(query)
    )
  }
  
  // 排序
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'title':
        return a.title.localeCompare(b.title)
      case 'createdAt':
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      default: // updatedAt
        return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
    }
  })
  
  return result
})

/**
 * 格式化日期
 */
const formatDate = (date: string) => {
  return formatRelativeTime(new Date(date))
}

/**
 * 编辑文件夹
 */
const editFolder = () => {
  // TODO: 打开编辑文件夹对话框
  ElMessage.info('编辑文件夹功能开发中')
}

/**
 * 分享文件夹
 */
const shareFolder = () => {
  // TODO: 实现分享功能
  ElMessage.info('分享功能开发中')
}

/**
 * 创建文档
 */
const createDocument = () => {
  // 直接跳转到文章创建页面，不发送后端请求
  router.push('/app/articles/create')
}

/**
 * 打开文档
 */
const openDocument = (document: DocumentItem) => {
  router.push(`/app/documents/${document.id}`)
}

/**
 * 编辑文档
 */
const editDocument = (document: DocumentItem) => {
  router.push(`/app/documents/${document.id}/edit`)
}

/**
 * 分享文档
 */
const shareDocument = (document: DocumentItem) => {
  // TODO: 实现分享功能
  ElMessage.info('分享功能开发中')
}

/**
 * 移动文档
 */
const moveDocument = (document: DocumentItem) => {
  // TODO: 实现移动功能
  ElMessage.info('移动功能开发中')
}

/**
 * 删除文档
 */
const deleteDocument = async (document: DocumentItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档 "${document.title}" 吗？`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }
    )
    
    // TODO: 调用API删除文档
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const index = documents.value.findIndex(d => d.id === document.id)
    if (index !== -1) {
      documents.value.splice(index, 1)
    }
    
    // 更新文件夹文档数量
    if (folder.value) {
      folder.value.documentCount--
    }
    
    ElMessage.success('文档删除成功')
  } catch (error) {
    // 用户取消删除
  }
}

/**
 * 加载文件夹和文档数据
 */
const loadData = async () => {
  try {
    loading.value = true
    const folderId = route.params.id as string
    
    // TODO: 调用API获取文件夹和文档数据
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    folder.value = {
      id: folderId,
      name: '工作文档',
      description: '存放工作相关的文档',
      documentCount: 5,
      createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString()
    }
    
    documents.value = [
      {
        id: '1',
        title: '项目计划书',
        preview: '这是一个详细的项目计划书，包含了项目的目标、时间线和资源分配...',
        createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(),
        updatedAt: new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString()
      },
      {
        id: '2',
        title: '会议纪要',
        preview: '2024年第一季度工作会议纪要，讨论了项目进展和下一步计划...',
        createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString(),
        updatedAt: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString()
      },
      {
        id: '3',
        title: '技术文档',
        preview: '系统架构设计文档，详细描述了系统的各个模块和接口...',
        createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
        updatedAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString()
      }
    ]
  } catch (error) {
    console.error('Load data error:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.folder-detail-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.folder-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.folder-actions {
  display: flex;
  gap: 12px;
}

.folder-info {
  padding: 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.folder-meta {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.folder-icon {
  color: var(--el-color-primary);
}

.folder-details {
  flex: 1;
}

.folder-name {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
}

.folder-description {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin: 0 0 12px 0;
  line-height: 1.5;
}

.folder-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.documents-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.section-header h2 {
  font-size: 18px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin: 0;
}

.section-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 200px;
}

.sort-select {
  width: 120px;
}

.documents-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.documents-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.document-card {
  position: relative;
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
  cursor: pointer;
  transition: all 0.2s ease;
}

.document-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.document-icon {
  color: var(--el-color-primary);
  margin-bottom: 12px;
}

.document-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.document-preview {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin: 0 0 12px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.document-meta {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.document-actions {
  position: absolute;
  top: 12px;
  right: 12px;
}

.document-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .folder-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .folder-meta {
    flex-direction: column;
    gap: 12px;
  }
  
  .folder-stats {
    flex-direction: column;
    gap: 8px;
  }
  
  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .section-actions {
    flex-direction: column;
  }
  
  .search-input {
    width: 100%;
  }
  
  .documents-grid {
    grid-template-columns: 1fr;
  }
}
</style>