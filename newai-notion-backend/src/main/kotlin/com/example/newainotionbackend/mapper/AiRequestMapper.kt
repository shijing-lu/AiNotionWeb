package com.example.newainotionbackend.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.example.newainotionbackend.entity.AiRequest
import com.example.newainotionbackend.entity.AiRequestStatus
import com.example.newainotionbackend.entity.AiRequestType
import org.apache.ibatis.annotations.*
import java.time.LocalDateTime

/**
 * AI请求数据访问层
 * 提供AI请求相关的数据库操作方法
 */
@Mapper
interface AiRequestMapper : BaseMapper<AiRequest> {
    
    /**
     * 根据用户ID查找AI请求
     */
    @Select("SELECT * FROM ai_requests WHERE user_id = #{userId} ORDER BY created_at DESC")
    fun findByUserId(userId: Long): List<AiRequest>
    
    /**
     * 分页查询用户AI请求
     */
    fun selectUserRequestsPage(page: Page<AiRequest>, userId: Long): Page<AiRequest>
    
    /**
     * 根据用户ID和状态查找AI请求
     */
    @Select("SELECT * FROM ai_requests WHERE user_id = #{userId} AND status = #{status} ORDER BY created_at DESC")
    fun findByUserIdAndStatus(userId: Long, status: AiRequestStatus): List<AiRequest>
    
    /**
     * 分页查询用户指定状态的AI请求
     */
    fun selectUserRequestsByStatusPage(page: Page<AiRequest>, userId: Long, status: AiRequestStatus): Page<AiRequest>
    
    /**
     * 根据用户ID和请求类型查找AI请求
     */
    @Select("SELECT * FROM ai_requests WHERE user_id = #{userId} AND request_type = #{requestType} ORDER BY created_at DESC")
    fun findByUserIdAndRequestType(userId: Long, requestType: AiRequestType): List<AiRequest>
    
    /**
     * 分页查询用户指定类型的AI请求
     */
    fun selectUserRequestsByTypePage(page: Page<AiRequest>, userId: Long, requestType: AiRequestType): Page<AiRequest>
    
    /**
     * 根据文档ID查找AI请求
     */
    @Select("SELECT * FROM ai_requests WHERE document_id = #{documentId} ORDER BY created_at DESC")
    fun findByDocumentId(documentId: String): List<AiRequest>
    
    /**
     * 分页查询文档相关的AI请求
     */
    fun selectDocumentRequestsPage(page: Page<AiRequest>, documentId: String): Page<AiRequest>
    
    /**
     * 根据用户ID和文档ID查找AI请求
     */
    @Select("SELECT * FROM ai_requests WHERE user_id = #{userId} AND document_id = #{documentId} ORDER BY created_at DESC")
    fun findByUserIdAndDocumentId(userId: Long, documentId: String): List<AiRequest>
    
    /**
     * 分页查询用户文档相关的AI请求
     */
    fun selectUserDocumentRequestsPage(page: Page<AiRequest>, userId: Long, documentId: String): Page<AiRequest>
    
    /**
     * 查找待处理的AI请求
     */
    @Select("SELECT * FROM ai_requests WHERE status = #{status} ORDER BY created_at ASC")
    fun findByStatusOrderByCreatedAtAsc(status: AiRequestStatus): List<AiRequest>
    
    /**
     * 分页查询待处理的AI请求
     */
    fun selectPendingRequestsPage(page: Page<AiRequest>, status: AiRequestStatus): Page<AiRequest>
    
    /**
     * 根据用户ID统计AI请求数量
     */
    @Select("SELECT COUNT(*) FROM ai_requests WHERE user_id = #{userId}")
    fun countByUserId(userId: Long): Long
    
    /**
     * 根据用户ID和状态统计AI请求数量
     */
    @Select("SELECT COUNT(*) FROM ai_requests WHERE user_id = #{userId} AND status = #{status}")
    fun countByUserIdAndStatus(userId: Long, status: AiRequestStatus): Long
    
    /**
     * 根据用户ID和请求类型统计AI请求数量
     */
    @Select("SELECT COUNT(*) FROM ai_requests WHERE user_id = #{userId} AND request_type = #{requestType}")
    fun countByUserIdAndRequestType(userId: Long, requestType: AiRequestType): Long
    
