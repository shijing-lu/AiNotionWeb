<template>
  <div class="auth-layout">
    <div class="auth-layout__background">
      <!-- 背景装饰 -->
      <div class="background-decoration">
        <div class="decoration-circle decoration-circle--1"></div>
        <div class="decoration-circle decoration-circle--2"></div>
        <div class="decoration-circle decoration-circle--3"></div>
      </div>
    </div>
    
    <div class="auth-layout__container">
      <!-- 左侧内容 -->
      <div class="auth-layout__left">
        <div class="brand">
          <img src="/logo.svg" alt="AI智能笔记" class="brand__logo" />
          <h1 class="brand__title">AI智能笔记</h1>
          <p class="brand__subtitle">让AI助力您的知识管理</p>
        </div>
        
        <div class="features">
          <div class="feature-item">
            <div class="feature-item__icon">
              <el-icon size="24"><MagicStick /></el-icon>
            </div>
            <div class="feature-item__content">
              <h3>AI智能助手</h3>
              <p>强大的AI功能帮助您更高效地创作和管理内容</p>
            </div>
          </div>
          
          <div class="feature-item">
            <div class="feature-item__icon">
              <el-icon size="24"><Document /></el-icon>
            </div>
            <div class="feature-item__content">
              <h3>富文本编辑</h3>
              <p>支持Markdown、表格、图片等多种格式的富文本编辑</p>
            </div>
          </div>
          
          <div class="feature-item">
            <div class="feature-item__icon">
              <el-icon size="24"><Share /></el-icon>
            </div>
            <div class="feature-item__content">
              <h3>协作分享</h3>
              <p>轻松分享文档，支持多人协作编辑</p>
            </div>
          </div>
          
          <div class="feature-item">
            <div class="feature-item__icon">
              <el-icon size="24"><Lock /></el-icon>
            </div>
            <div class="feature-item__content">
              <h3>安全可靠</h3>
              <p>企业级安全保障，数据加密存储</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧表单 -->
      <div class="auth-layout__right">
        <div class="auth-form">
          <router-view v-slot="{ Component }">
            <transition name="slide-fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
        
        <!-- 底部链接 -->
        <div class="auth-footer">
          <div class="auth-footer__links">
            <router-link to="/" class="footer-link">首页</router-link>
            <a href="#" class="footer-link">帮助中心</a>
            <a href="#" class="footer-link">隐私政策</a>
            <a href="#" class="footer-link">服务条款</a>
          </div>
          <div class="auth-footer__copyright">
            © 2024 AI智能笔记. All rights reserved.
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { MagicStick, Document, Share, Lock } from '@element-plus/icons-vue'
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.auth-layout__background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.background-decoration {
  position: relative;
  width: 100%;
  height: 100%;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.decoration-circle--1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.decoration-circle--2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.decoration-circle--3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.auth-layout__container {
  position: relative;
  z-index: 1;
  display: flex;
  min-height: 100vh;
}

.auth-layout__left {
  flex: 1;
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: white;
}

.brand {
  margin-bottom: 60px;
}

.brand__logo {
  width: 64px;
  height: 64px;
  margin-bottom: 24px;
  filter: brightness(0) invert(1);
}

.brand__title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 16px 0;
  background: linear-gradient(45deg, #fff, #f0f0f0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand__subtitle {
  font-size: 20px;
  margin: 0;
  opacity: 0.9;
  font-weight: 300;
}

.features {
  display: grid;
  gap: 32px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.feature-item__icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.feature-item__content h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: white;
}

.feature-item__content p {
  font-size: 14px;
  margin: 0;
  opacity: 0.8;
  line-height: 1.5;
}

.auth-layout__right {
  flex: 0 0 480px;
  background: white;
  display: flex;
  flex-direction: column;
  position: relative;
}

.auth-form {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 48px;
}

.auth-footer {
  padding: 24px 48px;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color-page);
}

.auth-footer__links {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 16px;
}

.footer-link {
  color: var(--el-text-color-regular);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.footer-link:hover {
  color: var(--el-color-primary);
}

.auth-footer__copyright {
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

/* 过渡动画 */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1.0, 0.5, 0.8, 1.0);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(20px);
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .auth-layout__left {
    padding: 40px;
  }
  
  .auth-layout__right {
    flex: 0 0 400px;
  }
  
  .auth-form {
    padding: 40px 32px;
  }
  
  .auth-footer {
    padding: 20px 32px;
  }
}

@media (max-width: 768px) {
  .auth-layout__container {
    flex-direction: column;
  }
  
  .auth-layout__left {
    flex: 0 0 auto;
    padding: 40px 24px;
    text-align: center;
  }
  
  .brand__title {
    font-size: 36px;
  }
  
  .brand__subtitle {
    font-size: 18px;
  }
  
  .features {
    display: none;
  }
  
  .auth-layout__right {
    flex: 1;
  }
  
  .auth-form {
    padding: 32px 24px;
  }
  
  .auth-footer {
    padding: 20px 24px;
  }
  
  .auth-footer__links {
    flex-wrap: wrap;
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .auth-layout__left {
    padding: 24px 16px;
  }
  
  .brand__title {
    font-size: 28px;
  }
  
  .brand__subtitle {
    font-size: 16px;
  }
  
  .auth-form {
    padding: 24px 16px;
  }
  
  .auth-footer {
    padding: 16px;
  }
  
  .auth-footer__links {
    gap: 12px;
  }
  
  .footer-link {
    font-size: 13px;
  }
}
</style>