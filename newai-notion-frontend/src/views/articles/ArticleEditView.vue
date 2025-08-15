<template>
  <div class="article-edit-view">
    <LoadingSpinner v-if="loading" />
    
    <div v-else class="edit-container">
      <!-- 编辑器头部 -->
      <div class="editor-header">
        <div class="header-content">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ name: 'Articles' }">文章管理</el-breadcrumb-item>
              <el-breadcrumb-item>
                {{ isEdit ? '编辑文章' : '创建文章' }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-actions">
            <el-button @click="saveDraft" :loading="saving">
              <el-icon><DocumentCopy /></el-icon>
              保存草稿
            </el-button>
            
            <el-button @click="previewArticle">
              <el-icon><View /></el-icon>
              预览
            </el-button>
            
            <el-button type="primary" @click="publishArticle" :loading="publishing">
              <el-icon><Upload /></el-icon>
              {{ form.status === 'PUBLISHED' ? '更新发布' : '发布文章' }}
            </el-button>
            
            <el-button @click="cancelEdit">
              取消
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 编辑器内容 -->
      <div class="editor-content">
        <!-- 文章标题 -->
        <div class="title-section">
          <el-input
            v-model="form.title"
            placeholder="请输入文章标题..."
            size="large"
            class="title-input"
            maxlength="100"
            show-word-limit
          />
        </div>
        
        <!-- 编辑器主体 -->
        <div class="editor-main">
          <div style="display: flex; gap: var(--spacing-md); height: 600px;">
          <!-- 左侧：Markdown编辑器 -->
          <div class="editor-left">
            <div class="editor-toolbar">
              <div class="toolbar-left">
                <span class="toolbar-title">Markdown 编辑</span>
              </div>
              <div class="toolbar-right">
                <el-button-group>
                  <el-button size="small" @click="insertBold">
                    <el-icon><EditPen /></el-icon>
                  </el-button>
                  <el-button size="small" @click="insertItalic">
                    <el-icon><Italic /></el-icon>
                  </el-button>
                  <el-button size="small" @click="insertStrike">
                    <el-icon><Strikethrough /></el-icon>
                  </el-button>
                  <el-button size="small" @click="insertLink">
                    <el-icon><LinkIcon /></el-icon>
                  </el-button>
                  <el-button size="small" @click="insertImage">
                    <el-icon><Picture /></el-icon>
                  </el-button>
                  <el-button size="small" @click="insertCodeBlock">
                    <el-icon><Document /></el-icon>
                  </el-button>
                </el-button-group>
              </div>
            </div>
            
            <!-- Markdown文本编辑器 -->
            <div class="markdown-editor">
              <el-input
                v-model="form.content"
                type="textarea"
                placeholder="请输入Markdown内容..."
                class="markdown-textarea"
                resize="none"
                @input="updatePreview"
              />
            </div>
          </div>
          
          <!-- 右侧：实时预览 -->
          <div class="editor-right">
            <div class="preview-toolbar">
              <span class="toolbar-title">实时预览</span>
            </div>
            
            <div class="preview-content">
              <div 
                class="markdown-preview"
                v-html="previewHtml"
              ></div>
            </div>
          </div>
        </div>
      </div>
        
        <!-- 侧边栏设置 -->
        <div class="editor-sidebar">
          <!-- 发布设置 -->
          <div class="settings-section">
            <h3>发布设置</h3>
            
            <div class="setting-item">
              <label>状态</label>
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已发布" value="PUBLISHED" />
                <el-option label="已归档" value="ARCHIVED" />
              </el-select>
            </div>
            
            <div class="setting-item">
              <label>可见性</label>
              <el-select v-model="form.visibility" style="width: 100%">
                <el-option label="公开" value="PUBLIC" />
                <el-option label="私有" value="PRIVATE" />
                <el-option label="受保护" value="PROTECTED" />
              </el-select>
            </div>
            
            <div class="setting-item">
              <label>分类</label>
              <el-input
                v-model="form.category"
                placeholder="请输入分类"
                maxlength="50"
              />
            </div>
            
            <div class="setting-item">
              <label>标签</label>
              <el-select
                v-model="form.tags"
                multiple
                filterable
                allow-create
                placeholder="请选择或输入标签"
                style="width: 100%"
              >
                <el-option
                  v-for="tag in availableTags"
                  :key="tag"
                  :label="tag"
                  :value="tag"
                />
              </el-select>
            </div>
            
            <div class="setting-item">
              <el-checkbox v-model="form.isPinned">置顶文章</el-checkbox>
            </div>
            
            <div class="setting-item">
              <el-checkbox v-model="form.allowComments">允许评论</el-checkbox>
            </div>
          </div>
          
          <!-- 文章统计 -->
          <div class="stats-section">
            <h3>文章统计</h3>
            
            <div class="stat-item">
              <span class="stat-label">字数：</span>
              <span class="stat-value">{{ wordCount }}</span>
            </div>
            
            <div class="stat-item">
              <span class="stat-label">预计阅读：</span>
              <span class="stat-value">{{ readingTime }} 分钟</span>
            </div>
            
            <div class="stat-item" v-if="isEdit">
              <span class="stat-label">创建时间：</span>
              <span class="stat-value">{{ formatDateTime(form.createdAt) }}</span>
            </div>
            
            <div class="stat-item" v-if="isEdit">
              <span class="stat-label">最后更新：</span>
              <span class="stat-value">{{ formatDateTime(form.updatedAt) }}</span>
            </div>
          </div>
          
          <!-- AI 助手 -->
          <div class="ai-section">
            <h3>AI 助手</h3>
            
            <el-button size="small" @click="generateSummary" :loading="aiLoading">
              <el-icon><MagicStick /></el-icon>
              生成摘要
            </el-button>
            
            <el-button size="small" @click="generateTags" :loading="aiLoading">
              <el-icon><Tag /></el-icon>
              生成标签
            </el-button>
            
            <el-button size="small" @click="improveContent" :loading="aiLoading">
              <el-icon><Edit /></el-icon>
              优化内容
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" title="文章预览" width="80%" class="preview-dialog">
      <div class="preview-content">
        <h1 class="preview-title">{{ form.title || '无标题' }}</h1>
        
        <div class="preview-meta">
          <div class="meta-item">
            <el-tag :type="getStatusType(form.status)">{{ getStatusText(form.status) }}</el-tag>
            <el-tag :type="getVisibilityType(form.visibility)">{{ getVisibilityText(form.visibility) }}</el-tag>
          </div>
          
          <div class="meta-item" v-if="form.tags.length > 0">
            <el-tag v-for="tag in form.tags" :key="tag" type="info" size="small">
              {{ tag }}
            </el-tag>
          </div>
        </div>
        
        <div v-if="form.summary" class="preview-summary">
          <p>{{ form.summary }}</p>
        </div>
        
        <div class="preview-body" v-html="form.content"></div>
      </div>
    </el-dialog>
    
    <!-- 链接插入对话框 -->
    <el-dialog v-model="linkDialogVisible" title="插入链接" width="400px">
      <el-form :model="linkForm" label-width="80px">
        <el-form-item label="链接文本">
          <el-input v-model="linkForm.text" placeholder="请输入链接文本" />
        </el-form-item>
        <el-form-item label="链接地址">
          <el-input v-model="linkForm.url" placeholder="请输入链接地址" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="linkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmInsertLink">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 图片上传对话框 -->
    <el-dialog v-model="imageDialogVisible" title="插入图片" width="400px">
      <el-upload
        class="image-uploader"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :show-file-list="false"
        :on-success="handleImageSuccess"
        :before-upload="beforeImageUpload"
        accept="image/*"
      >
        <el-button type="primary">
          <el-icon><Upload /></el-icon>
          选择图片
        </el-button>
      </el-upload>
      
      <div class="upload-tip">
        <p>支持 JPG、PNG、GIF 格式，文件大小不超过 5MB</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DocumentCopy, View, Upload, EditPen, Italic, Strikethrough,
  List, Rank, ChatQuote, Link as LinkIcon, Picture, Grid, Document,
  RefreshLeft, RefreshRight, MagicStick, Tag, Edit
} from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import { LoadingSpinner } from '@/components/common'
import { formatDateTime } from '@/utils'
import { articleApi, fileApi } from '@/api'
import { useAuthStore } from '@/stores'

