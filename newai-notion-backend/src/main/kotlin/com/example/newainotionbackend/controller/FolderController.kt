package com.example.newainotionbackend.controller

import com.example.newainotionbackend.dto.*
import com.example.newainotionbackend.service.FolderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/folders")
@Tag(name = "文件夹管理", description = "文件夹相关操作接口")
class FolderController(
    private val folderService: FolderService
) {
    
    @PostMapping
    @Operation(summary = "创建文件夹", description = "创建新的文件夹")
    fun createFolder(
        @Valid @RequestBody request: CreateFolderRequest,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<FolderDto>> {
        return try {
            val folder = folderService.createFolder(
                userId = userId,
                name = request.name,
                description = request.description,
                parentId = request.parentId,
                color = request.color,
                icon = request.icon
            )
            ResponseEntity.ok(ApiResponse.success(FolderDto.fromEntity(folder), "文件夹创建成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文件夹创建失败"))
        }
    }
    
    @GetMapping("/{folderId}")
    @Operation(summary = "获取文件夹详情", description = "根据文件夹ID获取文件夹详细信息")
    fun getFolder(
        @PathVariable folderId: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<FolderDto>> {
        return try {
            val folder = folderService.getFolderById(folderId, userId)
            ResponseEntity.ok(ApiResponse.success(FolderDto.fromEntity(folder)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "获取文件夹失败"))
        }
    }
    
    @PutMapping("/{folderId}")
    @Operation(summary = "更新文件夹", description = "更新文件夹信息")
    fun updateFolder(
        @PathVariable folderId: String,
        @Valid @RequestBody request: UpdateFolderRequest,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<FolderDto>> {
        return try {
            val folder = folderService.updateFolder(
                folderId = folderId,
                userId = userId,
                name = request.name,
                description = request.description,
                color = request.color,
                icon = request.icon
            )
            ResponseEntity.ok(ApiResponse.success(FolderDto.fromEntity(folder), "文件夹更新成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文件夹更新失败"))
        }
    }
    
    @DeleteMapping("/{folderId}")
    @Operation(summary = "删除文件夹", description = "删除指定文件夹")
    fun deleteFolder(
        @PathVariable folderId: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            folderService.deleteFolder(folderId, userId)
            ResponseEntity.ok(ApiResponse.success("文件夹删除成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文件夹删除失败"))
        }
    }
    
    @GetMapping
    @Operation(summary = "获取用户文件夹列表", description = "获取当前用户的文件夹列表")
    fun getUserFolders(
        @RequestAttribute("userId") userId: Long,
        @RequestParam(required = false) parentId: String?,
        @RequestParam(defaultValue = "false") includeArchived: Boolean
    ): ResponseEntity<ApiResponse<List<FolderDto>>> {
        return try {
            val folders = folderService.getUserFolders(userId, parentId, includeArchived)
            val folderDtos = folders.map { FolderDto.fromEntity(it) }
            ResponseEntity.ok(ApiResponse.success(folderDtos))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "获取文件夹列表失败"))
        }
    }
    
    @GetMapping("/tree")
    @Operation(summary = "获取文件夹树", description = "获取用户的完整文件夹树结构")
    fun getFolderTree(
        @RequestAttribute("userId") userId: Long,
        @RequestParam(defaultValue = "false") includeArchived: Boolean
    ): ResponseEntity<ApiResponse<List<FolderTreeDto>>> {
        return try {
            val folderTree = folderService.getFolderTree(userId, includeArchived)
            ResponseEntity.ok(ApiResponse.success(folderTree))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "获取文件夹树失败"))
        }
    }
    
    @GetMapping("/{folderId}/contents")
    @Operation(summary = "获取文件夹内容", description = "获取文件夹内的文档和子文件夹")
    fun getFolderContents(
        @PathVariable folderId: String,
        @RequestAttribute("userId") userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "name") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDir: String
    ): ResponseEntity<ApiResponse<FolderContentsResponse>> {
        return try {
            val contents = folderService.getFolderContents(
                folderId = folderId,
                userId = userId,
                page = page,
                size = size,
                sortBy = sortBy,
                sortDir = sortDir
            )
            ResponseEntity.ok(ApiResponse.success(contents))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "获取文件夹内容失败"))
        }
    }
    
    @PostMapping("/{folderId}/move")
    @Operation(summary = "移动文件夹", description = "移动文件夹到指定父文件夹")
    fun moveFolder(
        @PathVariable folderId: String,
        @RequestBody request: MoveFolderRequest,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<FolderDto>> {
        return try {
            val folder = folderService.moveFolder(
                folderId = folderId,
                userId = userId,
                targetParentId = request.targetParentId
            )
            ResponseEntity.ok(ApiResponse.success(FolderDto.fromEntity(folder), "文件夹移动成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文件夹移动失败"))
        }
    }
    
    @PostMapping("/{folderId}/copy")
    @Operation(summary = "复制文件夹", description = "复制文件夹到指定位置")
    fun copyFolder(
        @PathVariable folderId: String,
        @RequestBody request: CopyFolderRequest,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<FolderDto>> {
        return try {
            val folder = folderService.copyFolder(
                folderId = folderId,
                userId = userId,
                targetParentId = request.targetParentId,
                newName = request.newName
            )
            ResponseEntity.ok(ApiResponse.success(FolderDto.fromEntity(folder), "文件夹复制成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文件夹复制失败"))
        }
    }
    
    @PostMapping("/{folderId}/archive")
    @Operation(summary = "归档文件夹", description = "将文件夹归档")
    fun archiveFolder(
        @PathVariable folderId: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<FolderDto>> {
        return try {
            val folder = folderService.archiveFolder(folderId, userId)
            ResponseEntity.ok(ApiResponse.success(FolderDto.fromEntity(folder), "文件夹归档成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文件夹归档失败"))
        }
    }
    
    @PostMapping("/{folderId}/unarchive")
    @Operation(summary = "取消归档文件夹", description = "将文件夹从归档状态恢复")
    fun unarchiveFolder(
        @PathVariable folderId: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<FolderDto>> {
        return try {
            val folder = folderService.unarchiveFolder(folderId, userId)
            ResponseEntity.ok(ApiResponse.success(FolderDto.fromEntity(folder), "文件夹恢复成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文件夹恢复失败"))
        }
    }
    
    @GetMapping("/breadcrumb/{folderId}")
    @Operation(summary = "获取文件夹面包屑", description = "获取文件夹的层级路径")
    fun getFolderBreadcrumb(
        @PathVariable folderId: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<List<BreadcrumbItem>>> {
        return try {
            val breadcrumb = folderService.getFolderBreadcrumb(folderId, userId)
            ResponseEntity.ok(ApiResponse.success(breadcrumb))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "获取面包屑失败"))
        }
    }
}