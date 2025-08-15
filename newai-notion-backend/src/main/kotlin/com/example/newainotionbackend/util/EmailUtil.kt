package com.example.newainotionbackend.util


import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import jakarta.mail.internet.MimeMessage

@Component
class EmailUtil(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
    private val mailProperties: com.example.newainotionbackend.config.MailProperties
) {
    
    private val fromEmail: String
        get() = mailProperties.fromEmail
    
    /**
     * 发送邮箱验证邮件
     */
    fun sendVerificationEmail(toEmail: String, username: String, verificationToken: String, baseUrl: String) {
        val context = Context().apply {
            setVariable("username", username)
            setVariable("verificationUrl", "$baseUrl/auth/verify-email?token=$verificationToken")
            setVariable("baseUrl", baseUrl)
        }
        
        val htmlContent = templateEngine.process("email/verification", context)
        
        sendHtmlEmail(
            to = toEmail,
            subject = "验证您的邮箱地址 - NewAI Notion",
            htmlContent = htmlContent
        )
    }
    
    /**
     * 发送密码重置邮件
     */
    fun sendPasswordResetEmail(toEmail: String, username: String, resetToken: String, baseUrl: String) {
        val context = Context().apply {
            setVariable("username", username)
            setVariable("resetUrl", "$baseUrl/auth/reset-password?token=$resetToken")
            setVariable("baseUrl", baseUrl)
        }
        
        val htmlContent = templateEngine.process("email/reset-password", context)
        
        sendHtmlEmail(
            to = toEmail,
            subject = "重置您的密码 - NewAI Notion",
            htmlContent = htmlContent
        )
    }
    
    /**
     * 发送欢迎邮件
     */
    fun sendWelcomeEmail(toEmail: String, username: String, baseUrl: String) {
        val context = Context().apply {
            setVariable("username", username)
            setVariable("baseUrl", baseUrl)
            setVariable("loginUrl", "$baseUrl/login")
        }
        
        val htmlContent = templateEngine.process("email/welcome", context)
        
        sendHtmlEmail(
            to = toEmail,
            subject = "欢迎加入 NewAI Notion！",
            htmlContent = htmlContent
        )
    }
    
    /**
     * 发送登录提醒邮件
     */
    fun sendLoginAlertEmail(toEmail: String, username: String, loginTime: String, ipAddress: String, location: String?) {
        val context = Context().apply {
            setVariable("username", username)
            setVariable("loginTime", loginTime)
            setVariable("ipAddress", ipAddress)
            setVariable("location", location ?: "未知位置")
        }
        
        val htmlContent = templateEngine.process("email/login-alert", context)
        
        sendHtmlEmail(
            to = toEmail,
            subject = "账户登录提醒 - NewAI Notion",
            htmlContent = htmlContent
        )
    }
    
    /**
     * 发送文档分享通知邮件
     */
    fun sendDocumentShareEmail(
        toEmail: String, 
        recipientName: String, 
        senderName: String, 
        documentTitle: String, 
        shareUrl: String
    ) {
        val context = Context().apply {
            setVariable("recipientName", recipientName)
            setVariable("senderName", senderName)
            setVariable("documentTitle", documentTitle)
            setVariable("shareUrl", shareUrl)
        }
        
        val htmlContent = templateEngine.process("email/document-share", context)
        
        sendHtmlEmail(
            to = toEmail,
            subject = "$senderName 与您分享了文档：$documentTitle",
            htmlContent = htmlContent
        )
    }
    
    /**
     * 发送评论通知邮件
     */
    fun sendCommentNotificationEmail(
        toEmail: String,
        recipientName: String,
        commenterName: String,
        documentTitle: String,
        commentContent: String,
        documentUrl: String
    ) {
        val context = Context().apply {
            setVariable("recipientName", recipientName)
            setVariable("commenterName", commenterName)
            setVariable("documentTitle", documentTitle)
            setVariable("commentContent", commentContent)
            setVariable("documentUrl", documentUrl)
        }
        
        val htmlContent = templateEngine.process("email/comment-notification", context)
        
        sendHtmlEmail(
            to = toEmail,
            subject = "$commenterName 评论了您的文档：$documentTitle",
            htmlContent = htmlContent
        )
    }
    
    /**
     * 发送HTML邮件
     */
    private fun sendHtmlEmail(to: String, subject: String, htmlContent: String) {
        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            
            helper.setFrom(fromEmail)
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(htmlContent, true)
            
            mailSender.send(message)
        } catch (e: Exception) {
            throw RuntimeException("发送邮件失败: ${e.message}", e)
        }
    }
    
    /**
     * 验证邮箱地址格式
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }
}