/**
 * 文章表单接口
 */
interface ArticleForm {
  id?: string
  title: string
  summary: string
  content: string
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  visibility: 'PUBLIC' | 'PRIVATE' | 'PROTECTED'
  tags: string[]
  category: string
  isPinned: boolean
  allowComments: boolean
  createdAt?: string
  updatedAt?: string
}

/**
 * 链接表单接口
 */
interface LinkForm {
  text: string
  url: string
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// Markdown解析器
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
})

// 响应式数据
const loading = ref(true)
const saving = ref(false)
const publishing = ref(false)
const aiLoading = ref(false)
const previewVisible = ref(false)
const linkDialogVisible = ref(false)
const imageDialogVisible = ref(false)
const availableTags = ref<string[]>([])
const previewHtml = ref('')

// 表单数据
const form = ref<ArticleForm>({
  title: '',
  summary: '',
  content: '',
  status: 'DRAFT',
  visibility: 'PUBLIC',
  tags: [],
  category: '',
  isPinned: false,
  allowComments: true
})

// 链接表单
const linkForm = ref<LinkForm>({
  text: '',
  url: ''
})

// 计算属性
const isEdit = computed(() => {
  const id = route.params.id as string
  return id && id !== 'new'
})

const wordCount = computed(() => {
  return form.value.content.length
})

