package com.example.newainotionbackend.entity

import com.baomidou.mybatisplus.annotation.*
import java.time.LocalDateTime

/**
 * AI请求实体类
 * @author AI Assistant
 */
@TableName("ai_requests")
data class AiRequest(
    @TableId(type = IdType.ASSIGN_UUID)
    val id: String? = null,
    
    @TableField("user_id")
    val userId: Long,
    
    @TableField("document_id")
    val documentId: String? = null,
    
    @TableField("request_type")
    val requestType: AiRequestType,
    
    @TableField("prompt")
    val prompt: String,
    
    @TableField("model")
    val model: String,
    
    @TableField("parameters")
    val parameters: String? = null, // JSON字符串存储参数
    
    @TableField("response")
    val response: String? = null,
    
    @TableField("status")
    val status: AiRequestStatus = AiRequestStatus.PENDING,
    
    @TableField("error_message")
    val errorMessage: String? = null,
    
    @TableField("token_count")
    val tokenCount: Int = 0,
    
    @TableField("cost")
    val cost: Double = 0.0,
    
    @TableField("processing_time")
    val processingTime: Long = 0, // 处理时间（毫秒）
    
    @TableField("metadata")
    val metadata: String? = null, // JSON字符串存储元数据
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    val createdAt: LocalDateTime? = null,
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    val updatedAt: LocalDateTime? = null,
    
    @TableField("completed_at")
    val completedAt: LocalDateTime? = null
)

/**
 * AI请求类型枚举
 */
enum class AiRequestType {
    GENERATE_CONTENT,    // 生成内容
    SUMMARIZE,          // 总结
    TRANSLATE,          // 翻译
    REWRITE,           // 重写
    EXPAND,            // 扩展
    OPTIMIZE,          // 优化
    CHAT,              // 聊天
    CODE_GENERATION,   // 代码生成
    QUESTION_ANSWER    // 问答
}

/**
 * AI请求状态枚举
 */
enum class AiRequestStatus {
    PENDING,    // 待处理
    PROCESSING, // 处理中
    COMPLETED,  // 已完成
    FAILED,     // 失败
    CANCELLED   // 已取消
}