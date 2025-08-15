<template>
  <div class="article-create-view">
    <div class="create-container">
      <!-- 创建页面头部 -->
      <div class="create-header">
        <div class="header-content">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ name: 'Articles' }">文章管理</el-breadcrumb-item>
              <el-breadcrumb-item>创建文章</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-actions">
            <el-button @click="goBack">
              取消
            </el-button>
            
            <el-button type="primary" @click="createAndEdit">
              <el-icon><Edit /></el-icon>
              开始编辑
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 创建选项 -->
      <div class="create-content">
        <div class="create-options">
          <h2>选择创建方式</h2>
          
          <div class="option-grid">
            <!-- 空白文章 -->
            <div class="option-card" @click="createBlankArticle">
              <div class="option-icon">
                <el-icon size="48"><Document /></el-icon>
              </div>
              <h3>空白文章</h3>
              <p>从头开始创建一篇新文章</p>
            </div>
            
            <!-- 模板文章 -->
            <div class="option-card" @click="showTemplates">
              <div class="option-icon">
                <el-icon size="48"><Collection /></el-icon>
              </div>
              <h3>使用模板</h3>
              <p>从预设模板快速开始</p>
            </div>
            
            <!-- 导入文章 -->
            <div class="option-card" @click="importArticle">
              <div class="option-icon">
                <el-icon size="48"><Upload /></el-icon>
              </div>
              <h3>导入文章</h3>
              <p>从文件导入现有内容</p>
            </div>
            
            <!-- AI 生成 -->
            <div class="option-card" @click="showAIGenerator">
              <div class="option-icon">
                <el-icon size="48"><MagicStick /></el-icon>
              </div>
              <h3>AI 生成</h3>
              <p>让 AI 帮你生成文章大纲</p>
            </div>
          </div>
        </div>
        
        <!-- 快速设置 -->
        <div class="quick-settings">
          <h3>快速设置</h3>
          
          <el-form :model="quickForm" label-width="80px" class="settings-form">
            <el-form-item label="文章标题">
              <el-input
                v-model="quickForm.title"
                placeholder="请输入文章标题（可选）"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="分类">
              <el-select
                v-model="quickForm.category"
                placeholder="请选择分类"
                filterable
                allow-create
                style="width: 100%"
              >
                <el-option
                  v-for="category in availableCategories"
                  :key="category"
                  :label="category"
                  :value="category"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="标签">
              <el-select
                v-model="quickForm.tags"
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
            </el-form-item>
            
            <el-form-item label="可见性">
              <el-radio-group v-model="quickForm.visibility">
                <el-radio label="PUBLIC">公开</el-radio>
                <el-radio label="PRIVATE">私有</el-radio>
                <el-radio label="PROTECTED">受保护</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
    
    <!-- 模板选择对话框 -->
    <el-dialog v-model="templatesVisible" title="选择模板" width="80%" class="templates-dialog">
      <div class="templates-grid">
        <div
          v-for="template in templates"
          :key="template.id"
          class="template-card"
          @click="selectTemplate(template)"
        >
          <div class="template-preview">
            <div class="template-content" v-html="template.preview"></div>
          </div>
          <div class="template-info">
            <h4>{{ template.name }}</h4>
            <p>{{ template.description }}</p>
            <div class="template-meta">
              <el-tag size="small">{{ template.category }}</el-tag>
              <span class="template-usage">{{ template.usage }} 次使用</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- AI 生成对话框 -->
    <el-dialog v-model="aiGeneratorVisible" title="AI 文章生成" width="600px">
      <el-form :model="aiForm" label-width="100px">
        <el-form-item label="文章主题">
          <el-input
            v-model="aiForm.topic"
            placeholder="请输入文章主题或关键词"
            maxlength="100"
          />
        </el-form-item>
        
        <el-form-item label="文章类型">
          <el-select v-model="aiForm.type" style="width: 100%">
            <el-option label="技术教程" value="tutorial" />
            <el-option label="产品介绍" value="product" />
            <el-option label="行业分析" value="analysis" />
            <el-option label="经验分享" value="experience" />
            <el-option label="新闻资讯" value="news" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="目标受众">
          <el-select v-model="aiForm.audience" style="width: 100%">
            <el-option label="技术开发者" value="developer" />
            <el-option label="产品经理" value="pm" />
            <el-option label="设计师" value="designer" />
            <el-option label="普通用户" value="general" />
            <el-option label="行业专家" value="expert" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="文章长度">
          <el-radio-group v-model="aiForm.length">
            <el-radio label="short">短文（500-1000字）</el-radio>
            <el-radio label="medium">中等（1000-2000字）</el-radio>
            <el-radio label="long">长文（2000字以上）</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="写作风格">
          <el-checkbox-group v-model="aiForm.style">
            <el-checkbox label="professional">专业</el-checkbox>
            <el-checkbox label="casual">轻松</el-checkbox>
            <el-checkbox label="detailed">详细</el-checkbox>
            <el-checkbox label="concise">简洁</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="额外要求">
          <el-input
            v-model="aiForm.requirements"
            type="textarea"
            placeholder="请输入其他特殊要求（可选）"
            :rows="3"
            maxlength="300"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="aiGeneratorVisible = false">取消</el-button>
        <el-button type="primary" @click="generateWithAI" :loading="aiGenerating">
          <el-icon><MagicStick /></el-icon>
          生成文章大纲
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 文件导入对话框 -->
    <el-dialog v-model="importVisible" title="导入文章" width="500px">
      <div class="import-section">
        <el-upload
          class="file-uploader"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleImportSuccess"
          :before-upload="beforeImportUpload"
          accept=".md,.txt,.docx"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            选择文件
          </el-button>
        </el-upload>
        
        <div class="import-tip">
          <p>支持的文件格式：</p>
          <ul>
            <li>Markdown (.md)</li>
            <li>纯文本 (.txt)</li>
            <li>Word 文档 (.docx)</li>
          </ul>
          <p>文件大小不超过 10MB</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document, Collection, Upload, MagicStick, Edit
} from '@element-plus/icons-vue'
import { articleApi } from '@/api'
import { useAuthStore } from '@/stores'

