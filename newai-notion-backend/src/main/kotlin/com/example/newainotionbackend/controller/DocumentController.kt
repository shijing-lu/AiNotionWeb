package com.example.newainotionbackend.controller

import com.example.newainotionbackend.dto.*
import com.example.newainotionbackend.entity.DocumentStatus
import com.example.newainotionbackend.entity.DocumentVisibility
import com.example.newainotionbackend.service.DocumentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import jakarta.validation.Valid

@RestController
@RequestMapping("/documents")
@Tag(name = "文档管理", description = "文档相关操作接口")
class DocumentController(
    private val documentService: DocumentService
) {
    
    @PostMapping
    @Operation(summary = "创建文档", description = "创建新的文档")
    fun createDocument(
        @Valid @RequestBody request: CreateDocumentRequest,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<DocumentDto>> {
        return try {
            val document = documentService.createDocument(
                userId = userId,
                title = request.title,
                content = request.content ?: "",
                folderId = request.folderId,
                visibility = request.visibility ?: DocumentVisibility.PRIVATE
            )
            ResponseEntity.ok(ApiResponse.success(DocumentDto.fromEntity(document), "文档创建成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文档创建失败"))
        }
    }
    
    @GetMapping("/{documentId}")
    @Operation(summary = "获取文档详情", description = "根据文档ID获取文档详细信息")
    fun getDocument(
        @PathVariable documentId: String,
        @RequestAttribute("userId") userId: Long?
    ): ResponseEntity<ApiResponse<DocumentDto>> {
        return try {
            val document = documentService.getDocument(documentId, userId)
            ResponseEntity.ok(ApiResponse.success(DocumentDto.fromEntity(document)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "获取文档失败"))
        }
    }
    
    @PutMapping("/{documentId}")
    @Operation(summary = "更新文档", description = "更新文档内容和属性")
    fun updateDocument(
        @PathVariable documentId: String,
        @Valid @RequestBody request: UpdateDocumentRequest,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<DocumentDto>> {
        return try {
            val updates = mutableMapOf<String, Any?>()
            request.title?.let { updates["title"] = it }
            request.content?.let { updates["content"] = it }
            request.summary?.let { updates["summary"] = it }
            request.tags?.let { updates["tags"] = it }
            request.visibility?.let { visibility -> updates["visibility"] = visibility }
            request.coverImage?.let { updates["coverImage"] = it }
            
            val document = documentService.updateDocument(
                documentId = documentId,
                userId = userId,
                updates = updates
            )
            ResponseEntity.ok(ApiResponse.success(DocumentDto.fromEntity(document), "文档更新成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文档更新失败"))
        }
    }
    
    @DeleteMapping("/{documentId}")
    @Operation(summary = "删除文档", description = "删除指定文档")
    fun deleteDocument(
        @PathVariable documentId: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            documentService.deleteDocument(documentId, userId)
            ResponseEntity.ok(ApiResponse.success("文档删除成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文档删除失败"))
        }
    }
    
    @GetMapping
    @Operation(summary = "获取用户文档列表", description = "获取当前用户的文档列表")
    fun getUserDocuments(
        @RequestAttribute("userId") userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) folderId: String?,
        @RequestParam(required = false) status: DocumentStatus?,
        @RequestParam(defaultValue = "updatedAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String
    ): ResponseEntity<ApiResponse<PageResponse<DocumentDto>>> {
        return try {
            val pageable = org.springframework.data.domain.PageRequest.of(
                page, size, 
                if (sortDir == "desc") 
                    org.springframework.data.domain.Sort.by(sortBy).descending() 
                else 
                    org.springframework.data.domain.Sort.by(sortBy).ascending()
            )
            val documents = documentService.getUserDocuments(
                userId = userId,
                folderId = folderId,
                status = status,
                pageable = pageable
            )
            val documentDtos = documents.records.map { document -> DocumentDto.fromEntity(document) }
            val response = PageResponse(
                content = documentDtos,
                page = (documents.current - 1).toInt(),
                size = documents.size.toInt(),
                totalElements = documents.total,
                totalPages = documents.pages.toInt(),
                first = documents.current == 1L,
                last = documents.current == documents.pages
            )
            ResponseEntity.ok(ApiResponse.success(response))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error<PageResponse<DocumentDto>>(e.message ?: "获取文档列表失败", null))
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索文档", description = "根据关键词搜索文档")
    fun searchDocuments(
        @RequestParam query: String,
        @RequestAttribute("userId") userId: Long?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) tags: List<String>?,
        @RequestParam(required = false) visibility: DocumentVisibility?
    ): ResponseEntity<ApiResponse<PageResponse<DocumentDto>>> {
        return try {
            val pageable = org.springframework.data.domain.PageRequest.of(page, size)
            val documents = documentService.searchDocuments(
                userId = userId,
                keyword = query,
                pageable = pageable
            )
            val documentDtos = documents.records.map { document -> DocumentDto.fromEntity(document) }
            val response = PageResponse(
                content = documentDtos,
                page = (documents.current - 1).toInt(),
                size = documents.size.toInt(),
                totalElements = documents.total,
                totalPages = documents.pages.toInt(),
                first = documents.current == 1L,
                last = documents.current == documents.pages
            )
            ResponseEntity.ok(ApiResponse.success(response))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error<PageResponse<DocumentDto>>(e.message ?: "搜索失败", null))
        }
    }
    
    @GetMapping("/favorites")
    @Operation(summary = "获取收藏文档", description = "获取用户收藏的文档列表")
    fun getFavoriteDocuments(
        @RequestAttribute("userId") userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<PageResponse<DocumentDto>>> {
        return try {
            val pageable = org.springframework.data.domain.PageRequest.of(page, size)
            val documents = documentService.getFavoriteDocuments(userId, pageable)
            val documentDtos = documents.records.map { document -> DocumentDto.fromEntity(document) }
            val response = PageResponse(
                content = documentDtos,
                page = (documents.current - 1).toInt(),
                size = documents.size.toInt(),
                totalElements = documents.total,
                totalPages = documents.pages.toInt(),
                first = documents.current == 1L,
                last = documents.current == documents.pages
            )
            ResponseEntity.ok(ApiResponse.success(response))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error<PageResponse<DocumentDto>>(e.message ?: "获取收藏文档失败", null))
        }
    }
    
    @PostMapping("/{documentId}/favorite")
    @Operation(summary = "收藏文档", description = "将文档添加到收藏夹")
    fun favoriteDocument(
        @PathVariable documentId: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            documentService.favoriteDocument(documentId, userId)
            ResponseEntity.ok(ApiResponse.success("收藏成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "收藏失败"))
        }
    }
    
    @DeleteMapping("/{documentId}/favorite")
    @Operation(summary = "取消收藏", description = "从收藏夹移除文档")
    fun unfavoriteDocument(
        @PathVariable documentId: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            documentService.unfavoriteDocument(documentId, userId)
            ResponseEntity.ok(ApiResponse.success("取消收藏成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "取消收藏失败"))
        }
    }
    
    @PostMapping("/{documentId}/copy")
    @Operation(summary = "复制文档", description = "复制文档到指定文件夹")
    fun copyDocument(
        @PathVariable documentId: String,
        @RequestBody request: CopyDocumentRequest,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<DocumentDto>> {
        return try {
            val document = documentService.copyDocument(
                documentId = documentId,
                userId = userId,
                targetFolderId = request.targetFolderId,
                newTitle = request.newTitle
            )
            ResponseEntity.ok(ApiResponse.success(DocumentDto.fromEntity(document), "文档复制成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文档复制失败"))
        }
    }
    
    @PutMapping("/{documentId}/move")
    @Operation(summary = "移动文档", description = "移动文档到指定文件夹")
    fun moveDocument(
        @PathVariable documentId: String,
        @RequestBody request: MoveDocumentRequest,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<DocumentDto>> {
        return try {
            val document = documentService.moveDocument(
                documentId = documentId,
                userId = userId,
                targetFolderId = request.targetFolderId
            )
            ResponseEntity.ok(ApiResponse.success(DocumentDto.fromEntity(document), "文档移动成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文档移动失败"))
        }
    }
    
    @PostMapping("/{documentId}/publish")
    @Operation(summary = "发布文档", description = "发布文档为公开状态")
    fun publishDocument(
        @PathVariable documentId: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<DocumentDto>> {
        return try {
            val document = documentService.publishDocument(documentId, userId)
            ResponseEntity.ok(ApiResponse.success(DocumentDto.fromEntity(document), "文档发布成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文档发布失败"))
        }
    }
    
    @PostMapping("/{documentId}/archive")
    @Operation(summary = "归档文档", description = "将文档归档")
    fun archiveDocument(
        @PathVariable documentId: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<DocumentDto>> {
        return try {
            val document = documentService.archiveDocument(documentId, userId)
            ResponseEntity.ok(ApiResponse.success(DocumentDto.fromEntity(document), "文档归档成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "文档归档失败"))
        }
    }
    
    @PostMapping("/{documentId}/upload-image")
    @Operation(summary = "上传文档图片", description = "上传文档中使用的图片")
    fun uploadDocumentImage(
        @PathVariable documentId: String,
        @RequestParam("file") file: MultipartFile,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<ImageUploadResponse>> {
        return try {
            // 这里需要实现图片上传逻辑
            val imageUrl = "https://example.com/images/uploaded-image.jpg"
            val response = ImageUploadResponse(imageUrl = imageUrl)
            ResponseEntity.ok(ApiResponse.success(response, "图片上传成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "图片上传失败"))
        }
    }
}