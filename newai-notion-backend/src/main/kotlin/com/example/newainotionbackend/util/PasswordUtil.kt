package com.example.newainotionbackend.util

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*

@Component
class PasswordUtil {
    
    private val passwordEncoder = BCryptPasswordEncoder()
    private val secureRandom = SecureRandom()
    
    /**
     * 加密密码
     */
    fun encodePassword(rawPassword: String): String {
        return passwordEncoder.encode(rawPassword)
    }
    
    /**
     * 验证密码
     */
    fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }
    
    /**
     * 生成随机密码
     */
    fun generateRandomPassword(length: Int = 12): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        return (1..length)
            .map { chars[secureRandom.nextInt(chars.length)] }
            .joinToString("")
    }
    
    /**
     * 生成随机令牌
     */
    fun generateRandomToken(length: Int = 32): String {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
    
    /**
     * 验证密码强度（已简化，不再强制复杂性要求）
     */
    fun validatePasswordStrength(password: String): PasswordStrength {
        // 简化密码强度验证，只根据长度判断
        return when {
            password.length < 6 -> PasswordStrength.WEAK
            password.length < 12 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
    
    /**
     * 检查密码是否符合最低要求（已简化，不再强制复杂性要求）
     */
    fun isPasswordValid(password: String): Boolean {
        // 只检查密码不为空且长度大于0
        return password.isNotBlank()
    }
}

enum class PasswordStrength {
    WEAK,
    MEDIUM,
    STRONG
}