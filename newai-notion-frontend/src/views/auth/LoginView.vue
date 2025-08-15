<template>
  <div class="login-view">
    <div class="login-form">
      <div class="login-form__header">
        <h2 class="login-form__title">欢迎回来</h2>
        <p class="login-form__subtitle">登录您的账户以继续使用</p>
      </div>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form__content"
        size="large"
        @submit.prevent="handleSubmit"
      >
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="邮箱地址"
            prefix-icon="Message"
            clearable
            @keyup.enter="handleSubmit"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleSubmit"
          />
        </el-form-item>
        
        <div class="login-form__options">
          <el-checkbox v-model="form.rememberMe">
            记住我
          </el-checkbox>
          <router-link to="/forgot-password" class="forgot-link">
            忘记密码？
          </router-link>
        </div>
        
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleSubmit"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 第三方登录 -->
      <div class="social-login">
        <div class="social-login__divider">
          <span>或者使用以下方式登录</span>
        </div>
        
        <div class="social-login__buttons">
          <el-button
            class="social-btn social-btn--google"
            @click="handleSocialLogin('google')"
            :loading="socialLoading.google"
          >
            <svg class="social-icon" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            Google
          </el-button>
          
          <el-button
            class="social-btn social-btn--github"
            @click="handleSocialLogin('github')"
            :loading="socialLoading.github"
          >
            <svg class="social-icon" viewBox="0 0 24 24">
              <path fill="currentColor" d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
            GitHub
          </el-button>
        </div>
      </div>
      
      <!-- 注册链接 -->
      <div class="login-form__footer">
        <span>还没有账户？</span>
        <router-link to="/register" class="register-link">
          立即注册
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores'
import { useNotification } from '@/composables'
import { isValidEmail } from '@/utils'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { success, error } = useNotification()

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)
const socialLoading = reactive({
  google: false,
  github: false
})

// 表单数据
const form = reactive({
  email: '',
  password: '',
  rememberMe: false
})

// 表单验证规则
const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!isValidEmail(value)) {
          callback(new Error('请输入有效的邮箱地址'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

/**
 * 处理表单提交
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    // 执行登录
    const loginSuccess = await authStore.login({
      email: form.email,
      password: form.password,
      rememberMe: form.rememberMe
    })
    
    if (loginSuccess) {
      // 登录成功，显示成功消息
      success('登录成功')
      
      // 等待一小段时间确保状态更新完成
      await new Promise(resolve => setTimeout(resolve, 100))
      
      // 验证登录状态
      if (authStore.isAuthenticated) {
        // 重定向到目标页面或首页
        const redirect = route.query.redirect as string
        console.log('Redirecting to:', redirect || '/app/dashboard')
        
        try {
          await router.push(redirect || '/app/dashboard')
          console.log('Navigation successful')
        } catch (navError) {
          console.error('Navigation error:', navError)
          // 如果跳转失败，尝试使用 replace
          await router.replace(redirect || '/app/dashboard')
        }
      } else {
        console.warn('Login succeeded but isAuthenticated is false')
        error('登录状态异常，请刷新页面重试')
      }
    }
    
  } catch (err: any) {
    console.error('Login failed:', err)
    // authStore.login 已经显示了错误消息，这里不需要重复显示
    // 但如果没有显示错误消息，则显示通用错误
    if (!err.message || err.message === 'Login error') {
      error('登录失败，请检查网络连接')
    }
  } finally {
    loading.value = false
  }
}

/**
 * 处理第三方登录
 */
const handleSocialLogin = async (provider: 'google' | 'github') => {
  try {
    socialLoading[provider] = true
    
    // 调用第三方登录API
    await authStore.socialLogin(provider)
    
    success(`${provider === 'google' ? 'Google' : 'GitHub'} 登录成功`)
    
    // 重定向
    const redirect = route.query.redirect as string
    router.push(redirect || '/app/dashboard')
    
  } catch (err: any) {
    error(err.message || '第三方登录失败')
  } finally {
    socialLoading[provider] = false
  }
}


</script>

<style scoped>
.login-view {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.login-form {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.login-form__header {
  text-align: center;
  margin-bottom: 32px;
}

.login-form__title {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
}

.login-form__subtitle {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin: 0;
}

.login-form__content {
  margin-bottom: 24px;
}

.login-form__options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.forgot-link {
  color: var(--el-color-primary);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: var(--el-color-primary-dark-2);
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

/* 第三方登录样式 */
.social-login {
  margin-bottom: 24px;
}

.social-login__divider {
  position: relative;
  text-align: center;
  margin-bottom: 20px;
}

.social-login__divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background-color: var(--el-border-color-light);
}

.social-login__divider span {
  background: white;
  padding: 0 16px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.social-login__buttons {
  display: flex;
  gap: 12px;
}

.social-btn {
  flex: 1;
  height: 44px;
  border: 1px solid var(--el-border-color);
  background: white;
  color: var(--el-text-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
  transition: all 0.2s;
}

.social-btn:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

.social-btn--google:hover {
  border-color: #4285F4;
  color: #4285F4;
}

.social-btn--github:hover {
  border-color: #333;
  color: #333;
}

.social-icon {
  width: 18px;
  height: 18px;
}

/* 底部链接 */
.login-form__footer {
  text-align: center;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.register-link {
  color: var(--el-color-primary);
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
  transition: color 0.2s;
}

.register-link:hover {
  color: var(--el-color-primary-dark-2);
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-form {
    padding: 24px 20px;
    margin: 0 16px;
  }
  
  .login-form__title {
    font-size: 20px;
  }
  
  .social-login__buttons {
    flex-direction: column;
  }
  
  .social-btn {
    width: 100%;
  }
}

/* 暗色主题适配 */
.dark .login-form {
  background: var(--el-bg-color-page);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.dark .social-login__divider span {
  background: var(--el-bg-color-page);
}

.dark .social-btn {
  background: var(--el-bg-color);
  border-color: var(--el-border-color);
}
</style>