package com.example.newainotionbackend.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.example.newainotionbackend.entity.Comment
import org.apache.ibatis.annotations.*
import java.time.LocalDateTime

/**
 * 评论数据访问层
 * 提供评论相关的数据库操作方法
 */
@Mapper
interface CommentMapper : BaseMapper<Comment> {
    
    /**
     * 根据文档ID查找评论
     */
    @Select("SELECT * FROM comments WHERE document_id = #{documentId} AND is_deleted = false ORDER BY created_at DESC")
    fun findByDocumentIdAndIsDeletedFalse(documentId: String): List<Comment>
    
    /**
     * 分页查询文档评论
     */
    fun selectDocumentCommentsPage(page: Page<Comment>, documentId: String): Page<Comment>
    
    /**
     * 根据文档ID查找顶级评论（非回复）
     */
    @Select("SELECT * FROM comments WHERE document_id = #{documentId} AND parent_id IS NULL AND is_deleted = false ORDER BY created_at DESC")
    fun findByDocumentIdAndParentIdIsNullAndIsDeletedFalse(documentId: String): List<Comment>
    
    /**
     * 分页查询文档顶级评论
     */
    fun selectTopLevelCommentsPage(page: Page<Comment>, documentId: String): Page<Comment>
    
    /**
     * 根据父评论ID查找回复
     */
    @Select("SELECT * FROM comments WHERE parent_id = #{parentId} AND is_deleted = false ORDER BY created_at ASC")
    fun findByParentIdAndIsDeletedFalse(parentId: String): List<Comment>
    
    /**
     * 分页查询评论回复
     */
    fun selectCommentRepliesPage(page: Page<Comment>, parentId: String): Page<Comment>
    
    /**
     * 根据用户ID查找评论
     */
    @Select("SELECT * FROM comments WHERE user_id = #{userId} AND is_deleted = false ORDER BY created_at DESC")
    fun findByUserIdAndIsDeletedFalse(userId: Long): List<Comment>
    
    /**
     * 分页查询用户评论
     */
    fun selectUserCommentsPage(page: Page<Comment>, userId: Long): Page<Comment>
    
    /**
     * 根据文档ID和用户ID查找评论
     */
    @Select("SELECT * FROM comments WHERE document_id = #{documentId} AND user_id = #{userId} AND is_deleted = false ORDER BY created_at DESC")
    fun findByDocumentIdAndUserIdAndIsDeletedFalse(documentId: String, userId: Long): List<Comment>
    
    /**
     * 分页查询用户在指定文档的评论
     */
    fun selectUserDocumentCommentsPage(page: Page<Comment>, documentId: String, userId: Long): Page<Comment>
    
    /**
     * 查找提及某用户的评论
     */
    @Select("SELECT * FROM comments WHERE JSON_CONTAINS(mentioned_users, #{userId}) AND is_deleted = false ORDER BY created_at DESC")
    fun findByMentionedUsersContainingAndIsDeletedFalse(userId: Long): List<Comment>
    
    /**
     * 分页查询提及用户的评论
     */
    fun selectMentionedCommentsPage(page: Page<Comment>, userId: Long): Page<Comment>
    
    /**
     * 查找未解决的评论
     */
    @Select("SELECT * FROM comments WHERE document_id = #{documentId} AND is_resolved = false AND is_deleted = false ORDER BY created_at DESC")
    fun findByDocumentIdAndIsResolvedFalseAndIsDeletedFalse(documentId: String): List<Comment>
    
    /**
     * 分页查询未解决的评论
     */
    fun selectUnresolvedCommentsPage(page: Page<Comment>, documentId: String): Page<Comment>
    
    /**
     * 查找已解决的评论
     */
    @Select("SELECT * FROM comments WHERE document_id = #{documentId} AND is_resolved = true AND is_deleted = false ORDER BY created_at DESC")
    fun findByDocumentIdAndIsResolvedTrueAndIsDeletedFalse(documentId: String): List<Comment>
    
    /**
     * 分页查询已解决的评论
     */
    fun selectResolvedCommentsPage(page: Page<Comment>, documentId: String): Page<Comment>
    
    /**
     * 根据文档ID统计评论数量
     */
    @Select("SELECT COUNT(*) FROM comments WHERE document_id = #{documentId} AND is_deleted = false")
    fun countByDocumentIdAndIsDeletedFalse(documentId: String): Long
    
