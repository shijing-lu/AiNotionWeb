import { request } from './index'

/**
 * 文章接口
 */
export interface Article {
  id: string
  title: string
  summary?: string
  content: string
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  visibility: 'PUBLIC' | 'PRIVATE' | 'PROTECTED'
  tags: string[]
  category?: string
  isPinned: boolean
  allowComments: boolean
  isFavorited: boolean
  viewCount: number
  wordCount: number
  readingTime: number
  authorId: string
  authorName: string
  createdAt: string
  updatedAt: string
  publishedAt?: string
}

/**
 * 文章摘要接口
 */
export interface ArticleSummary {
  id: string
  title: string
  summary?: string
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  visibility: 'PUBLIC' | 'PRIVATE' | 'PROTECTED'
  tags: string[]
  category?: string
  isPinned: boolean
  isFavorited: boolean
  viewCount: number
  wordCount: number
  readingTime: number
  authorId: string
  authorName: string
  createdAt: string
  updatedAt: string
  publishedAt?: string
}

/**
 * 创建文章请求
 */
export interface CreateArticleRequest {
  title: string
  summary?: string
  content: string
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  visibility?: 'PUBLIC' | 'PRIVATE' | 'PROTECTED'
  tags?: string[]
  category?: string
  isPinned?: boolean
  allowComments?: boolean
}

/**
 * 更新文章请求
 */
export interface UpdateArticleRequest {
  title?: string
  summary?: string
  content?: string
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  visibility?: 'PUBLIC' | 'PRIVATE' | 'PROTECTED'
  tags?: string[]
  category?: string
  isPinned?: boolean
  allowComments?: boolean
}

/**
 * 文章搜索请求
 */
export interface ArticleSearchRequest {
  keyword?: string
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  visibility?: 'PUBLIC' | 'PRIVATE' | 'PROTECTED'
  tags?: string[]
  category?: string
  authorId?: string
  isPinned?: boolean
  isFavorited?: boolean
  startDate?: string
  endDate?: string
  page?: number
  size?: number
  sortBy?: 'createdAt' | 'updatedAt' | 'publishedAt' | 'viewCount' | 'title'
  sortOrder?: 'asc' | 'desc'
}

/**
 * 文章统计接口
 */
export interface ArticleStats {
  totalCount: number
  publishedCount: number
  draftCount: number
  archivedCount: number
  totalViews: number
  totalWords: number
  favoriteCount: number
  pinnedCount: number
}

/**
 * 分页响应接口
 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

/**
 * API响应接口
 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

/**
 * 文章API类
 */
class ArticleApi {
  /**
   * 获取文章列表
   */
  async getArticles(params?: ArticleSearchRequest): Promise<ApiResponse<PageResponse<ArticleSummary>>> {
    return request.get('/api/articles', { params })
  }

  /**
   * 获取文章详情
   */
  async getArticle(id: string): Promise<ApiResponse<Article>> {
    return request.get(`/api/articles/${id}`)
  }

  /**
   * 创建文章
   */
  async createArticle(data: CreateArticleRequest): Promise<ApiResponse<Article>> {
    return request.post('/api/articles', data)
  }

  /**
   * 更新文章
   */
  async updateArticle(id: string, data: UpdateArticleRequest): Promise<ApiResponse<Article>> {
    return request.put(`/api/articles/${id}`, data)
  }

  /**
   * 删除文章
   */
  async deleteArticle(id: string): Promise<ApiResponse<void>> {
    return request.delete(`/api/articles/${id}`)
  }

  /**
   * 批量删除文章
   */
  async deleteArticles(ids: string[]): Promise<ApiResponse<void>> {
    return request.delete('/api/articles/batch', { data: { ids } })
  }

  /**
   * 发布文章
   */
  async publishArticle(id: string): Promise<ApiResponse<Article>> {
    return request.post(`/api/articles/${id}/publish`)
  }

  /**
   * 取消发布文章
   */
  async unpublishArticle(id: string): Promise<ApiResponse<Article>> {
    return request.post(`/api/articles/${id}/unpublish`)
  }

  /**
   * 归档文章
   */
  async archiveArticle(id: string): Promise<ApiResponse<Article>> {
    return request.post(`/api/articles/${id}/archive`)
  }

  /**
   * 取消归档文章
   */
  async unarchiveArticle(id: string): Promise<ApiResponse<Article>> {
    return request.post(`/api/articles/${id}/unarchive`)
  }

  /**
   * 置顶文章
   */
  async pinArticle(id: string): Promise<ApiResponse<Article>> {
    return request.post(`/api/articles/${id}/pin`)
  }

  /**
   * 取消置顶文章
   */
  async unpinArticle(id: string): Promise<ApiResponse<Article>> {
    return request.post(`/api/articles/${id}/unpin`)
  }

  /**
   * 收藏文章
   */
  async favoriteArticle(id: string): Promise<ApiResponse<Article>> {
    return request.post(`/api/articles/${id}/favorite`)
  }

  /**
   * 取消收藏文章
   */
  async unfavoriteArticle(id: string): Promise<ApiResponse<Article>> {
    return request.post(`/api/articles/${id}/unfavorite`)
  }

