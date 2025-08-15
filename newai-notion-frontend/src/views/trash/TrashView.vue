<template>
  <div class="trash-view">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">回收站</h1>
        <span class="item-count">{{ trashedItems.length }} 个项目</span>
      </div>
      
      <div class="header-actions">
        <el-button
          v-if="selectedItems.length > 0"
          @click="restoreSelected"
          :loading="restoring"
        >
          <el-icon><RefreshRight /></el-icon>
          恢复选中
        </el-button>
        <el-button
          v-if="selectedItems.length > 0"
          type="danger"
          @click="deleteSelectedPermanently"
          :loading="deleting"
        >
          <el-icon><Delete /></el-icon>
          永久删除
        </el-button>
        <el-button
          v-if="trashedItems.length > 0"
          type="danger"
          @click="clearTrash"
          :loading="clearing"
        >
          <el-icon><DeleteFilled /></el-icon>
          清空回收站
        </el-button>
      </div>
    </div>
    
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索回收站..."
        clearable
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select v-model="filterType" placeholder="类型" class="filter-select">
        <el-option label="全部" value="all" />
        <el-option label="文档" value="document" />
        <el-option label="文件夹" value="folder" />
      </el-select>
      
      <el-select v-model="sortBy" placeholder="排序" class="sort-select">
        <el-option label="删除时间" value="deletedAt" />
        <el-option label="名称" value="name" />
        <el-option label="类型" value="type" />
      </el-select>
    </div>
    
    <div class="trash-content">
      <LoadingSpinner v-if="loading" text="加载中..." />
      
      <div v-else-if="filteredItems.length > 0" class="trash-table">
        <el-table
          :data="filteredItems"
          @selection-change="handleSelectionChange"
          row-key="id"
        >
          <el-table-column type="selection" width="55" />
          
          <el-table-column label="名称" min-width="200">
            <template #default="{ row }">
              <div class="item-name-cell">
                <el-icon v-if="row.type === 'document'" class="item-icon">
                  <Document />
                </el-icon>
                <el-icon v-else class="item-icon">
                  <Folder />
                </el-icon>
                <span class="item-name">{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.type === 'document' ? 'primary' : 'success'" size="small">
                {{ row.type === 'document' ? '文档' : '文件夹' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="原位置" min-width="150">
            <template #default="{ row }">
              <span class="original-path">{{ row.originalPath || '根目录' }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="deletedAt" label="删除时间" width="150">
            <template #default="{ row }">
              {{ formatDate(row.deletedAt) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button
                size="small"
                @click="restoreItem(row)"
                :loading="row.restoring"
              >
                <el-icon><RefreshRight /></el-icon>
                恢复
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="deleteItemPermanently(row)"
                :loading="row.deleting"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <EmptyState
        v-else
        title="回收站为空"
        description="回收站中没有任何项目"
      >
        <el-button @click="$router.push('/app/dashboard')">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
      </EmptyState>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  RefreshRight,
  Delete,
  DeleteFilled,
  Search,
  Document,
  Folder,
  House
} from '@element-plus/icons-vue'
import { LoadingSpinner, EmptyState } from '@/components/common'
import { formatRelativeTime } from '@/utils'

interface TrashedItem {
  id: string
  name: string
  type: 'document' | 'folder'
  originalPath?: string
  deletedAt: string
  restoring?: boolean
  deleting?: boolean
}

const loading = ref(true)
const restoring = ref(false)
const deleting = ref(false)
const clearing = ref(false)
const searchQuery = ref('')
const filterType = ref('all')
const sortBy = ref('deletedAt')
const trashedItems = ref<TrashedItem[]>([])
const selectedItems = ref<TrashedItem[]>([])

/**
 * 过滤和排序后的项目列表
 */
const filteredItems = computed(() => {
  let result = trashedItems.value
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(item => 
      item.name.toLowerCase().includes(query)
    )
  }
  
  // 类型过滤
  if (filterType.value !== 'all') {
    result = result.filter(item => item.type === filterType.value)
  }
  
  // 排序
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'name':
        return a.name.localeCompare(b.name)
      case 'type':
        return a.type.localeCompare(b.type)
      default: // deletedAt
        return new Date(b.deletedAt).getTime() - new Date(a.deletedAt).getTime()
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
 * 处理选择变化
 */
const handleSelectionChange = (selection: TrashedItem[]) => {
  selectedItems.value = selection
}

/**
 * 恢复单个项目
 */
const restoreItem = async (item: TrashedItem) => {
  try {
    item.restoring = true
    // TODO: 调用API恢复项目
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const index = trashedItems.value.findIndex(i => i.id === item.id)
    if (index !== -1) {
      trashedItems.value.splice(index, 1)
    }
    
    ElMessage.success(`${item.type === 'document' ? '文档' : '文件夹'} "${item.name}" 已恢复`)
  } catch (error) {
    console.error('Restore item error:', error)
    ElMessage.error('恢复失败')
  } finally {
    item.restoring = false
  }
}

/**
 * 恢复选中项目
 */
const restoreSelected = async () => {
  if (selectedItems.value.length === 0) return
  
  try {
    restoring.value = true
    // TODO: 调用API批量恢复
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    selectedItems.value.forEach(item => {
      const index = trashedItems.value.findIndex(i => i.id === item.id)
      if (index !== -1) {
        trashedItems.value.splice(index, 1)
      }
    })
    
    ElMessage.success(`已恢复 ${selectedItems.value.length} 个项目`)
    selectedItems.value = []
  } catch (error) {
    console.error('Restore selected error:', error)
    ElMessage.error('批量恢复失败')
  } finally {
    restoring.value = false
  }
}

/**
 * 永久删除单个项目
 */
const deleteItemPermanently = async (item: TrashedItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要永久删除 "${item.name}" 吗？此操作不可恢复！`,
      '确认永久删除',
      {
        type: 'error',
        confirmButtonText: '永久删除',
        cancelButtonText: '取消'
      }
    )
    
    item.deleting = true
    // TODO: 调用API永久删除
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const index = trashedItems.value.findIndex(i => i.id === item.id)
    if (index !== -1) {
      trashedItems.value.splice(index, 1)
    }
    
    ElMessage.success('永久删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete item permanently error:', error)
      ElMessage.error('删除失败')
    }
  } finally {
    item.deleting = false
  }
}

/**
 * 永久删除选中项目
 */
const deleteSelectedPermanently = async () => {
  if (selectedItems.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定要永久删除选中的 ${selectedItems.value.length} 个项目吗？此操作不可恢复！`,
      '确认永久删除',
      {
        type: 'error',
        confirmButtonText: '永久删除',
        cancelButtonText: '取消'
      }
    )
    
    deleting.value = true
    // TODO: 调用API批量永久删除
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    selectedItems.value.forEach(item => {
      const index = trashedItems.value.findIndex(i => i.id === item.id)
      if (index !== -1) {
        trashedItems.value.splice(index, 1)
      }
    })
    
    ElMessage.success(`已永久删除 ${selectedItems.value.length} 个项目`)
    selectedItems.value = []
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete selected permanently error:', error)
      ElMessage.error('批量删除失败')
    }
  } finally {
    deleting.value = false
  }
}

/**
 * 清空回收站
 */
const clearTrash = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空回收站吗？所有项目将被永久删除，此操作不可恢复！',
      '确认清空回收站',
      {
        type: 'error',
        confirmButtonText: '清空回收站',
        cancelButtonText: '取消'
      }
    )
    
    clearing.value = true
    // TODO: 调用API清空回收站
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    trashedItems.value = []
    selectedItems.value = []
    
    ElMessage.success('回收站已清空')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Clear trash error:', error)
      ElMessage.error('清空失败')
    }
  } finally {
    clearing.value = false
  }
}