    /**
     * 根据时间范围统计AI请求数量
     */
    @Select("SELECT COUNT(*) FROM ai_requests WHERE created_at BETWEEN #{startDate} AND #{endDate}")
    fun countByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): Long
    
    /**
     * 根据用户ID和时间范围统计AI请求数量
     */
    @Select("SELECT COUNT(*) FROM ai_requests WHERE user_id = #{userId} AND created_at BETWEEN #{startDate} AND #{endDate}")
    fun countByUserIdAndCreatedAtBetween(userId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Long
    
    /**
     * 查找最近的AI请求
     */
    @Select("SELECT * FROM ai_requests WHERE user_id = #{userId} ORDER BY created_at DESC")
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<AiRequest>
    
    /**
     * 分页查询用户最近的AI请求
     */
    fun selectUserRecentRequestsPage(page: Page<AiRequest>, userId: Long): Page<AiRequest>
    
    /**
     * 查找成功的AI请求
     */
    @Select("SELECT * FROM ai_requests WHERE user_id = #{userId} AND status = #{status} ORDER BY created_at DESC")
    fun findByUserIdAndStatusOrderByCreatedAtDesc(userId: Long, status: AiRequestStatus): List<AiRequest>
    
    /**
     * 查找失败的AI请求
     */
    @Select("SELECT * FROM ai_requests WHERE status = #{status} AND created_at > #{createdAt} ORDER BY created_at DESC")
    fun findByStatusAndCreatedAtAfter(status: AiRequestStatus, createdAt: LocalDateTime): List<AiRequest>
    
    /**
     * 分页查询失败的AI请求
     */
    fun selectFailedRequestsPage(page: Page<AiRequest>, status: AiRequestStatus, createdAt: LocalDateTime): Page<AiRequest>
    
    /**
     * 根据模型统计使用情况
     */
    @Select("SELECT COUNT(*) FROM ai_requests WHERE model = #{model} AND status = 'COMPLETED'")
    fun countByModelAndCompleted(model: String): Long
    
    /**
     * 计算用户的总token使用量
     */
    @Select("SELECT * FROM ai_requests WHERE user_id = #{userId} AND status = 'COMPLETED'")
    fun findCompletedRequestsByUserId(userId: Long): List<AiRequest>
    
    /**
     * 查找高质量的AI响应
     */
    @Select("SELECT * FROM ai_requests WHERE user_id = #{userId} AND status = 'COMPLETED' AND quality >= #{minQuality} ORDER BY created_at DESC")
    fun findHighQualityResponses(userId: Long, minQuality: Double): List<AiRequest>
    
    /**
     * 分页查询高质量的AI响应
     */
    fun selectHighQualityResponsesPage(page: Page<AiRequest>, userId: Long, minQuality: Double): Page<AiRequest>
    
    /**
     * 查找用户反馈的AI请求
     */
    @Select("SELECT * FROM ai_requests WHERE user_id = #{userId} AND user_feedback IS NOT NULL ORDER BY created_at DESC")
    fun findWithUserFeedback(userId: Long): List<AiRequest>
    
    /**
     * 分页查询有用户反馈的AI请求
     */
    fun selectRequestsWithFeedbackPage(page: Page<AiRequest>, userId: Long): Page<AiRequest>
    
    /**
     * 根据用户ID删除所有AI请求
     */
    @Delete("DELETE FROM ai_requests WHERE user_id = #{userId}")
    fun deleteByUserId(userId: Long): Int
    
    /**
     * 根据文档ID删除所有AI请求
     */
    @Delete("DELETE FROM ai_requests WHERE document_id = #{documentId}")
    fun deleteByDocumentId(documentId: String): Int
    
    /**
     * 更新AI请求状态
     */
    @Update("UPDATE ai_requests SET status = #{status}, updated_at = NOW() WHERE id = #{requestId}")
    fun updateRequestStatus(requestId: String, status: AiRequestStatus): Int
    
    /**
     * 更新AI请求响应
     */
    @Update("UPDATE ai_requests SET response = #{response}, status = 'COMPLETED', completed_at = NOW(), updated_at = NOW() WHERE id = #{requestId}")
    fun updateRequestResponse(requestId: String, response: String): Int
    
    /**
     * 更新AI请求错误信息
     */
    @Update("UPDATE ai_requests SET error_message = #{errorMessage}, status = 'FAILED', updated_at = NOW() WHERE id = #{requestId}")
    fun updateRequestError(requestId: String, errorMessage: String): Int
}