/**
 * 认证相关API
 */

import { request } from '../index'
import type { User, ApiResponse } from '@/types'

// 登录请求参数
export interface LoginParams {
  email: string
  password: string
  rememberMe?: boolean
}

// 注册请求参数
export interface RegisterParams {
  name: string
  email: string
  password: string
  confirmPassword: string
  inviteCode?: string
}

// 登录响应数据
export interface LoginResponse {
  user: User
  accessToken: string  // 匹配后端实际返回的字段名
  refreshToken: string
  expiresIn: number
  tokenType?: string   // 后端返回的 tokenType 字段
}

// 重置密码参数
export interface ResetPasswordParams {
  email: string
}

// 修改密码参数
export interface ChangePasswordParams {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

// 验证邮箱参数
export interface VerifyEmailParams {
  token: string
}

export const authApi = {
  /**
   * 用户登录
   */
  login(params: LoginParams | { username: string; password: string }): Promise<ApiResponse<LoginResponse>> {
    return request.post('/auth/login', params)
  },

  /**
   * 用户注册
   */
  register(params: RegisterParams | { username: string; email: string; password: string; displayName?: string }): Promise<ApiResponse<User>> {
    return request.post('/auth/register', params)
  },

  /**
   * 用户登出
   */
  logout(): Promise<ApiResponse> {
    return request.post('/auth/logout')
  },

  /**
   * 刷新token
   */
  refreshToken(refreshToken: string): Promise<ApiResponse<{ accessToken: string; expiresIn: number }>> {
    return request.post('/auth/refresh', { refreshToken })
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser(): Promise<ApiResponse<User>> {
    return request.get('/auth/me')
  },

  /**
   * 发送重置密码邮件
   */
  sendResetPasswordEmail(params: ResetPasswordParams): Promise<ApiResponse> {
    return request.post('/auth/reset-password', params)
  },

  /**
   * 重置密码
   */
  resetPassword(token: string, newPassword: string): Promise<ApiResponse> {
    return request.post('/auth/reset-password/confirm', {
      token,
      newPassword
    })
  },

  /**
   * 修改密码
   */
  changePassword(params: ChangePasswordParams): Promise<ApiResponse> {
    return request.post('/auth/change-password', params)
  },

  /**
   * 发送验证邮件
   */
  sendVerificationEmail(): Promise<ApiResponse> {
    return request.post('/auth/send-verification')
  },

  /**
   * 验证邮箱
   */
  verifyEmail(params: VerifyEmailParams): Promise<ApiResponse> {
    return request.post('/auth/verify-email', params)
  },

  /**
   * 检查邮箱是否已存在
   */
  checkEmailExists(email: string): Promise<ApiResponse<{ exists: boolean }>> {
    return request.get(`/auth/check-email?email=${encodeURIComponent(email)}`)
  },

  /**
   * 检查用户名是否已存在
   */
  checkUsernameExists(username: string): Promise<ApiResponse<{ exists: boolean }>> {
    return request.get(`/auth/check-username?username=${encodeURIComponent(username)}`)
  },

  /**
   * 第三方登录 - Google
   */
  loginWithGoogle(token: string): Promise<ApiResponse<LoginResponse>> {
    return request.post('/auth/google', { token })
  },

  /**
   * 第三方登录 - GitHub
   */
  loginWithGitHub(code: string): Promise<ApiResponse<LoginResponse>> {
    return request.post('/auth/github', { code })
  },

  /**
   * 绑定第三方账号
   */
  bindThirdPartyAccount(provider: 'google' | 'github', token: string): Promise<ApiResponse> {
    return request.post('/auth/bind', { provider, token })
  },

  /**
   * 解绑第三方账号
   */
  unbindThirdPartyAccount(provider: 'google' | 'github'): Promise<ApiResponse> {
    return request.delete(`/auth/bind/${provider}`)
  }
}