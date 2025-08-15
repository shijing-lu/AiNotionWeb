package com.example.newainotionbackend.config

import com.example.newainotionbackend.dto.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import java.nio.file.AccessDeniedException as FileAccessDeniedException

/**
 * 全局异常处理器
 * 统一处理应用中的异常并返回标准格式的错误响应
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Map<String, String>>> {
        val errors = mutableMapOf<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "验证失败"
            errors[fieldName] = errorMessage
        }
        
        logger.warn("参数验证失败: $errors")
        return ResponseEntity.badRequest().body(
            ApiResponse.error("参数验证失败", errors)
        )
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiResponse<String>> {
        logger.error("非法参数异常: ${ex.message}", ex)
        return ResponseEntity.badRequest().body(
            ApiResponse.error(ex.message ?: "参数错误")
        )
    }
    
    /**
     * 处理认证异常
     */
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(ex: BadCredentialsException): ResponseEntity<ApiResponse<String>> {
        logger.warn("认证失败: ${ex.message}")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ApiResponse.error("用户名或密码错误")
        )
    }
    
    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ApiResponse<String>> {
        logger.warn("访问被拒绝: ${ex.message}")
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiResponse.error("访问被拒绝，权限不足")
        )
    }
    
    /**
     * 处理文件访问拒绝异常
     */
    @ExceptionHandler(FileAccessDeniedException::class)
    fun handleFileAccessDeniedException(ex: FileAccessDeniedException): ResponseEntity<ApiResponse<String>> {
        logger.warn("文件访问被拒绝: ${ex.message}")
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiResponse.error("文件访问被拒绝")
        )
    }
    
    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceededException(ex: MaxUploadSizeExceededException): ResponseEntity<ApiResponse<String>> {
        logger.warn("文件上传大小超限: ${ex.message}")
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
            ApiResponse.error("文件大小超出限制")
        )
    }
    
    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<ApiResponse<String>> {
        logger.warn("资源未找到: ${ex.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiResponse.error(ex.message ?: "资源未找到")
        )
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ApiResponse<String>> {
        logger.error("运行时异常: ${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse.error(ex.message ?: "服务器内部错误")
        )
    }
    
    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<String>> {
        logger.error("未处理的异常: ${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse.error("服务器内部错误，请稍后重试")
        )
    }
}