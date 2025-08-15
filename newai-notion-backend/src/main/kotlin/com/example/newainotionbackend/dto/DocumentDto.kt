package com.example.newainotionbackend.dto

import com.example.newainotionbackend.entity.Document
import com.example.newainotionbackend.entity.DocumentVisibility
import com.example.newainotionbackend.entity.DocumentStatus
import com.example.newainotionbackend.entity.DocumentFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

/**
 * 文档信息DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DocumentDto(
    val id: String,
    val userId: Long,
    val title: String,
    val content: String?,
    val summary: String?,
    val folderId: String?,
    val tags: List<String>,
    val visibility: DocumentVisibility,
    val status: DocumentStatus,
    val format: DocumentFormat,
    val version: Int,
    val wordCount: Int,
    val readingTime: Int,
    val coverImage: String?,
    val templateId: String?,
    val templateName: String? = null,
    val isFavorite: Boolean,
    val isArchived: Boolean,
    val isPinned: Boolean,
    val allowComments: Boolean,
    val allowCopy: Boolean,
    val allowDownload: Boolean,
    val isPasswordProtected: Boolean,
    val expiresAt: LocalDateTime?,
    val lastViewedAt: LocalDateTime?,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val shareCount: Int,
    // TODO: 待实现协作者、分享链接、元数据和AI建议功能
    val collaborators: List<String>? = null, // 暂时存储为字符串列表
    val shareLinks: List<String>? = null, // 暂时存储为字符串列表
    val metadata: Map<String, Any>? = null, // 暂时存储为键值对
    val aiSuggestions: List<String>? = null, // 暂时存储为字符串列表
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val publishedAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(document: Document): DocumentDto {
            return DocumentDto(
                id = document.id!!,
                userId = document.userId,
                title = document.title,
                content = document.content,
                summary = document.summary,
                folderId = document.folderId,
                tags = emptyList(), // TODO: 解析JSON字符串为列表
                visibility = document.visibility,
                status = document.status,
                format = document.format,
                version = document.version,
                wordCount = document.wordCount,
                readingTime = document.readingTime,
                coverImage = document.coverImage,
                templateId = null, // TODO: 待实现模板功能
                templateName = null, // Document实体中没有templateName属性
                isFavorite = document.isFavorite,
                isArchived = document.isArchived,
                isPinned = document.isPinned,
                allowComments = document.allowComments,
                allowCopy = true, // TODO: 待实现
                allowDownload = true, // TODO: 待实现
                isPasswordProtected = false, // TODO: 待实现
                expiresAt = null, // TODO: 待实现
                lastViewedAt = document.lastViewedAt,
                viewCount = document.viewCount,
                likeCount = document.likeCount,
                commentCount = document.commentCount,
                shareCount = document.shareCount,
                collaborators = null, // TODO: 待实现
                shareLinks = null, // TODO: 待实现
                metadata = null, // TODO: 待实现
                aiSuggestions = null, // TODO: 待实现
                createdAt = document.createdAt,
                updatedAt = document.updatedAt,
                publishedAt = document.publishedAt
            )
        }
    }
}

/**
 * 创建文档请求
 */
data class CreateDocumentRequest(
    val title: String,
    val content: String?,
    val folderId: String?,
    val visibility: DocumentVisibility?,
    val tags: List<String>?,
    val templateId: String?
)

/**
 * 更新文档请求
 */
data class UpdateDocumentRequest(
    val title: String?,
    val content: String?,
    val summary: String?,
    val tags: List<String>?,
    val visibility: DocumentVisibility?,
    val coverImage: String?
)

/**
 * 复制文档请求
 */
data class CopyDocumentRequest(
    val targetFolderId: String?,
    val newTitle: String?
)

/**
 * 移动文档请求
 */
data class MoveDocumentRequest(
    val targetFolderId: String?
)

/**
 * 文档简要信息DTO（用于列表显示）
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DocumentSummaryDto(
    val id: String,
    val title: String,
    val summary: String?,
    val tags: List<String>,
    val visibility: DocumentVisibility,
    val status: DocumentStatus,
    val wordCount: Int,
    val readingTime: Int,
    val coverImage: String?,
    val isFavorite: Boolean,
    val isArchived: Boolean,
    val isPinned: Boolean,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(document: Document): DocumentSummaryDto {
            return DocumentSummaryDto(
                id = document.id!!,
                title = document.title,
                summary = document.summary,
                tags = emptyList(), // TODO: 解析JSON字符串为列表
                visibility = document.visibility,
                status = document.status,
                wordCount = document.wordCount,
                readingTime = document.readingTime,
                coverImage = document.coverImage,
                isFavorite = document.isFavorite,
                isArchived = document.isArchived,
                isPinned = document.isPinned,
                viewCount = document.viewCount,
                likeCount = document.likeCount,
                commentCount = document.commentCount,
                createdAt = document.createdAt,
                updatedAt = document.updatedAt
            )
        }
    }
}