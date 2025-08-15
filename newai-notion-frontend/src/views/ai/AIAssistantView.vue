<template>
  <div class="ai-assistant-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon class="title-icon"><ChatDotRound /></el-icon>
            AI助手
          </h1>
          <p class="page-description">智能写作助手，帮助您更高效地创作内容</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="startNewChat">
            <el-icon><Plus /></el-icon>
            新对话
          </el-button>
        </div>
      </div>
    </div>

    <div class="ai-assistant-container">
      <!-- 侧边栏 - 对话历史 -->
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <h3>对话历史</h3>
          <el-button text @click="clearHistory">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
        
        <div class="chat-list">
          <div 
            v-for="chat in chatHistory" 
            :key="chat.id"
            class="chat-item"
            :class="{ active: currentChatId === chat.id }"
            @click="selectChat(chat.id)"
          >
            <div class="chat-title">{{ chat.title }}</div>
            <div class="chat-time">{{ formatRelativeTime(chat.updatedAt) }}</div>
            <el-button 
              text 
              size="small" 
              class="delete-btn"
              @click.stop="deleteChat(chat.id)"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
        
        <EmptyState 
          v-if="chatHistory.length === 0"
          icon="ChatDotRound"
          title="暂无对话历史"
          description="开始您的第一次AI对话吧"
        />
      </div>

      <!-- 主聊天区域 -->
      <div class="chat-main">
        <div v-if="currentChatId" class="chat-container">
          <!-- 消息列表 -->
          <div ref="messagesContainer" class="messages-container">
            <div 
              v-for="message in currentMessages" 
              :key="message.id"
              class="message-item"
              :class="message.role"
            >
              <div class="message-avatar">
                <el-avatar v-if="message.role === 'user'" :size="32">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <el-avatar v-else :size="32" class="ai-avatar">
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
                  @click="regenerateResponse(message.id)"
                >
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </div>
            </div>
            
            <!-- 加载状态 -->
            <div v-if="isLoading" class="message-item assistant loading">
              <div class="message-avatar">
                <el-avatar :size="32" class="ai-avatar">
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
          <div class="input-container">
            <div class="input-wrapper">
              <el-input
                v-model="inputMessage"
                type="textarea"
                :rows="3"
                placeholder="输入您的问题或需求..."
                :disabled="isLoading"
                @keydown.ctrl.enter="sendMessage"
                @keydown.meta.enter="sendMessage"
              />
              <div class="input-actions">
                <div class="input-tips">
                  <span>Ctrl + Enter 发送</span>
                </div>
                <el-button 
                  type="primary" 
                  :loading="isLoading"
                  :disabled="!inputMessage.trim()"
                  @click="sendMessage"
                >
                  发送
                </el-button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 欢迎页面 -->
        <div v-else class="welcome-container">
          <div class="welcome-content">
            <el-icon class="welcome-icon"><ChatDotRound /></el-icon>
            <h2>欢迎使用AI助手</h2>
            <p>我可以帮助您：</p>
            <div class="feature-grid">
              <div class="feature-item" @click="useTemplate('写作助手')">
                <el-icon><EditPen /></el-icon>
                <span>写作助手</span>
              </div>
              <div class="feature-item" @click="useTemplate('内容总结')">
                <el-icon><Document /></el-icon>
                <span>内容总结</span>
              </div>
              <div class="feature-item" @click="useTemplate('翻译助手')">
                <el-icon><Switch /></el-icon>
                <span>翻译助手</span>
              </div>
              <div class="feature-item" @click="useTemplate('创意灵感')">
                <el-icon><MagicStick /></el-icon>
                <span>创意灵感</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatDotRound,
  Plus,
  Delete,
  Close,
  User,
  CopyDocument,
  Refresh,
  EditPen,
  Document,
  Switch,
  MagicStick
} from '@element-plus/icons-vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { formatRelativeTime } from '@/utils/date'

