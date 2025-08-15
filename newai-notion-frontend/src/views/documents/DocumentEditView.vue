<template>
  <div class="document-edit-view">
    <div class="editor-header">
      <div class="header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-input
          v-model="document.title"
          placeholder="无标题文档"
          class="title-input"
          size="large"
        />
      </div>
      
      <div class="header-right">
        <el-button @click="saveDocument" :loading="saving">
          <el-icon><DocumentCopy /></el-icon>
          保存
        </el-button>
        <el-button type="primary" @click="publishDocument">
          <el-icon><Upload /></el-icon>
          发布
        </el-button>
      </div>
    </div>
    
    <div class="editor-content">
      <el-input
        v-model="document.content"
        type="textarea"
        placeholder="开始写作..."
        class="content-editor"
        :autosize="{ minRows: 20 }"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, DocumentCopy, Upload } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const saving = ref(false)

// 文档数据
const document = reactive({
  id: '',
  title: '',
  content: ''
})

/**
 * 返回上一页
 */
const goBack = () => {
  router.go(-1)
}

/**
 * 保存文档
 */
const saveDocument = async () => {
  try {
    saving.value = true
    // TODO: 调用保存API
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('Save document error:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

/**
 * 发布文档
 */
const publishDocument = async () => {
  if (!document.title.trim()) {
    ElMessage.warning('请输入文档标题')
    return
  }
  
  if (!document.content.trim()) {
    ElMessage.warning('请输入文档内容')
    return
  }
  
  try {
    // TODO: 调用发布API
    await saveDocument()
    ElMessage.success('发布成功')
    router.push('/app/documents')
  } catch (error) {
    console.error('Publish document error:', error)
  }
}

/**
 * 自动保存
 */
let autoSaveTimer: NodeJS.Timeout | null = null

const startAutoSave = () => {
  autoSaveTimer = setInterval(() => {
    if (document.title.trim() || document.content.trim()) {
      saveDocument()
    }
  }, 30000) // 30秒自动保存一次
}

const stopAutoSave = () => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer)
    autoSaveTimer = null
  }
}

/**
 * 加载文档数据
 */
const loadDocument = async () => {
  const documentId = route.params.id as string
  
  if (documentId && documentId !== 'new') {
    try {
      // TODO: 调用API获取文档数据
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 模拟数据
      document.id = documentId
      document.title = '示例文档'
      document.content = '这是文档的内容...'
    } catch (error) {
      console.error('Load document error:', error)
      ElMessage.error('加载文档失败')
    }
  }
}

/**
 * 键盘快捷键
 */
const handleKeydown = (event: KeyboardEvent) => {
  const { ctrlKey, metaKey, key } = event
  const isCtrl = ctrlKey || metaKey
  
  if (isCtrl && key === 's') {
    event.preventDefault()
    saveDocument()
  }
}

onMounted(() => {
  loadDocument()
  startAutoSave()
  document.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  stopAutoSave()
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.document-edit-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.title-input {
  max-width: 400px;
}

.title-input :deep(.el-input__wrapper) {
  border: none;
  box-shadow: none;
  font-size: 18px;
  font-weight: 500;
}

.header-right {
  display: flex;
  gap: 12px;
}

.editor-content {
  flex: 1;
  padding: 20px;
  overflow: hidden;
}

.content-editor {
  height: 100%;
}

.content-editor :deep(.el-textarea__inner) {
  border: none;
  box-shadow: none;
  font-size: 16px;
  line-height: 1.8;
  resize: none;
  padding: 0;
}

.content-editor :deep(.el-textarea__inner):focus {
  box-shadow: none;
}

@media (max-width: 768px) {
  .editor-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-left {
    justify-content: space-between;
  }
  
  .title-input {
    max-width: none;
    flex: 1;
  }
}
</style>