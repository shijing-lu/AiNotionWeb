package com.example.newainotionbackend.dto

import com.example.newainotionbackend.entity.Folder
import com.example.newainotionbackend.entity.User
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

/**
 * 文件夹信息DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FolderDto(
    val id: String,
    val userId: Long,
    val name: String,
    val description: String?,
    val parentId: String?,
    val path: String,
    val level: Int,
    val color: String?,
    val icon: String?,
    val isShared: Boolean,
    val isArchived: Boolean,
    val isPinned: Boolean,
    val sortOrder: Int,
    val documentCount: Long,
    val subfolderCount: Long,
    val totalSize: Long,
    val permissions: String?, // JSON字符串存储权限信息
    val collaborators: String?, // JSON字符串存储协作者信息
    val tags: List<String>,
    val metadata: Map<String, Any>,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(folder: Folder): FolderDto {
            return FolderDto(
                id = folder.id!!,
                userId = folder.userId,
                name = folder.name,
                description = folder.description,
                parentId = folder.parentId,
                path = folder.path,
                level = folder.level,
                color = folder.color,
                icon = folder.icon,
                isShared = folder.isShared,
                isArchived = folder.isArchived,
                isPinned = folder.isPinned,
                sortOrder = folder.sortOrder,
                documentCount = folder.documentCount.toLong(),
                subfolderCount = folder.subfolderCount.toLong(),
                totalSize = folder.totalSize,
                permissions = null, // 权限信息待实现
                collaborators = null, // 协作者信息待实现
                tags = emptyList(), // 标签解析待实现
                metadata = emptyMap(), // 元数据解析待实现
                createdAt = folder.createdAt,
                updatedAt = folder.updatedAt
            )
        }
    }
}

/**
 * 文件夹树形结构DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FolderTreeDto(
    val id: String,
    val name: String,
    val parentId: String?,
    val level: Int,
    val color: String?,
    val icon: String?,
    val isArchived: Boolean,
    val isPinned: Boolean,
    val documentCount: Long,
    val subfolderCount: Long,
    val children: List<FolderTreeDto>?
) {
    companion object {
        fun fromEntity(folder: Folder, children: List<FolderTreeDto>? = null): FolderTreeDto {
            return FolderTreeDto(
                id = folder.id!!,
                name = folder.name,
                parentId = folder.parentId,
                level = folder.level,
                color = folder.color,
                icon = folder.icon,
                isArchived = folder.isArchived,
                isPinned = folder.isPinned,
                documentCount = folder.documentCount.toLong(),
                subfolderCount = folder.subfolderCount.toLong(),
                children = children
            )
        }
    }
}

/**
 * 创建文件夹请求
 */
data class CreateFolderRequest(
    val name: String,
    val description: String?,
    val parentId: String?,
    val color: String?,
    val icon: String?
)

/**
 * 更新文件夹请求
 */
data class UpdateFolderRequest(
    val name: String?,
    val description: String?,
    val color: String?,
    val icon: String?
)

/**
 * 移动文件夹请求
 */
data class MoveFolderRequest(
    val targetParentId: String?
)

/**
 * 复制文件夹请求
 */
data class CopyFolderRequest(
    val targetParentId: String?,
    val newName: String?
)

/**
 * 文件夹内容响应
 */
data class FolderContentsResponse(
    val folder: FolderDto,
    val subfolders: List<FolderDto>,
    val documents: PageResponse<DocumentSummaryDto>
)

/**
 * 面包屑项
 */
data class BreadcrumbItem(
    val id: String,
    val name: String,
    val level: Int
)

/**
 * 文件夹简要信息DTO（用于列表显示）
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FolderSummaryDto(
    val id: String,
    val name: String,
    val parentId: String?,
    val color: String?,
    val icon: String?,
    val isArchived: Boolean,
    val isPinned: Boolean,
    val documentCount: Long,
    val subfolderCount: Long,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(folder: Folder): FolderSummaryDto {
            return FolderSummaryDto(
                id = folder.id!!,
                name = folder.name,
                parentId = folder.parentId,
                color = folder.color,
                icon = folder.icon,
                isArchived = folder.isArchived,
                isPinned = folder.isPinned,
                documentCount = folder.documentCount.toLong(),
                subfolderCount = folder.subfolderCount.toLong(),
                createdAt = folder.createdAt,
                updatedAt = folder.updatedAt
            )
        }
    }
}