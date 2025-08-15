package com.example.newainotionbackend.config

import com.example.newainotionbackend.service.UserService
import com.example.newainotionbackend.util.JwtUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userService: UserService
) : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            print("认证失败:")
            val token = getTokenFromRequest(request)
            
            if (token != null && jwtUtil.validateToken(token)) {
                print("认证成功")
                val username = jwtUtil.getUsernameFromToken(token)
                println("${username}")

                val userId = jwtUtil.getUserIdFromToken(token)
                println("${userId}")
                
                if (username != null && userId != null && SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userService.loadUserByUsername(username)
                    
                    if (jwtUtil.validateToken(token, userDetails)) {
                        val authentication = UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.authorities
                        )
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authentication
                        print("认证成功")
                        // 将用户ID添加到请求属性中，供控制器使用
                        request.setAttribute("userId", userId)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("JWT认证失败: ${e.message}")
            print("认证失败:${e.message}")
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun getTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}