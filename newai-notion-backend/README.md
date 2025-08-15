# NewAI Notion Backend 🚀

基于 Spring Boot 3.x + Kotlin 的 AI 智能笔记软件后端服务

## 📋 项目简介

NewAI Notion Backend 是一个现代化的笔记管理系统后端，集成了 AI 功能，支持文档管理、用户认证、文件上传、评论系统等核心功能。

## ✨ 核心特性

### 🔐 认证授权
- JWT 令牌认证
- 用户注册/登录
- 邮箱验证
- 密码重置
- 权限控制

### 📝 文档管理
- 文档创建/编辑/删除
- 文件夹组织
- 文档搜索
- 版本控制
- 收藏/置顶
- 公开/私有设置

### 💬 评论系统
- 文档评论
- 嵌套回复
- 用户提及
- 评论解决状态

### 🤖 AI 集成
- OpenAI API 集成
- 智能文档生成
- 内容优化建议
- 自动摘要

### 📁 文件管理
- 文件上传/下载
- 图片处理
- 缩略图生成
- 文件类型验证

### 📧 邮件服务
- 邮箱验证
- 密码重置
- 通知邮件
- HTML 模板支持

## 🛠️ 技术栈

### 后端框架
- **Spring Boot 3.2.0** - 主框架
- **Kotlin 1.9.20** - 编程语言
- **Spring Security** - 安全框架
- **Spring Data JPA** - 数据访问层
- **Spring Data MongoDB** - NoSQL 数据库

### 数据库
- **MySQL 8.0+** - 关系型数据库
- **MongoDB** - 文档数据库
- **Redis** - 缓存数据库

### 工具库
- **JWT (JJWT)** - 令牌认证
- **SpringDoc OpenAPI** - API 文档
- **Jackson** - JSON 处理
- **Thumbnailator** - 图片处理
- **Apache Tika** - 文件类型检测

## 🚀 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- MongoDB 4.4+
- Redis 6.0+ (可选)
- Gradle 8.5+

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/your-repo/newai-notion-backend.git
cd newai-notion-backend
```

2. **配置数据库**
```sql
-- 创建 MySQL 数据库
CREATE DATABASE newai_notion CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **配置环境变量**
```bash
# 复制配置文件
cp src/main/resources/application.yml.example src/main/resources/application.yml

# 设置环境变量
export JWT_SECRET=your-jwt-secret-key
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-email-password
export OPENAI_API_KEY=your-openai-api-key
```

4. **构建并运行**
```bash
# 构建项目
./gradlew build

# 运行应用
./gradlew bootRun
```

5. **访问应用**
- API 服务: http://localhost:8080/api
- API 文档: http://localhost:8080/swagger-ui.html
- 健康检查: http://localhost:8080/actuator/health

## 📚 API 文档

### 认证接口
```
POST /api/v1/auth/register     # 用户注册
POST /api/v1/auth/login        # 用户登录
POST /api/v1/auth/refresh      # 刷新令牌
POST /api/v1/auth/logout       # 用户登出
```

### 文档接口
```
GET    /api/v1/documents       # 获取文档列表
POST   /api/v1/documents       # 创建文档
GET    /api/v1/documents/{id}  # 获取文档详情
PUT    /api/v1/documents/{id}  # 更新文档
DELETE /api/v1/documents/{id}  # 删除文档
```

### 用户接口
```
GET    /api/v1/users/profile   # 获取用户信息
PUT    /api/v1/users/profile   # 更新用户信息
POST   /api/v1/users/avatar    # 上传头像
```

完整的 API 文档请访问: http://localhost:8080/swagger-ui.html 

## 🗂️ 项目结构

```
src/main/kotlin/com/example/newainotionbackend/
├── config/                 # 配置类
│   ├── AppConfig.kt       # 应用配置
│   ├── SecurityConfig.kt  # 安全配置
│   ├── DatabaseConfig.kt  # 数据库配置
│   └── SwaggerConfig.kt   # API文档配置
├── controller/            # 控制器层
│   ├── AuthController.kt  # 认证控制器
│   ├── UserController.kt  # 用户控制器
│   ├── DocumentController.kt # 文档控制器
│   └── FolderController.kt   # 文件夹控制器
├── service/               # 服务层
│   ├── UserService.kt     # 用户服务
│   ├── DocumentService.kt # 文档服务
│   └── FolderService.kt   # 文件夹服务
├── repository/            # 数据访问层
│   ├── UserRepository.kt  # 用户仓库
│   ├── DocumentRepository.kt # 文档仓库
│   └── CommentRepository.kt  # 评论仓库
├── entity/                # 实体类
│   ├── User.kt           # 用户实体
│   ├── Document.kt       # 文档实体
│   └── Comment.kt        # 评论实体
├── dto/                   # 数据传输对象
│   ├── ApiResponse.kt    # 统一响应格式
│   ├── UserDto.kt        # 用户DTO
│   └── DocumentDto.kt    # 文档DTO
├── util/                  # 工具类
│   ├── JwtUtil.kt        # JWT工具
│   ├── PasswordUtil.kt   # 密码工具
│   ├── EmailUtil.kt      # 邮件工具
│   └── FileUtil.kt       # 文件工具
└── filter/                # 过滤器
    └── JwtAuthenticationFilter.kt # JWT认证过滤器
```

## 🔧 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/newai_notion
    username: root
    password: your-password
  data:
    mongodb:
      uri: mongodb://localhost:27017/newai_notion
```

### JWT 配置
```yaml
app:
  jwt:
    secret: your-secret-key
    access-token-expiration: 86400000  # 24小时
    refresh-token-expiration: 604800000 # 7天
```

### 文件上传配置
```yaml
app:
  file:
    upload-dir: uploads
    max-file-size: 10MB
    allowed-extensions: [jpg, jpeg, png, pdf, doc, docx]
```

## 🧪 测试

```bash
# 运行所有测试
./gradlew test

# 运行特定测试
./gradlew test --tests "*UserServiceTest*"

# 生成测试报告
./gradlew test jacocoTestReport
```

## 📦 部署

### Docker 部署
```bash
# 构建镜像
docker build -t newai-notion-backend .

# 运行容器
docker run -p 8080:8080 newai-notion-backend
```

### 生产环境
```bash
# 构建生产包
./gradlew bootJar

# 运行
java -jar build/libs/newai-notion-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系我们

- 项目主页: https://github.com/your-repo/newai-notion-backend
- 问题反馈: https://github.com/your-repo/newai-notion-backend/issues
- 邮箱: support@newainotion.com

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！

---

⭐ 如果这个项目对你有帮助，请给我们一个 Star！