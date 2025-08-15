<template>
  <div class="api-test-container">
    <el-card class="test-card">
      <template #header>
        <h2>🔧 API连接测试</h2>
      </template>
      
      <!-- 基础连接测试 -->
      <div class="test-section">
        <h3>📡 基础连接测试</h3>
        <el-button 
          @click="testBasicConnection" 
          :loading="loading.basic"
          type="primary"
        >
          测试后端连接
        </el-button>
        <div v-if="results.basic" class="result-box">
          <el-alert 
            :type="results.basic.success ? 'success' : 'error'"
            :title="results.basic.message"
            show-icon
          />
          <pre v-if="results.basic.data" class="result-data">{{ JSON.stringify(results.basic.data, null, 2) }}</pre>
        </div>
      </div>

      <!-- 注册API测试 -->
      <div class="test-section">
        <h3>👤 注册API测试</h3>
        <el-form :model="registerForm" label-width="100px" class="test-form">
          <el-form-item label="邮箱">
            <el-input 
              v-model="registerForm.email" 
              placeholder="输入测试邮箱"
              type="email"
            />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input 
              v-model="registerForm.username" 
              placeholder="输入用户名"
            />
          </el-form-item>
          <el-form-item label="密码">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="输入密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              @click="testRegister" 
              :loading="loading.register"
              type="success"
            >
              测试注册API
            </el-button>
            <el-button @click="generateRandomUser" type="info">
              生成随机用户
            </el-button>
          </el-form-item>
        </el-form>
        <div v-if="results.register" class="result-box">
          <el-alert 
            :type="results.register.success ? 'success' : 'error'"
            :title="results.register.message"
            show-icon
          />
          <pre v-if="results.register.data" class="result-data">{{ JSON.stringify(results.register.data, null, 2) }}</pre>
          <pre v-if="results.register.error" class="error-data">{{ JSON.stringify(results.register.error, null, 2) }}</pre>
        </div>
      </div>

      <!-- 网络诊断 -->
      <div class="test-section">
        <h3>🔍 网络诊断</h3>
        <el-button 
          @click="testNetworkDiagnostic" 
          :loading="loading.diagnostic"
          type="warning"
        >
          运行网络诊断
        </el-button>
        <div v-if="results.diagnostic" class="result-box">
          <div v-for="(test, index) in results.diagnostic" :key="index" class="diagnostic-item">
            <el-tag :type="test.success ? 'success' : 'danger'">
              {{ test.name }}: {{ test.success ? '✅ 成功' : '❌ 失败' }}
            </el-tag>
            <p class="diagnostic-message">{{ test.message }}</p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElCard, ElButton, ElForm, ElFormItem, ElInput, ElAlert, ElTag, ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'
import api from '@/api'

/**
 * 加载状态管理
 */
const loading = reactive({
  basic: false,
  register: false,
  diagnostic: false
})

/**
 * 测试结果存储
 */
const results = reactive({
  basic: null as any,
  register: null as any,
  diagnostic: null as any
})

/**
 * 注册表单数据
 */
const registerForm = reactive({
  email: 'test@example.com',
  username: 'testuser',
  password: '123456'
})

/**
 * 测试基础连接
 */
const testBasicConnection = async () => {
  loading.basic = true
  try {
    // 测试邮箱检查API
    const response = await fetch('http://localhost:3001/api/auth/check-email?email=test@example.com')
    const data = await response.json()
    
    results.basic = {
      success: response.ok,
      message: response.ok ? '✅ 后端连接成功' : '❌ 后端连接失败',
      data: data
    }
    
    if (response.ok) {
      ElMessage.success('后端API连接正常！')
    } else {
      ElMessage.error('后端API连接失败！')
    }
  } catch (error: any) {
    results.basic = {
      success: false,
      message: '❌ 网络连接失败',
      error: {
        name: error.name,
        message: error.message,
        stack: error.stack
      }
    }
    ElMessage.error('网络连接失败：' + error.message)
  } finally {
    loading.basic = false
  }
}

/**
 * 测试注册API
 */