// 接口定义
interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  createdAt: Date
}

interface ChatSession {
  id: string
  title: string
  messages: ChatMessage[]
  createdAt: Date
  updatedAt: Date
}

// 响应式数据
const chatHistory = ref<ChatSession[]>([])
const currentChatId = ref<string | null>(null)
const inputMessage = ref('')
const isLoading = ref(false)
const messagesContainer = ref<HTMLElement>()

// 计算属性
const currentMessages = computed(() => {
  const currentChat = chatHistory.value.find(chat => chat.id === currentChatId.value)
  return currentChat?.messages || []
})

/**
 * 开始新对话
 */
const startNewChat = () => {
  const newChat: ChatSession = {
    id: generateId(),
    title: '新对话',
    messages: [],
    createdAt: new Date(),
    updatedAt: new Date()
  }
  
  chatHistory.value.unshift(newChat)
  currentChatId.value = newChat.id
}

/**
 * 选择对话
 */
const selectChat = (chatId: string) => {
  currentChatId.value = chatId
}

/**
 * 发送消息
 */
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return
  
  if (!currentChatId.value) {
    startNewChat()
  }
  
  const userMessage: ChatMessage = {
    id: generateId(),
    role: 'user',
    content: inputMessage.value.trim(),
    createdAt: new Date()
  }
  
  // 添加用户消息
  const currentChat = chatHistory.value.find(chat => chat.id === currentChatId.value)
  if (currentChat) {
    currentChat.messages.push(userMessage)
    
    // 更新对话标题（如果是第一条消息）
    if (currentChat.messages.length === 1) {
      currentChat.title = inputMessage.value.slice(0, 20) + (inputMessage.value.length > 20 ? '...' : '')
    }
    
    currentChat.updatedAt = new Date()
  }
  
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
    
    const aiMessage: ChatMessage = {
      id: generateId(),
      role: 'assistant',
      content: aiResponse,
      createdAt: new Date()
    }
    
    const currentChat = chatHistory.value.find(chat => chat.id === currentChatId.value)
    if (currentChat) {
      currentChat.messages.push(aiMessage)
      currentChat.updatedAt = new Date()
    }
    
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
    `我理解您的问题："${userMessage}"。让我为您提供一些建议...`,
    `关于"${userMessage}"，我可以从以下几个方面来分析...`,
    `这是一个很好的问题。针对"${userMessage}"，我建议...`,
    `让我帮您解决这个问题。关于"${userMessage}"...`
  ]
  
  return responses[Math.floor(Math.random() * responses.length)]
}

/**
 * 使用模板
 */
const useTemplate = (template: string) => {
  const templates = {
    '写作助手': '请帮我写一篇关于...的文章',
    '内容总结': '请帮我总结以下内容的要点：',
    '翻译助手': '请帮我翻译以下内容：',
    '创意灵感': '请为我提供一些关于...的创意想法'
  }
  
  inputMessage.value = templates[template as keyof typeof templates] || ''
  
  if (!currentChatId.value) {
    startNewChat()
  }
}

/**
 * 删除对话
 */
const deleteChat = async (chatId: string) => {
  try {
    await ElMessageBox.confirm('确定要删除这个对话吗？', '确认删除', {
      type: 'warning'
    })
    
    const index = chatHistory.value.findIndex(chat => chat.id === chatId)
    if (index > -1) {
      chatHistory.value.splice(index, 1)
      
      if (currentChatId.value === chatId) {
        currentChatId.value = chatHistory.value.length > 0 ? chatHistory.value[0].id : null
      }
    }
    
    ElMessage.success('对话已删除')
  } catch {
    // 用户取消删除
  }
}

/**
 * 清空历史
 */
const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有对话历史吗？', '确认清空', {
      type: 'warning'
    })
    
    chatHistory.value = []
    currentChatId.value = null
    
    ElMessage.success('对话历史已清空')
  } catch {
    // 用户取消清空
  }
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
 * 重新生成回复
 */
