package com.example.newainotionbackend.service

import com.example.newainotionbackend.dto.*
import com.example.newainotionbackend.entity.Folder
import com.example.newainotionbackend.entity.Document
import com.example.newainotionbackend.mapper.DocumentMapper
import com.example.newainotionbackend.mapper.FolderMapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class FolderService(
    private val folderMapper: FolderMapper,
    private val documentMapper: DocumentMapper
) {
    
    /**
     * 创建文件夹
     */
    fun createFolder(
        userId: Long,
        name: String,
        description: String?,
        parentId: String?,
        color: String?,
        icon: String?
    ): Folder {
        // 检查同级文件夹名称是否重复
        val existingFolder = folderMapper.findByUserIdAndNameAndParentId(userId, name, parentId)
        if (existingFolder != null) {
            throw IllegalArgumentException("文件夹名称已存在")
        }
        
        // 计算路径和层级
        val (folderPath, folderLevel) = if (parentId != null) {
            val parentFolder = folderMapper.selectById(parentId)
                ?: throw IllegalArgumentException("父文件夹不存在")
            if (parentFolder.userId != userId) {
                throw IllegalArgumentException("父文件夹不存在")
            }
            Pair("${parentFolder.path}/${name}", parentFolder.level + 1)
        } else {
            Pair(name, 0)
        }
        
        val folder = Folder(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = name,
            description = description,
            parentId = parentId,
            path = folderPath,
            level = folderLevel,
            color = color ?: "#6366f1",
            icon = icon ?: "folder",
            isShared = false,
            isArchived = false,
            isPinned = false,
            sortOrder = 0,
            documentCount = 0,
            subfolderCount = 0,
            totalSize = 0,
            canRead = true,
            canWrite = true,
            canDelete = true,
            canShare = true,
            canManagePermissions = true,
            tags = null,
            metadata = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        folderMapper.insert(folder)
        
        // 更新父文件夹的子文件夹数量
        if (parentId != null) {
            updateSubfolderCount(parentId)
        }
        
        return folder
    }
    
    /**
     * 获取文件夹详情
     */
    fun getFolderById(folderId: String, userId: Long): Folder {
        val folder = folderMapper.selectById(folderId)
            ?: throw IllegalArgumentException("文件夹不存在")
        if (folder.userId != userId) {
            throw IllegalArgumentException("文件夹不存在")
        }
        return folder
    }
    
    /**
     * 更新文件夹
     */
    fun updateFolder(
        folderId: String,
        userId: Long,
        name: String?,
        description: String?,
        color: String?,
        icon: String?
    ): Folder {
        val folder = getFolderById(folderId, userId)
        
        // 检查名称是否重复（如果名称有变化）
        if (name != null && name != folder.name) {
            val existingFolder = folderMapper.findByUserIdAndNameAndParentId(userId, name, folder.parentId)
            if (existingFolder != null) {
                throw IllegalArgumentException("文件夹名称已存在")
            }
        }
        
        val updatedFolder = folder.copy(
            name = name ?: folder.name,
            description = description ?: folder.description,
            color = color ?: folder.color,
            icon = icon ?: folder.icon,
            updatedAt = LocalDateTime.now()
        )
        
        // 如果名称发生变化，需要更新路径
        if (name != null && name != folder.name) {
            updateFolderPath(updatedFolder)
        }
        
        folderMapper.updateById(updatedFolder)
        return updatedFolder
    }
    
    /**
     * 删除文件夹
     */
    fun deleteFolder(folderId: String, userId: Long) {
        val folder = getFolderById(folderId, userId)
        
        // 检查是否有子文件夹或文档
        if (folder.subfolderCount > 0 || folder.documentCount > 0) {
            throw IllegalArgumentException("文件夹不为空，无法删除")
        }
        
        folderMapper.deleteById(folderId as java.io.Serializable)
        
        // 更新父文件夹的子文件夹数量
        if (folder.parentId != null) {
            updateSubfolderCount(folder.parentId)
        }
    }
    
    /**
     * 获取用户文件夹列表
     */
    fun getUserFolders(userId: Long, parentId: String?, includeArchived: Boolean): List<Folder> {
        return if (includeArchived) {
            folderMapper.findByParentIdAndUserId(parentId, userId)
        } else {
            folderMapper.findByParentIdAndUserId(parentId, userId).filter { !it.isArchived }
        }
    }
    
    /**
     * 获取文件夹树
     */
    fun getFolderTree(userId: Long, includeArchived: Boolean): List<FolderTreeDto> {
        val allFolders = folderMapper.findByUserIdAndIsArchived(userId, includeArchived)
        
        return buildFolderTree(allFolders, null)
    }
    
    /**
     * 获取文件夹内容
     */
    fun getFolderContents(
        folderId: String,
        userId: Long,
        page: Int,
        size: Int,
        sortBy: String,
        sortDir: String
    ): FolderContentsResponse {
        val folder = getFolderById(folderId, userId)
        
        // 获取子文件夹
        val subfolders = folderMapper.findByParentIdAndUserId(folderId, userId).filter { !it.isArchived }
        
        // 获取文档
        val documentPage = Page<Document>(page.toLong(), size.toLong())
        val documents = documentMapper.selectUserDocumentsPage(
            documentPage,
            userId,
            folderId,
            null
        )
        
        val documentDtos = documents.getRecords().map { document: Document -> DocumentSummaryDto.fromEntity(document) }
        val documentPageResponse = PageResponse(
            content = documentDtos,
            page = documents.getCurrent().toInt() - 1,
            size = documents.getSize().toInt(),
            totalElements = documents.getTotal(),
            totalPages = documents.getPages().toInt(),
            first = documents.getCurrent() == 1L,
            last = documents.getCurrent() == documents.getPages()
        )
        
        return FolderContentsResponse(
            folder = FolderDto.fromEntity(folder),
            subfolders = subfolders.map { subfolder: Folder -> FolderDto.fromEntity(subfolder) },
            documents = documentPageResponse
        )
    }
    
    /**
     * 移动文件夹
     */
    fun moveFolder(folderId: String, userId: Long, targetParentId: String?): Folder {
        val folder = getFolderById(folderId, userId)
        
        // 检查目标父文件夹是否存在
        if (targetParentId != null) {
            val targetParent = getFolderById(targetParentId, userId)
            // 检查是否会形成循环引用
            if (isDescendant(targetParentId, folderId)) {
                throw IllegalArgumentException("无法移动到子文件夹")
            }
        }
        
        // 检查目标位置是否有同名文件夹
        val existingFolder = folderMapper.findByUserIdAndNameAndParentId(userId, folder.name, targetParentId)
        if (existingFolder != null) {
            throw IllegalArgumentException("目标位置已存在同名文件夹")
        }
        
        val oldParentId = folder.parentId
        
        // 计算新的路径和层级
        val (newPath, newLevel) = if (targetParentId != null) {
            val targetParent = getFolderById(targetParentId, userId)
            Pair("${targetParent.path}/${folder.name}", targetParent.level + 1)
        } else {
            Pair(folder.name, 0)
        }
        
        val updatedFolder = folder.copy(
            parentId = targetParentId,
            path = newPath,
            level = newLevel,
            updatedAt = LocalDateTime.now()
        )
        
        folderMapper.updateById(updatedFolder)
        
        // 更新路径（包括所有子文件夹）
        updateFolderPath(updatedFolder)
        
        // 更新父文件夹的子文件夹数量
        if (oldParentId != null) {
            updateSubfolderCount(oldParentId)
        }
        if (targetParentId != null) {
            updateSubfolderCount(targetParentId)
        }
        
        return updatedFolder
    }
    
    /**
     * 复制文件夹
     */
    fun copyFolder(folderId: String, userId: Long, targetParentId: String?, newName: String?): Folder {
        val sourceFolder = getFolderById(folderId, userId)
        
        val folderName = newName ?: "${sourceFolder.name} - 副本"
        
        // 检查目标位置是否有同名文件夹
        val existingFolder = folderMapper.findByUserIdAndNameAndParentId(userId, folderName, targetParentId)
        if (existingFolder != null) {
            throw IllegalArgumentException("目标位置已存在同名文件夹")
        }
        
        return createFolder(
            userId = userId,
            name = folderName,
            description = sourceFolder.description,
            parentId = targetParentId,
            color = sourceFolder.color,
            icon = sourceFolder.icon
        )
    }
    
    /**
     * 归档文件夹
     */
    fun archiveFolder(folderId: String, userId: Long): Folder {
        val folder = getFolderById(folderId, userId)
        
        val archivedFolder = folder.copy(
            isArchived = true,
            updatedAt = LocalDateTime.now()
        )
        
        folderMapper.updateById(archivedFolder)
        return archivedFolder
    }
    
    /**
     * 取消归档文件夹
     */
    fun unarchiveFolder(folderId: String, userId: Long): Folder {
        val folder = getFolderById(folderId, userId)
        
        val unarchivedFolder = folder.copy(
            isArchived = false,
            updatedAt = LocalDateTime.now()
        )
        
        folderMapper.updateById(unarchivedFolder)
        return unarchivedFolder
    }
    
    /**
     * 获取文件夹面包屑
     */
    fun getFolderBreadcrumb(folderId: String, userId: Long): List<BreadcrumbItem> {
        val folder = getFolderById(folderId, userId)
        val breadcrumb = mutableListOf<BreadcrumbItem>()
        
        var currentFolder: Folder? = folder
        while (currentFolder != null) {
            breadcrumb.add(0, BreadcrumbItem(
                id = currentFolder.id!!,
                name = currentFolder.name,
                level = currentFolder.level
            ))
            
            currentFolder = if (currentFolder.parentId != null) {
                val parentFolder = folderMapper.selectById(currentFolder.parentId!!)
                if (parentFolder?.userId == userId) parentFolder else null
            } else {
                null
            }
        }
        
        return breadcrumb
    }
    
    // 私有辅助方法
    
    private fun buildFolderTree(folders: List<Folder>, parentId: String?): List<FolderTreeDto> {
        return folders
            .filter { it.parentId == parentId }
            .map { folderItem: Folder ->
                val children = buildFolderTree(folders, folderItem.id)
                FolderTreeDto.fromEntity(folderItem, children.ifEmpty { null })
            }
    }
    
    private fun updateSubfolderCount(folderId: String) {
        val count = folderMapper.countByParentId(folderId)
        val folder = folderMapper.selectById(folderId)
        if (folder != null) {
            folderMapper.updateFolderStats(folderId, folder.documentCount.toLong(), count, folder.totalSize.toLong())
        }
    }
    
    private fun updateFolderPath(folderEntity: Folder) {
        // 更新当前文件夹的所有子文件夹路径
        val subfolders = folderMapper.findByParentId(folderEntity.id!!)
        subfolders.forEach { subfolder: Folder ->
            val newPath = "${folderEntity.path}/${subfolder.name}"
            val newLevel = (folderEntity.level + 1).toInt()
            folderMapper.updateFolderPath(subfolder.id!!, newPath, newLevel)
            val updatedSubfolder = subfolder.copy(
                path = newPath,
                level = newLevel,
                updatedAt = LocalDateTime.now()
            )
            updateFolderPath(updatedSubfolder)
        }
    }
    
    private fun isDescendant(ancestorId: String, descendantId: String): Boolean {
        var currentId: String? = descendantId
        while (currentId != null) {
            if (currentId == ancestorId) {
                return true
            }
            val folderEntity = folderMapper.selectById(currentId)
            currentId = folderEntity?.parentId
        }
        return false
    }
}