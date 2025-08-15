package com.example.newainotionbackend.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.example.newainotionbackend.entity.*
import com.example.newainotionbackend.mapper.DocumentMapper
import com.example.newainotionbackend.mapper.FolderMapper
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class DocumentService(
    private val documentMapper: DocumentMapper,
    private val folderMapper: FolderMapper
) {
    
    /**
     * 创建文档
     */
    fun createDocument(
        userId: Long,
        title: String,
        content: String = "",
        folderId: String? = null,
        visibility: DocumentVisibility = DocumentVisibility.PRIVATE,
        format: DocumentFormat = DocumentFormat.MARKDOWN
    ): Document {
        // 验证文件夹是否存在且属于用户
        if (folderId != null) {
            val folder = folderMapper.selectById(folderId)
                ?: throw IllegalArgumentException("文件夹不存在")
            if (folder.userId != userId) {
                throw IllegalArgumentException("无权限访问该文件夹")
            }
        }
        
        val document = Document(
            userId = userId,
            title = title,
            content = content,
            folderId = folderId,
            visibility = visibility,
            format = format,
            wordCount = calculateWordCount(content),
            readingTime = calculateReadingTime(content)
        )
        
        documentMapper.insert(document)
        return document
    }
    
    /**
     * 获取文档
     */
    fun getDocument(documentId: String, userId: Long? = null): Document {
        val document = documentMapper.selectById(documentId)
            ?: throw IllegalArgumentException("文档不存在")
        
        // 检查访问权限
        if (!canAccessDocument(document, userId)) {
            throw IllegalArgumentException("无权限访问该文档")
        }
        
        // 更新查看次数和最后查看时间
        if (userId != null) {
            updateDocumentView(document, userId)
        }
        
        return document
    }
    
    /**
     * 更新文档
     */
    fun updateDocument(
        documentId: String,
        userId: Long,
        updates: Map<String, Any?>
    ): Document {
        val document = getDocument(documentId, userId)
        
        // 检查编辑权限
        if (!canEditDocument(document, userId)) {
            throw IllegalArgumentException("无权限编辑该文档")
        }
        
        val content = updates["content"] as? String ?: document.content
        
        val updatedDocument = document.copy(
            title = updates["title"] as? String ?: document.title,
            content = content,
            summary = updates["summary"] as? String ?: document.summary,
            tags = updates["tags"] as? String ?: document.tags,
            visibility = updates["visibility"] as? DocumentVisibility ?: document.visibility,
            status = updates["status"] as? DocumentStatus ?: document.status,
            format = updates["format"] as? DocumentFormat ?: document.format,
            coverImage = updates["coverImage"] as? String ?: document.coverImage,
            isFavorite = updates["isFavorite"] as? Boolean ?: document.isFavorite,
            isPinned = updates["isPinned"] as? Boolean ?: document.isPinned,
            allowComments = updates["allowComments"] as? Boolean ?: document.allowComments,
            wordCount = calculateWordCount(content ?: ""),
            readingTime = calculateReadingTime(content ?: ""),
            version = document.version + 1,
            updatedAt = LocalDateTime.now()
        )
        
        documentMapper.updateById(updatedDocument)
        return updatedDocument
    }
    
    /**
     * 删除文档
     */
    fun deleteDocument(documentId: String, userId: Long) {
        val document = getDocument(documentId, userId)
        
        if (document.userId != userId) {
            throw IllegalArgumentException("无权限删除该文档")
        }
        
        // 软删除：更新状态
        val deletedDocument = document.copy(
            status = DocumentStatus.DELETED,
            updatedAt = LocalDateTime.now()
        )
        
        documentMapper.updateById(deletedDocument)
    }
    
    /**
     * 获取用户文档列表
     */
    fun getUserDocuments(
        userId: Long,
        folderId: String? = null,
        status: DocumentStatus? = null,
        pageable: Pageable
    ): Page<Document> {
        val page = Page<Document>(pageable.pageNumber.toLong() + 1, pageable.pageSize.toLong())
        return documentMapper.selectUserDocumentsPage(page, userId, folderId, status)
    }
    
    /**
     * 搜索文档
     */
    fun searchDocuments(
        userId: Long?,
        keyword: String,
        pageable: Pageable
    ): Page<Document> {
        val page = Page<Document>(pageable.pageNumber.toLong() + 1, pageable.pageSize.toLong())
        return if (userId != null) {
            documentMapper.searchUserDocumentsPage(page, userId, keyword)
        } else {
            documentMapper.searchPublicDocumentsPage(page, keyword)
        }
    }
    
    /**
     * 获取收藏文档
     */
    fun getFavoriteDocuments(userId: Long, pageable: Pageable): Page<Document> {
        val page = Page<Document>(pageable.pageNumber.toLong() + 1, pageable.pageSize.toLong())
        return documentMapper.selectFavoriteDocumentsPage(page, userId)
    }
    
    /**
     * 获取置顶文档
     */
    fun getPinnedDocuments(userId: Long, pageable: Pageable): Page<Document> {
        val page = Page<Document>(pageable.pageNumber.toLong() + 1, pageable.pageSize.toLong())
        return documentMapper.selectPinnedDocumentsPage(page, userId)
    }
    
    /**
     * 获取已归档文档
     */
    fun getArchivedDocuments(userId: Long, pageable: Pageable): Page<Document> {
        val page = Page<Document>(pageable.pageNumber.toLong() + 1, pageable.pageSize.toLong())
        return documentMapper.selectArchivedDocumentsPage(page, userId)
    }
    
    /**
     * 获取最近文档
     */
    fun getRecentDocuments(userId: Long, pageable: Pageable): Page<Document> {
        val sevenDaysAgo = LocalDateTime.now().minusDays(7)
        val page = Page<Document>(pageable.pageNumber.toLong() + 1, pageable.pageSize.toLong())
        return documentMapper.selectRecentDocumentsPage(page, userId, sevenDaysAgo)
    }
    
    /**
     * 获取公开文档
     */
    fun getPublicDocuments(pageable: Pageable): Page<Document> {
        val page = Page<Document>(pageable.pageNumber.toLong() + 1, pageable.pageSize.toLong())
        return documentMapper.selectPublicDocumentsPage(page, DocumentVisibility.PUBLIC, DocumentStatus.PUBLISHED)
    }
    
    /**
     * 获取热门文档
     */
    fun getPopularDocuments(pageable: Pageable): Page<Document> {
        val page = Page<Document>(pageable.pageNumber.toLong() + 1, pageable.pageSize.toLong())
        return documentMapper.selectPublicDocumentsPage(page, DocumentVisibility.PUBLIC, DocumentStatus.PUBLISHED)
    }
    
    /**
     * 复制文档
     */
    fun duplicateDocument(documentId: String, userId: Long, newTitle: String? = null): Document {
        val originalDocument = getDocument(documentId, userId)
        
        val duplicatedDocument = originalDocument.copy(
            id = null,
            userId = userId,
            title = newTitle ?: "${originalDocument.title} (副本)",
            status = DocumentStatus.DRAFT,
            version = 1,
            viewCount = 0,
            likeCount = 0,
            commentCount = 0,
            shareCount = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            publishedAt = null
        )
        
        documentMapper.insert(duplicatedDocument)
        return duplicatedDocument
    }
    
    /**
     * 移动文档到文件夹
     */
    fun moveDocument(documentId: String, userId: Long, targetFolderId: String?): Document {
        val document = getDocument(documentId, userId)
        
        if (document.userId != userId) {
            throw IllegalArgumentException("无权限移动该文档")
        }
        
        // 验证目标文件夹
        if (targetFolderId != null) {
            val folder = folderMapper.selectById(targetFolderId)
                ?: throw IllegalArgumentException("目标文件夹不存在")
            if (folder.userId != userId) {
                throw IllegalArgumentException("无权限访问目标文件夹")
            }
        }
        
        val movedDocument = document.copy(
            folderId = targetFolderId,
            updatedAt = LocalDateTime.now()
        )
        
        documentMapper.updateById(movedDocument)
        return movedDocument
    }
    
    /**
     * 发布文档
     */
    fun publishDocument(documentId: String, userId: Long): Document {
        val document = getDocument(documentId, userId)
        
        if (document.userId != userId) {
            throw IllegalArgumentException("无权限发布该文档")
        }
        
        val publishedDocument = document.copy(
            status = DocumentStatus.PUBLISHED,
            publishedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        documentMapper.updateById(publishedDocument)
        return publishedDocument
    }
    
    /**
     * 归档文档
     */
    fun archiveDocument(documentId: String, userId: Long): Document {
        val document = getDocument(documentId, userId)
        
        if (document.userId != userId) {
            throw IllegalArgumentException("无权限归档该文档")
        }
        
        val archivedDocument = document.copy(
            isArchived = true,
            updatedAt = LocalDateTime.now()
        )
        
        documentMapper.updateById(archivedDocument)
        return archivedDocument
    }
    
    /**
     * 收藏文档
     */
    fun favoriteDocument(documentId: String, userId: Long): Document {
        val document = getDocument(documentId, userId)
        
        val favoriteDocument = document.copy(
            isFavorite = true,
            updatedAt = LocalDateTime.now()
        )
        
        documentMapper.updateById(favoriteDocument)
        return favoriteDocument
    }
    
    /**
     * 取消收藏文档
     */
    fun unfavoriteDocument(documentId: String, userId: Long): Document {
        val document = getDocument(documentId, userId)
        
        val unfavoriteDocument = document.copy(
            isFavorite = false,
            updatedAt = LocalDateTime.now()
        )
        
        documentMapper.updateById(unfavoriteDocument)
        return unfavoriteDocument
    }
    
    /**
     * 复制文档
     */
    fun copyDocument(documentId: String, userId: Long, targetFolderId: String?, newTitle: String?): Document {
        val copiedDocument = duplicateDocument(documentId, userId, newTitle).copy(
            folderId = targetFolderId
        )
        documentMapper.updateById(copiedDocument)
        return copiedDocument
    }
    
    /**
     * 检查文档访问权限
     */
    private fun canAccessDocument(document: Document, userId: Long?): Boolean {
        return when {
            document.visibility == DocumentVisibility.PUBLIC -> true
            userId == null -> false
            document.userId == userId -> true
            // 协作功能待后续实现
            else -> false
        }
    }
    
    /**
     * 检查文档编辑权限
     */
    private fun canEditDocument(document: Document, userId: Long): Boolean {
        // 目前只检查文档所有者权限，协作功能待后续实现
        return document.userId == userId
    }
    
    /**
     * 更新文档查看信息
     */
    private fun updateDocumentView(document: Document, userId: Long) {
        if (document.userId != userId) { // 不统计作者自己的查看
            documentMapper.updateDocumentView(document.id!!, LocalDateTime.now())
        }
    }
    
    /**
     * 计算字数
     */
    private fun calculateWordCount(content: String): Int {
        return content.trim().split("\\s+".toRegex()).size
    }
    
    /**
     * 计算阅读时间（分钟）
     */
    private fun calculateReadingTime(content: String): Int {
        val wordCount = calculateWordCount(content)
        val wordsPerMinute = 200 // 平均阅读速度
        return maxOf(1, (wordCount / wordsPerMinute))
    }
}