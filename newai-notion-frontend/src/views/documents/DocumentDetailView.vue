<template>
  <div class="document-detail-view">
    <div class="document-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/app/documents' }">文档</el-breadcrumb-item>
        <el-breadcrumb-item>{{ document?.title || '文档详情' }}</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="document-actions">
        <el-button @click="editDocument">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button @click="shareDocument">
          <el-icon><Share /></el-icon>
          分享
        </el-button>
      </div>
    </div>
    
    <div class="document-content">
      <LoadingSpinner v-if="loading" text="加载中..." />
      <div v-else-if="document" class="document-body">
        <h1 class="document-title">{{ document.title }}</h1>
        <div class="document-meta">
          <span>创建时间：{{ formatDate(document.createdAt) }}</span>
          <span>更新时间：{{ formatDate(document.updatedAt) }}</span>
          <span>作者：{{ document.author }}</span>
        </div>
        <div class="document-text" v-html="document.content"></div>
      </div>
      <EmptyState v-else title="文档不存在" description="该文档可能已被删除或您没有访问权限" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Edit, Share } from '@element-plus/icons-vue'
import { LoadingSpinner, EmptyState } from '@/components/common'
import { formatRelativeTime } from '@/utils'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const document = ref<any>(null)

/**
 * 格式化日期
 */
const formatDate = (date: string) => {
  return formatRelativeTime(new Date(date))
}

/**
 * 编辑文档
 */
const editDocument = () => {
  router.push(`/app/documents/${route.params.id}/edit`)
}

/**
 * 分享文档
 */
const shareDocument = () => {
  // TODO: 实现分享功能
  console.log('Share document')
}

/**
 * 加载文档数据
 */
const loadDocument = async () => {
  try {
    loading.value = true
    // TODO: 调用API获取文档详情
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    document.value = {
      id: route.params.id,
      title: '示例文档',
      content: '<p>这是一个示例文档的内容...</p>',
      author: '用户',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
  } catch (error) {
    console.error('Load document error:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDocument()
})
</script>

<style scoped>
.document-detail-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.document-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.document-actions {
  display: flex;
  gap: 12px;
}

.document-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.document-body {
  max-width: 800px;
  margin: 0 auto;
}

.document-title {
  font-size: 32px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
}

.document-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 32px;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.document-text {
  line-height: 1.8;
  color: var(--el-text-color-primary);
}

@media (max-width: 768px) {
  .document-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .document-meta {
    flex-direction: column;
    gap: 8px;
  }
}
</style>