const readingTime = computed(() => {
  // 按照每分钟200字的阅读速度计算
  return Math.ceil(wordCount.value / 200)
})

/**
 * 更新预览内容
 */
const updatePreview = () => {
  previewHtml.value = md.render(form.value.content || '')
}

/**
 * 插入粗体文本
 */
const insertBold = () => {
  insertMarkdown('**', '**', '粗体文本')
}

/**
 * 插入斜体文本
 */
const insertItalic = () => {
  insertMarkdown('*', '*', '斜体文本')
}

/**
 * 插入删除线文本
 */
const insertStrike = () => {
  insertMarkdown('~~', '~~', '删除线文本')
}

/**
 * 插入代码块
 */
const insertCodeBlock = () => {
  insertMarkdown('\n```\n', '\n```\n', '代码内容')
}

/**
 * 通用Markdown插入方法
 */
const insertMarkdown = (prefix: string, suffix: string, placeholder: string) => {
  const textarea = document.querySelector('.markdown-textarea textarea') as HTMLTextAreaElement
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = form.value.content.substring(start, end)
  const replacement = selectedText || placeholder
  
  const newContent = 
    form.value.content.substring(0, start) +
    prefix + replacement + suffix +
    form.value.content.substring(end)
  
  form.value.content = newContent
  updatePreview()
  
  // 重新设置光标位置
  setTimeout(() => {
    const newStart = start + prefix.length
    const newEnd = newStart + replacement.length
    textarea.setSelectionRange(newStart, newEnd)
    textarea.focus()
  }, 0)
}

const uploadUrl = computed(() => {
  return '/api/files/upload'
})

const uploadHeaders = computed(() => {
  return {
    'Authorization': `Bearer ${authStore.token}`
  }
})

/**
 * 获取状态类型
 */
const getStatusType = (status: string) => {
  switch (status) {
    case 'PUBLISHED': return 'success'
    case 'DRAFT': return 'warning'
    case 'ARCHIVED': return 'info'
    default: return 'default'
  }
}

/**
 * 获取状态文本
 */
const getStatusText = (status: string) => {
  switch (status) {
    case 'PUBLISHED': return '已发布'
    case 'DRAFT': return '草稿'
    case 'ARCHIVED': return '已归档'
    default: return '未知'
  }
}

/**
 * 获取可见性类型
 */
const getVisibilityType = (visibility: string) => {
  switch (visibility) {
    case 'PUBLIC': return 'success'
    case 'PRIVATE': return 'danger'
    case 'PROTECTED': return 'warning'
    default: return 'default'
  }
}

/**
 * 获取可见性文本
 */
const getVisibilityText = (visibility: string) => {
  switch (visibility) {
    case 'PUBLIC': return '公开'
    case 'PRIVATE': return '私有'
    case 'PROTECTED': return '受保护'
    default: return '未知'
  }
}

/**
 * 初始化编辑器
 */
const initEditor = () => {
  // 初始化预览内容
  updatePreview()
}

/**
 * 加载文章数据
 */
