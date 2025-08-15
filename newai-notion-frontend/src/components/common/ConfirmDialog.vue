<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    class="confirm-dialog"
    align-center
  >
    <div class="confirm-dialog__content">
      <div v-if="icon" class="confirm-dialog__icon">
        <el-icon :size="48" :class="`confirm-dialog__icon--${type}`">
          <component :is="icon" />
        </el-icon>
      </div>
      
      <div class="confirm-dialog__message">
        <div class="confirm-dialog__title">{{ message }}</div>
        <div v-if="description" class="confirm-dialog__description">
          {{ description }}
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="confirm-dialog__footer">
        <el-button 
          @click="handleCancel"
          :disabled="loading"
        >
          {{ cancelText }}
        </el-button>
        <el-button 
          :type="confirmButtonType"
          :loading="loading"
          @click="handleConfirm"
        >
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Warning, QuestionFilled, CircleCheck, CircleClose, InfoFilled } from '@element-plus/icons-vue'
import type { Component } from 'vue'

type DialogType = 'warning' | 'info' | 'success' | 'error' | 'question'

interface Props {
  /** 是否显示对话框 */
  modelValue: boolean
  /** 对话框标题 */
  title?: string
  /** 主要消息 */
  message: string
  /** 描述信息 */
  description?: string
  /** 对话框类型 */
  type?: DialogType
  /** 自定义图标 */
  customIcon?: Component
  /** 确认按钮文本 */
  confirmText?: string
  /** 取消按钮文本 */
  cancelText?: string
  /** 对话框宽度 */
  width?: string
  /** 是否显示加载状态 */
  loading?: boolean
}

interface Emits {
  /** 更新显示状态 */
  'update:modelValue': [value: boolean]
  /** 确认事件 */
  confirm: []
  /** 取消事件 */
  cancel: []
}

const props = withDefaults(defineProps<Props>(), {
  title: '确认',
  description: '',
  type: 'warning',
  confirmText: '确定',
  cancelText: '取消',
  width: '420px',
  loading: false
})

const emit = defineEmits<Emits>()

// 对话框显示状态
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

/**
 * 获取图标组件
 */
const icon = computed(() => {
  if (props.customIcon) {
    return props.customIcon
  }
  
  const iconMap = {
    warning: Warning,
    info: InfoFilled,
    success: CircleCheck,
    error: CircleClose,
    question: QuestionFilled
  }
  
  return iconMap[props.type]
})

/**
 * 确认按钮类型
 */
const confirmButtonType = computed(() => {
  const typeMap = {
    warning: 'warning',
    info: 'primary',
    success: 'success',
    error: 'danger',
    question: 'primary'
  }
  
  return typeMap[props.type] as any
})

/**
 * 处理确认
 */
const handleConfirm = () => {
  emit('confirm')
}

/**
 * 处理取消
 */
const handleCancel = () => {
  visible.value = false
  emit('cancel')
}
</script>

<style scoped>
.confirm-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 0 24px;
}

.confirm-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.confirm-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 24px 24px;
}

.confirm-dialog__content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.confirm-dialog__icon {
  flex-shrink: 0;
  margin-top: 4px;
}

.confirm-dialog__icon--warning {
  color: var(--el-color-warning);
}

.confirm-dialog__icon--info {
  color: var(--el-color-info);
}

.confirm-dialog__icon--success {
  color: var(--el-color-success);
}

.confirm-dialog__icon--error {
  color: var(--el-color-danger);
}

.confirm-dialog__icon--question {
  color: var(--el-color-primary);
}

.confirm-dialog__message {
  flex: 1;
}

.confirm-dialog__title {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  line-height: 1.5;
  margin-bottom: 8px;
}

.confirm-dialog__description {
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
}

.confirm-dialog__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .confirm-dialog :deep(.el-dialog) {
    width: 90% !important;
    margin: 0 auto;
  }
  
  .confirm-dialog__content {
    flex-direction: column;
    text-align: center;
  }
  
  .confirm-dialog__footer {
    flex-direction: column-reverse;
  }
  
  .confirm-dialog__footer .el-button {
    width: 100%;
  }
}
</style>