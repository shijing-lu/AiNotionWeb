/**
 * AI功能相关API
 */

import { request } from '../index'
import type { AIRequest, AIResponse, ApiResponse } from '@/types'

// AI搜索参数
export interface AISearchParams {
  query: string
  documentIds?: string[]
  folderId?: string
  limit?: number
  includeContent?: boolean
}

// AI总结参数
export interface AISummarizeParams {
  documentId?: string
  content?: string
  summaryType: 'brief' | 'detailed' | 'bullet-points'
  language?: 'zh-CN' | 'en-US'
}

// AI翻译参数
export interface AITranslateParams {
  content: string
  fromLang: string
  toLang: string
  documentId?: string
}

// AI检查参数
export interface AICheckParams {
  content: string
  checkType: 'grammar' | 'spelling' | 'style' | 'all'
  language?: 'zh-CN' | 'en-US'
}

// AI问答参数
export interface AIQuestionParams {
  question: string
  documentIds?: string[]
  context?: string
  conversationId?: string
}

// AI写作助手参数
export interface AIWritingParams {
  prompt: string
  style?: 'formal' | 'casual' | 'academic' | 'creative'
  length?: 'short' | 'medium' | 'long'
  language?: 'zh-CN' | 'en-US'
}

// AI优化参数
export interface AIOptimizeParams {
  content: string
  optimizeType: 'clarity' | 'conciseness' | 'engagement' | 'tone'
  targetAudience?: string
}

export const aiApi = {
  /**
   * AI智能搜索
   */
  search(params: AISearchParams): Promise<ApiResponse<{
    results: any[]
    suggestions: string[]
    totalCount: number
  }>> {
    return request.post('/ai/search', params)
  },

  /**
   * AI文档总结
   */
  summarize(params: AISummarizeParams): Promise<ApiResponse<{
    summary: string
    keyPoints: string[]
    wordCount: number
  }>> {
    return request.post('/ai/summarize', params)
  },

  /**
   * AI文档翻译
   */
  translate(params: AITranslateParams): Promise<ApiResponse<{
    translatedContent: string
    confidence: number
    detectedLanguage?: string
  }>> {
    return request.post('/ai/translate', params)
  },

  /**
   * AI文档检查
   */
  check(params: AICheckParams): Promise<ApiResponse<{
    issues: {
      type: 'grammar' | 'spelling' | 'style'
      message: string
      suggestion: string
      position: { start: number; end: number }
      severity: 'low' | 'medium' | 'high'
    }[]
    score: number
    suggestions: string[]
  }>> {
    return request.post('/ai/check', params)
  },

  /**
   * AI问答
   */
  question(params: AIQuestionParams): Promise<ApiResponse<{
    answer: string
    sources: {
      documentId: string
      title: string
      snippet: string
      relevance: number
    }[]
    confidence: number
    conversationId: string
  }>> {
    return request.post('/ai/question', params)
  },

  /**
   * AI写作助手
   */
  writing(params: AIWritingParams): Promise<ApiResponse<{
    content: string
    alternatives: string[]
    wordCount: number
  }>> {
    return request.post('/ai/writing', params)
  },

  /**
   * AI内容优化
   */
  optimize(params: AIOptimizeParams): Promise<ApiResponse<{
    optimizedContent: string
    improvements: {
      type: string
      description: string
      before: string
      after: string
    }[]
    score: number
  }>> {
    return request.post('/ai/optimize', params)
  },

  /**
   * AI关键词提取
   */
  extractKeywords(content: string, limit = 10): Promise<ApiResponse<{
    keywords: {
      word: string
      score: number
      frequency: number
    }[]
  }>> {
    return request.post('/ai/keywords', { content, limit })
  },

  /**
   * AI内容分类
   */
  classify(content: string): Promise<ApiResponse<{
    categories: {
      name: string
      confidence: number
    }[]
    tags: string[]
  }>> {
    return request.post('/ai/classify', { content })
  },

  /**
   * AI相似文档推荐
   */
  recommend(documentId: string, limit = 5): Promise<ApiResponse<{
    documents: {
      id: string
      title: string
      similarity: number
      snippet: string
    }[]
  }>> {
    return request.get(`/ai/recommend/${documentId}?limit=${limit}`)
  },

  /**
   * AI对话历史
   */
  getConversationHistory(conversationId: string): Promise<ApiResponse<{
    messages: {
      id: string
      role: 'user' | 'assistant'
      content: string
      timestamp: string
    }[]
  }>> {
    return request.get(`/ai/conversations/${conversationId}`)
  },

  /**
   * 删除AI对话
   */
  deleteConversation(conversationId: string): Promise<ApiResponse> {
    return request.delete(`/ai/conversations/${conversationId}`)
  },

  /**
   * 获取AI使用统计
   */
  getUsageStats(): Promise<ApiResponse<{
    totalRequests: number
    requestsToday: number
    remainingQuota: number
    quotaLimit: number
    resetDate: string
    popularFeatures: {
      feature: string
      count: number
    }[]
  }>> {
    return request.get('/ai/usage')
  },

  /**
   * AI功能反馈
   */
  submitFeedback(params: {
    feature: string
    rating: number
    comment?: string
    requestId?: string
  }): Promise<ApiResponse> {
    return request.post('/ai/feedback', params)
  },

  /**
   * 获取AI模型信息
   */
  getModelInfo(): Promise<ApiResponse<{
    models: {
      name: string
      version: string
      capabilities: string[]
      languages: string[]
    }[]
    currentModel: string
  }>> {
    return request.get('/ai/models')
  },

  /**
   * AI内容生成（流式）
   */
  generateStream(params: {
    prompt: string
    type: 'completion' | 'chat'
    options?: Record<string, any>
  }): Promise<ReadableStream> {
    return fetch(`${process.env.VITE_API_BASE_URL}/ai/generate/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: JSON.stringify(params)
    }).then(response => {
      if (!response.ok) {
        throw new Error('Stream request failed')
      }
      return response.body!
    })
  }
}