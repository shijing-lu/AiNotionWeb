/**
 * 全局类型定义文件
 */

// 用户相关类型
export interface User {
  id: string
  username: string
  email: string
  avatar?: string
  role: UserRole
  createdAt: string
  updatedAt: string
}

export enum UserRole {
  GUEST = 'guest',
  FREE = 'free',
  STANDARD = 'standard',
  PROFESSIONAL = 'professional',
  ADMIN = 'admin'
}

// 文档相关类型
export interface Document {
  id: string
  title: string
  content: string
  folderId?: string
  tags: string[]
  isPublic: boolean
  createdAt: string
  updatedAt: string
  authorId: string
  collaborators: string[]
  version: number
}

export interface Folder {
  id: string
  name: string
  parentId?: string
  children: Folder[]
  documents: Document[]
  createdAt: string
  updatedAt: string
  authorId: string
}

// AI功能相关类型
export interface AIRequest {
  type: AIRequestType
  content: string
  documentIds?: string[]
  options?: Record<string, any>
}

export enum AIRequestType {
  SEARCH = 'search',
  SUMMARIZE = 'summarize',
  TRANSLATE = 'translate',
  CHECK = 'check',
  QUESTION = 'question'
}

export interface AIResponse {
  id: string
  type: AIRequestType
  result: string
  confidence?: number
  suggestions?: string[]
  createdAt: string
}

// 搜索相关类型
export interface SearchResult {
  id: string
  title: string
  content: string
  snippet: string
  score: number
  documentId: string
  highlights: string[]
}

export interface SearchQuery {
  query: string
  filters?: {
    tags?: string[]
    dateRange?: [string, string]
    authorId?: string
    folderId?: string
  }
  sort?: 'relevance' | 'date' | 'title'
  limit?: number
  offset?: number
}

// 协作相关类型
export interface Comment {
  id: string
  content: string
  documentId: string
  authorId: string
  position?: {
    start: number
    end: number
  }
  replies: Comment[]
  resolved: boolean
  createdAt: string
  updatedAt: string
}

export interface Collaboration {
  documentId: string
  users: {
    userId: string
    permission: 'read' | 'write' | 'admin'
    cursor?: {
      position: number
      selection?: [number, number]
    }
  }[]
  lastActivity: string
}

// API响应类型
export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  message?: string
  error?: string
  code?: number
}

export interface PaginatedResponse<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
  hasNext: boolean
  hasPrev: boolean
}

// 应用状态类型
export interface AppState {
  user: User | null
  currentDocument: Document | null
  sidebarCollapsed: boolean
  theme: 'light' | 'dark'
  loading: boolean
}

// 编辑器相关类型
export interface EditorState {
  content: string
  selection?: {
    from: number
    to: number
  }
  history: {
    undo: any[]
    redo: any[]
  }
}

// 文件上传类型
export interface FileUpload {
  file: File
  progress: number
  status: 'pending' | 'uploading' | 'success' | 'error'
  url?: string
  error?: string
}

// 通知类型
export interface Notification {
  id: string
  type: 'info' | 'success' | 'warning' | 'error'
  title: string
  message: string
  duration?: number
  actions?: {
    label: string
    action: () => void
  }[]
  createdAt: string
}

// 设置类型
export interface UserSettings {
  theme: 'light' | 'dark' | 'auto'
  language: 'zh-CN' | 'en-US'
  fontSize: number
  autoSave: boolean
  aiEnabled: boolean
  notifications: {
    email: boolean
    push: boolean
    mentions: boolean
    comments: boolean
  }
}

// 统计数据类型
export interface Statistics {
  documentsCount: number
  wordsCount: number
  aiUsageCount: number
  collaborationsCount: number
  storageUsed: number
  storageLimit: number
}

// 订阅计划类型
export interface SubscriptionPlan {
  id: string
  name: string
  price: number
  currency: string
  interval: 'month' | 'year'
  features: string[]
  limits: {
    storage: number // MB
    aiCalls: number // per day
    collaborators: number
  }
}

// 错误类型
export interface AppError {
  code: string
  message: string
  details?: any
  timestamp: string
}

// 路由元信息类型
export interface RouteMeta {
  title?: string
  requiresAuth?: boolean
  roles?: UserRole[]
  layout?: 'default' | 'auth' | 'minimal'
}

// 组件Props类型
export interface BaseComponentProps {
  class?: string
  style?: string | Record<string, any>
}

// 事件类型
export interface DocumentEvent {
  type: 'create' | 'update' | 'delete' | 'share'
  documentId: string
  userId: string
  timestamp: string
  data?: any
}

// WebSocket消息类型
export interface WebSocketMessage {
  type: 'document_update' | 'user_join' | 'user_leave' | 'cursor_update' | 'comment_add'
  payload: any
  timestamp: string
}

// 导出导入类型
export interface ExportOptions {
  format: 'markdown' | 'pdf' | 'docx' | 'html'
  includeImages: boolean
  includeComments: boolean
}

export interface ImportResult {
  success: boolean
  documentsCreated: number
  foldersCreated: number
  errors: string[]
}