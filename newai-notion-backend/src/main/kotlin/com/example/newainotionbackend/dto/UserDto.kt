package com.example.newainotionbackend.dto

import com.example.newainotionbackend.entity.User
import com.example.newainotionbackend.entity.UserRole
import com.example.newainotionbackend.entity.UserSettings
import com.example.newainotionbackend.entity.UserStatus
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

/**
 * 用户信息DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val displayName: String?,
    val avatarUrl: String?,
    val phone: String?,
    val bio: String?,
    val location: String?,
    val website: String?,
    val emailVerified: Boolean,
    val phoneVerified: Boolean,
//    val lastLoginAt: LocalDateTime?,
    val loginCount: Int,
    val status: UserStatus,
    val role: UserRole,
//    val createdAt: LocalDateTime,
//    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromEntity(user: User): UserDto {
            return UserDto(
                id = user.id!!,
                username = user.username,
                email = user.email,
                displayName = user.displayName,
                avatarUrl = user.avatarUrl,
                phone = user.phoneNumber,
                bio = user.bio,
                location = user.location,
                website = user.website,
                emailVerified = user.isEmailVerified,
                phoneVerified = user.isPhoneVerified,
                loginCount = user.loginCount.toInt(),
                status = user.status,
                role = user.role,
//                lastLoginAt = TODO(),
//                createdAt = TODO(),
//                updatedAt = TODO(),
            )
        }
    }
}

/**
 * 用户设置DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserSettingsDto(
    val id: Long,
    val userId: Long,
    val theme: String,
    val language: String,
    val timezone: String,
    val dateFormat: String,
    val timeFormat: String,
    val editorTheme: String,
    val editorFontSize: Int,
    val editorFontFamily: String,
    val editorLineHeight: Double,
    val editorTabSize: Int,
    val editorWordWrap: Boolean,
    val editorLineNumbers: Boolean,
    val editorMinimap: Boolean,
    val emailNotifications: Boolean,
    val pushNotifications: Boolean,
    val documentUpdates: Boolean,
    val commentNotifications: Boolean,
    val shareNotifications: Boolean,
    val marketingEmails: Boolean,
    val aiSuggestions: Boolean,
    val aiAutoComplete: Boolean,
    val aiModel: String,
    val aiTemperature: Double,
    val profileVisibility: String,
    val showEmail: Boolean,
    val showPhone: Boolean,
    val showLocation: Boolean,
    val allowSearch: Boolean,
    val twoFactorEnabled: Boolean,
    val sessionTimeout: Int,
    val autoSave: Boolean,
    val autoSaveInterval: Int,
    val defaultDocumentVisibility: String,
    val defaultFolderColor: String,
    val sidebarCollapsed: Boolean,
    val showLineNumbers: Boolean,
    val enableSpellCheck: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromEntity(settings: UserSettings): UserSettingsDto {
            return UserSettingsDto(
                id = settings.id!!,
                userId = settings.userId,
                theme = settings.theme,
                language = settings.language,
                timezone = settings.timezone,
                dateFormat = "yyyy-MM-dd", // 默认日期格式
                timeFormat = "HH:mm:ss", // 默认时间格式
                editorTheme = settings.theme,
                editorFontSize = settings.editorFontSize,
                editorFontFamily = settings.editorFontFamily,
                editorLineHeight = settings.editorLineHeight,
                editorTabSize = 4, // 默认制表符大小
                editorWordWrap = settings.editorWordWrap,
                editorLineNumbers = settings.showLineNumbers,
                editorMinimap = false, // 默认不显示小地图
                emailNotifications = settings.emailNotifications,
                pushNotifications = settings.pushNotifications,
                documentUpdates = settings.pushNotifications,
                commentNotifications = settings.notificationComment,
                shareNotifications = settings.notificationShare,
                marketingEmails = settings.emailNotifications,
                aiSuggestions = settings.aiSmartSuggestions,
                aiAutoComplete = settings.aiAutoComplete,
                aiModel = settings.aiModelPreference,
                aiTemperature = 0.7, // 默认AI温度值
                profileVisibility = settings.profileVisibility,
                showEmail = true, // 默认显示邮箱
                showPhone = false, // 默认不显示电话
                showLocation = false, // 默认不显示位置
                allowSearch = settings.allowSearchIndexing,
                twoFactorEnabled = settings.twoFactorEnabled,
                sessionTimeout = settings.sessionTimeout,
                autoSave = settings.editorAutoSave,
                autoSaveInterval = settings.editorAutoSaveInterval,
                defaultDocumentVisibility = settings.documentDefaultVisibility,
                defaultFolderColor = "#6366f1", // 默认文件夹颜色
                sidebarCollapsed = settings.sidebarCollapsed,
                showLineNumbers = settings.showLineNumbers,
                enableSpellCheck = settings.enableSpellCheck,
                createdAt = settings.createdAt,
                updatedAt = settings.updatedAt
            )
        }
    }
}

/**
 * 更新用户设置请求
 */
data class UpdateUserSettingsRequest(
    val theme: String?,
    val language: String?,
    val timezone: String?,
    val dateFormat: String?,
    val timeFormat: String?,
    val editorTheme: String?,
    val editorFontSize: Int?,
    val editorFontFamily: String?,
    val editorLineHeight: Double?,
    val editorTabSize: Int?,
    val editorWordWrap: Boolean?,
    val editorLineNumbers: Boolean?,
    val editorMinimap: Boolean?,
    val emailNotifications: Boolean?,
    val pushNotifications: Boolean?,
    val documentUpdates: Boolean?,
    val commentNotifications: Boolean?,
    val shareNotifications: Boolean?,
    val marketingEmails: Boolean?,
    val aiSuggestions: Boolean?,
    val aiAutoComplete: Boolean?,
    val aiModel: String?,
    val aiTemperature: Double?,
    val profileVisibility: String?,
    val showEmail: Boolean?,
    val showPhone: Boolean?,
    val showLocation: Boolean?,
    val allowSearch: Boolean?,
    val twoFactorEnabled: Boolean?,
    val sessionTimeout: Int?,
    val autoSave: Boolean?,
    val autoSaveInterval: Int?,
    val defaultDocumentVisibility: String?,
    val defaultFolderColor: String?,
    val sidebarCollapsed: Boolean?,
    val showLineNumbers: Boolean?,
    val enableSpellCheck: Boolean?
)