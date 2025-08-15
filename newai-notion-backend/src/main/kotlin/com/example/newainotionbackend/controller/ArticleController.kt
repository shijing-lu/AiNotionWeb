package com.example.newainotionbackend.controller

import com.example.newainotionbackend.dto.*
import com.example.newainotionbackend.entity.ArticleStatus
import com.example.newainotionbackend.service.ArticleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 文章控制器
 * 提供文章相关的RESTful API接口
 * @author AI Assistant
 */
@RestController
@RequestMapping("/v1/articles")
@Tag(name = "文章管理", description = "文章的增删改查操作")
class ArticleController(
    private val articleService: ArticleService
) {
    
    /**
     * 创建文章
     */
    @PostMapping
    @Operation(summary = "创建文章", description = "创建新的文章")
    fun createArticle(
        @Valid @RequestBody request: CreateArticleRequest,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<ArticleDto>> {
        return try {
            val article = articleService.createArticle(userId, request)
            ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ArticleDto.fromEntity(article), "文章创建成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "文章创建失败"))
        }
    }
    
    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情", description = "根据ID获取文章详细信息")
    fun getArticle(
        @Parameter(description = "文章ID") @PathVariable id: String,
        @RequestAttribute("userId", required = false) userId: Long?
    ): ResponseEntity<ApiResponse<ArticleDto>> {
        return try {
            val article = articleService.getArticleById(id, userId)
            if (article != null) {
                ResponseEntity.ok(ApiResponse.success(ArticleDto.fromEntity(article), "获取文章成功"))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "获取文章失败"))
        }
    }
    
    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新文章", description = "更新指定ID的文章")
    fun updateArticle(
        @Parameter(description = "文章ID") @PathVariable id: String,
        @Valid @RequestBody request: UpdateArticleRequest,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<ArticleDto>> {
        return try {
            val article = articleService.updateArticle(id, userId, request)
            if (article != null) {
                ResponseEntity.ok(ApiResponse.success(ArticleDto.fromEntity(article), "文章更新成功"))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.message ?: "无权限操作"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "文章更新失败"))
        }
    }
    
    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章", description = "删除指定ID的文章")
    fun deleteArticle(
        @Parameter(description = "文章ID") @PathVariable id: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val success = articleService.deleteArticle(id, userId)
            if (success) {
                ResponseEntity.ok(ApiResponse.success("文章删除成功"))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.message ?: "无权限操作"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "文章删除失败"))
        }
    }
    
    /**
     * 获取用户文章列表
     */
    @GetMapping
    @Operation(summary = "获取文章列表", description = "获取当前用户的文章列表")
    fun getUserArticles(
        @RequestAttribute("userId") userId: Long,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "排序字段") @RequestParam(defaultValue = "updatedAt") sortBy: String,
        @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") sortDirection: String
    ): ResponseEntity<ApiResponse<Page<ArticleSummaryDto>>> {
        return try {
            val articles = articleService.getUserArticles(userId, page, size, sortBy, sortDirection)
            val summaryPage = articles.map { ArticleSummaryDto.fromEntity(it) }
            ResponseEntity.ok(ApiResponse.success(summaryPage, "获取文章列表成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "获取文章列表失败"))
        }
    }
    
    /**
     * 根据状态获取文章列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取文章", description = "根据文章状态获取文章列表")
    fun getArticlesByStatus(
        @Parameter(description = "文章状态") @PathVariable status: ArticleStatus,
        @RequestAttribute("userId") userId: Long,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<ArticleSummaryDto>>> {
        return try {
            val articles = articleService.getUserArticlesByStatus(userId, status, page, size)
            val summaryPage = articles.map { ArticleSummaryDto.fromEntity(it) }
            ResponseEntity.ok(ApiResponse.success(summaryPage, "获取文章列表成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "获取文章列表失败"))
        }
    }
    
    /**
     * 获取收藏文章列表
     */
    @GetMapping("/favorites")
    @Operation(summary = "获取收藏文章", description = "获取用户收藏的文章列表")
    fun getFavoriteArticles(
        @RequestAttribute("userId") userId: Long,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<ArticleSummaryDto>>> {
        return try {
            val articles = articleService.getUserFavoriteArticles(userId, page, size)
            val summaryPage = articles.map { ArticleSummaryDto.fromEntity(it) }
            ResponseEntity.ok(ApiResponse.success(summaryPage, "获取收藏文章成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "获取收藏文章失败"))
        }
    }
    
    /**
     * 获取归档文章列表
     */
    @GetMapping("/archived")
    @Operation(summary = "获取归档文章", description = "获取用户归档的文章列表")
    fun getArchivedArticles(
        @RequestAttribute("userId") userId: Long,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<ArticleSummaryDto>>> {
        return try {
            val articles = articleService.getUserArchivedArticles(userId, page, size)
            val summaryPage = articles.map { ArticleSummaryDto.fromEntity(it) }
            ResponseEntity.ok(ApiResponse.success(summaryPage, "获取归档文章成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "获取归档文章失败"))
        }
    }
    
    /**
     * 搜索文章
     */
    @PostMapping("/search")
    @Operation(summary = "搜索文章", description = "根据条件搜索文章")
    fun searchArticles(
        @RequestBody request: ArticleSearchRequest,
        @RequestAttribute("userId", required = false) userId: Long?,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<ArticleSummaryDto>>> {
        return try {
            val articles = articleService.searchArticles(userId, request, page, size)
            val summaryPage = articles.map { ArticleSummaryDto.fromEntity(it) }
            ResponseEntity.ok(ApiResponse.success(summaryPage, "搜索文章成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "搜索文章失败"))
        }
    }
    
    /**
     * 获取公开文章列表
     */
    @GetMapping("/public")
    @Operation(summary = "获取公开文章", description = "获取所有公开发布的文章")
    fun getPublicArticles(
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<ArticleSummaryDto>>> {
        return try {
            val articles = articleService.getPublicArticles(page, size)
            val summaryPage = articles.map { ArticleSummaryDto.fromEntity(it) }
            ResponseEntity.ok(ApiResponse.success(summaryPage, "获取公开文章成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "获取公开文章失败"))
        }
    }
    
    /**
     * 获取热门文章
     */
    @GetMapping("/popular")
    @Operation(summary = "获取热门文章", description = "获取热门文章列表")
    fun getPopularArticles(
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<ArticleSummaryDto>>> {
        return try {
            val articles = articleService.getPopularArticles(page, size)
            val summaryPage = articles.map { ArticleSummaryDto.fromEntity(it) }
            ResponseEntity.ok(ApiResponse.success(summaryPage, "获取热门文章成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "获取热门文章失败"))
        }
    }
    
    /**
     * 切换文章收藏状态
     */
    @PostMapping("/{id}/favorite")
    @Operation(summary = "切换收藏状态", description = "切换文章的收藏状态")
    fun toggleFavorite(
        @Parameter(description = "文章ID") @PathVariable id: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<ArticleDto>> {
        return try {
            val article = articleService.toggleFavorite(id, userId)
            if (article != null) {
                ResponseEntity.ok(ApiResponse.success(ArticleDto.fromEntity(article), "操作成功"))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.message ?: "无权限操作"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "操作失败"))
        }
    }
    
    /**
     * 切换文章置顶状态
     */
    @PostMapping("/{id}/pin")
    @Operation(summary = "切换置顶状态", description = "切换文章的置顶状态")
    fun togglePin(
        @Parameter(description = "文章ID") @PathVariable id: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<ArticleDto>> {
        return try {
            val article = articleService.togglePin(id, userId)
            if (article != null) {
                ResponseEntity.ok(ApiResponse.success(ArticleDto.fromEntity(article), "操作成功"))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.message ?: "无权限操作"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "操作失败"))
        }
    }
    
    /**
     * 切换文章归档状态
     */
    @PostMapping("/{id}/archive")
    @Operation(summary = "切换归档状态", description = "切换文章的归档状态")
    fun toggleArchive(
        @Parameter(description = "文章ID") @PathVariable id: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<ArticleDto>> {
        return try {
            val article = articleService.toggleArchive(id, userId)
            if (article != null) {
                ResponseEntity.ok(ApiResponse.success(ArticleDto.fromEntity(article), "操作成功"))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.message ?: "无权限操作"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "操作失败"))
        }
    }
    
    /**
     * 发布文章
     */
    @PostMapping("/{id}/publish")
    @Operation(summary = "发布文章", description = "发布指定的文章")
    fun publishArticle(
        @Parameter(description = "文章ID") @PathVariable id: String,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<ArticleDto>> {
        return try {
            val article = articleService.publishArticle(id, userId)
            if (article != null) {
                ResponseEntity.ok(ApiResponse.success(ArticleDto.fromEntity(article), "文章发布成功"))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.message ?: "无权限操作"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "文章发布失败"))
        }
    }
    
    /**
     * 获取文章统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取文章统计", description = "获取用户的文章统计信息")
    fun getArticleStats(
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<ArticleStatsDto>> {
        return try {
            val stats = articleService.getArticleStats(userId)
            ResponseEntity.ok(ApiResponse.success(stats, "获取统计信息成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(ApiResponse.error(e.message ?: "获取统计信息失败"))
        }
    }
}