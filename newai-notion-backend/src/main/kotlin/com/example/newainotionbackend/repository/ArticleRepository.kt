package com.example.newainotionbackend.repository

import com.example.newainotionbackend.entity.Article
import com.example.newainotionbackend.entity.ArticleStatus
import com.example.newainotionbackend.entity.ArticleVisibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * MongoDB文章数据访问接口
 * 提供文章的CRUD操作和复杂查询
 * @author AI Assistant
 */
@Repository
interface ArticleRepository : MongoRepository<Article, String> {
    
    /**
     * 根据用户ID查找文章
     */
    fun findByUserId(userId: Long, pageable: Pageable): Page<Article>
    
    /**
     * 根据用户ID和状态查找文章
     */
    fun findByUserIdAndStatus(userId: Long, status: ArticleStatus, pageable: Pageable): Page<Article>
    
    /**
     * 根据用户ID和可见性查找文章
     */
    fun findByUserIdAndVisibility(userId: Long, visibility: ArticleVisibility, pageable: Pageable): Page<Article>
    
    /**
     * 根据用户ID查找收藏的文章
     */
    fun findByUserIdAndIsFavoriteTrue(userId: Long, pageable: Pageable): Page<Article>
    
    /**
     * 根据用户ID查找置顶的文章
     */
    fun findByUserIdAndIsPinnedTrue(userId: Long, pageable: Pageable): Page<Article>
    
    /**
     * 根据用户ID查找已归档的文章
     */
    fun findByUserIdAndIsArchivedTrue(userId: Long, pageable: Pageable): Page<Article>
    
    /**
     * 根据标签查找文章
     */
    fun findByTagsContaining(tag: String, pageable: Pageable): Page<Article>
    
    /**
     * 根据分类查找文章
     */
    fun findByCategory(category: String, pageable: Pageable): Page<Article>
    
    /**
     * 根据标题模糊查询文章
     */
    @Query("{'title': {\$regex: ?0, \$options: 'i'}}")
    fun findByTitleContainingIgnoreCase(title: String, pageable: Pageable): Page<Article>
    
    /**
     * 根据内容模糊查询文章
     */
    @Query("{'content': {\$regex: ?0, \$options: 'i'}}")
    fun findByContentContainingIgnoreCase(content: String, pageable: Pageable): Page<Article>
    
    /**
     * 全文搜索（标题和内容）
     */
    @Query("{\$or: [{'title': {\$regex: ?0, \$options: 'i'}}, {'content': {\$regex: ?0, \$options: 'i'}}]}")
    fun searchByTitleOrContent(keyword: String, pageable: Pageable): Page<Article>
    
    /**
     * 根据用户ID和关键词搜索文章
     */
    @Query("{'userId': ?0, \$or: [{'title': {\$regex: ?1, \$options: 'i'}}, {'content': {\$regex: ?1, \$options: 'i'}}]}")
    fun searchByUserIdAndKeyword(userId: Long, keyword: String, pageable: Pageable): Page<Article>
    
    /**
     * 查找公开的文章
     */
    fun findByVisibilityAndStatus(visibility: ArticleVisibility, status: ArticleStatus, pageable: Pageable): Page<Article>
    
    /**
     * 根据创建时间范围查找文章
     */
    fun findByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime, pageable: Pageable): Page<Article>
    
    /**
     * 根据用户ID统计文章数量
     */
    fun countByUserId(userId: Long): Long
    
    /**
     * 根据用户ID和状态统计文章数量
     */
    fun countByUserIdAndStatus(userId: Long, status: ArticleStatus): Long
    
    /**
     * 查找热门文章（按浏览量排序）
     */
    @Query("{'status': 'PUBLISHED', 'visibility': 'PUBLIC'}")
    fun findPopularArticles(pageable: Pageable): Page<Article>
    
    /**
     * 查找最新文章
     */
    @Query("{'status': 'PUBLISHED', 'visibility': 'PUBLIC'}")
    fun findLatestArticles(pageable: Pageable): Page<Article>
    
    /**
     * 根据用户ID删除所有文章
     */
    fun deleteByUserId(userId: Long)
    
    /**
     * 检查文章是否存在
     */
    fun existsByIdAndUserId(id: String, userId: Long): Boolean
}