package com.example.newainotionbackend.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.index.Indexed
import java.time.LocalDateTime

/**
 * MongoDB文章实体类
 * 用于存储文章的完整内容和元数据
 * @author AI Assistant
 */
@Document(collection = "articles")
data class Article(
    @Id
    val id: String? = null,
    
    @Field("user_id")
    @Indexed
    val userId: Long,
    
    @Field("title")
    @Indexed
    val title: String,
    
    @Field("content")
    val content: String? = null,
    
    @Field("summary")
    val summary: String? = null,
    
    @Field("cover_image")
    val coverImage: String? = null,
    
    @Field("tags")
    @Indexed
    val tags: List<String> = emptyList(),
    
    @Field("category")
    @Indexed
    val category: String? = null,
    
    @Field("status")
    @Indexed
    val status: ArticleStatus = ArticleStatus.DRAFT,
    
    @Field("visibility")
    @Indexed
    val visibility: ArticleVisibility = ArticleVisibility.PRIVATE,
    
    @Field("format")
    val format: ArticleFormat = ArticleFormat.MARKDOWN,
    
    @Field("word_count")
    val wordCount: Int = 0,
    
    @Field("reading_time")
    val readingTime: Int = 0,
    
    @Field("view_count")
    val viewCount: Int = 0,
    
    @Field("like_count")
    val likeCount: Int = 0,
    
    @Field("comment_count")
    val commentCount: Int = 0,
    
    @Field("share_count")
    val shareCount: Int = 0,
    
    @Field("is_favorite")
    val isFavorite: Boolean = false,
    
    @Field("is_archived")
    val isArchived: Boolean = false,
    
    @Field("is_pinned")
    val isPinned: Boolean = false,
    
    @Field("allow_comments")
    val allowComments: Boolean = true,
    
    @Field("metadata")
    val metadata: Map<String, Any> = emptyMap(),
    
    @Field("ai_generated")
    val aiGenerated: Boolean = false,
    
    @Field("ai_model")
    val aiModel: String? = null,
    
    @Field("ai_prompt")
    val aiPrompt: String? = null,
    
    @Field("last_viewed_at")
    val lastViewedAt: LocalDateTime? = null,
    
    @Field("published_at")
    val publishedAt: LocalDateTime? = null,
    
    @CreatedDate
    @Field("created_at")
    val createdAt: LocalDateTime? = null,
    
    @LastModifiedDate
    @Field("updated_at")
    val updatedAt: LocalDateTime? = null
)

/**
 * 文章状态枚举
 */
enum class ArticleStatus {
    DRAFT,      // 草稿
    PUBLISHED,  // 已发布
    ARCHIVED,   // 已归档
    DELETED     // 已删除
}

/**
 * 文章可见性枚举
 */
enum class ArticleVisibility {
    PRIVATE,    // 私有
    PUBLIC,     // 公开
    UNLISTED,   // 不公开列出
    PASSWORD    // 密码保护
}

/**
 * 文章格式枚举
 */
enum class ArticleFormat {
    MARKDOWN,   // Markdown格式
    HTML,       // HTML格式
    PLAIN_TEXT, // 纯文本
    RICH_TEXT   // 富文本
}