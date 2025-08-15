<template>
  <div 
    class="user-avatar" 
    :class="[
      `user-avatar--${size}`,
      { 'user-avatar--clickable': clickable }
    ]"
    :style="avatarStyle"
    @click="handleClick"
  >
    <img 
      v-if="src && !imageError" 
      :src="src" 
      :alt="name"
      class="user-avatar__image"
      @error="handleImageError"
      @load="handleImageLoad"
    />
    <span v-else class="user-avatar__text">
      {{ initials }}
    </span>
    
    <!-- 在线状态指示器 -->
    <div 
      v-if="showStatus" 
      class="user-avatar__status" 
      :class="`user-avatar__status--${status}`"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { getAvatarColor, getNameInitials } from '@/utils'

type AvatarSize = 'small' | 'default' | 'large'
type UserStatus = 'online' | 'offline' | 'away' | 'busy'

interface Props {
  /** 头像图片地址 */
  src?: string
  /** 用户名 */
  name: string
  /** 头像尺寸 */
  size?: AvatarSize
  /** 是否可点击 */
  clickable?: boolean
  /** 是否显示在线状态 */
  showStatus?: boolean
  /** 用户状态 */
  status?: UserStatus
  /** 自定义背景色 */
  backgroundColor?: string
}

interface Emits {
  /** 点击头像时触发 */
  click: []
}

const props = withDefaults(defineProps<Props>(), {
  src: '',
  size: 'default',
  clickable: false,
  showStatus: false,
  status: 'offline',
  backgroundColor: ''
})

const emit = defineEmits<Emits>()

// 图片加载状态
const imageError = ref(false)
const imageLoaded = ref(false)

/**
 * 获取用户名首字母
 */
const initials = computed(() => {
  return getNameInitials(props.name)
})

/**
 * 头像样式
 */
const avatarStyle = computed(() => {
  const backgroundColor = props.backgroundColor || getAvatarColor(props.name)
  return {
    backgroundColor,
    color: '#ffffff'
  }
})

/**
 * 处理图片加载错误
 */
const handleImageError = () => {
  imageError.value = true
}

/**
 * 处理图片加载成功
 */
const handleImageLoad = () => {
  imageLoaded.value = true
  imageError.value = false
}

/**
 * 处理点击事件
 */
const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}
</script>

<style scoped>
.user-avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  user-select: none;
  transition: all 0.2s ease;
}

.user-avatar--clickable {
  cursor: pointer;
}

.user-avatar--clickable:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 尺寸变体 */
.user-avatar--small {
  width: 24px;
  height: 24px;
  font-size: 10px;
}

.user-avatar--default {
  width: 32px;
  height: 32px;
  font-size: 12px;
}

.user-avatar--large {
  width: 48px;
  height: 48px;
  font-size: 16px;
}

.user-avatar__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar__text {
  font-weight: 500;
  line-height: 1;
}

/* 状态指示器 */
.user-avatar__status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 2px solid var(--el-bg-color);
  box-sizing: border-box;
}

.user-avatar--small .user-avatar__status {
  width: 6px;
  height: 6px;
  border-width: 1px;
}

.user-avatar--large .user-avatar__status {
  width: 12px;
  height: 12px;
  border-width: 2px;
}

.user-avatar__status--online {
  background-color: #52c41a;
}

.user-avatar__status--offline {
  background-color: #d9d9d9;
}

.user-avatar__status--away {
  background-color: #faad14;
}

.user-avatar__status--busy {
  background-color: #ff4d4f;
}

/* 加载动画 */
.user-avatar__image {
  opacity: 0;
  transition: opacity 0.2s ease;
}

.user-avatar__image[src] {
  opacity: 1;
}

/* 暗色主题适配 */
.dark .user-avatar__status {
  border-color: var(--el-bg-color);
}
</style>