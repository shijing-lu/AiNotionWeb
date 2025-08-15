/**
 * 文件夹相关API
 */

import { request } from '../index'
import type { Folder, ApiResponse } from '@/types'

// 创建文件夹参数
export interface CreateFolderParams {
  name: string
  parentId?: string
  description?: string
}

// 更新文件夹参数
export interface UpdateFolderParams {
  name?: string
  description?: string
  parentId?: string
}

export const folderApi = {
  /**
   * 获取文件夹树结构
   */
  getFolderTree(): Promise<ApiResponse<Folder[]>> {
    return request.get('/folders/tree')
  },

  /**
   * 获取单个文件夹详情
   */
  getFolder(id: string): Promise<ApiResponse<Folder>> {
    return request.get(`/folders/${id}`)
  },

  /**
   * 获取文件夹内容（子文件夹和文档）
   */
  getFolderContents(id: string): Promise<ApiResponse<{
    folders: Folder[]
    documents: any[]
  }>> {
    return request.get(`/folders/${id}/contents`)
  },

  /**
   * 创建文件夹
   */
  createFolder(params: CreateFolderParams): Promise<ApiResponse<Folder>> {
    return request.post('/folders', params)
  },

  /**
   * 更新文件夹
   */
  updateFolder(id: string, params: UpdateFolderParams): Promise<ApiResponse<Folder>> {
    return request.put(`/folders/${id}`, params)
  },

  /**
   * 删除文件夹
   */
  deleteFolder(id: string, force = false): Promise<ApiResponse> {
    return request.delete(`/folders/${id}?force=${force}`)
  },

  /**
   * 移动文件夹
   */
  moveFolder(id: string, parentId: string | null): Promise<ApiResponse> {
    return request.patch(`/folders/${id}/move`, { parentId })
  },

  /**
   * 复制文件夹
   */
  duplicateFolder(id: string, name?: string, parentId?: string): Promise<ApiResponse<Folder>> {
    return request.post(`/folders/${id}/duplicate`, { name, parentId })
  },

  /**
   * 获取文件夹路径（面包屑导航）
   */
  getFolderPath(id: string): Promise<ApiResponse<Folder[]>> {
    return request.get(`/folders/${id}/path`)
  },

  /**
   * 搜索文件夹
   */
  searchFolders(query: string): Promise<ApiResponse<Folder[]>> {
    return request.get(`/folders/search?q=${encodeURIComponent(query)}`)
  },

  /**
   * 获取根文件夹
   */
  getRootFolders(): Promise<ApiResponse<Folder[]>> {
    return request.get('/folders/root')
  },

  /**
   * 批量删除文件夹
   */
  batchDeleteFolders(ids: string[], force = false): Promise<ApiResponse> {
    return request.delete('/folders/batch', { 
      data: { ids, force } 
    })
  },

  /**
   * 获取文件夹统计信息
   */
  getFolderStats(id: string): Promise<ApiResponse<{
    totalDocuments: number
    totalSubfolders: number
    totalSize: number
    lastModified: string
  }>> {
    return request.get(`/folders/${id}/stats`)
  },

  /**
   * 设置文件夹权限
   */
  setFolderPermission(id: string, userId: string, permission: 'read' | 'write' | 'admin'): Promise<ApiResponse> {
    return request.patch(`/folders/${id}/permissions`, { userId, permission })
  },

  /**
   * 获取文件夹权限列表
   */
  getFolderPermissions(id: string): Promise<ApiResponse<any[]>> {
    return request.get(`/folders/${id}/permissions`)
  },

  /**
   * 分享文件夹
   */
  shareFolder(id: string, params: {
    emails: string[]
    permission: 'read' | 'write'
    message?: string
    expiresAt?: string
  }): Promise<ApiResponse> {
    return request.post(`/folders/${id}/share`, params)
  },

  /**
   * 取消分享文件夹
   */
  unshareFolder(id: string, userId: string): Promise<ApiResponse> {
    return request.delete(`/folders/${id}/share/${userId}`)
  },

  /**
   * 获取文件夹分享信息
   */
  getFolderShares(id: string): Promise<ApiResponse<any[]>> {
    return request.get(`/folders/${id}/shares`)
  },

  /**
   * 导出文件夹（包含所有子文档）
   */
  exportFolder(id: string, format: 'zip' | 'pdf'): Promise<Blob> {
    return request.get(`/folders/${id}/export?format=${format}`, {
      responseType: 'blob'
    }).then(response => response.data)
  },

  /**
   * 检查文件夹名称是否可用
   */
  checkFolderNameAvailable(name: string, parentId?: string): Promise<ApiResponse<{ available: boolean }>> {
    const params = new URLSearchParams({ name })
    if (parentId) {
      params.append('parentId', parentId)
    }
    return request.get(`/folders/check-name?${params.toString()}`)
  }
}