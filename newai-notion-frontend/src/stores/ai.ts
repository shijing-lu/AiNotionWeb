/**
 * AI功能状态管理
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api/modules'
import type { AIRequest, AIResponse } from '@/types'
import type {
  AISearchParams,
  AISummarizeParams,
  AITranslateParams,
  AICheckParams,
  AIQuestionParams,
  AIWritingParams,
  AIOptimizeParams
} from '@/api/modules'

export const useAIStore = defineStore('ai', () => {
  // 状态
  const isProcessing = ref(false)
  const searchResults = ref<any[]>([])
  const conversations = ref<Map<string, any[]>>(new Map())
  const currentConversationId = ref<string | null>(null)
  const usageStats = ref({
    totalRequests: 0,
    requestsToday: 0,
    remainingQuota: 0,
    quotaLimit: 0,
    resetDate: '',
    popularFeatures: []
  })
  const aiHistory = ref<AIResponse[]>([])
  const streamingResponse = ref('')
  const isStreaming = ref(false)

  // 计算属性
  const hasQuotaRemaining = computed(() => usageStats.value.remainingQuota > 0)
  const quotaUsagePercentage = computed(() => {
    const { remainingQuota, quotaLimit } = usageStats.value
    if (quotaLimit === 0) return 0
    return ((quotaLimit - remainingQuota) / quotaLimit) * 100
  })
  const currentConversation = computed(() => {
    if (!currentConversationId.value) return []
    return conversations.value.get(currentConversationId.value) || []
  })

  // AI搜索
  const search = async (params: AISearchParams): Promise<any[]> => {
    try {
      isProcessing.value = true
      const response = await aiApi.search(params)
      
      if (response.success && response.data) {
        searchResults.value = response.data.results
        return response.data.results
      }
      return []
    } catch (error) {
      console.error('AI search error:', error)
      ElMessage.error('AI搜索失败')
      return []
    } finally {
      isProcessing.value = false
    }
  }

  // AI总结
  const summarize = async (params: AISummarizeParams): Promise<string | null> => {
    try {
      isProcessing.value = true
      const response = await aiApi.summarize(params)
      
      if (response.success && response.data) {
        // 添加到历史记录
        aiHistory.value.unshift({
          id: Date.now().toString(),
          type: 'summarize' as any,
          result: response.data.summary,
          createdAt: new Date().toISOString()
        })
        
        ElMessage.success('文档总结完成')
        return response.data.summary
      }
      return null
    } catch (error) {
      console.error('AI summarize error:', error)
      ElMessage.error('AI总结失败')
      return null
    } finally {
      isProcessing.value = false
    }
  }

  // AI翻译
  const translate = async (params: AITranslateParams): Promise<string | null> => {
    try {
      isProcessing.value = true
      const response = await aiApi.translate(params)
      
      if (response.success && response.data) {
        // 添加到历史记录
        aiHistory.value.unshift({
          id: Date.now().toString(),
          type: 'translate' as any,
          result: response.data.translatedContent,
          createdAt: new Date().toISOString()
        })
        
        ElMessage.success('翻译完成')
        return response.data.translatedContent
      }
      return null
    } catch (error) {
      console.error('AI translate error:', error)
      ElMessage.error('AI翻译失败')
      return null
    } finally {
      isProcessing.value = false
    }
  }

  // AI检查
  const check = async (params: AICheckParams): Promise<any | null> => {
    try {
      isProcessing.value = true
      const response = await aiApi.check(params)
      
      if (response.success && response.data) {
        ElMessage.success(`检查完成，发现 ${response.data.issues.length} 个问题`)
        return response.data
      }
      return null
    } catch (error) {
      console.error('AI check error:', error)
      ElMessage.error('AI检查失败')
      return null
    } finally {
      isProcessing.value = false
    }
  }

  // AI问答
  const question = async (params: AIQuestionParams): Promise<any | null> => {
    try {
      isProcessing.value = true
      const response = await aiApi.question(params)
      
      if (response.success && response.data) {
        const { answer, conversationId } = response.data
        
        // 更新对话记录
        if (!conversations.value.has(conversationId)) {
          conversations.value.set(conversationId, [])
        }
        
        const conversation = conversations.value.get(conversationId)!
        conversation.push(
          {
            id: Date.now().toString(),
            role: 'user',
            content: params.question,
            timestamp: new Date().toISOString()
          },
          {
            id: (Date.now() + 1).toString(),
            role: 'assistant',
            content: answer,
            timestamp: new Date().toISOString(),
            sources: response.data.sources
          }
        )
        
        currentConversationId.value = conversationId
        
        return response.data
      }
      return null
    } catch (error) {
      console.error('AI question error:', error)
      ElMessage.error('AI问答失败')
      return null
    } finally {
      isProcessing.value = false
    }
  }

  // AI写作助手
  const writing = async (params: AIWritingParams): Promise<string | null> => {
    try {
      isProcessing.value = true
      const response = await aiApi.writing(params)
      
      if (response.success && response.data) {
        // 添加到历史记录
        aiHistory.value.unshift({
          id: Date.now().toString(),
          type: 'writing' as any,
          result: response.data.content,
          createdAt: new Date().toISOString()
        })
        
        ElMessage.success('AI写作完成')
        return response.data.content
      }
      return null
    } catch (error) {
      console.error('AI writing error:', error)
      ElMessage.error('AI写作失败')
      return null
    } finally {
      isProcessing.value = false
    }
  }

  // AI优化
  const optimize = async (params: AIOptimizeParams): Promise<any | null> => {
    try {
      isProcessing.value = true
      const response = await aiApi.optimize(params)
      
      if (response.success && response.data) {
        ElMessage.success('内容优化完成')
        return response.data
      }
      return null
    } catch (error) {
      console.error('AI optimize error:', error)
      ElMessage.error('AI优化失败')
      return null
    } finally {
      isProcessing.value = false
    }
  }

  // 提取关键词
  const extractKeywords = async (content: string, limit = 10): Promise<any[]> => {
    try {
      isProcessing.value = true
      const response = await aiApi.extractKeywords(content, limit)
      
      if (response.success && response.data) {
        return response.data.keywords
      }
      return []
    } catch (error) {
      console.error('Extract keywords error:', error)
      ElMessage.error('关键词提取失败')
      return []
    } finally {
      isProcessing.value = false
    }
  }

  // 内容分类
  const classify = async (content: string): Promise<any | null> => {
    try {
      isProcessing.value = true
      const response = await aiApi.classify(content)
      
      if (response.success && response.data) {
        return response.data
      }
      return null
    } catch (error) {
      console.error('Classify error:', error)
      ElMessage.error('内容分类失败')
      return null
    } finally {
      isProcessing.value = false
    }
  }

  // 获取推荐文档
  const recommend = async (documentId: string, limit = 5): Promise<any[]> => {
    try {
      const response = await aiApi.recommend(documentId, limit)
      
      if (response.success && response.data) {
        return response.data.documents
      }
      return []
    } catch (error) {
      console.error('Recommend error:', error)
      return []
    }
  }

  // 获取使用统计
  const fetchUsageStats = async (): Promise<void> => {
    try {
      const response = await aiApi.getUsageStats()
      
      if (response.success && response.data) {
        usageStats.value = response.data
      }
    } catch (error) {
      console.error('Fetch usage stats error:', error)
    }
  }

  // 流式生成内容
  const generateStream = async (params: {
    prompt: string
    type: 'completion' | 'chat'
    options?: Record<string, any>
  }): Promise<void> => {
    try {
      isStreaming.value = true
      streamingResponse.value = ''
      
      const stream = await aiApi.generateStream(params)
      const reader = stream.getReader()
      const decoder = new TextDecoder()
      
      while (true) {
        const { done, value } = await reader.read()
        
        if (done) break
        
        const chunk = decoder.decode(value)
        const lines = chunk.split('\n')
        
        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.slice(6)
            if (data === '[DONE]') {
              isStreaming.value = false
              return
            }
            
            try {
              const parsed = JSON.parse(data)
              if (parsed.content) {
                streamingResponse.value += parsed.content
              }
            } catch (e) {
              // 忽略解析错误
            }
          }
        }
      }
    } catch (error) {
      console.error('Stream generation error:', error)
      ElMessage.error('流式生成失败')
    } finally {
      isStreaming.value = false
    }
  }

  // 创建新对话
  const createConversation = (): string => {
    const conversationId = `conv_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    conversations.value.set(conversationId, [])
    currentConversationId.value = conversationId
    return conversationId
  }

  // 切换对话
  const switchConversation = (conversationId: string): void => {
    if (conversations.value.has(conversationId)) {
      currentConversationId.value = conversationId
    }
  }

  // 删除对话
  const deleteConversation = async (conversationId: string): Promise<void> => {
    try {
      await aiApi.deleteConversation(conversationId)
      conversations.value.delete(conversationId)
      
      if (currentConversationId.value === conversationId) {
        currentConversationId.value = null
      }
      
      ElMessage.success('对话已删除')
    } catch (error) {
      console.error('Delete conversation error:', error)
      ElMessage.error('删除对话失败')
    }
  }

  // 清空搜索结果
  const clearSearchResults = (): void => {
    searchResults.value = []
  }

  // 清空历史记录
  const clearHistory = (): void => {
    aiHistory.value = []
  }

  // 清空流式响应
  const clearStreamingResponse = (): void => {
    streamingResponse.value = ''
  }

  // 提交反馈
  const submitFeedback = async (params: {
    feature: string
    rating: number
    comment?: string
    requestId?: string
  }): Promise<boolean> => {
    try {
      const response = await aiApi.submitFeedback(params)
      
      if (response.success) {
        ElMessage.success('反馈提交成功')
        return true
      }
      return false
    } catch (error) {
      console.error('Submit feedback error:', error)
      ElMessage.error('反馈提交失败')
      return false
    }
  }

  return {
    // 状态
    isProcessing,
    searchResults,
    conversations,
    currentConversationId,
    usageStats,
    aiHistory,
    streamingResponse,
    isStreaming,
    
    // 计算属性
    hasQuotaRemaining,
    quotaUsagePercentage,
    currentConversation,
    
    // 方法
    search,
    summarize,
    translate,
    check,
    question,
    writing,
    optimize,
    extractKeywords,
    classify,
    recommend,
    fetchUsageStats,
    generateStream,
    createConversation,
    switchConversation,
    deleteConversation,
    clearSearchResults,
    clearHistory,
    clearStreamingResponse,
    submitFeedback
  }
})