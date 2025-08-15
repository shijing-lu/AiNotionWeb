<template>
  <div class="recent-view">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">最近访问</h1>
        <span class="item-count">{{ recentItems.length }} 个项目</span>
      </div>
      
      <div class="header-actions">
        <el-button @click="clearRecent" :disabled="recentItems.length === 0">
          <el-icon><Delete /></el-icon>
          清空记录
        </el-button>
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
        placeholder="搜索最近访问..."
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
      
      <el-select v-model="timeRange" placeholder="时间范围" class="time-select">
        <el-option label="全部时间" value="all" />
        <el-option label="今天" value="today" />
        <el-option label="昨天" value="yesterday" />
        <el-option label="本周" value="week" />
        <el-option label="本月" value="month" />
      </el-select>
    </div>
    
    <div class="recent-content">
      <LoadingSpinner v-if="loading" text="加载中..." />
      
      <div v-else-if="filteredItems.length > 0">
        <!-- 网格视图 -->
        <div v-if="viewMode === 'grid'" class="recent-grid">
          <div
            v-for="item in filteredItems"
            :key="item.id"
            class="recent-card"
            @click="openItem(item)"
          >
            <div class="recent-icon">
              <el-icon v-if="item.type === 'document'" size="32">
                <Document />
              </el-icon>
              <el-icon v-else size="32">
                <Folder />
              </el-icon>
            </div>
            
            <div class="recent-info">
              <h3 class="recent-name">{{ item.name }}</h3>
              <p class="recent-path">{{ item.path || '根目录' }}</p>
              <div class="recent-meta">
                <span class="visit-time">{{ formatDate(item.lastVisitAt) }}</span>
                <span class="visit-count">访问 {{ item.visitCount }} 次</span>
              </div>
            </div>
            
            <div class="recent-actions" @click.stop>
              <el-button
                text
                @click="toggleFavorite(item)"
                :class="{ 'favorited': item.isFavorite }"
              >
                <el-icon v-if="item.isFavorite"><StarFilled /></el-icon>
                <el-icon v-else><Star /></el-icon>
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
                    <el-dropdown-item divided @click="removeFromRecent(item)">
                      <el-icon><Delete /></el-icon>
                      从记录中移除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
        
        <!-- 列表视图 -->
        <el-table v-else :data="filteredItems" @row-click="openItem">
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
          
          <el-table-column prop="lastVisitAt" label="最后访问" width="150">
            <template #default="{ row }">
              {{ formatDate(row.lastVisitAt) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="visitCount" label="访问次数" width="100">
            <template #default="{ row }">
              {{ row.visitCount }} 次
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                text
                @click.stop="toggleFavorite(row)"
                :class="{ 'favorited': row.isFavorite }"
              >
                <el-icon v-if="row.isFavorite"><StarFilled /></el-icon>
                <el-icon v-else><Star /></el-icon>
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
                    <el-dropdown-item divided @click="removeFromRecent(row)">
                      <el-icon><Delete /></el-icon>
                      从记录中移除
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
        title="暂无访问记录"
        description="开始使用文档和文件夹，这里会显示您的访问记录"
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
  Delete,
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

interface RecentItem {
  id: string
  name: string
  type: 'document' | 'folder'
  path?: string
  lastVisitAt: string
  visitCount: number
  isFavorite: boolean
}

const router = useRouter()
const loading = ref(true)
const searchQuery = ref('')
const filterType = ref('all')
const timeRange = ref('all')
const viewMode = ref<'grid' | 'list'>('grid')
const recentItems = ref<RecentItem[]>([])

/**
 * 过滤后的项目列表
 */
const filteredItems = computed(() => {
  let result = recentItems.value
  
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
  
  // 时间范围过滤
  if (timeRange.value !== 'all') {
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
    const weekStart = new Date(today.getTime() - (today.getDay() * 24 * 60 * 60 * 1000))
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
    
    result = result.filter(item => {
      const visitDate = new Date(item.lastVisitAt)
      
      switch (timeRange.value) {
        case 'today':
          return visitDate >= today
        case 'yesterday':
          return visitDate >= yesterday && visitDate < today
        case 'week':
          return visitDate >= weekStart
        case 'month':
          return visitDate >= monthStart
        default:
          return true
      }
    })
  }
  
  // 按最后访问时间排序
  result.sort((a, b) => {
    return new Date(b.lastVisitAt).getTime() - new Date(a.lastVisitAt).getTime()
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
const openItem = (item: RecentItem) => {
  if (item.type === 'document') {
    router.push(`/app/documents/${item.id}`)
  } else {
    router.push(`/app/folders/${item.id}`)
  }
}

/**
 * 编辑项目
 */
const editItem = (item: RecentItem) => {
  if (item.type === 'document') {
    router.push(`/app/documents/${item.id}/edit`)
  }
}

/**
 * 分享项目
 */
const shareItem = (item: RecentItem) => {
  // TODO: 实现分享功能
  ElMessage.info('分享功能开发中')
}

/**
 * 切换收藏状态
 */
const toggleFavorite = async (item: RecentItem) => {
  try {
    // TODO: 调用API切换收藏状态
    await new Promise(resolve => setTimeout(resolve, 300))
    
    item.isFavorite = !item.isFavorite
    
    ElMessage.success(item.isFavorite ? '已添加到收藏' : '已取消收藏')
  } catch (error) {
    console.error('Toggle favorite error:', error)
    ElMessage.error('操作失败')
  }
}

/**
 * 从最近访问中移除
 */
const removeFromRecent = async (item: RecentItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要从最近访问中移除 "${item.name}" 吗？`,
      '确认移除',
      {
        type: 'warning',
        confirmButtonText: '移除',
        cancelButtonText: '取消'
      }
    )
    
    // TODO: 调用API移除记录
    await new Promise(resolve => setTimeout(resolve, 300))
    
    const index = recentItems.value.findIndex(r => r.id === item.id)
    if (index !== -1) {
      recentItems.value.splice(index, 1)
    }
    
    ElMessage.success('已从最近访问中移除')
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 清空最近访问记录
 */
const clearRecent = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有最近访问记录吗？此操作不可恢复。',
      '确认清空',
      {
        type: 'warning',
        confirmButtonText: '清空',
        cancelButtonText: '取消'
      }
    )
    
    // TODO: 调用API清空记录
    await new Promise(resolve => setTimeout(resolve, 500))
    
    recentItems.value = []
    
    ElMessage.success('已清空最近访问记录')
  } catch (error) {
    // 用户取消操作
  }
}

/**
 * 加载最近访问记录
 */
const loadRecentItems = async () => {
  try {
    loading.value = true
    // TODO: 调用API获取最近访问记录
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    recentItems.value = [
      {
        id: '1',
        name: '项目计划书',
        type: 'document',
        path: '工作文档',
        lastVisitAt: new Date(Date.now() - 30 * 60 * 1000).toISOString(), // 30分钟前
        visitCount: 15,
        isFavorite: true
      },
      {
        id: '2',
        name: '学习笔记',
        type: 'folder',
        path: '根目录',
        lastVisitAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(), // 2小时前
        visitCount: 8,
        isFavorite: false
      },
      {
        id: '3',
        name: '会议纪要',
        type: 'document',
        path: '工作文档',
        lastVisitAt: new Date(Date.now() - 4 * 60 * 60 * 1000).toISOString(), // 4小时前
        visitCount: 3,
        isFavorite: false
      },
      {
        id: '4',
        name: 'API 文档',
        type: 'document',
        path: '技术文档',
        lastVisitAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(), // 1天前
        visitCount: 12,
        isFavorite: true
      },
      {
        id: '5',
        name: '个人项目',
        type: 'folder',
        path: '根目录',
        lastVisitAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(), // 2天前
        visitCount: 5,
        isFavorite: false
      },
      {
        id: '6',
        name: '读书笔记',
        type: 'document',
        path: '个人笔记',
        lastVisitAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(), // 3天前
        visitCount: 7,
        isFavorite: false
      }
    ]
  } catch (error) {
    console.error('Load recent items error:', error)
    ElMessage.error('加载最近访问失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRecentItems()
})
</script>

<style scoped>
.recent-view {
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
  align-items: center;
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
.time-select {
  width: 120px;
}

.recent-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.recent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.recent-card {
  position: relative;
  padding: 20px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
  cursor: pointer;
  transition: all 0.2s ease;
}

.recent-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.recent-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
  color: var(--el-color-primary);
}

.recent-info {
  text-align: center;
}

.recent-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
}

.recent-path {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin: 0 0 12px 0;
}

.recent-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.recent-actions {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  gap: 4px;
}

.favorited {
  color: var(--el-color-warning);
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
  
  .header-actions {
    flex-wrap: wrap;
  }
  
  .search-bar {
    flex-direction: column;
  }
  
  .search-input {
    max-width: none;
  }
  
  .recent-grid {
    grid-template-columns: 1fr;
  }
}
</style>