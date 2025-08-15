package com.example.newainotionbackend.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

/**
 * 统一API响应格式
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val error: String?,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = data,
                error = null
            )
        }
        
        fun success(message: String): ApiResponse<String> {
            return ApiResponse(
                success = true,
                message = message,
                data = null,
                error = null
            )
        }
        
        fun <T> error(error: String, data: T? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = null,
                data = data,
                error = error
            )
        }
    }
}

/**
 * 分页响应
 */
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
)

/**
 * 用户注册请求
 */
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val displayName: String?
)

/**
 * 用户登录请求
 */
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * 刷新令牌请求
 */
data class RefreshTokenRequest(
    val refreshToken: String
)

/**
 * 邮箱请求
 */
data class EmailRequest(
    val email: String
)

/**
 * 重置密码请求
 */
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

/**
 * 修改密码请求
 */
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

/**
 * 认证响应
 */
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: UserDto
)

/**
 * 令牌响应
 */
data class TokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long
)

/**
 * 可用性响应
 */
data class AvailabilityResponse(
    val available: Boolean
)

/**
 * 更新用户信息请求
 */
data class UpdateUserRequest(
    val displayName: String?,
    val bio: String?,
    val location: String?,
    val website: String?,
    val phone: String?
)

/**
 * 头像响应
 */
data class AvatarResponse(
    val avatarUrl: String
)

/**
 * 用户统计响应
 */
data class UserStatsResponse(
    val documentsCount: Long,
    val foldersCount: Long,
    val wordsCount: Long,
    val viewsCount: Long,
    val likesCount: Long,
    val commentsCount: Long,
    val sharesCount: Long
)

/**
 * 图片上传响应
 */
data class ImageUploadResponse(
    val imageUrl: String
)