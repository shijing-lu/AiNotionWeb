<template>
  <div class="file-upload">
    <!-- 上传区域 -->
    <div 
      class="file-upload__area"
      :class="{
        'file-upload__area--dragover': isDragOver,
        'file-upload__area--disabled': disabled
      }"
      @click="handleClick"
      @drop="handleDrop"
      @dragover="handleDragOver"
      @dragenter="handleDragEnter"
      @dragleave="handleDragLeave"
    >
      <input 
        ref="fileInputRef"
        type="file"
        :accept="accept"
        :multiple="multiple"
        :disabled="disabled"
        class="file-upload__input"
        @change="handleFileSelect"
      />
      
      <div class="file-upload__content">
        <div class="file-upload__icon">
          <el-icon :size="48">
            <UploadFilled />
          </el-icon>
        </div>
        
        <div class="file-upload__text">
          <div class="file-upload__title">
            {{ isDragOver ? '释放文件以上传' : '点击或拖拽文件到此处上传' }}
          </div>
          <div class="file-upload__subtitle">
            {{ subtitle }}
          </div>
        </div>
      </div>
    </div>
    
    <!-- 文件列表 -->
    <div v-if="fileList.length > 0" class="file-upload__list">
      <div 
        v-for="(file, index) in fileList"
        :key="file.uid"
        class="file-upload__item"
      >
        <div class="file-item">
          <div class="file-item__icon">
            <el-icon>
              <component :is="getFileIcon(file.name)" />
            </el-icon>
          </div>
          
          <div class="file-item__info">
            <div class="file-item__name">{{ file.name }}</div>
            <div class="file-item__size">{{ formatFileSize(file.size) }}</div>
          </div>
          
          <div class="file-item__status">
            <el-progress 
              v-if="file.status === 'uploading'"
              :percentage="file.progress"
              :stroke-width="4"
              :show-text="false"
              class="file-item__progress"
            />
            <el-icon 
              v-else-if="file.status === 'success'"
              class="file-item__success"
            >
              <CircleCheck />
            </el-icon>
            <el-icon 
              v-else-if="file.status === 'error'"
              class="file-item__error"
            >
              <CircleClose />
            </el-icon>
          </div>
          
          <div class="file-item__actions">
            <el-button 
              type="text"
              size="small"
              @click="handleRemove(index)"
              :disabled="file.status === 'uploading'"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { UploadFilled, Document, Picture, VideoCamera, Headset, Folder, CircleCheck, CircleClose, Delete } from '@element-plus/icons-vue'
import { formatFileSize, getFileExtension, generateId } from '@/utils'
import type { Component } from 'vue'

type FileStatus = 'ready' | 'uploading' | 'success' | 'error'

interface UploadFile {
  uid: string
  name: string
  size: number
  status: FileStatus
  progress: number
  file: File
  response?: any
  error?: string
}

interface Props {
  /** 接受的文件类型 */
  accept?: string
  /** 是否支持多选 */
  multiple?: boolean
  /** 是否禁用 */
  disabled?: boolean
  /** 最大文件大小（字节） */
  maxSize?: number
  /** 最大文件数量 */
  maxCount?: number
  /** 副标题文本 */
  subtitle?: string
}

interface Emits {
  /** 文件选择事件 */
  'file-select': [files: File[]]
  /** 文件上传事件 */
  'file-upload': [file: UploadFile]
  /** 文件移除事件 */
  'file-remove': [file: UploadFile, index: number]
  /** 文件列表变化事件 */
  'change': [fileList: UploadFile[]]
}

const props = withDefaults(defineProps<Props>(), {
  accept: '*',
  multiple: false,
  disabled: false,
  maxSize: 10 * 1024 * 1024, // 10MB
  maxCount: 10,
  subtitle: '支持单个文件上传，文件大小不超过 10MB'
})

const emit = defineEmits<Emits>()

// 组件引用
const fileInputRef = ref<HTMLInputElement>()

// 状态
const isDragOver = ref(false)
const fileList = ref<UploadFile[]>([])

/**
 * 获取文件图标
 */
const getFileIcon = (filename: string): Component => {
  const ext = getFileExtension(filename).toLowerCase()
  
  const iconMap: Record<string, Component> = {
    // 图片类型
    'jpg': Picture,
    'jpeg': Picture,
    'png': Picture,
    'gif': Picture,
    'bmp': Picture,
    'svg': Picture,
    'webp': Picture,
    
    // 视频类型
    'mp4': VideoCamera,
    'avi': VideoCamera,
    'mov': VideoCamera,
    'wmv': VideoCamera,
    'flv': VideoCamera,
    'webm': VideoCamera,
    
    // 音频类型
    'mp3': Headset,
    'wav': Headset,
    'flac': Headset,
    'aac': Headset,
    'ogg': Headset,
    
    // 压缩文件
    'zip': Folder,
    'rar': Folder,
    '7z': Folder,
    'tar': Folder,
    'gz': Folder
  }
  
  return iconMap[ext] || Document
}

/**
 * 验证文件
 */
