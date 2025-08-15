<template>
  <div class="empty-state">
    <div class="empty-state__icon">
      <el-icon v-if="icon" :size="iconSize">
        <component :is="icon" />
      </el-icon>
      <div v-else class="default-icon">
        <svg viewBox="0 0 64 41" xmlns="http://www.w3.org/2000/svg">
          <g transform="translate(0 1)" fill="none" fill-rule="evenodd">
            <ellipse fill="#f5f5f5" cx="32" cy="33" rx="32" ry="7"></ellipse>
            <g fill-rule="nonzero" stroke="#d9d9d9">
              <path d="M55 12.76L44.854 1.258C44.367.474 43.656 0 42.907 0H21.093c-.749 0-1.46.474-1.947 1.257L9 12.761V22h46v-9.24z"></path>
              <path d="M41.613 15.931c0-1.605.994-2.93 2.227-2.931H55v18.137C55 33.26 53.68 35 52.05 35h-40.1C10.32 35 9 33.259 9 31.137V13h11.16c1.233 0 2.227 1.323 2.227 2.928v.022c0 1.605 1.005 2.901 2.237 2.901h14.752c1.232 0 2.237-1.308 2.237-2.913v-.007z" fill="#fafafa"></path>
            </g>
          </g>
        </svg>
      </div>
    </div>
    
    <div class="empty-state__content">
      <h3 class="empty-state__title">{{ title }}</h3>
      <p v-if="description" class="empty-state__description">{{ description }}</p>
    </div>
    
    <div v-if="$slots.actions || actionText" class="empty-state__actions">
      <slot name="actions">
        <el-button v-if="actionText" type="primary" @click="handleAction">
          {{ actionText }}
        </el-button>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Component } from 'vue'

interface Props {
  /** 标题 */
  title: string
  /** 描述文本 */
  description?: string
  /** 图标组件 */
  icon?: Component
  /** 图标大小 */
  iconSize?: number
  /** 操作按钮文本 */
  actionText?: string
}

interface Emits {
  /** 点击操作按钮时触发 */
  action: []
}

withDefaults(defineProps<Props>(), {
  description: '',
  iconSize: 64,
  actionText: ''
})

const emit = defineEmits<Emits>()

/**
 * 处理操作按钮点击
 */
const handleAction = () => {
  emit('action')
}
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
  min-height: 200px;
}

.empty-state__icon {
  margin-bottom: 24px;
  color: var(--el-text-color-placeholder);
}

.default-icon {
  width: 64px;
  height: 41px;
  opacity: 0.4;
}

.default-icon svg {
  width: 100%;
  height: 100%;
}

.empty-state__content {
  margin-bottom: 24px;
}

.empty-state__title {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
}

.empty-state__description {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin: 0;
  line-height: 1.5;
  max-width: 400px;
}

.empty-state__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .empty-state {
    padding: 32px 16px;
  }
  
  .empty-state__title {
    font-size: 14px;
  }
  
  .empty-state__description {
    font-size: 13px;
  }
}
</style>