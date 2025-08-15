/**
 * 文档相关API
 */

import { request } from '../index'
import type { Document, Folder, ApiResponse, PaginatedResponse, SearchQuery, SearchResult } from '@/types'

// 创建文档参数
export interface CreateDocumentParams {
  title: string
  content?: string
  folderId?: string
  tags?: string[]
  isPublic?: boolean
}

// 更新文档参数
export interface UpdateDocumentParams {
  title?: string
  content?: string
  folderId?: string
  tags?: string[]
  isPublic?: boolean
}

// 文档列表查询参数
export interface DocumentListParams {
  page?: number
  pageSize?: number
  folderId?: string
  tags?: string[]
  search?: string
  sortBy?: 'createdAt' | 'updatedAt' | 'title'
  sortOrder?: 'asc' | 'desc'
}

// 文档分享参数
export interface ShareDocumentParams {
  emails: string[]
  permission: 'read' | 'write'
  message?: string
  expiresAt?: string
}

export const documentApi = {
  /**
   * 获取文档列表
   */
  getDocuments(params?: DocumentListParams): Promise<ApiResponse<PaginatedResponse<Document>>> {
    return request.get('/documents', { params })
  },

  /**
   * 获取单个文档详情
   */
  getDocument(id: string): Promise<ApiResponse<Document>> {
    return request.get(`/documents/${id}`)
  },

  /**
   * 创建文档
   */
  createDocument(params: CreateDocumentParams): Promise<ApiResponse<Document>> {
    return request.post('/documents', params)
  },

  /**
   * 更新文档
   */
  updateDocument(id: string, params: UpdateDocumentParams): Promise<ApiResponse<Document>> {
    return request.put(`/documents/${id}`, params)
  },

  /**
   * 删除文档
   */
  deleteDocument(id: string): Promise<ApiResponse> {
    return request.delete(`/documents/${id}`)
  },

  /**
   * 批量删除文档
   */
  batchDeleteDocuments(ids: string[]): Promise<ApiResponse> {
    return request.delete('/documents/batch', { data: { ids } })
  },

  /**
   * 复制文档
   */
  duplicateDocument(id: string, title?: string): Promise<ApiResponse<Document>> {
    return request.post(`/documents/${id}/duplicate`, { title })
  },

  /**
   * 移动文档到文件夹
   */
  moveDocument(id: string, folderId: string | null): Promise<ApiResponse> {
    return request.patch(`/documents/${id}/move`, { folderId })
  },

  /**
   * 分享文档
   */
  shareDocument(id: string, params: ShareDocumentParams): Promise<ApiResponse> {
    return request.post(`/documents/${id}/share`, params)
  },

  /**
   * 取消分享文档
   */
  unshareDocument(id: string, userId: string): Promise<ApiResponse> {
    return request.delete(`/documents/${id}/share/${userId}`)
  },

  /**
   * 获取文档分享信息
   */
  getDocumentShares(id: string): Promise<ApiResponse<any[]>> {
    return request.get(`/documents/${id}/shares`)
  },

  /**
   * 搜索文档
   */
  searchDocuments(query: SearchQuery): Promise<ApiResponse<SearchResult[]>> {
    return request.post('/documents/search', query)
  },

  /**
   * 获取文档历史版本
   */
  getDocumentVersions(id: string): Promise<ApiResponse<any[]>> {
    return request.get(`/documents/${id}/versions`)
  },

  /**
   * 恢复文档版本
   */
  restoreDocumentVersion(id: string, versionId: string): Promise<ApiResponse<Document>> {
    return request.post(`/documents/${id}/versions/${versionId}/restore`)
  },

  /**
   * 导出文档
   */
  exportDocument(id: string, format: 'markdown' | 'pdf' | 'docx' | 'html'): Promise<Blob> {
    return request.get(`/documents/${id}/export?format=${format}`, {
      responseType: 'blob'
    }).then(response => response.data)
  },

  /**
   * 导入文档
   */
  importDocument(file: File, folderId?: string): Promise<ApiResponse<Document>> {
    return request.upload('/documents/import', file).then(response => {
      if (folderId) {
        return request.patch(`/documents/${response.data.id}/move`, { folderId })
      }
      return response
    })
  },

  /**
   * 获取最近访问的文档
   */
  getRecentDocuments(limit = 10): Promise<ApiResponse<Document[]>> {
    return request.get(`/documents/recent?limit=${limit}`)
  },

  /**
   * 获取收藏的文档
   */
  getFavoriteDocuments(): Promise<ApiResponse<Document[]>> {
    return request.get('/documents/favorites')
  },

  /**
   * 添加文档到收藏
   */
  addToFavorites(id: string): Promise<ApiResponse> {
    return request.post(`/documents/${id}/favorite`)
  },

  /**
   * 从收藏中移除文档
   */
  removeFromFavorites(id: string): Promise<ApiResponse> {
    return request.delete(`/documents/${id}/favorite`)
  },

  /**
   * 获取文档统计信息
   */
  getDocumentStats(id: string): Promise<ApiResponse<{
    wordCount: number
    characterCount: number
    readingTime: number
    lastModified: string
    collaborators: number
  }>> {
    return request.get(`/documents/${id}/stats`)
  },

  /**
   * 设置文档权限
   */
  setDocumentPermission(id: string, userId: string, permission: 'read' | 'write' | 'admin'): Promise<ApiResponse> {
    return request.patch(`/documents/${id}/permissions`, { userId, permission })
  },

  /**
   * 获取文档权限列表
   */
  getDocumentPermissions(id: string): Promise<ApiResponse<any[]>> {
    return request.get(`/documents/${id}/permissions`)
  }
}