  /**
   * 复制文章
   */
  async duplicateArticle(id: string): Promise<ApiResponse<Article>> {
    return request.post(`/api/articles/${id}/duplicate`)
  }

  /**
   * 获取按状态分组的文章
   */
  async getArticlesByStatus(status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED', params?: Omit<ArticleSearchRequest, 'status'>): Promise<ApiResponse<PageResponse<ArticleSummary>>> {
    return request.get(`/api/articles/status/${status}`, { params })
  }

  /**
   * 获取收藏的文章
   */
  async getFavoriteArticles(params?: Omit<ArticleSearchRequest, 'isFavorited'>): Promise<ApiResponse<PageResponse<ArticleSummary>>> {
    return request.get('/api/articles/favorites', { params })
  }

  /**
   * 搜索文章
   */
  async searchArticles(params: ArticleSearchRequest): Promise<ApiResponse<PageResponse<ArticleSummary>>> {
    return request.get('/api/articles/search', { params })
  }

  /**
   * 获取文章统计
   */
  async getArticleStats(): Promise<ApiResponse<ArticleStats>> {
    return request.get('/api/articles/stats')
  }

  /**
   * 获取所有标签
   */
  async getTags(): Promise<ApiResponse<string[]>> {
    return request.get('/api/articles/tags')
  }

  /**
   * 获取所有分类
   */
  async getCategories(): Promise<ApiResponse<string[]>> {
    return request.get('/api/articles/categories')
  }

  /**
   * 获取热门标签
   */
  async getPopularTags(limit?: number): Promise<ApiResponse<Array<{ tag: string; count: number }>>> {
    return request.get('/api/articles/tags/popular', { params: { limit } })
  }

  /**
   * 获取热门分类
   */
  async getPopularCategories(limit?: number): Promise<ApiResponse<Array<{ category: string; count: number }>>> {
    return request.get('/api/articles/categories/popular', { params: { limit } })
  }

  /**
   * 导出文章
   */
  async exportArticle(id: string, format: 'markdown' | 'html' | 'pdf'): Promise<Blob> {
    const response = await request.get(`/api/articles/${id}/export`, {
      params: { format },
      responseType: 'blob'
    })
    return response.data
  }

  /**
   * 批量导出文章
   */
  async exportArticles(ids: string[], format: 'markdown' | 'html' | 'pdf'): Promise<Blob> {
    const response = await request.post('/api/articles/export/batch', 
      { ids, format },
      { responseType: 'blob' }
    )
    return response.data
  }

  /**
   * 获取相关文章
   */
  async getRelatedArticles(id: string, limit?: number): Promise<ApiResponse<ArticleSummary[]>> {
    return request.get(`/api/articles/${id}/related`, { params: { limit } })
  }

  /**
   * 获取文章历史版本
   */
  async getArticleVersions(id: string): Promise<ApiResponse<Array<{ version: number; createdAt: string; summary: string }>>> {
    return request.get(`/api/articles/${id}/versions`)
  }

  /**
   * 恢复文章版本
   */
  async restoreArticleVersion(id: string, version: number): Promise<ApiResponse<Article>> {
    return request.post(`/api/articles/${id}/versions/${version}/restore`)
  }

  /**
   * 获取文章评论
   */
  async getArticleComments(id: string, params?: { page?: number; size?: number }): Promise<ApiResponse<PageResponse<any>>> {
    return request.get(`/api/articles/${id}/comments`, { params })
  }

  /**
   * 添加文章评论
   */
  async addArticleComment(id: string, content: string, parentId?: string): Promise<ApiResponse<any>> {
    return request.post(`/api/articles/${id}/comments`, { content, parentId })
  }

  /**
   * 删除文章评论
   */
  async deleteArticleComment(articleId: string, commentId: string): Promise<ApiResponse<void>> {
    return request.delete(`/api/articles/${articleId}/comments/${commentId}`)
  }

  /**
   * 增加文章浏览量
   */
  async incrementViewCount(id: string): Promise<ApiResponse<void>> {
    return request.post(`/api/articles/${id}/view`)
  }

  /**
   * 检查文章标题是否重复
   */
  async checkTitleExists(title: string, excludeId?: string): Promise<ApiResponse<boolean>> {
    return request.get('/api/articles/check-title', { params: { title, excludeId } })
  }

  /**
   * 获取文章草稿自动保存
   */
  async getArticleDraft(id: string): Promise<ApiResponse<{ content: string; savedAt: string }>> {
    return request.get(`/api/articles/${id}/draft`)
  }

  /**
   * 保存文章草稿
   */
  async saveArticleDraft(id: string, content: string): Promise<ApiResponse<void>> {
    return request.post(`/api/articles/${id}/draft`, { content })
  }

  /**
   * 清除文章草稿
   */
  async clearArticleDraft(id: string): Promise<ApiResponse<void>> {
    return request.delete(`/api/articles/${id}/draft`)
  }
}

// 导出文章API实例
export const articleApi = new ArticleApi()

// 导出所有接口类型
export type {
  Article,
  ArticleSummary,
  CreateArticleRequest,
  UpdateArticleRequest,
  ArticleSearchRequest,
  ArticleStats,
  PageResponse,
  ApiResponse
}