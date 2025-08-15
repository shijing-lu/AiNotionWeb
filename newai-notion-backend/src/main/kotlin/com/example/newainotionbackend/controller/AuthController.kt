package com.example.newainotionbackend.controller

import com.example.newainotionbackend.dto.*
import com.example.newainotionbackend.service.UserService
import com.example.newainotionbackend.util.JwtUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = ["http://localhost:5175"])
@Tag(name = "认证授权", description = "用户认证和授权相关接口")
class AuthController(
    private val userService: UserService,
    private val jwtUtil: JwtUtil
) {
    
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账户")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        print("autuing!")
        return try {
            val user = userService.registerUser(
                username = request.username,
                email = request.email,
                password = request.password,
                displayName = request.displayName
            )
            
            val accessToken = jwtUtil.generateAccessToken(user.id.toString(), user.username)
            val refreshToken = jwtUtil.generateRefreshToken(user.id.toString(), user.username)
            
            val response = AuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                tokenType = "Bearer",
                expiresIn = 86400, // 24小时
                user = UserDto.fromEntity(user)
            )
            
            ResponseEntity.ok(ApiResponse.success(response, "注册成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "注册失败"))
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取访问令牌")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        println("Login request: username=${request.username}, password=${request.password}")
        return try {
            val user = userService.loginUser(request.username, request.password)
            println("Login successful for user: ${user.username}")
            
            val accessToken = jwtUtil.generateAccessToken(user.id.toString(), user.username)
            val refreshToken = jwtUtil.generateRefreshToken(user.id.toString(), user.username)
            
            val response = AuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                tokenType = "Bearer",
                expiresIn = 86400,
                user = UserDto.fromEntity(user)
            )
            
            ResponseEntity.ok(ApiResponse.success(response, "登录成功"))
        } catch (e: Exception) {
            println("Login failed with exception: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "登录失败"))
        }
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<TokenResponse>> {
        return try {
            val username = jwtUtil.getUsernameFromToken(request.refreshToken)
            val user = userService.loadUserByUsername(username)
            
            if (!jwtUtil.validateRefreshToken(request.refreshToken, user)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("无效的刷新令牌"))
            }
            
            val userId = jwtUtil.getUserIdFromToken(request.refreshToken)
            val newAccessToken = jwtUtil.generateAccessToken(userId ?: user.username, user.username)
            
            val response = TokenResponse(
                accessToken = newAccessToken,
                tokenType = "Bearer",
                expiresIn = 86400
            )
            
            ResponseEntity.ok(ApiResponse.success(response, "令牌刷新成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "令牌刷新失败"))
        }
    }
    
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出（客户端需要清除令牌）")
    fun logout(): ResponseEntity<ApiResponse<String>> {
        // 在实际应用中，可以将令牌加入黑名单
        return ResponseEntity.ok(ApiResponse.success("登出成功"))
    }
    
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
    
    @GetMapping("/verify-email")
    @Operation(summary = "验证邮箱", description = "通过邮箱验证令牌验证用户邮箱")
    fun verifyEmail(@RequestParam token: String): ResponseEntity<ApiResponse<String>> {
        return try {
            userService.verifyEmail(token)
            ResponseEntity.ok(ApiResponse.success("邮箱验证成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "邮箱验证失败"))
        }
    }
    
    @PostMapping("/send-verification")
    @Operation(summary = "重发验证邮件", description = "重新发送邮箱验证邮件")
    fun sendVerificationEmail(@Valid @RequestBody request: EmailRequest): ResponseEntity<ApiResponse<String>> {
        return try {
            // 这里需要实现重发验证邮件的逻辑
            ResponseEntity.ok(ApiResponse.success("验证邮件已发送"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "发送失败"))
        }
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码", description = "发送密码重置邮件")
    fun forgotPassword(@Valid @RequestBody request: EmailRequest): ResponseEntity<ApiResponse<String>> {
        return try {
            userService.sendPasswordResetEmail(request.email)
            ResponseEntity.ok(ApiResponse.success("密码重置邮件已发送"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "发送失败"))
        }
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "通过重置令牌重置密码")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<ApiResponse<String>> {
        return try {
            userService.resetPassword(request.token, request.newPassword)
            ResponseEntity.ok(ApiResponse.success("密码重置成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "密码重置失败"))
        }
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "用户修改密码")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            userService.changePassword(userId, request.oldPassword, request.newPassword)
            ResponseEntity.ok(ApiResponse.success("密码修改成功"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "密码修改失败"))
        }
    }
    
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名可用性", description = "检查用户名是否已被使用")
    fun checkUsername(@RequestParam username: String): ResponseEntity<ApiResponse<AvailabilityResponse>> {
        val available = userService.isUsernameAvailable(username)
        val response = AvailabilityResponse(available = available)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
    
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱可用性", description = "检查邮箱是否已被使用")
    fun checkEmail(@RequestParam email: String): ResponseEntity<ApiResponse<AvailabilityResponse>> {
        val available = userService.isEmailAvailable(email)
        val response = AvailabilityResponse(available = available)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}