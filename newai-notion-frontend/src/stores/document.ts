/**
 * 文档状态管理
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { documentApi } from '@/api/modules'
import type { Document, SearchResult, PaginatedResponse } from '@/types'
import type { CreateDocumentParams, UpdateDocumentParams, DocumentListParams } from '@/api/modules'

export const useDocumentStore = defineStore('document', () => {
  // 状态
  const documents = ref<Document[]>([])
  const currentDocument = ref<Document | null>(null)
  const recentDocuments = ref<Document[]>([])
  const favoriteDocuments = ref<Document[]>([])
  const searchResults = ref<SearchResult[]>([])
  const isLoading = ref(false)
  const isSaving = ref(false)
  const pagination = ref({
    page: 1,
    pageSize: 20,
    total: 0,
    hasNext: false,
    hasPrev: false
  })

  // 编辑器状态
  const editorContent = ref('')
  const hasUnsavedChanges = ref(false)
  const lastSavedAt = ref<Date | null>(null)
  const autoSaveTimer = ref<NodeJS.Timeout | null>(null)

  // 计算属性
  const documentCount = computed(() => documents.value.length)
  const hasDocuments = computed(() => documentCount.value > 0)
  const currentDocumentId = computed(() => currentDocument.value?.id)
  const isCurrentDocumentFavorite = computed(() => {
    if (!currentDocument.value) return false
    return favoriteDocuments.value.some(doc => doc.id === currentDocument.value!.id)
  })

  // 获取文档列表
  const fetchDocuments = async (params?: DocumentListParams): Promise<void> => {
    try {
      isLoading.value = true
      const response = await documentApi.getDocuments(params)
      
      if (response.success && response.data) {
        documents.value = response.data.items
        pagination.value = {
          page: response.data.page,
          pageSize: response.data.pageSize,
          total: response.data.total,
          hasNext: response.data.hasNext,
          hasPrev: response.data.hasPrev
        }
      }
    } catch (error) {
      console.error('Fetch documents error:', error)
      ElMessage.error('获取文档列表失败')
    } finally {
      isLoading.value = false
    }
  }

  // 获取单个文档
  const fetchDocument = async (id: string): Promise<Document | null> => {
    try {
      isLoading.value = true
      const response = await documentApi.getDocument(id)
      
      if (response.success && response.data) {
        currentDocument.value = response.data
        editorContent.value = response.data.content
        hasUnsavedChanges.value = false
        return response.data
      }
      return null
    } catch (error) {
      console.error('Fetch document error:', error)
      ElMessage.error('获取文档失败')
      return null
    } finally {
      isLoading.value = false
    }
  }

  // 创建文档
  const createDocument = async (params: CreateDocumentParams): Promise<Document | null> => {
    try {
      isLoading.value = true
      const response = await documentApi.createDocument(params)
      
      if (response.success && response.data) {
        documents.value.unshift(response.data)
        currentDocument.value = response.data
        editorContent.value = response.data.content
        hasUnsavedChanges.value = false
        ElMessage.success('文档创建成功')
        return response.data
      }
      return null
    } catch (error) {
      console.error('Create document error:', error)
      ElMessage.error('创建文档失败')
      return null
    } finally {
      isLoading.value = false
    }
  }

  // 更新文档
  const updateDocument = async (id: string, params: UpdateDocumentParams): Promise<boolean> => {
    try {
      isSaving.value = true
      const response = await documentApi.updateDocument(id, params)
      
      if (response.success && response.data) {
        // 更新本地状态
        const index = documents.value.findIndex(doc => doc.id === id)
        if (index !== -1) {
          documents.value[index] = response.data
        }
        
        if (currentDocument.value?.id === id) {
          currentDocument.value = response.data
        }
        
        hasUnsavedChanges.value = false
        lastSavedAt.value = new Date()
        return true
      }
      return false
    } catch (error) {
      console.error('Update document error:', error)
      ElMessage.error('保存文档失败')
      return false
    } finally {
      isSaving.value = false
    }
  }

  // 删除文档
  const deleteDocument = async (id: string): Promise<boolean> => {
    try {
      isLoading.value = true
      const response = await documentApi.deleteDocument(id)
      
      if (response.success) {
        // 从列表中移除
        documents.value = documents.value.filter(doc => doc.id !== id)
        recentDocuments.value = recentDocuments.value.filter(doc => doc.id !== id)
        favoriteDocuments.value = favoriteDocuments.value.filter(doc => doc.id !== id)
        
        // 如果删除的是当前文档，清空当前文档
        if (currentDocument.value?.id === id) {
          currentDocument.value = null
          editorContent.value = ''
          hasUnsavedChanges.value = false
        }
        
        ElMessage.success('文档删除成功')
        return true
      }
      return false
    } catch (error) {
      console.error('Delete document error:', error)
      ElMessage.error('删除文档失败')
      return false
    } finally {
      isLoading.value = false
    }
  }

  // 搜索文档
  const searchDocuments = async (query: string): Promise<void> => {
    try {
      isLoading.value = true
      const response = await documentApi.searchDocuments({
        query,
        limit: 20
      })
      
      if (response.success && response.data) {
        searchResults.value = response.data
      }
    } catch (error) {
      console.error('Search documents error:', error)
      ElMessage.error('搜索文档失败')
    } finally {
      isLoading.value = false
    }
  }

  // 获取最近文档
  const fetchRecentDocuments = async (): Promise<void> => {
    try {
      const response = await documentApi.getRecentDocuments(10)
      
      if (response.success && response.data) {
        recentDocuments.value = response.data
      }
    } catch (error) {
      console.error('Fetch recent documents error:', error)
    }
  }

  // 获取收藏文档
  const fetchFavoriteDocuments = async (): Promise<void> => {
    try {
      const response = await documentApi.getFavoriteDocuments()
      
      if (response.success && response.data) {
        favoriteDocuments.value = response.data
      }
    } catch (error) {
      console.error('Fetch favorite documents error:', error)
    }
  }

  // 添加到收藏
  const addToFavorites = async (id: string): Promise<boolean> => {
    try {
      const response = await documentApi.addToFavorites(id)
      
      if (response.success) {
        // 重新获取收藏列表
        await fetchFavoriteDocuments()
        ElMessage.success('已添加到收藏')
        return true
      }
      return false
    } catch (error) {
      console.error('Add to favorites error:', error)
      ElMessage.error('添加收藏失败')
      return false
    }
  }

  // 从收藏中移除
  const removeFromFavorites = async (id: string): Promise<boolean> => {
    try {
      const response = await documentApi.removeFromFavorites(id)
      
      if (response.success) {
        favoriteDocuments.value = favoriteDocuments.value.filter(doc => doc.id !== id)
        ElMessage.success('已从收藏中移除')
        return true
      }
      return false
    } catch (error) {
      console.error('Remove from favorites error:', error)
      ElMessage.error('移除收藏失败')
      return false
    }
  }

  // 复制文档
  const duplicateDocument = async (id: string, title?: string): Promise<Document | null> => {
    try {
      isLoading.value = true
      const response = await documentApi.duplicateDocument(id, title)
      
      if (response.success && response.data) {
        documents.value.unshift(response.data)
        ElMessage.success('文档复制成功')
        return response.data
      }
      return null
    } catch (error) {
      console.error('Duplicate document error:', error)
      ElMessage.error('复制文档失败')
      return null
    } finally {
      isLoading.value = false
    }
  }

  // 设置编辑器内容
  const setEditorContent = (content: string): void => {
    editorContent.value = content
    hasUnsavedChanges.value = currentDocument.value?.content !== content
  }

  // 自动保存
  const startAutoSave = (): void => {
    if (autoSaveTimer.value) {
      clearInterval(autoSaveTimer.value)
    }
    
    autoSaveTimer.value = setInterval(async () => {
      if (hasUnsavedChanges.value && currentDocument.value) {
        await updateDocument(currentDocument.value.id, {
          content: editorContent.value
        })
      }
    }, 30000) // 30秒自动保存
  }

  // 停止自动保存
  const stopAutoSave = (): void => {
    if (autoSaveTimer.value) {
      clearInterval(autoSaveTimer.value)
      autoSaveTimer.value = null
    }
  }

  // 手动保存
  const saveDocument = async (): Promise<boolean> => {
    if (!currentDocument.value || !hasUnsavedChanges.value) {
      return true
    }
    
    return await updateDocument(currentDocument.value.id, {
      content: editorContent.value
    })
  }

  // 清空当前文档
  const clearCurrentDocument = (): void => {
    currentDocument.value = null
    editorContent.value = ''
    hasUnsavedChanges.value = false
    stopAutoSave()
  }

  // 导出文档
  const exportDocument = async (id: string, format: 'markdown' | 'pdf' | 'docx' | 'html'): Promise<void> => {
    try {
      isLoading.value = true
      const blob = await documentApi.exportDocument(id, format)
      
      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `document.${format}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('文档导出成功')
    } catch (error) {
      console.error('Export document error:', error)
      ElMessage.error('导出文档失败')
    } finally {
      isLoading.value = false
    }
  }

  return {
    // 状态
    documents,
    currentDocument,
    recentDocuments,
    favoriteDocuments,
    searchResults,
    isLoading,
    isSaving,
    pagination,
    editorContent,
    hasUnsavedChanges,
    lastSavedAt,
    
    // 计算属性
    documentCount,
    hasDocuments,
    currentDocumentId,
    isCurrentDocumentFavorite,
    
    // 方法
    fetchDocuments,
    fetchDocument,
    createDocument,
    updateDocument,
    deleteDocument,
    searchDocuments,
    fetchRecentDocuments,
    fetchFavoriteDocuments,
    addToFavorites,
    removeFromFavorites,
    duplicateDocument,
    setEditorContent,
    startAutoSave,
    stopAutoSave,
    saveDocument,
    clearCurrentDocument,
    exportDocument
  }
})