    /**
     * 根据父评论ID统计回复数量
     */
    @Select("SELECT COUNT(*) FROM comments WHERE parent_id = #{parentId} AND is_deleted = false")
    fun countByParentIdAndIsDeletedFalse(parentId: String): Long
    
    /**
     * 根据用户ID统计评论数量
     */
    @Select("SELECT COUNT(*) FROM comments WHERE user_id = #{userId} AND is_deleted = false")
    fun countByUserIdAndIsDeletedFalse(userId: Long): Long
    
    /**
     * 查找最近的评论
     */
    @Select("SELECT * FROM comments WHERE document_id = #{documentId} AND is_deleted = false ORDER BY created_at DESC")
    fun findByDocumentIdAndIsDeletedFalseOrderByCreatedAtDesc(documentId: String): List<Comment>
    
    /**
     * 分页查询最近评论
     */
    fun selectRecentCommentsPage(page: Page<Comment>, documentId: String): Page<Comment>
    
    /**
     * 查找热门评论（按点赞数排序）
     */
    @Select("SELECT * FROM comments WHERE document_id = #{documentId} AND is_deleted = false ORDER BY like_count DESC")
    fun findByDocumentIdAndIsDeletedFalseOrderByLikeCountDesc(documentId: String): List<Comment>
    
    /**
     * 分页查询热门评论
     */
    fun selectPopularCommentsPage(page: Page<Comment>, documentId: String): Page<Comment>
    
    /**
     * 根据时间范围查找评论
     */
    @Select("SELECT * FROM comments WHERE document_id = #{documentId} AND created_at BETWEEN #{startDate} AND #{endDate} AND is_deleted = false ORDER BY created_at DESC")
    fun findByDocumentIdAndCreatedAtBetweenAndIsDeletedFalse(
        documentId: String, 
        startDate: LocalDateTime, 
        endDate: LocalDateTime
    ): List<Comment>
    
    /**
     * 分页查询时间范围内的评论
     */
    fun selectCommentsByDateRangePage(
        page: Page<Comment>,
        documentId: String, 
        startDate: LocalDateTime, 
        endDate: LocalDateTime
    ): Page<Comment>
    
    /**
     * 查找包含附件的评论
     */
    @Select("SELECT * FROM comments WHERE document_id = #{documentId} AND attachments IS NOT NULL AND JSON_LENGTH(attachments) > 0 AND is_deleted = false ORDER BY created_at DESC")
    fun findByDocumentIdAndHasAttachments(documentId: String): List<Comment>
    
    /**
     * 分页查询包含附件的评论
     */
    fun selectCommentsWithAttachmentsPage(page: Page<Comment>, documentId: String): Page<Comment>
    
    /**
     * 根据文档ID删除所有评论（软删除）
     */
    @Update("UPDATE comments SET is_deleted = true, updated_at = NOW() WHERE document_id = #{documentId}")
    fun deleteByDocumentId(documentId: String): Int
    
    /**
     * 根据用户ID删除所有评论（软删除）
     */
    @Update("UPDATE comments SET is_deleted = true, updated_at = NOW() WHERE user_id = #{userId}")
    fun deleteByUserId(userId: Long): Int
    
    /**
     * 更新评论点赞数
     */
    @Update("UPDATE comments SET like_count = like_count + #{increment}, updated_at = NOW() WHERE id = #{commentId}")
    fun updateLikeCount(commentId: String, increment: Int): Int
    
    /**
     * 标记评论为已解决
     */
    @Update("UPDATE comments SET is_resolved = true, resolved_at = NOW(), updated_at = NOW() WHERE id = #{commentId}")
    fun markAsResolved(commentId: String): Int
    
    /**
     * 标记评论为未解决
     */
    @Update("UPDATE comments SET is_resolved = false, resolved_at = NULL, updated_at = NOW() WHERE id = #{commentId}")
    fun markAsUnresolved(commentId: String): Int
    
    /**
     * 软删除评论
     */
    @Update("UPDATE comments SET is_deleted = true, updated_at = NOW() WHERE id = #{commentId}")
    fun softDeleteComment(commentId: String): Int
    
    /**
     * 批量软删除评论
     */
    @Update("UPDATE comments SET is_deleted = true, updated_at = NOW() WHERE id IN (#{commentIds})")
    fun batchSoftDeleteComments(@Param("commentIds") commentIds: List<String>): Int
}