<template>
  <div class="favorites-view">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">收藏</h1>
        <span class="item-count">{{ favorites.length }} 个收藏</span>
      </div>
      
      <div class="header-actions">
        <el-button-group>
          <el-button
            :type="viewMode === 'grid' ? 'primary' : 'default'"
            @click="viewMode = 'grid'"
          >
            <el-icon><Grid /></el-icon>
            网格
          </el-button>
          <el-button
            :type="viewMode === 'list' ? 'primary' : 'default'"
            @click="viewMode = 'list'"
          >
            <el-icon><List /></el-icon>
            列表
          </el-button>
        </el-button-group>
      </div>
    </div>
    
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索收藏..."
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
        <el-option label="收藏时间" value="favoriteAt" />
        <el-option label="更新时间" value="updatedAt" />
        <el-option label="名称" value="name" />
      </el-select>
    </div>
    
    <div class="favorites-content">
      <LoadingSpinner v-if="loading" text="加载中..." />
      
      <div v-else-if="filteredFavorites.length > 0">
        <!-- 网格视图 -->
        <div v-if="viewMode === 'grid'" class="favorites-grid">
          <div
            v-for="item in filteredFavorites"
            :key="item.id"
            class="favorite-card"
            @click="openItem(item)"
          >
            <div class="favorite-icon">
              <el-icon v-if="item.type === 'document'" size="32">
                <Document />
              </el-icon>
              <el-icon v-else size="32">
                <Folder />
              </el-icon>
            </div>
            
            <div class="favorite-info">
              <h3 class="favorite-name">{{ item.name }}</h3>
              <p class="favorite-path">{{ item.path || '根目录' }}</p>
              <div class="favorite-meta">
                <span class="favorite-time">收藏于 {{ formatDate(item.favoriteAt) }}</span>
                <span class="update-time">更新于 {{ formatDate(item.updatedAt) }}</span>
              </div>
            </div>
            
            <div class="favorite-actions" @click.stop>
              <el-button
                text
                @click="removeFavorite(item)"
                class="unfavorite-btn"
              >
                <el-icon><StarFilled /></el-icon>
              </el-button>
              <el-dropdown trigger="click">
                <el-button text>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="openItem(item)">
                      <el-icon><View /></el-icon>
                      打开
                    </el-dropdown-item>
                    <el-dropdown-item v-if="item.type === 'document'" @click="editItem(item)">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item @click="shareItem(item)">
                      <el-icon><Share /></el-icon>
                      分享
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="removeFavorite(item)">
                      <el-icon><Star /></el-icon>
                      取消收藏
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
        
        <!-- 列表视图 -->
        <el-table v-else :data="filteredFavorites" @row-click="openItem">
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
          
          <el-table-column label="位置" min-width="150">
            <template #default="{ row }">
              <span class="item-path">{{ row.path || '根目录' }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="favoriteAt" label="收藏时间" width="150">
            <template #default="{ row }">
              {{ formatDate(row.favoriteAt) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="updatedAt" label="更新时间" width="150">
            <template #default="{ row }">
              {{ formatDate(row.updatedAt) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                text
                @click.stop="removeFavorite(row)"
                class="unfavorite-btn"
              >
                <el-icon><StarFilled /></el-icon>
              </el-button>
              <el-dropdown trigger="click" @click.stop>
                <el-button text>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="openItem(row)">
                      <el-icon><View /></el-icon>
                      打开
                    </el-dropdown-item>
                    <el-dropdown-item v-if="row.type === 'document'" @click="editItem(row)">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item @click="shareItem(row)">
                      <el-icon><Share /></el-icon>
                      分享
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
        title="暂无收藏"
        description="收藏您喜欢的文档和文件夹，方便快速访问"
      >
        <el-button @click="$router.push('/app/documents')">
          <el-icon><Document /></el-icon>
          浏览文档
        </el-button>
      </EmptyState>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Grid,
  List,
  Search,
  Document,
  Folder,
  StarFilled,
  Star,
  MoreFilled,
  View,
  Edit,
  Share
} from '@element-plus/icons-vue'
import { LoadingSpinner, EmptyState } from '@/components/common'
import { formatRelativeTime } from '@/utils'

interface FavoriteItem {
  id: string
  name: string
  type: 'document' | 'folder'
  path?: string
  favoriteAt: string
  updatedAt: string
}

const router = useRouter()
const loading = ref(true)
const searchQuery = ref('')
const filterType = ref('all')
const sortBy = ref('favoriteAt')
const viewMode = ref<'grid' | 'list'>('grid')
const favorites = ref<FavoriteItem[]>([])

/**
 * 过滤和排序后的收藏列表
 */
const filteredFavorites = computed(() => {
  let result = favorites.value
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(item => 
      item.name.toLowerCase().includes(query) ||
      item.path?.toLowerCase().includes(query)
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
      case 'updatedAt':
        return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
      default: // favoriteAt
        return new Date(b.favoriteAt).getTime() - new Date(a.favoriteAt).getTime()
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
 * 打开项目
 */
const openItem = (item: FavoriteItem) => {
  if (item.type === 'document') {
    router.push(`/app/documents/${item.id}`)
  } else {
    router.push(`/app/folders/${item.id}`)
  }
}

/**
 * 编辑项目
 */
const editItem = (item: FavoriteItem) => {
  if (item.type === 'document') {
    router.push(`/app/documents/${item.id}/edit`)
  }
}

/**
 * 分享项目
 */
const shareItem = (item: FavoriteItem) => {
  // TODO: 实现分享功能
  ElMessage.info('分享功能开发中')
}

/**
 * 取消收藏
 */
const removeFavorite = async (item: FavoriteItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消收藏 "${item.name}" 吗？`,
      '确认取消收藏',
      {
        type: 'warning',
        confirmButtonText: '取消收藏',
        cancelButtonText: '保留'
      }
    )
    
    // TODO: 调用API取消收藏
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const index = favorites.value.findIndex(f => f.id === item.id)
    if (index !== -1) {
      favorites.value.splice(index, 1)
    }
    
    ElMessage.success('已取消收藏')
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 加载收藏列表
 */
const loadFavorites = async () => {
  try {
    loading.value = true
    // TODO: 调用API获取收藏列表
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    favorites.value = [
      {
        id: '1',
        name: '重要项目文档',
        type: 'document',
        path: '工作文档',
        favoriteAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
        updatedAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString()
      },
      {
        id: '2',
        name: '学习笔记',
        type: 'folder',
        path: '根目录',
        favoriteAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(),
        updatedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString()
      },
      {
        id: '3',
        name: 'API 设计文档',
        type: 'document',
        path: '技术文档',
        favoriteAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString(),
        updatedAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString()
      },
      {
        id: '4',
        name: '会议记录',
        type: 'document',
        path: '工作文档',
        favoriteAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
        updatedAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString()
      },
      {
        id: '5',
        name: '个人项目',
        type: 'folder',
        path: '根目录',
        favoriteAt: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000).toISOString(),
        updatedAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString()
      }
    ]
  } catch (error) {
    console.error('Load favorites error:', error)
    ElMessage.error('加载收藏失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-view {
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

.favorites-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.favorite-card {
  position: relative;
  padding: 20px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
  cursor: pointer;
  transition: all 0.2s ease;
}

.favorite-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.favorite-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
  color: var(--el-color-primary);
}

.favorite-info {
  text-align: center;
}

.favorite-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
}

.favorite-path {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin: 0 0 12px 0;
}

.favorite-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.favorite-actions {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  gap: 4px;
}

.unfavorite-btn {
  color: var(--el-color-warning);
}

.unfavorite-btn:hover {
  color: var(--el-color-warning-dark-2);
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

.item-path {
  color: var(--el-text-color-secondary);
  font-size: 14px;
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
  
  .favorites-grid {
    grid-template-columns: 1fr;
  }
}
</style>