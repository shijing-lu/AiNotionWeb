package com.example.newainotionbackend.service

import com.example.newainotionbackend.dto.*
import com.example.newainotionbackend.entity.Article
import com.example.newainotionbackend.entity.ArticleStatus
import com.example.newainotionbackend.entity.ArticleVisibility
import com.example.newainotionbackend.repository.ArticleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * 文章服务类
 * 提供文章的业务逻辑处理
 * @author AI Assistant
 */
@Service
@Transactional
class ArticleService(
    private val articleRepository: ArticleRepository
) {
    
    /**
     * 创建文章
     */
    fun createArticle(userId: Long, request: CreateArticleRequest): Article {
        val article = Article(
            userId = userId,
            title = request.title,
            content = request.content,
            summary = request.summary,
            coverImage = request.coverImage,
            tags = request.tags,
            category = request.category,
            status = request.status,
            visibility = request.visibility,
            format = request.format,
            allowComments = request.allowComments,
            metadata = request.metadata,
            wordCount = calculateWordCount(request.content),
            readingTime = calculateReadingTime(request.content),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        return articleRepository.save(article)
    }
    
    /**
     * 根据ID获取文章
     */
    @Transactional(readOnly = true)
    fun getArticleById(id: String, userId: Long? = null): Article? {
        val article = articleRepository.findById(id).orElse(null) ?: return null
        
        // 如果指定了用户ID，检查权限
        if (userId != null && article.userId != userId && 
            article.visibility == ArticleVisibility.PRIVATE) {
            return null
        }
        
        // 增加浏览量
        if (userId != article.userId) {
            incrementViewCount(id)
        }
        
        return article
    }
    
    /**
     * 更新文章
     */
    fun updateArticle(id: String, userId: Long, request: UpdateArticleRequest): Article? {
        val existingArticle = articleRepository.findById(id).orElse(null) ?: return null
        
        // 检查权限
        if (existingArticle.userId != userId) {
            throw IllegalAccessException("无权限修改此文章")
        }
        
        val updatedArticle = existingArticle.copy(
            title = request.title ?: existingArticle.title,
            content = request.content ?: existingArticle.content,
            summary = request.summary ?: existingArticle.summary,
            coverImage = request.coverImage ?: existingArticle.coverImage,
            tags = request.tags ?: existingArticle.tags,
            category = request.category ?: existingArticle.category,
            status = request.status ?: existingArticle.status,
            visibility = request.visibility ?: existingArticle.visibility,
            format = request.format ?: existingArticle.format,
            allowComments = request.allowComments ?: existingArticle.allowComments,
            metadata = request.metadata ?: existingArticle.metadata,
            wordCount = if (request.content != null) calculateWordCount(request.content) else existingArticle.wordCount,
            readingTime = if (request.content != null) calculateReadingTime(request.content) else existingArticle.readingTime,
            updatedAt = LocalDateTime.now(),
            publishedAt = if (request.status == ArticleStatus.PUBLISHED && existingArticle.status != ArticleStatus.PUBLISHED) 
                LocalDateTime.now() else existingArticle.publishedAt
        )
        
        return articleRepository.save(updatedArticle)
    }
    
    /**
     * 删除文章
     */
    fun deleteArticle(id: String, userId: Long): Boolean {
        val article = articleRepository.findById(id).orElse(null) ?: return false
        
        // 检查权限
        if (article.userId != userId) {
            throw IllegalAccessException("无权限删除此文章")
        }
        
        articleRepository.deleteById(id)
        return true
    }
    
    /**
     * 获取用户的文章列表
     */
    @Transactional(readOnly = true)
    fun getUserArticles(
        userId: Long, 
        page: Int = 0, 
        size: Int = 20, 
        sortBy: String = "updatedAt", 
        sortDirection: String = "desc"
    ): Page<Article> {
        val sort = Sort.by(
            if (sortDirection.lowercase() == "asc") Sort.Direction.ASC else Sort.Direction.DESC,
            sortBy
        )
        val pageable = PageRequest.of(page, size, sort)
        return articleRepository.findByUserId(userId, pageable)
    }
    
    /**
     * 根据状态获取用户文章
     */
    @Transactional(readOnly = true)
    fun getUserArticlesByStatus(
        userId: Long, 
        status: ArticleStatus, 
        page: Int = 0, 
        size: Int = 20
    ): Page<Article> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"))
        return articleRepository.findByUserIdAndStatus(userId, status, pageable)
    }
    
    /**
     * 获取用户收藏的文章
     */
    @Transactional(readOnly = true)
    fun getUserFavoriteArticles(userId: Long, page: Int = 0, size: Int = 20): Page<Article> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"))
        return articleRepository.findByUserIdAndIsFavoriteTrue(userId, pageable)
    }
    
    /**
     * 获取用户归档的文章
     */
    @Transactional(readOnly = true)
    fun getUserArchivedArticles(userId: Long, page: Int = 0, size: Int = 20): Page<Article> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"))
        return articleRepository.findByUserIdAndIsArchivedTrue(userId, pageable)
    }
    
    /**
     * 搜索文章
     */
    @Transactional(readOnly = true)
    fun searchArticles(
        userId: Long?,
        request: ArticleSearchRequest,
        page: Int = 0,
        size: Int = 20
    ): Page<Article> {
        val sort = Sort.by(
            if (request.sortDirection.lowercase() == "asc") Sort.Direction.ASC else Sort.Direction.DESC,
            request.sortBy
        )
        val pageable = PageRequest.of(page, size, sort)
        
        return when {
            userId != null && !request.keyword.isNullOrBlank() -> {
                articleRepository.searchByUserIdAndKeyword(userId, request.keyword, pageable)
            }
            !request.keyword.isNullOrBlank() -> {
                articleRepository.searchByTitleOrContent(request.keyword, pageable)
            }
            userId != null -> {
                articleRepository.findByUserId(userId, pageable)
            }
            else -> {
                articleRepository.findByVisibilityAndStatus(
                    ArticleVisibility.PUBLIC, 
                    ArticleStatus.PUBLISHED, 
                    pageable
                )
            }
        }
    }
    
    /**
     * 获取公开文章列表
     */
    @Transactional(readOnly = true)
    fun getPublicArticles(page: Int = 0, size: Int = 20): Page<Article> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"))
        return articleRepository.findByVisibilityAndStatus(
            ArticleVisibility.PUBLIC, 
            ArticleStatus.PUBLISHED, 
            pageable
        )
    }
    
    /**
     * 获取热门文章
     */
    @Transactional(readOnly = true)
    fun getPopularArticles(page: Int = 0, size: Int = 20): Page<Article> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "viewCount"))
        return articleRepository.findPopularArticles(pageable)
    }
    
    /**
     * 切换文章收藏状态
     */
    fun toggleFavorite(id: String, userId: Long): Article? {
        val article = articleRepository.findById(id).orElse(null) ?: return null
        
        if (article.userId != userId) {
            throw IllegalAccessException("无权限操作此文章")
        }
        
        val updatedArticle = article.copy(
            isFavorite = !article.isFavorite,
            updatedAt = LocalDateTime.now()
        )
        
        return articleRepository.save(updatedArticle)
    }
    
    /**
     * 切换文章置顶状态
     */
    fun togglePin(id: String, userId: Long): Article? {
        val article = articleRepository.findById(id).orElse(null) ?: return null
        
        if (article.userId != userId) {
            throw IllegalAccessException("无权限操作此文章")
        }
        
        val updatedArticle = article.copy(
            isPinned = !article.isPinned,
            updatedAt = LocalDateTime.now()
        )
        
        return articleRepository.save(updatedArticle)
    }
    
    /**
     * 切换文章归档状态
     */
    fun toggleArchive(id: String, userId: Long): Article? {
        val article = articleRepository.findById(id).orElse(null) ?: return null
        
        if (article.userId != userId) {
            throw IllegalAccessException("无权限操作此文章")
        }
        
        val updatedArticle = article.copy(
            isArchived = !article.isArchived,
            updatedAt = LocalDateTime.now()
        )
        
        return articleRepository.save(updatedArticle)
    }
    
    /**
     * 发布文章
     */
    fun publishArticle(id: String, userId: Long): Article? {
        val article = articleRepository.findById(id).orElse(null) ?: return null
        
        if (article.userId != userId) {
            throw IllegalAccessException("无权限操作此文章")
        }
        
        val updatedArticle = article.copy(
            status = ArticleStatus.PUBLISHED,
            publishedAt = if (article.publishedAt == null) LocalDateTime.now() else article.publishedAt,
            updatedAt = LocalDateTime.now()
        )
        
        return articleRepository.save(updatedArticle)
    }
    
    /**
     * 获取文章统计信息
     */
    @Transactional(readOnly = true)
    fun getArticleStats(userId: Long): ArticleStatsDto {
        val totalCount = articleRepository.countByUserId(userId)
        val draftCount = articleRepository.countByUserIdAndStatus(userId, ArticleStatus.DRAFT)
        val publishedCount = articleRepository.countByUserIdAndStatus(userId, ArticleStatus.PUBLISHED)
        val archivedCount = articleRepository.countByUserIdAndStatus(userId, ArticleStatus.ARCHIVED)
        
        val userArticles = articleRepository.findByUserId(userId, Pageable.unpaged())
        val favoriteCount = userArticles.content.count { it.isFavorite }.toLong()
        val totalViews = userArticles.content.sumOf { it.viewCount }.toLong()
        val totalLikes = userArticles.content.sumOf { it.likeCount }.toLong()
        val totalComments = userArticles.content.sumOf { it.commentCount }.toLong()
        
        return ArticleStatsDto(
            totalCount = totalCount,
            draftCount = draftCount,
            publishedCount = publishedCount,
            archivedCount = archivedCount,
            favoriteCount = favoriteCount,
            totalViews = totalViews,
            totalLikes = totalLikes,
            totalComments = totalComments
        )
    }
    
    /**
     * 增加浏览量
     */
    private fun incrementViewCount(id: String) {
        val article = articleRepository.findById(id).orElse(null) ?: return
        val updatedArticle = article.copy(
            viewCount = article.viewCount + 1,
            lastViewedAt = LocalDateTime.now()
        )
        articleRepository.save(updatedArticle)
    }
    
    /**
     * 计算字数
     */
    private fun calculateWordCount(content: String?): Int {
        if (content.isNullOrBlank()) return 0
        return content.trim().split("\\s+".toRegex()).size
    }
    
    /**
     * 计算阅读时间（分钟）
     */
    private fun calculateReadingTime(content: String?): Int {
        if (content.isNullOrBlank()) return 0
        val wordCount = calculateWordCount(content)
        // 假设每分钟阅读200个单词
        return maxOf(1, (wordCount / 200.0).toInt())
    }
}