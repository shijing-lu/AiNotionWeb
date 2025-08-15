<template>
  <div class="ai-panel">
    <!-- 面板头部 -->
    <div class="panel-header">
      <div class="header-left">
        <el-icon class="panel-icon"><ChatDotRound /></el-icon>
        <h3 class="panel-title">AI助手</h3>
      </div>
      <div class="header-right">
        <el-button text @click="minimizePanel">
          <el-icon><Minus /></el-icon>
        </el-button>
        <el-button text @click="closePanel">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>
    
    <!-- 快速操作 -->
    <div class="quick-actions">
      <el-button 
        v-for="action in quickActions" 
        :key="action.key"
        size="small" 
        :type="action.type"
        @click="handleQuickAction(action.key)"
      >
        <el-icon><component :is="action.icon" /></el-icon>
        {{ action.label }}
      </el-button>
    </div>
    
    <!-- 聊天区域 -->
    <div class="chat-area">
      <!-- 消息列表 -->
      <div ref="messagesContainer" class="messages-container">
        <div 
          v-for="message in messages" 
          :key="message.id"
          class="message-item"
          :class="message.role"
        >
          <div class="message-avatar">
            <el-avatar v-if="message.role === 'user'" :size="24">
              <el-icon><User /></el-icon>
            </el-avatar>
            <el-avatar v-else :size="24" class="ai-avatar">
              <el-icon><ChatDotRound /></el-icon>
            </el-avatar>
          </div>
          
          <div class="message-content">
            <div class="message-text" v-html="formatMessage(message.content)"></div>
            <div class="message-time">{{ formatTime(message.createdAt) }}</div>
          </div>
          
          <div class="message-actions">
            <el-button text size="small" @click="copyMessage(message.content)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
            <el-button 
              v-if="message.role === 'assistant'" 
              text 
              size="small" 
              @click="insertToDocument(message.content)"
            >
              <el-icon><DocumentAdd /></el-icon>
            </el-button>
          </div>
        </div>
        
        <!-- 加载状态 -->
        <div v-if="isLoading" class="message-item assistant loading">
          <div class="message-avatar">
            <el-avatar :size="24" class="ai-avatar">
              <el-icon><ChatDotRound /></el-icon>
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          placeholder="向AI助手提问..."
          :disabled="isLoading"
          @keydown.ctrl.enter="sendMessage"
          @keydown.meta.enter="sendMessage"
        />
        <div class="input-actions">
          <div class="input-tips">
            <el-tooltip content="Ctrl + Enter 发送">
              <el-icon><InfoFilled /></el-icon>
            </el-tooltip>
          </div>
          <el-button 
            type="primary" 
            size="small"
            :loading="isLoading"
            :disabled="!inputMessage.trim()"
            @click="sendMessage"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 建议模板 -->
    <div v-if="messages.length === 0" class="suggestions">
      <h4>试试这些：</h4>
      <div class="suggestion-list">
        <div 
          v-for="suggestion in suggestions" 
          :key="suggestion.text"
          class="suggestion-item"
          @click="useSuggestion(suggestion.text)"
        >
          <el-icon><component :is="suggestion.icon" /></el-icon>
          <span>{{ suggestion.text }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  Minus,
  Close,
  User,
  CopyDocument,
  DocumentAdd,
  InfoFilled,
  EditPen,
  Document,
  MagicStick,
  Switch
} from '@element-plus/icons-vue'

// 接口定义
interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  createdAt: Date
}

interface QuickAction {
  key: string
  label: string
  icon: string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
}

interface Suggestion {
  text: string
  icon: string
}

// 事件定义
const emit = defineEmits<{
  close: []
  minimize: []
  insertText: [text: string]
}>()

// 响应式数据
const messages = ref<Message[]>([])
const inputMessage = ref('')
const isLoading = ref(false)
const messagesContainer = ref<HTMLElement>()

// 快速操作
const quickActions: QuickAction[] = [
  { key: 'summarize', label: '总结', icon: 'Document', type: 'primary' },
  { key: 'improve', label: '改进', icon: 'EditPen', type: 'success' },
  { key: 'translate', label: '翻译', icon: 'Switch', type: 'warning' },
  { key: 'creative', label: '创意', icon: 'MagicStick', type: 'info' }
]

// 建议模板
const suggestions: Suggestion[] = [
  { text: '帮我写一份会议纪要', icon: 'Document' },
  { text: '总结这段文字的要点', icon: 'Document' },
  { text: '改进这段文字的表达', icon: 'EditPen' },
  { text: '翻译成英文', icon: 'Switch' },
  { text: '给我一些创意想法', icon: 'MagicStick' }
]

/**
 * 发送消息
 */
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return
  
  const userMessage: Message = {
    id: generateId(),
    role: 'user',
    content: inputMessage.value.trim(),
    createdAt: new Date()
  }
  
  messages.value.push(userMessage)
  const userInput = inputMessage.value
  inputMessage.value = ''
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  // 发送到AI
  await sendToAI(userInput)
}

/**
 * 发送到AI并获取回复
 */