const testRegister = async () => {
  loading.register = true
  try {
    const response = await authApi.register({
      email: registerForm.email,
      username: registerForm.username,
      password: registerForm.password
    })
    
    results.register = {
      success: true,
      message: '✅ 注册API测试成功',
      data: response
    }
    ElMessage.success('注册API测试成功！')
  } catch (error: any) {
    results.register = {
      success: false,
      message: '❌ 注册API测试失败',
      error: {
        name: error.name,
        message: error.message,
        response: error.response?.data,
        status: error.response?.status,
        stack: error.stack
      }
    }
    ElMessage.error('注册API测试失败：' + error.message)
  } finally {
    loading.register = false
  }
}

/**
 * 生成随机用户数据
 */
const generateRandomUser = () => {
  const timestamp = Date.now()
  registerForm.email = `test${timestamp}@example.com`
  registerForm.username = `testuser${timestamp}`
  registerForm.password = '123456'
  ElMessage.info('已生成随机用户数据')
}

/**
 * 网络诊断测试
 */
const testNetworkDiagnostic = async () => {
  loading.diagnostic = true
  const diagnosticTests = []
  
  try {
    // 测试1: 基础网络连接
    try {
      const response = await fetch('http://localhost:3001')
      diagnosticTests.push({
        name: '基础网络连接',
        success: response.ok,
        message: response.ok ? '可以访问后端服务器' : `HTTP ${response.status}`,
      })
    } catch (error: any) {
      diagnosticTests.push({
        name: '基础网络连接',
        success: false,
        message: `连接失败: ${error.message}`,
      })
    }
    
    // 测试2: CORS预检请求
    try {
      const response = await fetch('http://localhost:3001/api/auth/check-email?email=test@example.com', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      })
      diagnosticTests.push({
        name: 'CORS配置',
        success: response.ok,
        message: response.ok ? 'CORS配置正常' : `CORS错误: HTTP ${response.status}`,
      })
    } catch (error: any) {
      diagnosticTests.push({
        name: 'CORS配置',
        success: false,
        message: `CORS失败: ${error.message}`,
      })
    }
    
    // 测试3: API路由
    try {
      const response = await fetch('http://localhost:3001/api/auth/check-email?email=test@example.com')
      const data = await response.json()
      diagnosticTests.push({
        name: 'API路由',
        success: response.ok && data.success,
        message: response.ok ? 'API路由正常工作' : `API错误: ${data.message || 'Unknown error'}`,
      })
    } catch (error: any) {
      diagnosticTests.push({
        name: 'API路由',
        success: false,
        message: `API路由失败: ${error.message}`,
      })
    }
    
    results.diagnostic = diagnosticTests
    
    const successCount = diagnosticTests.filter(test => test.success).length
    if (successCount === diagnosticTests.length) {
      ElMessage.success(`网络诊断完成：${successCount}/${diagnosticTests.length} 项测试通过`)
    } else {
      ElMessage.warning(`网络诊断完成：${successCount}/${diagnosticTests.length} 项测试通过`)
    }
  } catch (error: any) {
    ElMessage.error('网络诊断失败：' + error.message)
  } finally {
    loading.diagnostic = false
  }
}
</script>

<style scoped>
.api-test-container {
  max-width: 1000px;
  margin: 20px auto;
  padding: 20px;
}

.test-card {
  margin-bottom: 20px;
}

.test-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fafafa;
}

.test-section h3 {
  margin-top: 0;
  color: #303133;
  font-size: 16px;
}

.test-form {
  max-width: 400px;
}

.result-box {
  margin-top: 15px;
  padding: 15px;
  background-color: #fff;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.result-data, .error-data {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.4;
  overflow-x: auto;
  white-space: pre-wrap;
}

.error-data {
  background-color: #fef0f0;
  color: #f56c6c;
}

.diagnostic-item {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.diagnostic-message {
  margin: 5px 0 0 0;
  font-size: 14px;
  color: #606266;
}

.el-button {
  margin-right: 10px;
  margin-bottom: 10px;
}
</style>