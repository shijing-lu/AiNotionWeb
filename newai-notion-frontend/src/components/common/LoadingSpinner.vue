<template>
  <div class="loading-spinner" :class="{ 'loading-spinner--overlay': overlay }">
    <div class="spinner" :style="{ width: size + 'px', height: size + 'px' }">
      <div class="spinner__circle"></div>
      <div class="spinner__circle"></div>
      <div class="spinner__circle"></div>
      <div class="spinner__circle"></div>
    </div>
    <div v-if="text" class="loading-text">{{ text }}</div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  /** 加载文本 */
  text?: string
  /** 尺寸大小 */
  size?: number
  /** 是否显示遮罩层 */
  overlay?: boolean
}

withDefaults(defineProps<Props>(), {
  text: '',
  size: 40,
  overlay: false
})
</script>

<style scoped>
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.loading-spinner--overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  z-index: 9999;
}

.spinner {
  position: relative;
  display: inline-block;
}

.spinner__circle {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 2px solid transparent;
  border-top: 2px solid var(--el-color-primary);
  border-radius: 50%;
  animation: spin 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
}

.spinner__circle:nth-child(1) {
  animation-delay: -0.45s;
}

.spinner__circle:nth-child(2) {
  animation-delay: -0.3s;
}

.spinner__circle:nth-child(3) {
  animation-delay: -0.15s;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-text {
  color: var(--el-text-color-regular);
  font-size: 14px;
  text-align: center;
}

/* 暗色主题适配 */
.dark .loading-spinner--overlay {
  background-color: rgba(0, 0, 0, 0.8);
}
</style>