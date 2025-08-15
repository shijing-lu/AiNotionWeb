package com.example.newainotionbackend.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.time.LocalDateTime


/**
 * JWT认证入口点
 * 处理未认证请求的响应
 */
@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {

    private val objectMapper = ObjectMapper()

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.contentType = "${MediaType.APPLICATION_JSON_VALUE};charset=UTF-8"
        response.characterEncoding = "UTF-8"
        response.status = HttpServletResponse.SC_UNAUTHORIZED

        val errorResponse = mapOf(
            "success" to false,
            "message" to null,
            "data" to null,
            "error" to "未授权访问，请先登录",
            "timestamp" to LocalDateTime.now().toString()
        )

        try {
            response.writer.use { writer ->
                writer.write(objectMapper.writeValueAsString(errorResponse))
            }
        } catch (e: Exception) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.use { writer ->
                writer.write("""{"error":"Internal server error"}""")
            }
        }
    }
}