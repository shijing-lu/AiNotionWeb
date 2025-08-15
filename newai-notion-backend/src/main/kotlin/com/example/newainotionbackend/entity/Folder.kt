package com.example.newainotionbackend.entity

import com.baomidou.mybatisplus.annotation.*
import java.time.LocalDateTime

/**
 * 文件夹实体类
 * @author AI Assistant
 */
@TableName("folders")
data class Folder(
    @TableId(type = IdType.ASSIGN_UUID)
    val id: String? = null,
    
    @TableField("user_id")
    val userId: Long,
    
    @TableField("name")
    val name: String,
    
    @TableField("description")
    val description: String? = null,
    
    @TableField("parent_id")
    val parentId: String? = null,
    
    @TableField("path")
    val path: String,
    
    @TableField("level")
    val level: Int = 0,
    
    @TableField("color")
    val color: String = "#6366f1",
    
    @TableField("icon")
    val icon: String = "folder",
    
    @TableField("is_shared")
    val isShared: Boolean = false,
    
    @TableField("is_archived")
    val isArchived: Boolean = false,
    
    @TableField("is_pinned")
    val isPinned: Boolean = false,
    
    @TableField("sort_order")
    val sortOrder: Int = 0,
    
    @TableField("document_count")
    val documentCount: Int = 0,
    
    @TableField("subfolder_count")
    val subfolderCount: Int = 0,
    
    @TableField("total_size")
    val totalSize: Long = 0,
    
    @TableField("can_read")
    val canRead: Boolean = true,
    
    @TableField("can_write")
    val canWrite: Boolean = true,
    
    @TableField("can_delete")
    val canDelete: Boolean = true,
    
    @TableField("can_share")
    val canShare: Boolean = true,
    
    @TableField("can_manage_permissions")
    val canManagePermissions: Boolean = true,
    
    @TableField("tags")
    val tags: String? = null, // JSON字符串存储标签
    
    @TableField("metadata")
    val metadata: String? = null, // JSON字符串存储元数据
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    val createdAt: LocalDateTime? = null,
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    val updatedAt: LocalDateTime? = null
) {
    /**
     * 获取权限对象
     */
    fun getPermissions(): FolderPermissions {
        return FolderPermissions(
            canRead = canRead,
            canWrite = canWrite,
            canDelete = canDelete,
            canShare = canShare,
            canManagePermissions = canManagePermissions
        )
    }
}

/**
 * 文件夹权限数据类
 */
data class FolderPermissions(
    val canRead: Boolean = true,
    val canWrite: Boolean = true,
    val canDelete: Boolean = true,
    val canShare: Boolean = true,
    val canManagePermissions: Boolean = true
)