/**
 * 用户认证状态管理
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/modules'
import type { User } from '@/types'
import { UserRole } from '@/types'
import type { LoginParams, RegisterParams, LoginResponse } from '@/api/modules'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('auth_token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refresh_token'))
  const isLoading = ref(false)
  const loginTime = ref<number | null>(null)
  const initialized = ref(false)

  // 计算属性
  const isAuthenticated = computed(() => !!user.value && !!token.value)
  const userRole = computed(() => user.value?.role || UserRole.GUEST)
  const isAdmin = computed(() => userRole.value === UserRole.ADMIN)
  const isPremium = computed(() => {
    return [UserRole.STANDARD, UserRole.PROFESSIONAL, UserRole.ADMIN].includes(userRole.value)
  })

  // 检查权限
  const hasPermission = (requiredRole: UserRole): boolean => {
    const roleHierarchy = {
      [UserRole.GUEST]: 0,
      [UserRole.FREE]: 1,
      [UserRole.STANDARD]: 2,
      [UserRole.PROFESSIONAL]: 3,
      [UserRole.ADMIN]: 4
    }
    return roleHierarchy[userRole.value] >= roleHierarchy[requiredRole]
  }

  // 登录
  const login = async (params: LoginParams): Promise<boolean> => {
    try {
      isLoading.value = true
      // 将前端的email字段映射为后端期望的username字段
      const loginData = {
        username: params.email,
        password: params.password
      }
      const response = await authApi.login(loginData)
      
      if (response.success && response.data) {
        // 适配后端返回的字段名
        const { 
          user: userData, 
          accessToken, 
          refreshToken: refToken 
        } = response.data
        
        // 数据类型转换和适配
        const adaptedUser = {
          ...userData,
             id: String(userData.id), // 将数字ID转换为字符串
          role: (userData.role === 'USER' ? UserRole.FREE : userData.role.toLowerCase()) as UserRole, // 映射角色
          createdAt: new Date().toISOString(), // 如果后端没有返回，使用当前时间
          updatedAt: new Date().toISOString()
        }
        
        // 保存用户信息和token
        user.value = adaptedUser
        token.value = accessToken  // 使用 accessToken 而不是 token
        refreshToken.value = refToken
        loginTime.value = Date.now()
        
        // 持久化存储
        localStorage.setItem('auth_token', accessToken)
        localStorage.setItem('refresh_token', refToken)
        localStorage.setItem('user_info', JSON.stringify(adaptedUser))
        
        // 调试日志
        console.log('🔐 Login success - tokens saved:')
        console.log('  - auth_token:', accessToken ? 'saved' : 'failed')
        console.log('  - refresh_token:', refToken ? 'saved' : 'failed')
        console.log('  - localStorage auth_token:', localStorage.getItem('auth_token') ? 'exists' : 'missing')
        
        // 标记为已初始化
        initialized.value = true
        
        ElMessage.success('登录成功')
        return true
      } else {
        // 处理登录失败的情况
        const errorMessage = response.error || '登录失败，请检查用户名和密码'
        ElMessage.error(errorMessage)
        throw new Error(errorMessage)
      }
    } catch (error: any) {
      console.error('Login error:', error)
      // 如果是我们抛出的错误，直接重新抛出
      if (error.message && error.message !== 'Login error') {
        throw error
      }
      // 处理网络错误或其他未知错误
      const errorMessage = error.response?.data?.error || error.message || '登录失败，请检查网络连接'
      ElMessage.error(errorMessage)
      throw new Error(errorMessage)
    } finally {
      isLoading.value = false
    }
  }

  // 注册
  const register = async (params: RegisterParams): Promise<boolean> => {
    try {
      isLoading.value = true
      // 将前端的name字段映射为后端期望的username字段
      const registerData = {
        username: params.name,
        email: params.email,
        password: params.password,
        displayName: params.name
      }
      const response = await authApi.register(registerData)
      
      if (response.success && response.data) {
        ElMessage.success('注册成功！正在为您自动登录...')
        
        // 注册成功后自动登录
        const loginSuccess = await login({
          email: params.email,
          password: params.password
        })
        
        if (loginSuccess) {
          ElMessage.success('欢迎加入！已为您自动登录')
        }
        
        return true
      }
      return false
    } catch (error: any) {
      console.error('Register error:', error)
      
      // 处理后端返回的详细错误信息
      if (error.response?.data?.message) {
        const errorMessages = Array.isArray(error.response.data.message) 
          ? error.response.data.message.join('；') 
          : error.response.data.message
        ElMessage.error(`注册失败：${errorMessages}`)
      } else {
        ElMessage.error('注册失败，请稍后重试')
      }
      
      throw error // 重新抛出错误，让组件可以处理
    } finally {
      isLoading.value = false
    }
  }

  // 登出
  const logout = async (): Promise<void> => {
    try {
      if (token.value) {
        await authApi.logout()
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      // 清除本地状态
      user.value = null
      token.value = null
      refreshToken.value = null
      loginTime.value = null
      
      // 清除本地存储
      localStorage.removeItem('auth_token')
      localStorage.removeItem('refresh_token')
      localStorage.removeItem('user_info')
      
      ElMessage.success('已退出登录')
    }
  }

  // 刷新token
  const refreshAuthToken = async (): Promise<boolean> => {
    try {
      if (!refreshToken.value) {
        throw new Error('No refresh token available')
      }
      
      const response = await authApi.refreshToken(refreshToken.value)
      
      if (response.success && response.data) {
        token.value = response.data.accessToken
        localStorage.setItem('auth_token', response.data.accessToken)
        return true
      }
      
      // 刷新失败，清除认证状态
      await logout()
      return false
    } catch (error) {
      console.error('Token refresh error:', error)
      await logout()
      return false
    }
  }

  // 获取当前用户信息
  const fetchCurrentUser = async (): Promise<boolean> => {
    try {
      if (!token.value) {
        return false
      }
      
      const response = await authApi.getCurrentUser()
      
      if (response.success && response.data) {
        user.value = response.data
        localStorage.setItem('user_info', JSON.stringify(response.data))
        return true
      }
      
      return false
    } catch (error) {
      console.error('Fetch user error:', error)
      return false
    }
  }

  // 初始化认证状态
  const initializeAuth = async (): Promise<void> => {
    try {
      // 从本地存储恢复用户信息
      const storedUser = localStorage.getItem('user_info')
      if (storedUser) {
        user.value = JSON.parse(storedUser)
      }
      
      // 如果有token，尝试获取最新用户信息
      if (token.value) {
        const success = await fetchCurrentUser()
        if (!success) {
          // 获取用户信息失败，尝试刷新token
          const refreshSuccess = await refreshAuthToken()
          if (!refreshSuccess) {
            // 刷新token也失败，但不立即清除状态，让用户手动重新登录
            console.warn('Token refresh failed, user may need to re-login')
          }
        }
      }
    } catch (error) {
      console.error('Initialize auth error:', error)
      // 不要在初始化时自动登出，避免登录后立即跳转
      console.warn('Auth initialization failed, user may need to re-login')
    } finally {
      initialized.value = true
    }
  }

  // 修改密码
  const changePassword = async (oldPassword: string, newPassword: string): Promise<boolean> => {
    try {
      isLoading.value = true
      const response = await authApi.changePassword({
        oldPassword,
        newPassword,
        confirmPassword: newPassword
      })
      
      if (response.success) {
        ElMessage.success('密码修改成功')
        return true
      }
      return false
    } catch (error) {
      console.error('Change password error:', error)
      ElMessage.error('密码修改失败')
      return false
    } finally {
      isLoading.value = false
    }
  }

  // 发送重置密码邮件
  const sendResetPasswordEmail = async (email: string): Promise<boolean> => {
    try {
      isLoading.value = true
      const response = await authApi.sendResetPasswordEmail({ email })
      
      if (response.success) {
        ElMessage.success('重置密码邮件已发送')
        return true
      }
      return false
    } catch (error) {
      console.error('Send reset email error:', error)
      ElMessage.error('发送重置邮件失败')
      return false
    } finally {
      isLoading.value = false
    }
  }

  // 验证邮箱
  const verifyEmail = async (token: string): Promise<boolean> => {
    try {
      isLoading.value = true
      const response = await authApi.verifyEmail({ token })
      
      if (response.success) {
        ElMessage.success('邮箱验证成功')
        // 重新获取用户信息
        await fetchCurrentUser()
        return true
      }
      return false
    } catch (error) {
      console.error('Verify email error:', error)
      ElMessage.error('邮箱验证失败')
      return false
    } finally {
      isLoading.value = false
    }
  }

  // 检查登录状态是否过期
  const checkAuthExpiry = (): boolean => {
    if (!loginTime.value || !token.value) {
      return false
    }
    
    // 检查是否超过24小时未活动
    const now = Date.now()
    const maxInactiveTime = 24 * 60 * 60 * 1000 // 24小时
    
    if (now - loginTime.value > maxInactiveTime) {
      logout()
      return false
    }
    
    return true
  }

  // 更新最后活动时间
  const updateLastActivity = (): void => {
    if (isAuthenticated.value) {
      loginTime.value = Date.now()
    }
  }

  return {
    // 状态
    user,
    token,
    isLoading,
    initialized,
    
    // 计算属性
    isAuthenticated,
    userRole,
    isAdmin,
    isPremium,
    
    // 方法
    hasPermission,
    login,
    register,
    logout,
    refreshAuthToken,
    fetchCurrentUser,
    initializeAuth,
    changePassword,
    sendResetPasswordEmail,
    verifyEmail,
    checkAuthExpiry,
    updateLastActivity
  }
})