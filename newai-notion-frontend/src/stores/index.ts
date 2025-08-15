/**
 * Pinia stores 统一导出
 */

export { useAuthStore } from './auth'
export { useDocumentStore } from './document'
export { useFolderStore } from './folder'
export { useAIStore } from './ai'
export { useAppStore } from './app'

// 导出所有store的类型
export type { User, UserRole } from '@/types'
export type { Document, Folder, AIRequest, AIResponse } from '@/types'
export type { UserSettings, Notification } from '@/types'