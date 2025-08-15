<template>
  <div class="test-connection">
    <h2>API连接测试</h2>
    
    <div class="test-section">
      <h3>基础连接测试</h3>
      <el-button @click="testBasicConnection" :loading="loading.basic">测试基础连接</el-button>
      <div v-if="results.basic" class="result">
        <p>状态: {{ results.basic.status }}</p>
        <p>消息: {{ results.basic.message }}</p>
      </div>
    </div>

    <div class="test-section">
      <h3>注册API测试</h3>
      <el-form :model="registerForm" label-width="80px">
        <el-form-item label="邮箱">
          <el-input v-model="registerForm.email" placeholder="test@example.com" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" placeholder="testuser" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.password" type="password" placeholder="123456" />
        </el-form-item>
        <el-form-item>
          <el-button @click="testRegister" :loading="loading.register">测试注册</el-button>
        </el-form-item>
      </el-form>
      <div v-if="results.register" class="result">
        <p>状态: {{ results.register.status }}</p>
        <p>消息: {{ results.register.message }}</p>
        <pre v-if="results.register.data">{{ JSON.stringify(results.register.data, null, 2) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElButton, ElForm, ElFormItem, ElInput, ElMessage } from 'element-plus'
import api from '@/api'
import { authApi } from '@/api/auth'

const loading = reactive({
  basic: false,
  register: false
})

const results = reactive({
  basic: null as any,
  register: null as any
})

const registerForm = reactive({
  email: 'test@example.com',
  username: 'testuser',
  password: '123456'
})

// 测试基础连接
const testBasicConnection = async () => {
  loading.basic = true
  try {
    const response = await api.get('/health')
    results.basic = {
      status: 'success',
      message: '连接成功',
      data: response
    }
    ElMessage.success('基础连接测试成功')
  } catch (error: any) {
    results.basic = {
      status: 'error',
      message: error.message || '连接失败',
      error: error
    }
    ElMessage.error('基础连接测试失败')
  } finally {
    loading.basic = false
  }
}

// 测试注册API
const testRegister = async () => {
  loading.register = true
  try {
    const response = await authApi.register({
      email: registerForm.email,
      username: registerForm.username,
      password: registerForm.password
    })
    results.register = {
      status: 'success',
      message: '注册测试成功',
      data: response
    }
    ElMessage.success('注册API测试成功')
  } catch (error: any) {
    results.register = {
      status: 'error',
      message: error.message || '注册失败',
      error: error
    }
    ElMessage.error('注册API测试失败')
  } finally {
    loading.register = false
  }
}
</script>

<style scoped>
.test-connection {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.test-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.result {
  margin-top: 15px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.result pre {
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
}
</style>