const sendToAI = async (message: string) => {
  isLoading.value = true
  
  try {
    // 模拟AI回复（实际项目中应该调用真实的AI API）
    await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 2000))
    
    const aiResponse = generateAIResponse(message)
    
    const aiMessage: Message = {
      id: generateId(),
      role: 'assistant',
      content: aiResponse,
      createdAt: new Date()
    }
    
    messages.value.push(aiMessage)
    
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('AI回复失败，请重试')
  } finally {
    isLoading.value = false
  }
}

/**
 * 生成AI回复（模拟）
 */
const generateAIResponse = (userMessage: string): string => {
  const responses = [
    `我理解您的需求："${userMessage}"。让我为您提供一些建议...`,
    `关于"${userMessage}"，我可以从以下几个方面来帮助您...`,
    `这是一个很好的问题。针对"${userMessage}"，我建议...`,
    `让我帮您解决这个问题。关于"${userMessage}"...`
  ]
  
  return responses[Math.floor(Math.random() * responses.length)]
}

/**
 * 处理快速操作
 */
const handleQuickAction = (actionKey: string) => {
  const templates = {
    summarize: '请帮我总结当前文档的主要内容',
    improve: '请帮我改进当前文档的表达和结构',
    translate: '请帮我将当前文档翻译成英文',
    creative: '请为当前文档提供一些创意想法和建议'
  }
  
  const template = templates[actionKey as keyof typeof templates]
  if (template) {
    inputMessage.value = template
    sendMessage()
  }
}

/**
 * 使用建议
 */
const useSuggestion = (text: string) => {
  inputMessage.value = text
}

/**
 * 复制消息
 */
const copyMessage = async (content: string) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

/**
 * 插入到文档
 */
const insertToDocument = (content: string) => {
  emit('insertText', content)
  ElMessage.success('内容已插入到文档')
}

/**
 * 格式化消息内容
 */
const formatMessage = (content: string): string => {
  // 简单的markdown格式化
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br>')
}

/**
 * 格式化时间
 */
const formatTime = (date: Date): string => {
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 滚动到底部
 */
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

/**
 * 最小化面板
 */
const minimizePanel = () => {
  emit('minimize')
}

/**
 * 关闭面板
 */
const closePanel = () => {
  emit('close')
}

/**
 * 生成唯一ID
 */
const generateId = (): string => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

// 组件挂载
onMounted(() => {
  // 初始化欢迎消息
  const welcomeMessage: Message = {
    id: generateId(),
    role: 'assistant',
    content: '您好！我是您的AI写作助手，可以帮助您进行文档创作、内容优化、翻译等工作。有什么可以帮助您的吗？',
    createdAt: new Date()
  }
  
  messages.value.push(welcomeMessage)
})
</script>

<style scoped>
.ai-panel {
  width: 320px;
  height: 500px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  box-shadow: var(--el-box-shadow);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-lighter);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.panel-icon {
  color: var(--el-color-primary);
}

.panel-title {
  margin: 0;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.header-right {
  display: flex;
  gap: var(--spacing-xs);
}

.quick-actions {
  display: flex;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm);
  border-bottom: 1px solid var(--el-border-color-lighter);
  overflow-x: auto;
}

.quick-actions .el-button {
  flex-shrink: 0;
  font-size: 0.75rem;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-sm);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.message-item {
  display: flex;
  gap: var(--spacing-xs);
  max-width: 90%;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-item.assistant {
  align-self: flex-start;
}

.message-avatar {
  flex-shrink: 0;
}

.ai-avatar {
  background: var(--el-color-primary);
  color: white;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-text {
  background: var(--el-fill-color-light);
  padding: var(--spacing-sm);
  border-radius: var(--border-radius-base);
  font-size: 0.875rem;
  line-height: 1.4;
  word-wrap: break-word;
}

.message-item.user .message-text {
  background: var(--el-color-primary);
  color: white;
}

.message-time {
  font-size: 0.75rem;
  color: var(--el-text-color-placeholder);
  margin-top: var(--spacing-xs);
  text-align: right;
}

.message-item.user .message-time {
  text-align: left;
}

.message-actions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  opacity: 0;
  transition: opacity 0.2s;
}

.message-item:hover .message-actions {
  opacity: 1;
}

.loading .message-text {
  padding: var(--spacing-md);
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--el-color-primary);
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-8px);
  }
}

.input-area {
  border-top: 1px solid var(--el-border-color-lighter);
  padding: var(--spacing-sm);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--spacing-xs);
}

.input-tips {
  font-size: 0.75rem;
  color: var(--el-text-color-placeholder);
}

.suggestions {
  padding: var(--spacing-sm);
  border-top: 1px solid var(--el-border-color-lighter);
}

.suggestions h4 {
  margin: 0 0 var(--spacing-sm) 0;
  font-size: 0.875rem;
  color: var(--el-text-color-regular);
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-small);
  cursor: pointer;
  font-size: 0.75rem;
  color: var(--el-text-color-regular);
  transition: all 0.2s;
}

.suggestion-item:hover {
  background: var(--el-fill-color-light);
  color: var(--el-color-primary);
}

.suggestion-item .el-icon {
  font-size: 0.875rem;
}

/* 滚动条样式 */
.messages-container::-webkit-scrollbar {
  width: 4px;
}

.messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.messages-container::-webkit-scrollbar-thumb {
  background: var(--el-border-color-light);
  border-radius: 2px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color);
}
</style>