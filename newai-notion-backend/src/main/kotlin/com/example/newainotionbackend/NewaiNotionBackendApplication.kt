package com.example.newainotionbackend

import com.example.newainotionbackend.config.*
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * NewAI Notion 后端应用程序主启动类
 * 
 * 功能特性：
 * - Spring Boot 3.x 框架
 * - JWT 认证授权
 * - MySQL + MongoDB 双数据库
 * - 文件上传管理
 * - 邮件发送服务
 * - AI 集成支持
 * - RESTful API 设计
 * - Swagger API 文档
 * - 全局异常处理
 * - CORS 跨域支持
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan("com.example.newainotionbackend.mapper")
@EnableConfigurationProperties(
    FileUploadProperties::class,
    JwtProperties::class,
    MailProperties::class,
    AiProperties::class
)
class NewaiNotionBackendApplication

fun main(args: Array<String>) {
	runApplication<NewaiNotionBackendApplication>(*args)
}