const loadArticle = async () => {
  const articleId = route.params.id as string
  
  // 如果是新建文章（id为'new'），初始化表单并从查询参数获取初始值
  if (articleId === 'new') {
    // 从查询参数获取初始值
    const query = route.query
    form.value = {
      title: (query.title as string) || '',
      summary: '',
      content: '',
      status: 'DRAFT',
      visibility: (query.visibility as any) || 'PUBLIC',
      tags: query.tags ? (query.tags as string).split(',') : [],
      category: (query.category as string) || '',
      isPinned: false,
      allowComments: true
    }
    
    // 如果有模板ID，加载模板内容
    if (query.template) {
      // TODO: 加载模板内容
    }
    
    loading.value = false
    return
  }
  
  // 如果没有ID，返回文章列表
  if (!articleId) {
    router.push('/app/articles')
    return
  }
  
  try {
    const response = await articleApi.getArticle(articleId)
    const article = response.data
    
    form.value = {
      id: article.id,
      title: article.title,
      summary: article.summary || '',
      content: article.content,
      status: article.status,
      visibility: article.visibility,
      tags: article.tags || [],
      category: article.category || '',
      isPinned: article.isPinned,
      allowComments: article.allowComments ?? true,
      createdAt: article.createdAt,
      updatedAt: article.updatedAt
    }
    
    // 更新预览内容
    updatePreview()
    
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载文章失败')
    router.push('/app/articles')
  } finally {
    loading.value = false
  }
}

/**
 * 加载可用标签
 */