/**
 * 快速设置表单接口
 */
interface QuickForm {
  title: string
  category: string
  tags: string[]
  visibility: 'PUBLIC' | 'PRIVATE' | 'PROTECTED'
}

/**
 * AI 生成表单接口
 */
interface AIForm {
  topic: string
  type: string
  audience: string
  length: 'short' | 'medium' | 'long'
  style: string[]
  requirements: string
}

/**
 * 模板接口
 */
interface Template {
  id: string
  name: string
  description: string
  category: string
  content: string
  preview: string
  usage: number
}

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const templatesVisible = ref(false)
const aiGeneratorVisible = ref(false)
const importVisible = ref(false)
const aiGenerating = ref(false)
const availableCategories = ref<string[]>([])
const availableTags = ref<string[]>([])
const templates = ref<Template[]>([])

// 表单数据
const quickForm = ref<QuickForm>({
  title: '',
  category: '',
  tags: [],
  visibility: 'PUBLIC'
})

const aiForm = ref<AIForm>({
  topic: '',
  type: 'tutorial',
  audience: 'developer',
  length: 'medium',
  style: ['professional'],
  requirements: ''
})

// 计算属性
const uploadUrl = '/api/files/import'
const uploadHeaders = {
  'Authorization': `Bearer ${authStore.token}`
}

/**
 * 返回上一页
 */
const goBack = () => {
  router.push('/app/articles')
}

/**
 * 创建空白文章
 */
const createBlankArticle = () => {
  createAndEdit()
}

/**
 * 创建并编辑
 */
const createAndEdit = () => {
  const query: any = {}
  
  if (quickForm.value.title) {
    query.title = quickForm.value.title
  }
  
  if (quickForm.value.category) {
    query.category = quickForm.value.category
  }
  
  if (quickForm.value.tags.length > 0) {
    query.tags = quickForm.value.tags.join(',')
  }
  
  if (quickForm.value.visibility !== 'PUBLIC') {
    query.visibility = quickForm.value.visibility
  }
  
  router.push({
    name: 'ArticleEdit',
    params: { id: 'new' },
    query
  })
}

/**
 * 显示模板选择
 */
const showTemplates = () => {
  templatesVisible.value = true
}

/**
 * 选择模板
 */
