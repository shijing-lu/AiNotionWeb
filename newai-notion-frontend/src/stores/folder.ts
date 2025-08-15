/**
 * 文件夹状态管理
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { folderApi } from '@/api/modules'
import type { Folder } from '@/types'
import type { CreateFolderParams, UpdateFolderParams } from '@/api/modules'

export const useFolderStore = defineStore('folder', () => {
  // 状态
  const folders = ref<Folder[]>([])
  const currentFolder = ref<Folder | null>(null)
  const folderTree = ref<Folder[]>([])
  const breadcrumbs = ref<Folder[]>([])
  const isLoading = ref(false)
  const expandedFolders = ref<Set<string>>(new Set())

  // 计算属性
  const rootFolders = computed(() => {
    return folderTree.value.filter(folder => !folder.parentId)
  })
  
  const currentFolderId = computed(() => currentFolder.value?.id)
  
  const hasSubfolders = computed(() => {
    return currentFolder.value?.children && currentFolder.value.children.length > 0
  })

  // 获取文件夹树
  const fetchFolderTree = async (): Promise<void> => {
    try {
      isLoading.value = true
      const response = await folderApi.getFolderTree()
      
      if (response.success && response.data) {
        folderTree.value = response.data
        folders.value = flattenFolders(response.data)
      }
    } catch (error) {
      console.error('Fetch folder tree error:', error)
      ElMessage.error('获取文件夹列表失败')
    } finally {
      isLoading.value = false
    }
  }

  // 扁平化文件夹树
  const flattenFolders = (folders: Folder[]): Folder[] => {
    const result: Folder[] = []
    
    const flatten = (folderList: Folder[]) => {
      folderList.forEach(folder => {
        result.push(folder)
        if (folder.children && folder.children.length > 0) {
          flatten(folder.children)
        }
      })
    }
    
    flatten(folders)
    return result
  }

  // 获取单个文件夹
  const fetchFolder = async (id: string): Promise<Folder | null> => {
    try {
      isLoading.value = true
      const response = await folderApi.getFolder(id)
      
      if (response.success && response.data) {
        currentFolder.value = response.data
        return response.data
      }
      return null
    } catch (error) {
      console.error('Fetch folder error:', error)
      ElMessage.error('获取文件夹失败')
      return null
    } finally {
      isLoading.value = false
    }
  }

  // 获取文件夹内容
  const fetchFolderContents = async (id: string): Promise<void> => {
    try {
      isLoading.value = true
      const response = await folderApi.getFolderContents(id)
      
      if (response.success && response.data) {
        if (currentFolder.value) {
          currentFolder.value.children = response.data.folders
          currentFolder.value.documents = response.data.documents
        }
      }
    } catch (error) {
      console.error('Fetch folder contents error:', error)
      ElMessage.error('获取文件夹内容失败')
    } finally {
      isLoading.value = false
    }
  }

  // 创建文件夹
  const createFolder = async (params: CreateFolderParams): Promise<Folder | null> => {
    try {
      isLoading.value = true
      const response = await folderApi.createFolder(params)
      
      if (response.success && response.data) {
        // 重新获取文件夹树
        await fetchFolderTree()
        ElMessage.success('文件夹创建成功')
        return response.data
      }
      return null
    } catch (error) {
      console.error('Create folder error:', error)
      ElMessage.error('创建文件夹失败')
      return null
    } finally {
      isLoading.value = false
    }
  }

  // 更新文件夹
  const updateFolder = async (id: string, params: UpdateFolderParams): Promise<boolean> => {
    try {
      isLoading.value = true
      const response = await folderApi.updateFolder(id, params)
      
      if (response.success && response.data) {
        // 更新本地状态
        const index = folders.value.findIndex(folder => folder.id === id)
        if (index !== -1) {
          folders.value[index] = response.data
        }
        
        if (currentFolder.value?.id === id) {
          currentFolder.value = response.data
        }
        
        // 重新获取文件夹树
        await fetchFolderTree()
        ElMessage.success('文件夹更新成功')
        return true
      }
      return false
    } catch (error) {
      console.error('Update folder error:', error)
      ElMessage.error('更新文件夹失败')
      return false
    } finally {
      isLoading.value = false
    }
  }

  // 删除文件夹
  const deleteFolder = async (id: string, force = false): Promise<boolean> => {
    try {
      isLoading.value = true
      const response = await folderApi.deleteFolder(id, force)
      
      if (response.success) {
        // 从列表中移除
        folders.value = folders.value.filter(folder => folder.id !== id)
        
        // 如果删除的是当前文件夹，清空当前文件夹
        if (currentFolder.value?.id === id) {
          currentFolder.value = null
        }
        
        // 重新获取文件夹树
        await fetchFolderTree()
        ElMessage.success('文件夹删除成功')
        return true
      }
      return false
    } catch (error) {
      console.error('Delete folder error:', error)
      ElMessage.error('删除文件夹失败')
      return false
    } finally {
      isLoading.value = false
    }
  }

  // 移动文件夹
  const moveFolder = async (id: string, parentId: string | null): Promise<boolean> => {
    try {
      isLoading.value = true
      const response = await folderApi.moveFolder(id, parentId)
      
      if (response.success) {
        // 重新获取文件夹树
        await fetchFolderTree()
        ElMessage.success('文件夹移动成功')
        return true
      }
      return false
    } catch (error) {
      console.error('Move folder error:', error)
      ElMessage.error('移动文件夹失败')
      return false
    } finally {
      isLoading.value = false
    }
  }

  // 复制文件夹
  const duplicateFolder = async (id: string, name?: string, parentId?: string): Promise<Folder | null> => {
    try {
      isLoading.value = true
      const response = await folderApi.duplicateFolder(id, name, parentId)
      
      if (response.success && response.data) {
        // 重新获取文件夹树
        await fetchFolderTree()
        ElMessage.success('文件夹复制成功')
        return response.data
      }
      return null
    } catch (error) {
      console.error('Duplicate folder error:', error)
      ElMessage.error('复制文件夹失败')
      return null
    } finally {
      isLoading.value = false
    }
  }

  // 获取文件夹路径（面包屑）
  const fetchFolderPath = async (id: string): Promise<void> => {
    try {
      const response = await folderApi.getFolderPath(id)
      
      if (response.success && response.data) {
        breadcrumbs.value = response.data
      }
    } catch (error) {
      console.error('Fetch folder path error:', error)
    }
  }

  // 搜索文件夹
  const searchFolders = async (query: string): Promise<Folder[]> => {
    try {
      const response = await folderApi.searchFolders(query)
      
      if (response.success && response.data) {
        return response.data
      }
      return []
    } catch (error) {
      console.error('Search folders error:', error)
      ElMessage.error('搜索文件夹失败')
      return []
    }
  }

  // 展开/折叠文件夹
  const toggleFolderExpansion = (folderId: string): void => {
    if (expandedFolders.value.has(folderId)) {
      expandedFolders.value.delete(folderId)
    } else {
      expandedFolders.value.add(folderId)
    }
  }

  // 展开文件夹
  const expandFolder = (folderId: string): void => {
    expandedFolders.value.add(folderId)
  }

  // 折叠文件夹
  const collapseFolder = (folderId: string): void => {
    expandedFolders.value.delete(folderId)
  }

  // 展开所有文件夹
  const expandAllFolders = (): void => {
    folders.value.forEach(folder => {
      expandedFolders.value.add(folder.id)
    })
  }

  // 折叠所有文件夹
  const collapseAllFolders = (): void => {
    expandedFolders.value.clear()
  }

  // 检查文件夹是否展开
  const isFolderExpanded = (folderId: string): boolean => {
    return expandedFolders.value.has(folderId)
  }

  // 根据ID查找文件夹
  const findFolderById = (id: string): Folder | null => {
    return folders.value.find(folder => folder.id === id) || null
  }

  // 获取文件夹的子文件夹
  const getSubfolders = (parentId: string): Folder[] => {
    return folders.value.filter(folder => folder.parentId === parentId)
  }

  // 获取文件夹的完整路径
  const getFolderFullPath = (folderId: string): string => {
    const path: string[] = []
    let currentId: string | undefined = folderId
    
    while (currentId) {
      const folder = findFolderById(currentId)
      if (folder) {
        path.unshift(folder.name)
        currentId = folder.parentId
      } else {
        break
      }
    }
    
    return path.join(' / ')
  }

  // 设置当前文件夹
  const setCurrentFolder = async (folder: Folder | null): Promise<void> => {
    currentFolder.value = folder
    
    if (folder) {
      // 获取面包屑路径
      await fetchFolderPath(folder.id)
      // 获取文件夹内容
      await fetchFolderContents(folder.id)
    } else {
      breadcrumbs.value = []
    }
  }

  // 清空当前文件夹
  const clearCurrentFolder = (): void => {
    currentFolder.value = null
    breadcrumbs.value = []
  }

  // 导出文件夹
  const exportFolder = async (id: string, format: 'zip' | 'pdf'): Promise<void> => {
    try {
      isLoading.value = true
      const blob = await folderApi.exportFolder(id, format)
      
      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `folder.${format}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('文件夹导出成功')
    } catch (error) {
      console.error('Export folder error:', error)
      ElMessage.error('导出文件夹失败')
    } finally {
      isLoading.value = false
    }
  }

  return {
    // 状态
    folders,
    currentFolder,
    folderTree,
    breadcrumbs,
    isLoading,
    expandedFolders,
    
    // 计算属性
    rootFolders,
    currentFolderId,
    hasSubfolders,
    
    // 方法
    fetchFolderTree,
    fetchFolder,
    fetchFolderContents,
    createFolder,
    updateFolder,
    deleteFolder,
    moveFolder,
    duplicateFolder,
    fetchFolderPath,
    searchFolders,
    toggleFolderExpansion,
    expandFolder,
    collapseFolder,
    expandAllFolders,
    collapseAllFolders,
    isFolderExpanded,
    findFolderById,
    getSubfolders,
    getFolderFullPath,
    setCurrentFolder,
    clearCurrentFolder,
    exportFolder
  }
})