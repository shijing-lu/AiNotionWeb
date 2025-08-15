<template>
  <div class="folder-list-view">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">文件夹</h1>
        <span class="folder-count">{{ folders.length }} 个文件夹</span>
      </div>
      
      <div class="header-actions">
        <el-button type="primary" @click="createFolder">
          <el-icon><FolderAdd /></el-icon>
          新建文件夹
        </el-button>
      </div>
    </div>
    
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索文件夹..."
        clearable
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select v-model="sortBy" placeholder="排序方式" class="sort-select">
        <el-option label="名称" value="name" />
        <el-option label="创建时间" value="createdAt" />
        <el-option label="更新时间" value="updatedAt" />
        <el-option label="文档数量" value="documentCount" />
      </el-select>
    </div>
    
    <div class="folder-content">
      <LoadingSpinner v-if="loading" text="加载中..." />
      
      <div v-else-if="filteredFolders.length > 0" class="folder-grid">
        <div
          v-for="folder in filteredFolders"
          :key="folder.id"
          class="folder-card"
          @click="openFolder(folder)"
        >
          <div class="folder-icon">
            <el-icon size="48"><Folder /></el-icon>
          </div>
          
          <div class="folder-info">
            <h3 class="folder-name">{{ folder.name }}</h3>
            <p class="folder-description">{{ folder.description || '暂无描述' }}</p>
            <div class="folder-meta">
              <span class="document-count">{{ folder.documentCount }} 个文档</span>
              <span class="update-time">{{ formatRelativeTime(new Date(folder.updatedAt)) }}</span>
            </div>
          </div>
          
          <div class="folder-actions" @click.stop>
            <el-dropdown trigger="click">
              <el-button text>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="editFolder(folder)">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item @click="shareFolder(folder)">
                    <el-icon><Share /></el-icon>
                    分享
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="deleteFolder(folder)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
      
      <EmptyState
        v-else
        title="暂无文件夹"
        description="创建您的第一个文件夹来组织文档"
      >
        <el-button type="primary" @click="createFolder">
          <el-icon><FolderAdd /></el-icon>
          新建文件夹
        </el-button>
      </EmptyState>
    </div>
    
    <!-- 创建/编辑文件夹对话框 -->
    <el-dialog
      v-model="folderDialogVisible"
      :title="editingFolder ? '编辑文件夹' : '新建文件夹'"
      width="500px"
    >
      <el-form :model="folderForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="folderForm.name" placeholder="请输入文件夹名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="folderForm.description"
            type="textarea"
            placeholder="请输入文件夹描述（可选）"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="folderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFolder" :loading="saving">
          {{ editingFolder ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  FolderAdd,
  Search,
  Folder,
  MoreFilled,
  Edit,
  Share,
  Delete
} from '@element-plus/icons-vue'
import { LoadingSpinner, EmptyState } from '@/components/common'
import { formatRelativeTime } from '@/utils'

interface FolderItem {
  id: string
  name: string
  description?: string
  documentCount: number
  createdAt: string
  updatedAt: string
}

const router = useRouter()
const loading = ref(true)
const saving = ref(false)
const searchQuery = ref('')
const sortBy = ref('updatedAt')
const folders = ref<FolderItem[]>([])
const folderDialogVisible = ref(false)
const editingFolder = ref<FolderItem | null>(null)

// 文件夹表单
const folderForm = ref({
  name: '',
  description: ''
})

/**
 * 过滤和排序后的文件夹列表
 */
const filteredFolders = computed(() => {
  let result = folders.value
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(folder => 
      folder.name.toLowerCase().includes(query) ||
      folder.description?.toLowerCase().includes(query)
    )
  }
  
  // 排序
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'name':
        return a.name.localeCompare(b.name)
      case 'createdAt':
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      case 'documentCount':
        return b.documentCount - a.documentCount
      default: // updatedAt
        return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
    }
  })
  
  return result
})

/**
 * 打开文件夹
 */
const openFolder = (folder: FolderItem) => {
  router.push(`/app/folders/${folder.id}`)
}

/**
 * 创建文件夹
 */
const createFolder = () => {
  editingFolder.value = null
  folderForm.value = {
    name: '',
    description: ''
  }
  folderDialogVisible.value = true
}

/**
 * 编辑文件夹
 */
const editFolder = (folder: FolderItem) => {
  editingFolder.value = folder
  folderForm.value = {
    name: folder.name,
    description: folder.description || ''
  }
  folderDialogVisible.value = true
}

/**
 * 保存文件夹
 */
const saveFolder = async () => {
  if (!folderForm.value.name.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }
  
  try {
    saving.value = true
    // TODO: 调用API保存文件夹
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    if (editingFolder.value) {
      // 更新现有文件夹
      const index = folders.value.findIndex(f => f.id === editingFolder.value!.id)
      if (index !== -1) {
        folders.value[index] = {
          ...folders.value[index],
          name: folderForm.value.name,
          description: folderForm.value.description,
          updatedAt: new Date().toISOString()
        }
      }
      ElMessage.success('文件夹更新成功')
    } else {
      // 创建新文件夹
      const newFolder: FolderItem = {
        id: Date.now().toString(),
        name: folderForm.value.name,
        description: folderForm.value.description,
        documentCount: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
      folders.value.unshift(newFolder)
      ElMessage.success('文件夹创建成功')
    }
    
    folderDialogVisible.value = false
  } catch (error) {
    console.error('Save folder error:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

/**
 * 分享文件夹
 */
const shareFolder = (folder: FolderItem) => {
  // TODO: 实现分享功能
  ElMessage.info('分享功能开发中')
}

/**
 * 删除文件夹
 */
const deleteFolder = async (folder: FolderItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件夹 "${folder.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }
    )
    
    // TODO: 调用API删除文件夹
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const index = folders.value.findIndex(f => f.id === folder.id)
    if (index !== -1) {
      folders.value.splice(index, 1)
    }
    
    ElMessage.success('文件夹删除成功')
  } catch (error) {
    // 用户取消删除
  }
}

/**
 * 加载文件夹列表
 */
const loadFolders = async () => {
  try {
    loading.value = true
    // TODO: 调用API获取文件夹列表
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    folders.value = [
      {
        id: '1',
        name: '工作文档',
        description: '存放工作相关的文档',
        documentCount: 15,
        createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
        updatedAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString()
      },
      {
        id: '2',
        name: '个人笔记',
        description: '个人学习和思考的记录',
        documentCount: 8,
        createdAt: new Date(Date.now() - 14 * 24 * 60 * 60 * 1000).toISOString(),
        updatedAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString()
      },
      {
        id: '3',
        name: '项目资料',
        documentCount: 23,
        createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(),
        updatedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString()
      }
    ]
  } catch (error) {
    console.error('Load folders error:', error)
    ElMessage.error('加载文件夹失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFolders()
})
</script>

<style scoped>
.folder-list-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
}

.folder-count {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.search-bar {
  display: flex;
  gap: 16px;
  padding: 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.search-input {
  flex: 1;
  max-width: 400px;
}

.sort-select {
  width: 120px;
}

.folder-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.folder-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.folder-card {
  position: relative;
  padding: 20px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
  cursor: pointer;
  transition: all 0.2s ease;
}

.folder-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.folder-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
  color: var(--el-color-primary);
}

.folder-info {
  text-align: center;
}

.folder-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
}

.folder-description {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.folder-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.folder-actions {
  position: absolute;
  top: 12px;
  right: 12px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .search-bar {
    flex-direction: column;
  }
  
  .search-input {
    max-width: none;
  }
  
  .folder-grid {
    grid-template-columns: 1fr;
  }
}
</style>