const loadAvailableTags = async () => {
  try {
    const response = await articleApi.getTags()
    availableTags.value = response.data
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

/**
 * 保存草稿
 */
const saveDraft = async () => {
  try {
    saving.value = true
    
    const articleData = {
      ...form.value,
      status: 'DRAFT' as const
    }
    
    if (isEdit.value) {
      await articleApi.updateArticle(form.value.id!, articleData)
      ElMessage.success('草稿保存成功')
    } else {
      const response = await articleApi.createArticle(articleData)
      form.value.id = response.data.id
      ElMessage.success('草稿创建成功')
      // 更新路由到编辑模式
      router.replace(`/app/articles/${form.value.id}/edit`)
    }
    
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存草稿失败')
  } finally {
    saving.value = false
  }
}

/**
 * 发布文章
 */
const publishArticle = async () => {
  if (!form.value.title.trim()) {
    ElMessage.error('请输入文章标题')
    return
  }
  
  if (!form.value.content.trim()) {
    ElMessage.error('请输入文章内容')
    return
  }
  
  try {
    publishing.value = true
    
    const articleData = {
      ...form.value,
      status: 'PUBLISHED' as const
    }
    
    if (isEdit.value) {
      await articleApi.updateArticle(form.value.id!, articleData)
      ElMessage.success('文章更新成功')
    } else {
      const response = await articleApi.createArticle(articleData)
      form.value.id = response.data.id
      ElMessage.success('文章发布成功')
    }
    
    router.push('/app/articles')
    
  } catch (error) {
    console.error('发布文章失败:', error)
    ElMessage.error('发布文章失败')
  } finally {
    publishing.value = false
  }
}

/**
 * 预览文章
 */
const previewArticle = () => {
  previewVisible.value = true
}

/**
 * 取消编辑
 */
const cancelEdit = async () => {
  const confirmed = await ElMessageBox.confirm(
    '确定要取消编辑吗？未保存的内容将丢失。',
    '确认取消',
    { type: 'warning' }
  ).catch(() => false)
  
  if (confirmed) {
    router.push('/app/articles')
  }
}

/**
 * 插入链接
 */
const insertLink = () => {
  linkDialogVisible.value = true
}

/**
 * 确认插入链接
 */
const confirmInsertLink = () => {
  if (!linkForm.value.text || !linkForm.value.url) {
    ElMessage.error('请填写链接文本和地址')
    return
  }
  
  const linkMarkdown = `[${linkForm.value.text}](${linkForm.value.url})`
  insertMarkdownAtCursor(linkMarkdown)
  
  linkForm.value.text = ''
  linkForm.value.url = ''
  linkDialogVisible.value = false
}

/**
 * 插入图片
 */
const insertImage = () => {
  imageDialogVisible.value = true
}

/**
 * 在光标位置插入Markdown文本
 */
const insertMarkdownAtCursor = (text: string) => {
  const textarea = document.querySelector('.markdown-textarea textarea') as HTMLTextAreaElement
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  
  const newContent = 
    form.value.content.substring(0, start) +
    text +
    form.value.content.substring(end)
  
  form.value.content = newContent
  updatePreview()
  
  // 设置光标位置到插入文本的末尾
  setTimeout(() => {
    textarea.setSelectionRange(start + text.length, start + text.length)
    textarea.focus()
  }, 0)
}

/**
 * 图片上传前检查
 */
const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  
  return true
}

/**
 * 图片上传成功
 */
const handleImageSuccess = (response: any) => {
  const imageUrl = response.data.url
  const imageMarkdown = `![图片](${imageUrl})`
  insertMarkdownAtCursor(imageMarkdown)
  imageDialogVisible.value = false
  ElMessage.success('图片上传成功')
}

/**
 * 插入表格
 */
const insertTable = () => {
  const tableMarkdown = `
| 列1 | 列2 | 列3 |
|-----|-----|-----|
| 内容1 | 内容2 | 内容3 |
| 内容4 | 内容5 | 内容6 |
`
  insertMarkdownAtCursor(tableMarkdown)
}

/**
 * 生成摘要
 */
const generateSummary = async () => {
  if (!form.value.content.trim()) {
    ElMessage.error('请先输入文章内容')
    return
  }
  
  try {
    aiLoading.value = true
    // 这里调用AI接口生成摘要
    // const response = await aiApi.generateSummary(form.value.content)
    // form.value.summary = response.data.summary
    
    // 模拟AI生成
    await new Promise(resolve => setTimeout(resolve, 2000))
    form.value.summary = '这是AI生成的文章摘要...'
    ElMessage.success('摘要生成成功')
    
  } catch (error) {
    console.error('生成摘要失败:', error)
    ElMessage.error('生成摘要失败')
  } finally {
    aiLoading.value = false
  }
}

/**
 * 生成标签
 */
const generateTags = async () => {
  if (!form.value.content.trim()) {
    ElMessage.error('请先输入文章内容')
    return
  }
  
  try {
    aiLoading.value = true
    // 这里调用AI接口生成标签
    // const response = await aiApi.generateTags(form.value.content)
    // form.value.tags = response.data.tags
    
    // 模拟AI生成
    await new Promise(resolve => setTimeout(resolve, 2000))
    form.value.tags = ['技术', '前端', 'Vue.js']
    ElMessage.success('标签生成成功')
    
  } catch (error) {
    console.error('生成标签失败:', error)
    ElMessage.error('生成标签失败')
  } finally {
    aiLoading.value = false
  }
}

/**
 * 优化内容
 */
const improveContent = async () => {
  if (!form.value.content.trim()) {
    ElMessage.error('请先输入文章内容')
    return
  }
  
  try {
    aiLoading.value = true
    // 这里调用AI接口优化内容
    ElMessage.info('内容优化功能开发中')
    
  } catch (error) {
    console.error('优化内容失败:', error)
    ElMessage.error('优化内容失败')
  } finally {
    aiLoading.value = false
  }
}

// 监听内容变化，自动保存草稿
let autoSaveTimer: NodeJS.Timeout | null = null
watch(
  () => [form.value.title, form.value.content, form.value.summary],
  () => {
    if (autoSaveTimer) {
      clearTimeout(autoSaveTimer)
    }
    
    autoSaveTimer = setTimeout(() => {
      if (isEdit.value && form.value.content.trim()) {
        saveDraft()
      }
    }, 30000) // 30秒自动保存
  },
  { deep: true }
)

// 监听内容变化，更新预览
watch(
  () => form.value.content,
  () => {
    updatePreview()
  }
)

// 组件挂载
onMounted(async () => {
  initEditor()
  await loadAvailableTags()
  await loadArticle()
})

// 组件销毁时清理
onBeforeUnmount(() => {
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
  }
})
</script>

<style scoped>
.article-edit-view {
  min-height: 100vh;
  background-color: var(--el-bg-color);
}

/* 编辑器头部 */
.editor-header {
  background-color: var(--el-bg-color-page);
  border-bottom: 1px solid var(--el-border-color-light);
  padding: var(--spacing-lg) 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 var(--spacing-xl);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: var(--spacing-sm);
}

