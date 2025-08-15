<template>
  <div class="test-api-view">
    <h2>API 连接测试</h2>
    
    <div class="test-section">
      <h3>测试后端连接</h3>
      <el-button @click="testConnection" :loading="loading.connection">
        测试连接
      </el-button>
      <div v-if="results.connection" class="result">
        <pre>{{ results.connection }}</pre>
      </div>
    </div>
    
    <div class="test-section">
      <h3>测试登录API</h3>
      <el-form :model="loginForm" style="max-width: 400px;">
        <el-form-item label="邮箱">
          <el-input v-model="loginForm.email" placeholder="test@example.com" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="123456" />
        </el-form-item>
        <el-form-item>
          <el-button @click="testLogin" :loading="loading.login" type="primary">
            测试登录
          </el-button>
        </el-form-item>
      </el-form>
      <div v-if="results.login" class="result">
        <pre>{{ results.login }}</pre>
      </div>
    </div>
    
    <div class="test-section">
      <h3>测试注册API</h3>
      <el-form :model="registerForm" style="max-width: 400px;">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" placeholder="testuser" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="registerForm.email" placeholder="test@example.com" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.password" type="password" placeholder="123456" />
        </el-form-item>
        <el-form-item>
          <el-button @click="testRegister" :loading="loading.register" type="primary">
            测试注册
          </el-button>
        </el-form-item>
      </el-form>
      <div v-if="results.register" class="result">
        <pre>{{ results.register }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { authApi } from '@/api/modules'
import { apiService } from '@/api'

// 加载状态
const loading = reactive({
  connection: false,
  login: false,
  register: false
})

// 测试结果
const results = reactive({
  connection: '',
  login: '',
  register: ''
})

// 表单数据
const loginForm = reactive({
  email: 'test@example.com',
  password: '123456',
  rememberMe: false
})

const registerForm = reactive({
  username: 'testuser',
  email: 'test@example.com',
  password: '123456'
})

/**
 * 测试基础连接
 */
const testConnection = async () => {
  loading.connection = true
  try {
    // 直接使用fetch测试连接
    const response = await fetch('http://localhost:3001/api/auth/check-email?email=test@example.com')
    const data = await response.json()
    results.connection = JSON.stringify({
      status: response.status,
      statusText: response.statusText,
      data: data
    }, null, 2)
  } catch (error: any) {
    results.connection = JSON.stringify({
      error: error.message,
      stack: error.stack
    }, null, 2)
  } finally {
    loading.connection = false
  }
}

/**
 * 测试登录API
 */
const testLogin = async () => {
  loading.login = true
  try {
    const response = await authApi.login(loginForm)
    results.login = JSON.stringify(response, null, 2)
  } catch (error: any) {
    results.login = JSON.stringify({
      error: error.message,
      response: error.response?.data,
      status: error.response?.status
    }, null, 2)
  } finally {
    loading.login = false
  }
}

/**
 * 测试注册API
 */
const testRegister = async () => {
  loading.register = true
  try {
    const response = await authApi.register(registerForm)
    results.register = JSON.stringify(response, null, 2)
  } catch (error: any) {
    results.register = JSON.stringify({
      error: error.message,
      response: error.response?.data,
      status: error.response?.status
    }, null, 2)
  } finally {
    loading.register = false
  }
}
</script>

<style scoped>
.test-api-view {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.test-section {
  margin-bottom: 40px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.result {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
  max-height: 300px;
  overflow-y: auto;
}

h2 {
  color: #333;
  margin-bottom: 30px;
}

h3 {
  color: #666;
  margin-bottom: 15px;
}
</style>