const validateFile = (file: File): string | null => {
  // 检查文件大小
  if (file.size > props.maxSize) {
    return `文件大小不能超过 ${formatFileSize(props.maxSize)}`
  }
  
  // 检查文件数量
  if (fileList.value.length >= props.maxCount) {
    return `最多只能上传 ${props.maxCount} 个文件`
  }
  
  return null
}

/**
 * 添加文件到列表
 */
const addFiles = (files: File[]) => {
  const validFiles: File[] = []
  
  for (const file of files) {
    const error = validateFile(file)
    if (error) {
      console.warn(error)
      continue
    }
    validFiles.push(file)
  }
  
  if (validFiles.length === 0) return
  
  const uploadFiles: UploadFile[] = validFiles.map(file => ({
    uid: generateId('file_'),
    name: file.name,
    size: file.size,
    status: 'ready' as FileStatus,
    progress: 0,
    file
  }))
  
  fileList.value.push(...uploadFiles)
  emit('file-select', validFiles)
  emit('change', fileList.value)
}

/**
 * 处理点击上传
 */
const handleClick = () => {
  if (props.disabled) return
  fileInputRef.value?.click()
}

/**
 * 处理文件选择
 */
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = Array.from(target.files || [])
  if (files.length > 0) {
    addFiles(files)
  }
  // 清空input值，允许重复选择同一文件
  target.value = ''
}

/**
 * 处理拖拽进入
 */
const handleDragEnter = (event: DragEvent) => {
  event.preventDefault()
  if (props.disabled) return
  isDragOver.value = true
}

/**
 * 处理拖拽悬停
 */
const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
}

/**
 * 处理拖拽离开
 */
const handleDragLeave = (event: DragEvent) => {
  event.preventDefault()
  // 检查是否真的离开了拖拽区域
  const rect = (event.currentTarget as HTMLElement).getBoundingClientRect()
  const x = event.clientX
  const y = event.clientY
  
  if (x < rect.left || x > rect.right || y < rect.top || y > rect.bottom) {
    isDragOver.value = false
  }
}

/**
 * 处理文件拖拽放置
 */
const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  isDragOver.value = false
  
  if (props.disabled) return
  
  const files = Array.from(event.dataTransfer?.files || [])
  if (files.length > 0) {
    addFiles(files)
  }
}

/**
 * 移除文件
 */
const handleRemove = (index: number) => {
  const file = fileList.value[index]
  fileList.value.splice(index, 1)
  emit('file-remove', file, index)
  emit('change', fileList.value)
}

/**
 * 更新文件上传进度
 */
const updateFileProgress = (uid: string, progress: number) => {
  const file = fileList.value.find(f => f.uid === uid)
  if (file) {
    file.progress = progress
    file.status = progress === 100 ? 'success' : 'uploading'
  }
}

/**
 * 设置文件上传状态
 */
const setFileStatus = (uid: string, status: FileStatus, error?: string) => {
  const file = fileList.value.find(f => f.uid === uid)
  if (file) {
    file.status = status
    if (error) {
      file.error = error
    }
  }
}

// 暴露方法给父组件
defineExpose({
  updateFileProgress,
  setFileStatus,
  fileList
})
</script>

<style scoped>
.file-upload {
  width: 100%;
}

.file-upload__area {
  border: 2px dashed var(--el-border-color);
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: var(--el-fill-color-lighter);
}

.file-upload__area:hover {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}

.file-upload__area--dragover {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  transform: scale(1.02);
}

.file-upload__area--disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.file-upload__area--disabled:hover {
  border-color: var(--el-border-color);
  background-color: var(--el-fill-color-lighter);
}

.file-upload__input {
  display: none;
}

.file-upload__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.file-upload__icon {
  color: var(--el-text-color-placeholder);
}

.file-upload__text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-upload__title {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.file-upload__subtitle {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.file-upload__list {
  margin-top: 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  overflow: hidden;
}

.file-upload__item {
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.file-upload__item:last-child {
  border-bottom: none;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  gap: 12px;
  background-color: var(--el-bg-color);
  transition: background-color 0.2s;
}

.file-item:hover {
  background-color: var(--el-fill-color-lighter);
}

.file-item__icon {
  flex-shrink: 0;
  color: var(--el-text-color-regular);
}

.file-item__info {
  flex: 1;
  min-width: 0;
}

.file-item__name {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-item__size {
  font-size: 12px;
  color: var(--el-text-color-regular);
  margin-top: 2px;
}

.file-item__status {
  flex-shrink: 0;
  width: 80px;
}

.file-item__progress {
  width: 100%;
}

.file-item__success {
  color: var(--el-color-success);
}

.file-item__error {
  color: var(--el-color-danger);
}

.file-item__actions {
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .file-upload__area {
    padding: 24px 16px;
  }
  
  .file-upload__title {
    font-size: 14px;
  }
  
  .file-upload__subtitle {
    font-size: 12px;
  }
  
  .file-item {
    padding: 8px 12px;
    gap: 8px;
  }
  
  .file-item__status {
    width: 60px;
  }
}
</style>