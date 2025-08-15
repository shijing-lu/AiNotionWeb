/**
 * 用户相关API
 */

import { request } from '../index'
import type { User, UserSettings, Statistics, ApiResponse } from '@/types'

// 更新用户信息参数
export interface UpdateUserParams {
  username?: string
  email?: string
  avatar?: string
  bio?: string
  location?: string
  website?: string
}

// 用户搜索参数
export interface UserSearchParams {
  query: string
  limit?: number
  excludeIds?: string[]
}

export const userApi = {
  /**
   * 获取用户信息
   */
  getUser(id: string): Promise<ApiResponse<User>> {
    return request.get(`/users/${id}`)
  },

  /**
   * 更新用户信息
   */
  updateUser(params: UpdateUserParams): Promise<ApiResponse<User>> {
    return request.put('/users/profile', params)
  },

  /**
   * 上传用户头像
   */
  uploadAvatar(file: File): Promise<ApiResponse<{ url: string }>> {
    return request.upload('/users/avatar', file)
  },

  /**
   * 获取用户设置
   */
  getSettings(): Promise<ApiResponse<UserSettings>> {
    return request.get('/users/settings')
  },

  /**
   * 更新用户设置
   */
  updateSettings(settings: Partial<UserSettings>): Promise<ApiResponse<UserSettings>> {
    return request.put('/users/settings', settings)
  },

  /**
   * 获取用户统计信息
   */
  getStatistics(): Promise<ApiResponse<Statistics>> {
    return request.get('/users/statistics')
  },

  /**
   * 搜索用户
   */
  searchUsers(params: UserSearchParams): Promise<ApiResponse<User[]>> {
    return request.get('/users/search', { params })
  },

  /**
   * 获取用户活动日志
   */
  getActivityLog(params?: {
    page?: number
    pageSize?: number
    type?: string
    startDate?: string
    endDate?: string
  }): Promise<ApiResponse<{
    activities: {
      id: string
      type: string
      description: string
      metadata: Record<string, any>
      createdAt: string
    }[]
    total: number
  }>> {
    return request.get('/users/activity', { params })
  },

  /**
   * 获取用户通知
   */
  getNotifications(params?: {
    page?: number
    pageSize?: number
    unreadOnly?: boolean
  }): Promise<ApiResponse<{
    notifications: {
      id: string
      type: string
      title: string
      message: string
      read: boolean
      data?: Record<string, any>
      createdAt: string
    }[]
    unreadCount: number
    total: number
  }>> {
    return request.get('/users/notifications', { params })
  },

  /**
   * 标记通知为已读
   */
  markNotificationAsRead(id: string): Promise<ApiResponse> {
    return request.patch(`/users/notifications/${id}/read`)
  },

  /**
   * 批量标记通知为已读
   */
  markAllNotificationsAsRead(): Promise<ApiResponse> {
    return request.patch('/users/notifications/read-all')
  },

  /**
   * 删除通知
   */
  deleteNotification(id: string): Promise<ApiResponse> {
    return request.delete(`/users/notifications/${id}`)
  },

  /**
   * 获取用户订阅信息
   */
  getSubscription(): Promise<ApiResponse<{
    plan: {
      id: string
      name: string
      price: number
      features: string[]
    }
    status: 'active' | 'canceled' | 'expired'
    currentPeriodEnd: string
    usage: {
      storage: number
      aiCalls: number
      collaborators: number
    }
    limits: {
      storage: number
      aiCalls: number
      collaborators: number
    }
  }>> {
    return request.get('/users/subscription')
  },

  /**
   * 更新订阅计划
   */
  updateSubscription(planId: string): Promise<ApiResponse<{ paymentUrl?: string }>> {
    return request.post('/users/subscription', { planId })
  },

  /**
   * 取消订阅
   */
  cancelSubscription(): Promise<ApiResponse> {
    return request.delete('/users/subscription')
  },

  /**
   * 获取账单历史
   */
  getBillingHistory(): Promise<ApiResponse<{
    invoices: {
      id: string
      amount: number
      currency: string
      status: string
      date: string
      downloadUrl: string
    }[]
  }>> {
    return request.get('/users/billing')
  },

  /**
   * 获取用户邀请码
   */
  getInviteCodes(): Promise<ApiResponse<{
    codes: {
      code: string
      used: boolean
      usedBy?: string
      createdAt: string
      expiresAt: string
    }[]
    remaining: number
  }>> {
    return request.get('/users/invite-codes')
  },

  /**
   * 生成邀请码
   */
  generateInviteCode(): Promise<ApiResponse<{ code: string }>> {
    return request.post('/users/invite-codes')
  },

  /**
   * 获取用户团队信息
   */
  getTeams(): Promise<ApiResponse<{
    teams: {
      id: string
      name: string
      role: 'owner' | 'admin' | 'member'
      memberCount: number
      createdAt: string
    }[]
  }>> {
    return request.get('/users/teams')
  },

  /**
   * 删除用户账号
   */
  deleteAccount(password: string): Promise<ApiResponse> {
    return request.delete('/users/account', {
      data: { password }
    })
  },

  /**
   * 导出用户数据
   */
  exportData(): Promise<ApiResponse<{ downloadUrl: string }>> {
    return request.post('/users/export')
  },

  /**
   * 获取用户偏好设置
   */
  getPreferences(): Promise<ApiResponse<{
    theme: 'light' | 'dark' | 'auto'
    language: string
    timezone: string
    dateFormat: string
    emailNotifications: boolean
    pushNotifications: boolean
  }>> {
    return request.get('/users/preferences')
  },

  /**
   * 更新用户偏好设置
   */
  updatePreferences(preferences: Record<string, any>): Promise<ApiResponse> {
    return request.put('/users/preferences', preferences)
  },

  /**
   * 获取用户安全设置
   */
  getSecuritySettings(): Promise<ApiResponse<{
    twoFactorEnabled: boolean
    lastPasswordChange: string
    activeSessions: {
      id: string
      device: string
      location: string
      lastActive: string
      current: boolean
    }[]
    loginHistory: {
      ip: string
      device: string
      location: string
      timestamp: string
      success: boolean
    }[]
  }>> {
    return request.get('/users/security')
  },

  /**
   * 启用两步验证
   */
  enableTwoFactor(): Promise<ApiResponse<{
    qrCode: string
    secret: string
    backupCodes: string[]
  }>> {
    return request.post('/users/security/2fa/enable')
  },

  /**
   * 确认启用两步验证
   */
  confirmTwoFactor(code: string): Promise<ApiResponse> {
    return request.post('/users/security/2fa/confirm', { code })
  },

  /**
   * 禁用两步验证
   */
  disableTwoFactor(password: string): Promise<ApiResponse> {
    return request.post('/users/security/2fa/disable', { password })
  },

  /**
   * 终止会话
   */
  terminateSession(sessionId: string): Promise<ApiResponse> {
    return request.delete(`/users/security/sessions/${sessionId}`)
  },

  /**
   * 终止所有其他会话
   */
  terminateAllOtherSessions(): Promise<ApiResponse> {
    return request.delete('/users/security/sessions/others')
  }
}