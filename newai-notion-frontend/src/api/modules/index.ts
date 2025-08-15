/**
 * API模块统一导出
 */

export { authApi } from './auth'
export { documentApi } from './document'
export { folderApi } from './folder'
export { aiApi } from './ai'
export { userApi } from './user'

// 导出所有API类型
export type {
  LoginParams,
  RegisterParams,
  LoginResponse,
  ResetPasswordParams,
  ChangePasswordParams,
  VerifyEmailParams
} from './auth'

export type {
  CreateDocumentParams,
  UpdateDocumentParams,
  DocumentListParams,
  ShareDocumentParams
} from './document'

export type {
  CreateFolderParams,
  UpdateFolderParams
} from './folder'

export type {
  AISearchParams,
  AISummarizeParams,
  AITranslateParams,
  AICheckParams,
  AIQuestionParams,
  AIWritingParams,
  AIOptimizeParams
} from './ai'

export type {
  UpdateUserParams,
  UserSearchParams
} from './user'