const regenerateResponse = async (messageId: string) => {
  const currentChat = chatHistory.value.find(chat => chat.id === currentChatId.value)
  if (!currentChat) return
  
  const messageIndex = currentChat.messages.findIndex(msg => msg.id === messageId)
  if (messageIndex === -1) return
  
  // 获取用户的上一条消息
  const userMessage = currentChat.messages[messageIndex - 1]
  if (!userMessage || userMessage.role !== 'user') return
  
  // 删除当前AI回复
  currentChat.messages.splice(messageIndex, 1)
  
  // 重新生成回复
  await sendToAI(userMessage.content)
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
 * 生成唯一ID
 */
const generateId = (): string => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

// 组件挂载
onMounted(() => {
  // 加载历史对话（实际项目中从API获取）
  // loadChatHistory()
})
</script>

<style scoped>
.ai-assistant-view {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color-page);
}

.page-header {
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  padding: var(--spacing-lg) var(--spacing-xl);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.header-left {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin: 0 0 var(--spacing-xs) 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.title-icon {
  color: var(--el-color-primary);
}

.page-description {
  margin: 0;
  color: var(--el-text-color-regular);
}

.ai-assistant-container {
  flex: 1;
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  overflow: hidden;
}

.chat-sidebar {
  width: 280px;
  background: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color-light);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--el-border-color-lighter);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-sm);
}

.chat-item {
  padding: var(--spacing-md);
  border-radius: var(--border-radius-base);
  cursor: pointer;
  position: relative;
  margin-bottom: var(--spacing-xs);
  transition: all 0.2s;
}

.chat-item:hover {
  background: var(--el-fill-color-light);
}

.chat-item.active {
  background: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-7);
}

.chat-title {
  font-weight: 500;
  margin-bottom: var(--spacing-xs);
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-time {
  font-size: 0.75rem;
  color: var(--el-text-color-placeholder);
}

.delete-btn {
  position: absolute;
  top: var(--spacing-xs);
  right: var(--spacing-xs);
  opacity: 0;
  transition: opacity 0.2s;
}

.chat-item:hover .delete-btn {
  opacity: 1;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color);
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.message-item {
  display: flex;
  gap: var(--spacing-md);
  max-width: 80%;
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
  padding: var(--spacing-md);
  border-radius: var(--border-radius-base);
  line-height: 1.6;
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
  padding: var(--spacing-lg);
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
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
    transform: translateY(-10px);
  }
}

.input-container {
  border-top: 1px solid var(--el-border-color-light);
  padding: var(--spacing-lg);
  background: var(--el-bg-color);
}

.input-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--spacing-md);
}

.input-tips {
  font-size: 0.75rem;
  color: var(--el-text-color-placeholder);
}

.welcome-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);
}

.welcome-content {
  text-align: center;
  max-width: 600px;
}

.welcome-icon {
  font-size: 4rem;
  color: var(--el-color-primary);
  margin-bottom: var(--spacing-lg);
}

.welcome-content h2 {
  margin: 0 0 var(--spacing-md) 0;
  font-size: 1.5rem;
  color: var(--el-text-color-primary);
}

.welcome-content p {
  margin: 0 0 var(--spacing-xl) 0;
  color: var(--el-text-color-regular);
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: var(--spacing-lg);
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--border-radius-base);
  cursor: pointer;
  transition: all 0.2s;
}

.feature-item:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.feature-item .el-icon {
  font-size: 1.5rem;
  color: var(--el-color-primary);
}

.feature-item span {
  font-size: 0.875rem;
  color: var(--el-text-color-primary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-assistant-container {
    flex-direction: column;
  }
  
  .chat-sidebar {
    width: 100%;
    height: 200px;
  }
  
  .message-item {
    max-width: 95%;
  }
  
  .feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>