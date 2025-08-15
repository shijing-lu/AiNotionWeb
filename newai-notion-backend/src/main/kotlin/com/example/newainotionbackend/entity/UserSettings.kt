package com.example.newainotionbackend.entity

import com.baomidou.mybatisplus.annotation.*
import java.time.LocalDateTime

@TableName("user_settings")
data class UserSettings(
    @TableId(value = "id", type = IdType.AUTO)
    val id: Long? = null,
    
    @TableField("user_id")
    val userId: Long,
    
    // 主题设置
    @TableField("theme")
    val theme: String = "light", // light, dark, auto
    
    @TableField("primary_color")
    val primaryColor: String = "#1976d2",
    
    // 语言设置
    @TableField("language")
    val language: String = "zh-CN",
    
    @TableField("timezone")
    val timezone: String = "Asia/Shanghai",
    
    // 编辑器设置
    @TableField("editor_font_size")
    val editorFontSize: Int = 14,
    
    @TableField("editor_font_family")
    val editorFontFamily: String = "'Helvetica Neue', Arial, sans-serif",
    
    @TableField("editor_line_height")
    val editorLineHeight: Double = 1.6,
    
    @TableField("editor_word_wrap")
    val editorWordWrap: Boolean = true,
    
    @TableField("editor_vim_mode")
    val editorVimMode: Boolean = false,
    
    @TableField("editor_auto_save")
    val editorAutoSave: Boolean = true,
    
    @TableField("editor_auto_save_interval")
    val editorAutoSaveInterval: Int = 30, // 秒
    
    // 通知设置
    @TableField("email_notifications")
    val emailNotifications: Boolean = true,
    
    @TableField("push_notifications")
    val pushNotifications: Boolean = true,
    
    @TableField("notification_sound")
    val notificationSound: Boolean = true,
    
    @TableField("notification_comment")
    val notificationComment: Boolean = true,
    
    @TableField("notification_mention")
    val notificationMention: Boolean = true,
    
    @TableField("notification_share")
    val notificationShare: Boolean = true,
    
    // AI设置
    @TableField("ai_auto_complete")
    val aiAutoComplete: Boolean = true,
    
    @TableField("ai_writing_assistant")
    val aiWritingAssistant: Boolean = true,
    
    @TableField("ai_smart_suggestions")
    val aiSmartSuggestions: Boolean = true,
    
    @TableField("ai_model_preference")
    val aiModelPreference: String = "gpt-3.5-turbo",
    
    // 隐私设置
    @TableField("profile_visibility")
    val profileVisibility: String = "public", // public, friends, private
    
    @TableField("document_default_visibility")
    val documentDefaultVisibility: String = "private", // public, private, unlisted
    
    @TableField("allow_search_indexing")
    val allowSearchIndexing: Boolean = false,
    
    @TableField("show_online_status")
    val showOnlineStatus: Boolean = true,
    
    // 安全设置
    @TableField("two_factor_enabled")
    val twoFactorEnabled: Boolean = false,
    
    @TableField("login_alerts")
    val loginAlerts: Boolean = true,
    
    @TableField("session_timeout")
    val sessionTimeout: Int = 1440, // 分钟，默认24小时
    
    // 其他设置
    @TableField("sidebar_collapsed")
    val sidebarCollapsed: Boolean = false,
    
    @TableField("show_line_numbers")
    val showLineNumbers: Boolean = false,
    
    @TableField("enable_spell_check")
    val enableSpellCheck: Boolean = true,
    
    @TableField("enable_markdown_preview")
    val enableMarkdownPreview: Boolean = true,
    
    @TableField("default_document_format")
    val defaultDocumentFormat: String = "markdown", // markdown, rich-text
    
    @TableField("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @TableField("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)