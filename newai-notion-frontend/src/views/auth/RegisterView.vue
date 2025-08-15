<template>
  <div class="register-view">
    <div class="register-form">
      <div class="register-form__header">
        <h2 class="register-form__title">创建账户</h2>
        <p class="register-form__subtitle">加入我们，开始您的智能文档之旅</p>
      </div>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form__content"
        size="large"
        @submit.prevent="handleSubmit"
      >
        <el-form-item prop="name">
          <el-input
            v-model="form.name"
            placeholder="姓名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="邮箱地址"
            prefix-icon="Message"
            clearable
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
            @input="checkPasswordStrength"
          />
          <!-- 密码强度指示器 -->
          <div v-if="form.password" class="password-strength">
            <div class="password-strength__bar">
              <div 
                class="password-strength__fill"
                :class="`password-strength__fill--${passwordStrength.level}`"
                :style="{ width: passwordStrength.percentage + '%' }"
              ></div>
            </div>
            <span 
              class="password-strength__text"
              :class="`password-strength__text--${passwordStrength.level}`"
            >
              {{ passwordStrength.text }}
            </span>
          </div>
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleSubmit"
          />
        </el-form-item>
        
        <el-form-item prop="agreement">
          <el-checkbox v-model="form.agreement">
            我已阅读并同意
            <a href="#" class="agreement-link" @click.prevent="showTerms">
              《服务条款》
            </a>
            和
            <a href="#" class="agreement-link" @click.prevent="showPrivacy">
              《隐私政策》
            </a>
          </el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            class="register-btn"
            :loading="loading"
            @click="handleSubmit"
          >
            注册账户
          </el-button>
          <el-button
            type="info"
            :loading="testLoading"
            @click="testApiConnection"
            style="margin-left: 10px;"
          >
            🔧 测试API连接
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 第三方注册 -->
      <div class="social-register">
        <div class="social-register__divider">
          <span>或者使用以下方式注册</span>
        </div>
        
        <div class="social-register__buttons">
          <el-button
            class="social-btn social-btn--google"
            @click="handleSocialRegister('google')"
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
            @click="handleSocialRegister('github')"
            :loading="socialLoading.github"
          >
            <svg class="social-icon" viewBox="0 0 24 24">
              <path fill="currentColor" d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
            </svg>
            GitHub
          </el-button>
        </div>
      </div>
      
      <!-- 登录链接 -->
      <div class="register-form__footer">
        <span>已有账户？</span>
        <router-link to="/login" class="login-link">
          立即登录
        </router-link>
      </div>
    </div>
    
    <!-- 服务条款对话框 -->
    <el-dialog
      v-model="termsVisible"
      title="服务条款"
      width="600px"
      :before-close="() => termsVisible = false"
    >
      <div class="terms-content">
        <p>欢迎使用我们的服务。请仔细阅读以下服务条款...</p>
        <!-- 这里可以添加完整的服务条款内容 -->
      </div>
      <template #footer>
        <el-button @click="termsVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    
    <!-- 隐私政策对话框 -->
    <el-dialog
      v-model="privacyVisible"
      title="隐私政策"
      width="600px"
      :before-close="() => privacyVisible = false"
    >
      <div class="privacy-content">
        <p>我们重视您的隐私。请阅读我们的隐私政策...</p>
        <!-- 这里可以添加完整的隐私政策内容 -->
      </div>
      <template #footer>
        <el-button @click="privacyVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores'
import { useNotification } from '@/composables'
import { isValidEmail, getPasswordStrength, getPasswordStrengthText } from '@/utils'

const router = useRouter()
const authStore = useAuthStore()
const { success, error } = useNotification()

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)
const testLoading = ref(false)
const socialLoading = reactive({
  google: false,
  github: false
})

// 对话框显示状态
const termsVisible = ref(false)
const privacyVisible = ref(false)

// 表单数据
const form = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

// 密码强度
const passwordStrength = ref({
  level: 'weak',
  percentage: 0,
  text: '弱'
})

// 表单验证规则
const rules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
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
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  agreement: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请阅读并同意服务条款和隐私政策'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

/**
 * 检查密码强度
 */
const checkPasswordStrength = () => {
  const strength = getPasswordStrength(form.password)
  
  const levels = ['weak', 'weak', 'fair', 'good', 'strong']
  const texts = ['弱', '弱', '一般', '良好', '强']
  const percentages = [20, 20, 40, 70, 100]
  
  passwordStrength.value = {
    level: levels[strength] || 'weak',
    percentage: percentages[strength] || 20,
    text: texts[strength] || '弱'
  }
}

