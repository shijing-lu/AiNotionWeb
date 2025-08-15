package com.example.newainotionbackend.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.example.newainotionbackend.entity.Document
import com.example.newainotionbackend.entity.DocumentStatus
import com.example.newainotionbackend.entity.DocumentVisibility
import org.apache.ibatis.annotations.*
import java.time.LocalDateTime

/**
 * 文档数据访问层
 * 提供文档的CRUD操作和复杂查询功能
 */
@Mapper
interface DocumentMapper : BaseMapper<Document> {
    
    /**
     * 根据用户ID查询文档
     */
    @Select("SELECT * FROM documents WHERE user_id = #{userId} AND status != 'DELETED' ORDER BY updated_at DESC")
    fun findByUserId(userId: Long): List<Document>
    
    /**
     * 根据用户ID和状态查询文档
     */
    @Select("SELECT * FROM documents WHERE user_id = #{userId} AND status = #{status} ORDER BY updated_at DESC")
    fun findByUserIdAndStatus(userId: Long, status: DocumentStatus): List<Document>
    
    /**
     * 根据用户ID和文件夹ID查询文档
     */
    @Select("SELECT * FROM documents WHERE user_id = #{userId} AND folder_id = #{folderId} AND status != 'DELETED' ORDER BY updated_at DESC")
    fun findByUserIdAndFolderId(userId: Long, folderId: String): List<Document>
    
    /**
     * 根据用户ID查询收藏文档
     */
    @Select("SELECT * FROM documents WHERE user_id = #{userId} AND is_favorite = true AND status != 'DELETED' ORDER BY updated_at DESC")
    fun findByUserIdAndIsFavoriteTrue(userId: Long): List<Document>
    
    /**
     * 根据用户ID查询置顶文档
     */
    @Select("SELECT * FROM documents WHERE user_id = #{userId} AND is_pinned = true AND status != 'DELETED' ORDER BY updated_at DESC")
    fun findByUserIdAndIsPinnedTrue(userId: Long): List<Document>
    
    /**
     * 根据用户ID查询已归档文档
     */
    @Select("SELECT * FROM documents WHERE user_id = #{userId} AND is_archived = true ORDER BY updated_at DESC")
    fun findByUserIdAndIsArchivedTrue(userId: Long): List<Document>
    
    /**
     * 根据用户ID和更新时间查询最近文档
     */
    @Select("SELECT * FROM documents WHERE user_id = #{userId} AND updated_at > #{after} AND status != 'DELETED' ORDER BY updated_at DESC")
    fun findByUserIdAndUpdatedAtAfter(userId: Long, after: LocalDateTime): List<Document>
    
    /**
     * 根据可见性和状态查询公开文档
     */
    @Select("SELECT * FROM documents WHERE visibility = #{visibility} AND status = #{status} ORDER BY updated_at DESC")
    fun findByVisibilityAndStatus(visibility: DocumentVisibility, status: DocumentStatus): List<Document>
    
    /**
     * 根据用户ID和关键词搜索文档
     */
    @Select("""SELECT * FROM documents 
              WHERE user_id = #{userId} 
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')) 
              AND status != 'DELETED' 
              ORDER BY updated_at DESC""")
    fun searchByUserIdAndKeyword(userId: Long, keyword: String): List<Document>
    
    /**
     * 搜索公开文档
     */
    @Select("""SELECT * FROM documents 
              WHERE visibility = 'PUBLIC' 
              AND status = 'PUBLISHED' 
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')) 
              ORDER BY updated_at DESC""")
    fun searchPublicDocuments(keyword: String): List<Document>
    
    /**
     * 分页查询用户文档
     */
    fun selectUserDocumentsPage(page: Page<Document>, userId: Long, folderId: String?, status: DocumentStatus?): Page<Document>
    
    /**
     * 分页搜索用户文档
     */
    fun searchUserDocumentsPage(page: Page<Document>, userId: Long, keyword: String): Page<Document>
    
    /**
     * 分页搜索公开文档
     */
    fun searchPublicDocumentsPage(page: Page<Document>, keyword: String): Page<Document>
    
    /**
     * 分页查询收藏文档
     */
    fun selectFavoriteDocumentsPage(page: Page<Document>, userId: Long): Page<Document>
    
    /**
     * 分页查询置顶文档
     */
    fun selectPinnedDocumentsPage(page: Page<Document>, userId: Long): Page<Document>
    
    /**
     * 分页查询已归档文档
     */
    fun selectArchivedDocumentsPage(page: Page<Document>, userId: Long): Page<Document>
    
    /**
     * 分页查询最近文档
     */
    fun selectRecentDocumentsPage(page: Page<Document>, userId: Long, after: LocalDateTime): Page<Document>
    
    /**
     * 分页查询公开文档
     */
    fun selectPublicDocumentsPage(page: Page<Document>, visibility: DocumentVisibility, status: DocumentStatus): Page<Document>
    
    /**
     * 更新文档查看信息
     */
    @Update("UPDATE documents SET view_count = view_count + 1, last_viewed_at = #{viewedAt} WHERE id = #{documentId}")
    fun updateDocumentView(documentId: String, viewedAt: LocalDateTime): Int
    
    /**
     * 统计用户文档数量
     */
    @Select("SELECT COUNT(*) FROM documents WHERE user_id = #{userId} AND status != 'DELETED'")
    fun countByUserId(userId: Long): Long
    
    /**
     * 统计文件夹中的文档数量
     */
    @Select("SELECT COUNT(*) FROM documents WHERE folder_id = #{folderId} AND status != 'DELETED'")
    fun countByFolderId(folderId: String): Long
    
