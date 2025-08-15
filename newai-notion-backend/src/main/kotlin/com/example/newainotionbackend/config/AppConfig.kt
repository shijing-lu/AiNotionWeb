package com.example.newainotionbackend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*

/**
 * 应用程序配置类
 * 配置CORS、静态资源、邮件发送等
 */
@Configuration
class AppConfig(private val mailProperties: MailProperties) : WebMvcConfigurer {
    
    /**
     * 配置CORS跨域访问
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }
    
    /**
     * 配置静态资源处理
     */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/")
            .setCachePeriod(3600)
    }
    
    /**
     * 配置JavaMailSender Bean
     */
    @Bean
    fun javaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = mailProperties.host
        mailSender.port = mailProperties.port
        mailSender.username = mailProperties.username
        mailSender.password = mailProperties.password
        mailSender.protocol = mailProperties.protocol
        mailSender.defaultEncoding = mailProperties.encoding
        
        val props = Properties()
        props["mail.smtp.auth"] = mailProperties.smtpAuth
        props["mail.smtp.starttls.enable"] = mailProperties.smtpStarttlsEnable
        props["mail.smtp.timeout"] = "25000"
        props["mail.smtp.connectiontimeout"] = "25000"
        props["mail.smtp.writetimeout"] = "25000"
        
        mailSender.javaMailProperties = props
        return mailSender
    }
}

/**
 * 文件上传配置属性
 */
@Component
@ConfigurationProperties(prefix = "app.file")
data class FileUploadProperties(
    var uploadDir: String = "uploads",
    var maxFileSize: String = "10MB",
    var maxRequestSize: String = "50MB",
    var allowedExtensions: List<String> = listOf(
        "jpg", "jpeg", "png", "gif", "bmp", "webp",
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "txt", "md", "zip", "rar", "7z"
    ),
    var imageExtensions: List<String> = listOf(
        "jpg", "jpeg", "png", "gif", "bmp", "webp"
    )
)

/**
 * JWT配置属性
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    var secret: String = "newai-notion-secret-key-2024",
    var accessTokenExpiration: Long = 86400000, // 24小时
    var refreshTokenExpiration: Long = 604800000, // 7天
    var issuer: String = "newai-notion"
)

/**
 * 邮件配置属性
 */
@Component
@ConfigurationProperties(prefix = "app.mail")
data class MailProperties(
    var host: String = "smtp.gmail.com",
    var port: Int = 587,
    var username: String = "",
    var password: String = "",
    var protocol: String = "smtp",
    var encoding: String = "UTF-8",
    var smtpAuth: Boolean = true,
    var smtpStarttlsEnable: Boolean = true,
    var fromEmail: String = "noreply@newainotion.com",
    var fromName: String = "NewAI Notion"
)

/**
 * AI配置属性
 */
@Component
@ConfigurationProperties(prefix = "app.ai")
data class AiProperties(
    var openaiApiKey: String = "",
    var openaiBaseUrl: String = "https://api.openai.com/v1",
    var defaultModel: String = "gpt-3.5-turbo",
    var maxTokens: Int = 2048,
    var temperature: Double = 0.7,
    var requestTimeout: Long = 30000, // 30秒
    var maxRequestsPerMinute: Int = 60,
    var maxRequestsPerDay: Int = 1000
)