/**
 * 加载回收站数据
 */
const loadTrashedItems = async () => {
  try {
    loading.value = true
    // TODO: 调用API获取回收站数据
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    trashedItems.value = [
      {
        id: '1',
        name: '已删除的文档.md',
        type: 'document',
        originalPath: '工作文档',
        deletedAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString()
      },
      {
        id: '2',
        name: '旧项目文件夹',
        type: 'folder',
        originalPath: '根目录',
        deletedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString()
      },
      {
        id: '3',
        name: '临时笔记.md',
        type: 'document',
        originalPath: '个人笔记',
        deletedAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString()
      }
    ]
  } catch (error) {
    console.error('Load trashed items error:', error)
    ElMessage.error('加载回收站失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTrashedItems()
})
</script>

<style scoped>
.trash-view {
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

.item-count {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.search-bar {
  display: flex;
  gap: 16px;
  padding: 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.search-input {
  flex: 1;
  max-width: 300px;
}

.filter-select,
.sort-select {
  width: 120px;
}

.trash-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.item-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-icon {
  color: var(--el-text-color-secondary);
}

.item-name {
  color: var(--el-text-color-primary);
}

.original-path {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    flex-wrap: wrap;
  }
  
  .search-bar {
    flex-direction: column;
  }
  
  .search-input {
    max-width: none;
  }
}
</style>