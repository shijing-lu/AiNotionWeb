package com.example.newainotionbackend.dto

import com.example.newainotionbackend.entity.Article
import com.example.newainotionbackend.entity.ArticleStatus
import com.example.newainotionbackend.entity.ArticleVisibility
import com.example.newainotionbackend.entity.ArticleFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * 文章数据传输对象
 * @author AI Assistant
 */
data class ArticleDto(
    val id: String?,
    val userId: Long,
    @field:NotBlank(message = "文章标题不能为空")
    @field:Size(max = 200, message = "文章标题长度不能超过200个字符")
    val title: String,
    val content: String?,
    val summary: String?,
    val coverImage: String?,
    val tags: List<String>,
    val category: String?,
    val status: ArticleStatus,
    val visibility: ArticleVisibility,
    val format: ArticleFormat,
    val wordCount: Int,
    val readingTime: Int,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val shareCount: Int,
    val isFavorite: Boolean,
    val isArchived: Boolean,
    val isPinned: Boolean,
    val allowComments: Boolean,
    val metadata: Map<String, Any>,
    val aiGenerated: Boolean,
    val aiModel: String?,
    val aiPrompt: String?,
    val lastViewedAt: LocalDateTime?,
    val publishedAt: LocalDateTime?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        /**
         * 从Article实体转换为DTO
         */
        fun fromEntity(article: Article): ArticleDto {
            return ArticleDto(
                id = article.id,
                userId = article.userId,
                title = article.title,
                content = article.content,
                summary = article.summary,
                coverImage = article.coverImage,
                tags = article.tags,
                category = article.category,
                status = article.status,
                visibility = article.visibility,
                format = article.format,
                wordCount = article.wordCount,
                readingTime = article.readingTime,
                viewCount = article.viewCount,
                likeCount = article.likeCount,
                commentCount = article.commentCount,
                shareCount = article.shareCount,
                isFavorite = article.isFavorite,
                isArchived = article.isArchived,
                isPinned = article.isPinned,
                allowComments = article.allowComments,
                metadata = article.metadata,
                aiGenerated = article.aiGenerated,
                aiModel = article.aiModel,
                aiPrompt = article.aiPrompt,
                lastViewedAt = article.lastViewedAt,
                publishedAt = article.publishedAt,
                createdAt = article.createdAt,
                updatedAt = article.updatedAt
            )
        }
    }
    
    /**
     * 转换为Article实体
     */
    fun toEntity(): Article {
        return Article(
            id = this.id,
            userId = this.userId,
            title = this.title,
            content = this.content,
            summary = this.summary,
            coverImage = this.coverImage,
            tags = this.tags,
            category = this.category,
            status = this.status,
            visibility = this.visibility,
            format = this.format,
            wordCount = this.wordCount,
            readingTime = this.readingTime,
            viewCount = this.viewCount,
            likeCount = this.likeCount,
            commentCount = this.commentCount,
            shareCount = this.shareCount,
            isFavorite = this.isFavorite,
            isArchived = this.isArchived,
            isPinned = this.isPinned,
            allowComments = this.allowComments,
            metadata = this.metadata,
            aiGenerated = this.aiGenerated,
            aiModel = this.aiModel,
            aiPrompt = this.aiPrompt,
            lastViewedAt = this.lastViewedAt,
            publishedAt = this.publishedAt,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}

/**
 * 文章摘要DTO（用于列表显示）
 */
data class ArticleSummaryDto(
    val id: String?,
    val title: String,
    val summary: String?,
    val coverImage: String?,
    val tags: List<String>,
    val category: String?,
    val status: ArticleStatus,
    val visibility: ArticleVisibility,
    val wordCount: Int,
    val readingTime: Int,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val isFavorite: Boolean,
    val isArchived: Boolean,
    val isPinned: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val publishedAt: LocalDateTime?
) {
    companion object {
        /**
         * 从Article实体转换为摘要DTO
         */
        fun fromEntity(article: Article): ArticleSummaryDto {
            return ArticleSummaryDto(
                id = article.id,
                title = article.title,
                summary = article.summary,
                coverImage = article.coverImage,
                tags = article.tags,
                category = article.category,
                status = article.status,
                visibility = article.visibility,
                wordCount = article.wordCount,
                readingTime = article.readingTime,
                viewCount = article.viewCount,
                likeCount = article.likeCount,
                commentCount = article.commentCount,
                isFavorite = article.isFavorite,
                isArchived = article.isArchived,
                isPinned = article.isPinned,
                createdAt = article.createdAt,
                updatedAt = article.updatedAt,
                publishedAt = article.publishedAt
            )
        }
    }
}

/**
 * 创建文章请求DTO
 */
data class CreateArticleRequest(
    @field:NotBlank(message = "文章标题不能为空")
    @field:Size(max = 200, message = "文章标题长度不能超过200个字符")
    val title: String,
    val content: String? = null,
    val summary: String? = null,
    val coverImage: String? = null,
    val tags: List<String> = emptyList(),
    val category: String? = null,
    val status: ArticleStatus = ArticleStatus.DRAFT,
    val visibility: ArticleVisibility = ArticleVisibility.PRIVATE,
    val format: ArticleFormat = ArticleFormat.MARKDOWN,
    val allowComments: Boolean = true,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * 更新文章请求DTO
 */
data class UpdateArticleRequest(
    @field:Size(max = 200, message = "文章标题长度不能超过200个字符")
    val title: String? = null,
    val content: String? = null,
    val summary: String? = null,
    val coverImage: String? = null,
    val tags: List<String>? = null,
    val category: String? = null,
    val status: ArticleStatus? = null,
    val visibility: ArticleVisibility? = null,
    val format: ArticleFormat? = null,
    val allowComments: Boolean? = null,
    val metadata: Map<String, Any>? = null
)

/**
 * 文章搜索请求DTO
 */
data class ArticleSearchRequest(
    val keyword: String? = null,
    val tags: List<String>? = null,
    val category: String? = null,
    val status: ArticleStatus? = null,
    val visibility: ArticleVisibility? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val sortBy: String = "createdAt",
    val sortDirection: String = "desc"
)

/**
 * 文章统计DTO
 */
data class ArticleStatsDto(
    val totalCount: Long,
    val draftCount: Long,
    val publishedCount: Long,
    val archivedCount: Long,
    val favoriteCount: Long,
    val totalViews: Long,
    val totalLikes: Long,
    val totalComments: Long
)