const selectTemplate = (template: Template) => {
  router.push({
    name: 'ArticleEdit',
    params: { id: 'new' },
    query: {
      template: template.id,
      title: quickForm.value.title || template.name,
      category: quickForm.value.category || template.category,
      tags: quickForm.value.tags.join(','),
      visibility: quickForm.value.visibility
    }
  })
}

/**
 * 显示 AI 生成器
 */
const showAIGenerator = () => {
  aiGeneratorVisible.value = true
}

/**
 * AI 生成文章
 */
const generateWithAI = async () => {
  if (!aiForm.value.topic.trim()) {
    ElMessage.error('请输入文章主题')
    return
  }
  
  try {
    aiGenerating.value = true
    
    // 这里调用AI接口生成文章大纲
    // const response = await aiApi.generateArticle(aiForm.value)
    
    // 模拟AI生成
    await new Promise(resolve => setTimeout(resolve, 3000))
    
    const generatedContent = {
      title: `关于${aiForm.value.topic}的深度解析`,
      content: `# ${aiForm.value.topic}\n\n## 引言\n\n这里是AI生成的文章内容...\n\n## 主要内容\n\n### 第一部分\n\n内容详情...\n\n### 第二部分\n\n更多内容...\n\n## 总结\n\n文章总结...`,
      category: aiForm.value.type,
      tags: [aiForm.value.topic, aiForm.value.type]
    }
    
    router.push({
      name: 'ArticleEdit',
      params: { id: 'new' },
      query: {
        ai: 'true',
        title: generatedContent.title,
        content: encodeURIComponent(generatedContent.content),
        category: generatedContent.category,
        tags: generatedContent.tags.join(','),
        visibility: quickForm.value.visibility
      }
    })
    
    ElMessage.success('AI 文章生成成功')
    
  } catch (error) {
    console.error('AI 生成失败:', error)
    ElMessage.error('AI 生成失败')
  } finally {
    aiGenerating.value = false
    aiGeneratorVisible.value = false
  }
}

/**
 * 导入文章
 */
const importArticle = () => {
  importVisible.value = true
}

/**
 * 文件上传前检查
 */
const beforeImportUpload = (file: File) => {
  const allowedTypes = ['.md', '.txt', '.docx']
  const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase()
  const isAllowedType = allowedTypes.includes(fileExtension)
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isAllowedType) {
    ElMessage.error('只能上传 .md、.txt、.docx 格式的文件')
    return false
  }
  
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  
  return true
}

/**
 * 文件导入成功
 */
const handleImportSuccess = (response: any) => {
  const { title, content, metadata } = response.data
  
  router.push({
    name: 'ArticleEdit',
    params: { id: 'new' },
    query: {
      import: 'true',
      title: title || quickForm.value.title,
      content: encodeURIComponent(content),
      category: metadata?.category || quickForm.value.category,
      tags: (metadata?.tags || quickForm.value.tags).join(','),
      visibility: quickForm.value.visibility
    }
  })
  
  importVisible.value = false
  ElMessage.success('文件导入成功')
}

/**
 * 加载可用分类
 */
