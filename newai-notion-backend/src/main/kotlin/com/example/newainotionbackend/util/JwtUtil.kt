package com.example.newainotionbackend.util

import com.example.newainotionbackend.config.JwtProperties
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil(
    private val jwtProperties: JwtProperties
) {
    
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }
    
    /**
     * 生成访问令牌
     */
    fun generateAccessToken(userDetails: UserDetails): String {
        val claims = HashMap<String, Any>()
        claims["type"] = "access"
        return createToken(claims, userDetails.username, jwtProperties.accessTokenExpiration)
    }
    
    /**
     * 生成刷新令牌
     */
    fun generateRefreshToken(userDetails: UserDetails): String {
        val claims = HashMap<String, Any>()
        claims["type"] = "refresh"
        return createToken(claims, userDetails.username, jwtProperties.refreshTokenExpiration)
    }
    
    /**
     * 生成令牌
     */
    private fun createToken(claims: Map<String, Any>, subject: String, expiration: Long): String {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(key, Jwts.SIG.HS512)
            .compact()
    }
    
    /**
     * 生成包含用户ID的访问令牌
     */
    fun generateAccessToken(userId: String, username: String): String {
        val claims = HashMap<String, Any>()
        claims["type"] = "access"
        claims["userId"] = userId
        return createToken(claims, username, jwtProperties.accessTokenExpiration)
    }
    
    /**
     * 生成包含用户ID的刷新令牌
     */
    fun generateRefreshToken(userId: String, username: String): String {
        val claims = HashMap<String, Any>()
        claims["type"] = "refresh"
        claims["userId"] = userId
        return createToken(claims, username, jwtProperties.refreshTokenExpiration)
    }
    
    /**
     * 从令牌中获取用户名
     */
    fun getUsernameFromToken(token: String): String {
        return getClaimFromToken(token, Claims::getSubject)
    }
    
    /**
     * 从令牌中获取用户ID
     */
    fun getUserIdFromToken(token: String): String? {
        return getClaimFromToken(token) { claims -> claims["userId"] as? String }
    }
    
    /**
     * 从令牌中获取过期时间
     */
    fun getExpirationDateFromToken(token: String): Date {
        return getClaimFromToken(token, Claims::getExpiration)
    }
    
    /**
     * 从令牌中获取令牌类型
     */
    fun getTokenTypeFromToken(token: String): String? {
        return getClaimFromToken(token) { claims -> claims["type"] as? String }
    }
    
    /**
     * 从令牌中获取指定声明
     */
    fun <T> getClaimFromToken(token: String, claimsResolver: (Claims) -> T): T {
        val claims = getAllClaimsFromToken(token)
        return claimsResolver(claims)
    }
    
    /**
     * 从令牌中获取所有声明
     */
    private fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
    
    /**
     * 检查令牌是否过期
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val expiration = getExpirationDateFromToken(token)
            expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }
    
    /**
     * 验证令牌
     */
    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        return try {
            val username = getUsernameFromToken(token)
            username == userDetails.username && !isTokenExpired(token)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 验证访问令牌
     */
    fun validateAccessToken(token: String, userDetails: UserDetails): Boolean {
        return try {
            val tokenType = getTokenTypeFromToken(token)
            tokenType == "access" && validateToken(token, userDetails)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 验证刷新令牌
     */
    fun validateRefreshToken(token: String, userDetails: UserDetails): Boolean {
        return try {
            val tokenType = getTokenTypeFromToken(token)
            tokenType == "refresh" && validateToken(token, userDetails)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 验证令牌（不验证用户）
     */
    fun validateToken(token: String): Boolean {
        return try {
            getAllClaimsFromToken(token)
            !isTokenExpired(token)
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    

    
    /**
     * 检查令牌是否有效（不验证用户）
     */
    fun isTokenValid(token: String): Boolean {
        return validateToken(token)
    }
    
    /**
     * 获取令牌剩余有效时间（毫秒）
     */
    fun getTokenRemainingTime(token: String): Long {
        return try {
            val expiration = getExpirationDateFromToken(token)
            expiration.time - System.currentTimeMillis()
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * 刷新令牌（生成新的访问令牌）
     */
    fun refreshToken(refreshToken: String, userDetails: UserDetails): String? {
        return if (validateRefreshToken(refreshToken, userDetails)) {
            generateAccessToken(userDetails)
        } else {
            null
        }
    }
}