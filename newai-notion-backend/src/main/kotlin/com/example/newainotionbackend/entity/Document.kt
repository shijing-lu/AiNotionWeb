package com.example.newainotionbackend.entity

import com.baomidou.mybatisplus.annotation.*
import java.time.LocalDateTime

/**
 * 文档实体类
 * @author AI Assistant
 */
@TableName("documents")
data class Document(
    @TableId(type = IdType.ASSIGN_UUID)
    val id: String? = null,
    
    @TableField("user_id")
    val userId: Long,
    
    @TableField("folder_id")
    val folderId: String? = null,
    
    @TableField("title")
    val title: String,
    
    @TableField("content")
    val content: String? = null,
    
    @TableField("summary")
    val summary: String? = null,
    
    @TableField("cover_image")
    val coverImage: String? = null,
    
    @TableField("icon")
    val icon: String? = null,
    
    @TableField("status")
    val status: DocumentStatus = DocumentStatus.DRAFT,
    
    @TableField("is_public")
    val isPublic: Boolean = false,
    
    @TableField("is_archived")
    val isArchived: Boolean = false,
    
    @TableField("is_pinned")
    val isPinned: Boolean = false,
    
    @TableField("is_template")
    val isTemplate: Boolean = false,
    
    @TableField("is_favorite")
    val isFavorite: Boolean = false,
    
    @TableField("template_category")
    val templateCategory: String? = null,
    
    @TableField("visibility")
    val visibility: DocumentVisibility = DocumentVisibility.PRIVATE,
    
    @TableField("format")
    val format: DocumentFormat = DocumentFormat.MARKDOWN,
    
    @TableField("allow_comments")
    val allowComments: Boolean = true,
    
    @TableField("word_count")
    val wordCount: Int = 0,
    
    @TableField("reading_time")
    val readingTime: Int = 0,
    
    @TableField("view_count")
    val viewCount: Int = 0,
    
    @TableField("like_count")
    val likeCount: Int = 0,
    
    @TableField("comment_count")
    val commentCount: Int = 0,
    
    @TableField("share_count")
    val shareCount: Int = 0,
    
    @TableField("version")
    val version: Int = 1,
    
    @TableField("parent_version_id")
    val parentVersionId: String? = null,
    
    @TableField("tags")
    val tags: String? = null, // JSON字符串存储标签
    
    @TableField("metadata")
    val metadata: String? = null, // JSON字符串存储元数据
    
    @TableField("ai_generated")
    val aiGenerated: Boolean = false,
    
    @TableField("ai_model")
    val aiModel: String? = null,
    
    @TableField("ai_prompt")
    val aiPrompt: String? = null,
    
    @TableField("last_viewed_at")
    val lastViewedAt: LocalDateTime? = null,
    
    @TableField("published_at")
    val publishedAt: LocalDateTime? = null,
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    val createdAt: LocalDateTime? = null,
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    val updatedAt: LocalDateTime? = null
)

/**
 * 文档状态枚举
 */
enum class DocumentStatus {
    DRAFT,      // 草稿
    PUBLISHED,  // 已发布
    ARCHIVED,   // 已归档
    DELETED     // 已删除
}

/**
 * 文档可见性枚举
 */
enum class DocumentVisibility {
    PRIVATE,    // 私有
    PUBLIC,     // 公开
    SHARED      // 共享
}

/**
 * 文档格式枚举
 */
enum class DocumentFormat {
    MARKDOWN,   // Markdown格式
    HTML,       // HTML格式
    PLAIN_TEXT  // 纯文本格式
}

/**
 * 协作者权限枚举
 */
enum class CollaboratorPermission {
    READ,       // 只读权限
    EDIT,       // 编辑权限
    ADMIN       // 管理权限
}