const loadAvailableCategories = async () => {
  try {
    const response = await articleApi.getCategories()
    availableCategories.value = response.data
  } catch (error) {
    console.error('加载分类失败:', error)
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
 * 加载模板
 */
const loadTemplates = async () => {
  try {
    // 这里应该调用模板API
    // const response = await templateApi.getTemplates()
    // templates.value = response.data
    
    // 模拟模板数据
    templates.value = [
      {
        id: '1',
        name: '技术教程模板',
        description: '适用于技术教程和操作指南',
        category: '技术',
        content: '# 教程标题\n\n## 前言\n\n## 准备工作\n\n## 步骤详解\n\n### 步骤一\n\n### 步骤二\n\n## 总结',
        preview: '<h1>教程标题</h1><h2>前言</h2><h2>准备工作</h2><h2>步骤详解</h2>',
        usage: 156
      },
      {
        id: '2',
        name: '产品介绍模板',
        description: '适用于产品功能介绍和说明',
        category: '产品',
        content: '# 产品名称\n\n## 产品概述\n\n## 核心功能\n\n## 使用场景\n\n## 优势特点',
        preview: '<h1>产品名称</h1><h2>产品概述</h2><h2>核心功能</h2><h2>使用场景</h2>',
        usage: 89
      },
      {
        id: '3',
        name: '会议纪要模板',
        description: '适用于会议记录和纪要整理',
        category: '办公',
        content: '# 会议纪要\n\n## 会议信息\n\n## 参会人员\n\n## 会议议程\n\n## 讨论要点\n\n## 行动项',
        preview: '<h1>会议纪要</h1><h2>会议信息</h2><h2>参会人员</h2><h2>会议议程</h2>',
        usage: 234
      }
    ]
  } catch (error) {
    console.error('加载模板失败:', error)
  }
}

// 组件挂载
onMounted(async () => {
  await Promise.all([
    loadAvailableCategories(),
    loadAvailableTags(),
    loadTemplates()
  ])
})
</script>

<style scoped>
.article-create-view {
  min-height: 100vh;
  background-color: var(--el-bg-color);
}

/* 创建页面头部 */
.create-header {
  background-color: var(--el-bg-color-page);
  border-bottom: 1px solid var(--el-border-color-light);
  padding: var(--spacing-lg) 0;
}

.header-content {
  max-width: 1200px;
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

/* 创建内容 */
.create-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-xl);
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--spacing-xl);
}

.create-options h2 {
  margin: 0 0 var(--spacing-xl) 0;
  color: var(--el-text-color-primary);
  font-size: 1.5rem;
}

/* 选项网格 */
.option-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
}

.option-card {
  background-color: var(--el-bg-color-page);
  border: 2px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-xl);
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.option-card:hover {
  border-color: var(--el-color-primary);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.option-icon {
  margin-bottom: var(--spacing-lg);
  color: var(--el-color-primary);
}

.option-card h3 {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--el-text-color-primary);
  font-size: 1.1rem;
}

.option-card p {
  margin: 0;
  color: var(--el-text-color-regular);
  font-size: 0.9rem;
  line-height: 1.5;
}

/* 快速设置 */
.quick-settings {
  background-color: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-xl);
  height: fit-content;
}

.quick-settings h3 {
  margin: 0 0 var(--spacing-lg) 0;
  color: var(--el-text-color-primary);
  font-size: 1.1rem;
}

.settings-form {
  margin: 0;
}

/* 模板对话框 */
.templates-dialog :deep(.el-dialog__body) {
  padding: var(--spacing-lg);
}

.templates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--spacing-lg);
  max-height: 60vh;
  overflow-y: auto;
}

.template-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.template-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.template-preview {
  height: 120px;
  background-color: var(--el-fill-color-lighter);
  padding: var(--spacing-md);
  overflow: hidden;
  position: relative;
}

.template-content {
  font-size: 0.8rem;
  line-height: 1.4;
  color: var(--el-text-color-regular);
  transform: scale(0.8);
  transform-origin: top left;
}

.template-info {
  padding: var(--spacing-lg);
}

.template-info h4 {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--el-text-color-primary);
  font-size: 1rem;
}

.template-info p {
  margin: 0 0 var(--spacing-md) 0;
  color: var(--el-text-color-regular);
  font-size: 0.9rem;
  line-height: 1.4;
}

.template-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.template-usage {
  font-size: 0.8rem;
  color: var(--el-text-color-placeholder);
}

/* 导入区域 */
.import-section {
  text-align: center;
}

.file-uploader {
  margin-bottom: var(--spacing-lg);
}

.import-tip {
  text-align: left;
  background-color: var(--el-fill-color-lighter);
  padding: var(--spacing-lg);
  border-radius: var(--border-radius-base);
}

.import-tip p {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--el-text-color-regular);
  font-size: 0.9rem;
}

.import-tip ul {
  margin: var(--spacing-sm) 0;
  padding-left: var(--spacing-lg);
}

.import-tip li {
  margin-bottom: var(--spacing-xs);
  color: var(--el-text-color-regular);
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .create-content {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }
  
  .quick-settings {
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
  }
  
  .create-content {
    padding: var(--spacing-lg);
  }
  
  .option-grid {
    grid-template-columns: 1fr;
  }
  
  .templates-grid {
    grid-template-columns: 1fr;
  }
}
</style>