package com.example.newainotionbackend.entity

import com.baomidou.mybatisplus.annotation.*
import java.time.LocalDateTime

/**
 * 评论实体类
 * @author AI Assistant
 */
@TableName("comments")
data class Comment(
    @TableId(type = IdType.ASSIGN_UUID)
    val id: String? = null,
    
    @TableField("document_id")
    val documentId: String,
    
    @TableField("user_id")
    val userId: Long,
    
    @TableField("parent_id")
    val parentId: String? = null,
    
    @TableField("content")
    val content: String,
    
    @TableField("is_resolved")
    val isResolved: Boolean = false,
    
    @TableField("resolved_by")
    val resolvedBy: Long? = null,
    
    @TableField("resolved_at")
    val resolvedAt: LocalDateTime? = null,
    
    @TableField("like_count")
    val likeCount: Int = 0,
    
    @TableField("reply_count")
    val replyCount: Int = 0,
    
    @TableField("mentioned_users")
    val mentionedUsers: String? = null, // JSON字符串存储提及的用户
    
    @TableField("attachments")
    val attachments: String? = null, // JSON字符串存储附件
    
    @TableField("metadata")
    val metadata: String? = null, // JSON字符串存储元数据
    
    @TableField("is_deleted")
    val isDeleted: Boolean = false,
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    val createdAt: LocalDateTime? = null,
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    val updatedAt: LocalDateTime? = null
)

/**
 * 评论状态枚举
 */
enum class CommentStatus {
    ACTIVE,     // 活跃
    RESOLVED,   // 已解决
    DELETED     // 已删除
}