    /**
     * 根据标签查询文档
     */
    @Select("SELECT * FROM documents WHERE JSON_CONTAINS(tags, JSON_QUOTE(#{tag})) AND status != 'DELETED' ORDER BY updated_at DESC")
    fun findByTag(tag: String): List<Document>
    
    /**
     * 批量删除文档（软删除）
     */
    @Update("UPDATE documents SET status = 'DELETED', updated_at = NOW() WHERE id IN (#{ids}) AND user_id = #{userId}")
    fun batchSoftDelete(@Param("ids") ids: List<String>, userId: Long): Int
    
    /**
     * 根据用户ID和状态统计文档数量
     */
    @Select("SELECT COUNT(*) FROM documents WHERE user_id = #{userId} AND status = #{status}")
    fun countByUserIdAndStatus(userId: Long, status: DocumentStatus): Long
    
    /**
     * 根据用户ID和文件夹ID统计文档数量
     */
    @Select("SELECT COUNT(*) FROM documents WHERE user_id = #{userId} AND folder_id = #{folderId} AND status != 'DELETED'")
    fun countByUserIdAndFolderId(userId: Long, folderId: String?): Long
    
    /**
     * 查找最近查看的文档
     */
    @Select("SELECT * FROM documents WHERE user_id = #{userId} AND last_viewed_at IS NOT NULL AND status != 'DELETED' ORDER BY last_viewed_at DESC")
    fun findByUserIdAndLastViewedAtIsNotNull(userId: Long): List<Document>
    
    /**
     * 分页查询最近查看的文档
     */
    fun selectRecentViewedDocumentsPage(page: Page<Document>, userId: Long): Page<Document>
    
    /**
     * 查找热门文档（按查看次数排序）
     */
    @Select("SELECT * FROM documents WHERE visibility = #{visibility} AND status = #{status} ORDER BY view_count DESC")
    fun findByVisibilityAndStatusOrderByViewCountDesc(visibility: DocumentVisibility, status: DocumentStatus): List<Document>
    
    /**
     * 分页查询热门文档
     */
    fun selectPopularDocumentsPage(page: Page<Document>, visibility: DocumentVisibility, status: DocumentStatus): Page<Document>
    
    /**
     * 查找最新发布的文档
     */
    @Select("SELECT * FROM documents WHERE visibility = #{visibility} AND status = #{status} ORDER BY published_at DESC")
    fun findByVisibilityAndStatusOrderByPublishedAtDesc(visibility: DocumentVisibility, status: DocumentStatus): List<Document>
    
    /**
     * 分页查询最新发布的文档
     */
    fun selectLatestPublishedDocumentsPage(page: Page<Document>, visibility: DocumentVisibility, status: DocumentStatus): Page<Document>
    
    /**
     * 根据模板ID查找文档
     */
    @Select("SELECT * FROM documents WHERE template_id = #{templateId} AND status != 'DELETED' ORDER BY created_at DESC")
    fun findByTemplateId(templateId: String): List<Document>
    
    /**
     * 分页查询模板文档
     */
    fun selectTemplateDocumentsPage(page: Page<Document>, templateId: String): Page<Document>
    
    /**
     * 查找模板文档
     */
    @Select("SELECT * FROM documents WHERE is_template = true AND status != 'DELETED' ORDER BY created_at DESC")
    fun findByIsTemplateTrue(): List<Document>
    
    /**
     * 分页查询模板文档
     */
    fun selectTemplatesPage(page: Page<Document>): Page<Document>
    
    /**
     * 根据协作者查找文档
     */
    @Select("SELECT * FROM documents WHERE JSON_CONTAINS(collaborators, JSON_OBJECT('userId', #{userId})) AND status != 'DELETED' ORDER BY updated_at DESC")
    fun findByCollaboratorUserId(userId: Long): List<Document>
    
    /**
     * 分页查询协作文档
     */
    fun selectCollaborativeDocumentsPage(page: Page<Document>, userId: Long): Page<Document>
    
    /**
     * 查找即将过期的文档
     */
    @Select("SELECT * FROM documents WHERE expires_at BETWEEN #{start} AND #{end} AND status != 'DELETED'")
    fun findByExpiresAtBetween(start: LocalDateTime, end: LocalDateTime): List<Document>
    
    /**
     * 查找已过期的文档
     */
    @Select("SELECT * FROM documents WHERE expires_at < #{dateTime} AND status != 'DELETED'")
    fun findByExpiresAtBefore(dateTime: LocalDateTime): List<Document>
    
    /**
     * 根据用户ID删除所有文档（软删除）
     */
    @Update("UPDATE documents SET status = 'DELETED', updated_at = NOW() WHERE user_id = #{userId}")
    fun deleteByUserId(userId: Long): Int
    
    /**
     * 根据用户ID和标签查询文档
     */
    @Select("SELECT * FROM documents WHERE user_id = #{userId} AND JSON_OVERLAPS(tags, #{tags}) AND status != 'DELETED' ORDER BY updated_at DESC")
    fun findByUserIdAndTagsIn(userId: Long, @Param("tags") tags: List<String>): List<Document>
    
    /**
     * 分页查询带标签的文档
     */
    fun selectDocumentsByTagsPage(page: Page<Document>, userId: Long, @Param("tags") tags: List<String>): Page<Document>
    
    /**
     * 根据用户ID和文件夹ID查询未归档文档
     */
    @Select("SELECT * FROM documents WHERE user_id = #{userId} AND folder_id = #{folderId} AND is_archived = false AND status != 'DELETED' ORDER BY updated_at DESC")
    fun findByUserIdAndFolderIdAndIsArchivedFalse(userId: Long, folderId: String?): List<Document>
    
    /**
     * 分页查询未归档文档
     */
    fun selectNonArchivedDocumentsPage(page: Page<Document>, userId: Long, folderId: String?): Page<Document>
}