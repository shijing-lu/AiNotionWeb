package com.example.newainotionbackend.controller

import com.example.newainotionbackend.dto.*
import com.example.newainotionbackend.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = ["http://localhost:5175"])
@Tag(name = "用户管理", description = "用户信息管理相关接口")
class UserController(
    private val userService: UserService
) {
    
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    fun getCurrentUser(@RequestAttribute("userId") userId: Long): ResponseEntity<ApiResponse<UserDto>> {
        return try {
            val user = userService.getUserById(userId)
            ResponseEntity.ok(ApiResponse.success(UserDto.fromEntity(user)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "获取用户信息失败"))
        }
    }
    
    @PutMapping("/me")
    @Operation(summary = "更新当前用户信息", description = "更新当前登录用户的基本信息")
    fun updateCurrentUser(
        @Valid @RequestBody request: UpdateUserRequest,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<UserDto>> {
        return try {
            val userUpdates = mapOf(
                "displayName" to request.displayName,
                "bio" to request.bio,
                "location" to request.location,
                "website" to request.website,
                "phoneNumber" to request.phone
            )
            val user = userService.updateUser(userId, userUpdates)
            ResponseEntity.ok(ApiResponse.success(UserDto.fromEntity(user)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "更新用户信息失败"))
        }
    }
    
    @PostMapping("/me/avatar")
    @Operation(summary = "上传用户头像", description = "上传并更新用户头像")
    fun uploadAvatar(
        @RequestParam("file") file: MultipartFile,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<AvatarResponse>> {
        return try {
            // 这里需要实现文件上传逻辑，暂时返回默认头像URL
            val avatarUrl = "https://example.com/default-avatar.png"
            val user = userService.updateUserAvatar(userId, avatarUrl)
            val response = AvatarResponse(avatarUrl = avatarUrl)
            ResponseEntity.ok(ApiResponse.success(response, "头像上传成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "头像上传失败"))
        }
    }
    
    @GetMapping("/me/settings")
    @Operation(summary = "获取用户设置", description = "获取当前用户的个性化设置")
    fun getUserSettings(@RequestAttribute("userId") userId: Long): ResponseEntity<ApiResponse<UserSettingsDto>> {
        return try {
            val settings = userService.getUserSettings(userId)
            ResponseEntity.ok(ApiResponse.success(UserSettingsDto.fromEntity(settings)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "获取设置失败"))
        }
    }
    
    @PutMapping("/me/settings")
    @Operation(summary = "更新用户设置", description = "更新当前用户的个性化设置")
    fun updateUserSettings(
        @Valid @RequestBody request: UpdateUserSettingsRequest,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<UserSettingsDto>> {
        return try {
            val settingsMap = mapOf(
                "theme" to request.theme,
                "language" to request.language,
                "timezone" to request.timezone,
                "editorFontSize" to request.editorFontSize,
                "editorFontFamily" to request.editorFontFamily,
                "editorLineHeight" to request.editorLineHeight,
                "editorWordWrap" to request.editorWordWrap,
                "emailNotifications" to request.emailNotifications,
                "pushNotifications" to request.pushNotifications,
                "notificationComment" to request.commentNotifications,
                "notificationShare" to request.shareNotifications,
                "aiSmartSuggestions" to request.aiSuggestions,
                "aiAutoComplete" to request.aiAutoComplete,
                "aiModelPreference" to request.aiModel,
                "profileVisibility" to request.profileVisibility,
                "allowSearchIndexing" to request.allowSearch,
                "twoFactorEnabled" to request.twoFactorEnabled,
                "sessionTimeout" to request.sessionTimeout,
                "editorAutoSave" to request.autoSave,
                "editorAutoSaveInterval" to request.autoSaveInterval,
                "documentDefaultVisibility" to request.defaultDocumentVisibility,
                "sidebarCollapsed" to request.sidebarCollapsed,
                "showLineNumbers" to request.showLineNumbers,
                "enableSpellCheck" to request.enableSpellCheck
            )
            val settings = userService.updateUserSettings(userId, settingsMap)
            ResponseEntity.ok(ApiResponse.success(UserSettingsDto.fromEntity(settings)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "更新设置失败"))
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索用户", description = "根据用户名或显示名称搜索用户")
    fun searchUsers(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<PageResponse<UserDto>>> {
        return try {
            val pageable = PageRequest.of(page, size)
            val users = userService.searchUsers(query, pageable)
            val userDtos = users.records.map { UserDto.fromEntity(it) }
            val response = PageResponse(
                content = userDtos,
                page = (users.current - 1).toInt(),
                size = users.size.toInt(),
                totalElements = users.total,
                totalPages = users.pages.toInt(),
                first = users.current == 1L,
                last = users.current == users.pages
            )
            ResponseEntity.ok(ApiResponse.success(response, null))
        } catch (e: Exception) {
            val errorResponse: ApiResponse<PageResponse<UserDto>> = ApiResponse.error(e.message ?: "搜索失败")
            ResponseEntity.badRequest().body(errorResponse)
        }
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户公开信息")
    fun getUserById(@PathVariable userId: Long): ResponseEntity<ApiResponse<UserDto>> {
        return try {
            val user = userService.getUserById(userId)
            ResponseEntity.ok(ApiResponse.success(UserDto.fromEntity(user)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "获取用户信息失败"))
        }
    }
    
    @DeleteMapping("/me")
    @Operation(summary = "删除用户账户", description = "软删除当前用户账户")
    fun deleteCurrentUser(@RequestAttribute("userId") userId: Long): ResponseEntity<ApiResponse<String>> {
        return try {
            userService.deleteUser(userId)
            ResponseEntity.ok(ApiResponse.success("账户删除成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "账户删除失败"))
        }
    }
    
    @GetMapping("/me/stats")
    @Operation(summary = "获取用户统计信息", description = "获取当前用户的统计数据")
    fun getUserStats(@RequestAttribute("userId") userId: Long): ResponseEntity<ApiResponse<UserStatsResponse>> {
        return try {
            // 这里需要实现获取用户统计信息的逻辑
            val stats = UserStatsResponse(
                documentsCount = 0,
                foldersCount = 0,
                wordsCount = 0,
                viewsCount = 0,
                likesCount = 0,
                commentsCount = 0,
                sharesCount = 0
            )
            ResponseEntity.ok(ApiResponse.success(stats))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "获取统计信息失败"))
        }
    }
}