/* 编辑器内容 */
.editor-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--spacing-xl);
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: var(--spacing-xl);
}

.editor-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.editor-left,
.editor-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  overflow: hidden;
}

.editor-toolbar,
.preview-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-md);
  background-color: var(--el-fill-color-lighter);
  border-bottom: 1px solid var(--el-border-color-light);
}

.toolbar-title {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--el-text-color-regular);
}

.markdown-editor {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.markdown-textarea {
  flex: 1;
}

.markdown-textarea :deep(.el-textarea__inner) {
  height: 100% !important;
  border: none;
  border-radius: 0;
  resize: none;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  padding: var(--spacing-md);
}

.preview-content {
  flex: 1;
  overflow-y: auto;
}

.markdown-preview {
  padding: var(--spacing-md);
  line-height: 1.6;
  color: var(--el-text-color-primary);
}

.markdown-preview h1,
.markdown-preview h2,
.markdown-preview h3,
.markdown-preview h4,
.markdown-preview h5,
.markdown-preview h6 {
  margin: 1.5em 0 0.5em 0;
  font-weight: 600;
  line-height: 1.3;
}

.markdown-preview h1 {
  font-size: 2em;
  border-bottom: 1px solid var(--el-border-color-light);
  padding-bottom: 0.3em;
}

.markdown-preview h2 {
  font-size: 1.5em;
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding-bottom: 0.3em;
}

.markdown-preview h3 {
  font-size: 1.25em;
}

.markdown-preview p {
  margin: 1em 0;
}

.markdown-preview code {
  background-color: var(--el-fill-color-light);
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}

.markdown-preview pre {
  background-color: var(--el-fill-color-light);
  padding: 1em;
  border-radius: var(--border-radius-base);
  overflow-x: auto;
  margin: 1em 0;
}

.markdown-preview pre code {
  background: none;
  padding: 0;
}

.markdown-preview blockquote {
  border-left: 4px solid var(--el-color-primary);
  padding-left: 1em;
  margin: 1em 0;
  color: var(--el-text-color-regular);
  background-color: var(--el-fill-color-lighter);
  padding: 0.5em 1em;
  border-radius: 0 var(--border-radius-base) var(--border-radius-base) 0;
}

.markdown-preview ul,
.markdown-preview ol {
  padding-left: 2em;
  margin: 1em 0;
}

.markdown-preview li {
  margin: 0.5em 0;
}

.markdown-preview a {
  color: var(--el-color-primary);
  text-decoration: none;
}

.markdown-preview a:hover {
  text-decoration: underline;
}

.markdown-preview img {
  max-width: 100%;
  height: auto;
  border-radius: var(--border-radius-base);
  margin: 1em 0;
}

.markdown-preview table {
  border-collapse: collapse;
  width: 100%;
  margin: 1em 0;
}

.markdown-preview th,
.markdown-preview td {
  border: 1px solid var(--el-border-color-light);
  padding: 0.5em 1em;
  text-align: left;
}

.markdown-preview th {
  background-color: var(--el-fill-color-lighter);
  font-weight: 600;
}

/* 标题区域 */
.title-section {
  margin-bottom: var(--spacing-lg);
}

.title-input :deep(.el-input__wrapper) {
  font-size: 1.5rem;
  font-weight: 600;
  padding: var(--spacing-lg);
}

/* 摘要区域 */
.summary-section {
  margin-bottom: var(--spacing-lg);
}

/* 编辑器区域 */
.editor-section {
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  overflow: hidden;
}

.editor-toolbar {
  background-color: var(--el-bg-color-page);
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: var(--spacing-md);
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.toolbar-group {
  display: flex;
  align-items: center;
}

.toolbar-group .el-button.is-active {
  background-color: var(--el-color-primary);
  color: white;
}

.editor-wrapper {
  min-height: 500px;
  background-color: var(--el-bg-color);
}

.editor-content-area {
  padding: var(--spacing-xl);
  min-height: 500px;
  outline: none;
}

/* 编辑器内容样式 */
.editor-content-area :deep(.ProseMirror) {
  outline: none;
  line-height: 1.8;
  color: var(--el-text-color-primary);
}

.editor-content-area :deep(.ProseMirror h1) {
  font-size: 1.8rem;
  font-weight: 700;
  margin: 1.5em 0 0.5em 0;
}

.editor-content-area :deep(.ProseMirror h2) {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 1.3em 0 0.5em 0;
}

.editor-content-area :deep(.ProseMirror h3) {
  font-size: 1.3rem;
  font-weight: 600;
  margin: 1.2em 0 0.5em 0;
}

.editor-content-area :deep(.ProseMirror p) {
  margin: 1em 0;
}

.editor-content-area :deep(.ProseMirror blockquote) {
  border-left: 4px solid var(--el-color-primary);
  padding-left: var(--spacing-lg);
  margin: 1.5em 0;
  background-color: var(--el-fill-color-lighter);
  padding: var(--spacing-lg);
  border-radius: var(--border-radius-base);
}

.editor-content-area :deep(.ProseMirror code) {
  background-color: var(--el-fill-color-light);
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.editor-content-area :deep(.ProseMirror pre) {
  background-color: var(--el-fill-color-darker);
  padding: var(--spacing-lg);
  border-radius: var(--border-radius-base);
  overflow-x: auto;
  margin: 1.5em 0;
}

.editor-content-area :deep(.ProseMirror img) {
  max-width: 100%;
  height: auto;
  border-radius: var(--border-radius-base);
}

.editor-content-area :deep(.ProseMirror table) {
  border-collapse: collapse;
  margin: 1.5em 0;
  width: 100%;
}

.editor-content-area :deep(.ProseMirror table td),
.editor-content-area :deep(.ProseMirror table th) {
  border: 1px solid var(--el-border-color);
  padding: var(--spacing-sm) var(--spacing-md);
  text-align: left;
}

.editor-content-area :deep(.ProseMirror table th) {
  background-color: var(--el-fill-color-light);
  font-weight: 600;
}

/* 侧边栏 */
.editor-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.settings-section,
.stats-section,
.ai-section {
  background-color: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
}

.settings-section h3,
.stats-section h3,
.ai-section h3 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: 1rem;
  color: var(--el-text-color-primary);
}

.setting-item {
  margin-bottom: var(--spacing-md);
}

.setting-item:last-child {
  margin-bottom: 0;
}

.setting-item label {
  display: block;
  margin-bottom: var(--spacing-xs);
  font-size: 0.9rem;
  color: var(--el-text-color-regular);
  font-weight: 500;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
  font-size: 0.9rem;
}

.stat-item:last-child {
  margin-bottom: 0;
}

.stat-label {
  color: var(--el-text-color-regular);
}

.stat-value {
  color: var(--el-text-color-primary);
  font-weight: 500;
}

.ai-section .el-button {
  width: 100%;
  margin-bottom: var(--spacing-sm);
}

.ai-section .el-button:last-child {
  margin-bottom: 0;
}

/* 预览对话框 */
.preview-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.preview-content {
  padding: var(--spacing-xl);
  max-height: 70vh;
  overflow-y: auto;
}

.preview-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 var(--spacing-lg) 0;
  color: var(--el-text-color-primary);
}

.preview-meta {
  margin-bottom: var(--spacing-lg);
}

.meta-item {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
  flex-wrap: wrap;
}

.meta-item:last-child {
  margin-bottom: 0;
}

.preview-summary {
  background-color: var(--el-fill-color-lighter);
  padding: var(--spacing-lg);
  border-radius: var(--border-radius-base);
  margin-bottom: var(--spacing-lg);
}

.preview-summary p {
  margin: 0;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  font-style: italic;
}

.preview-body {
  line-height: 1.8;
  color: var(--el-text-color-primary);
}

/* 图片上传 */
.image-uploader {
  text-align: center;
}

.upload-tip {
  margin-top: var(--spacing-md);
  text-align: center;
}

.upload-tip p {
  margin: 0;
  font-size: 0.9rem;
  color: var(--el-text-color-placeholder);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .editor-content {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }
  
  .editor-sidebar {
    order: -1;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .editor-content {
    padding: var(--spacing-lg);
  }
  
  .editor-toolbar {
    flex-direction: column;
    gap: var(--spacing-sm);
  }
  
  .toolbar-group {
    justify-content: center;
  }
}
</style>