/**
 * 处理表单提交
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    const registerSuccess = await authStore.register({
      name: form.name,
      email: form.email,
      password: form.password,
      confirmPassword: form.confirmPassword
    })
    
    if (registerSuccess) {
      // 注册成功后跳转到首页
      router.push('/app/dashboard')
    }
    
  } catch (err: any) {
    // 错误信息已在 auth store 中处理，这里不需要重复显示
    console.error('注册失败:', err)
  } finally {
    loading.value = false
  }
}

/**
 * 处理第三方注册
 */
const handleSocialRegister = async (provider: 'google' | 'github') => {
  try {
    socialLoading[provider] = true
    
    // 调用第三方注册API
    await authStore.socialLogin(provider)
    
    success(`${provider === 'google' ? 'Google' : 'GitHub'} 注册成功`)
    
    // 重定向到首页
    router.push('/app/dashboard')
    
  } catch (err: any) {
    error(err.message || '第三方注册失败')
  } finally {
    socialLoading[provider] = false
  }
}

/**
 * 显示服务条款
 */
const showTerms = () => {
  termsVisible.value = true
}

/**
 * 显示隐私政策
 */
const showPrivacy = () => {
  privacyVisible.value = true
}

/**
 * 测试API连接
 */
const testApiConnection = async () => {
  testLoading.value = true
  
  try {
    // 测试基础连接 - 使用相对路径，通过Vite代理转发
    const response = await fetch('/api/auth/check-email?email=test@example.com', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (response.ok) {
      const data = await response.json()
      success('🎉 API连接测试成功！后端服务正常运行')
      console.log('API测试结果:', data)
    } else {
      error(`❌ API连接失败：HTTP ${response.status}`)
      console.error('API测试失败:', response.status, response.statusText)
    }
  } catch (err: any) {
    error(`❌ 网络连接失败：${err.message}`)
    console.error('网络连接错误:', err)
    
    // 提供详细的错误信息
    if (err.name === 'TypeError' && err.message.includes('fetch')) {
      error('🔧 可能的解决方案：\n1. 检查后端服务是否在 http://localhost:3001 运行\n2. 检查CORS配置\n3. 检查防火墙设置')
    }
  } finally {
    testLoading.value = false
  }
}

/**
 * 组件挂载时检查是否已登录
 */
onMounted(() => {
  if (authStore.isAuthenticated) {
    router.push('/app/dashboard')
  }
})
</script>

<style scoped>
.register-view {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.register-form {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.register-form__header {
  text-align: center;
  margin-bottom: 32px;
}

.register-form__title {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
}

.register-form__subtitle {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin: 0;
}

.register-form__content {
  margin-bottom: 24px;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

/* 密码强度指示器 */
.password-strength {
  margin-top: 8px;
}

.password-strength__bar {
  height: 4px;
  background-color: var(--el-border-color-light);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 4px;
}

.password-strength__fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 2px;
}

.password-strength__fill--weak {
  background-color: #f56c6c;
}

.password-strength__fill--fair {
  background-color: #e6a23c;
}

.password-strength__fill--good {
  background-color: #409eff;
}

.password-strength__fill--strong {
  background-color: #67c23a;
}

.password-strength__text {
  font-size: 12px;
  font-weight: 500;
}

.password-strength__text--weak {
  color: #f56c6c;
}

.password-strength__text--fair {
  color: #e6a23c;
}

.password-strength__text--good {
  color: #409eff;
}

.password-strength__text--strong {
  color: #67c23a;
}

/* 协议链接 */
.agreement-link {
  color: var(--el-color-primary);
  text-decoration: none;
  transition: color 0.2s;
}

.agreement-link:hover {
  color: var(--el-color-primary-dark-2);
}

/* 第三方注册样式 */
.social-register {
  margin-bottom: 24px;
}

.social-register__divider {
  position: relative;
  text-align: center;
  margin-bottom: 20px;
}

.social-register__divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background-color: var(--el-border-color-light);
}

.social-register__divider span {
  background: white;
  padding: 0 16px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.social-register__buttons {
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
.register-form__footer {
  text-align: center;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.login-link {
  color: var(--el-color-primary);
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
  transition: color 0.2s;
}

.login-link:hover {
  color: var(--el-color-primary-dark-2);
}

/* 对话框内容 */
.terms-content,
.privacy-content {
  max-height: 400px;
  overflow-y: auto;
  line-height: 1.6;
  color: var(--el-text-color-regular);
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-form {
    padding: 24px 20px;
    margin: 0 16px;
  }
  
  .register-form__title {
    font-size: 20px;
  }
  
  .social-register__buttons {
    flex-direction: column;
  }
  
  .social-btn {
    width: 100%;
  }
}

/* 暗色主题适配 */
.dark .register-form {
  background: var(--el-bg-color-page);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.dark .social-register__divider span {
  background: var(--el-bg-color-page);
}

.dark .social-btn {
  background: var(--el-bg-color);
  border-color: var(--el-border-color);
}
</style>