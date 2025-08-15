package com.example.newainotionbackend.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.example.newainotionbackend.entity.User
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import java.time.LocalDateTime

/**
 * 用户数据访问层接口
 * 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作
 */
@Mapper
interface UserMapper : BaseMapper<User> {
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户信息
     */
    fun findByUsername(username: String): User?
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户信息
     */
    fun findByEmail(email: String): User?
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    fun existsByUsername(username: String): Boolean
    
    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    fun existsByEmail(email: String): Boolean
    
    /**
     * 根据邮箱验证令牌查找用户
     * @param token 验证令牌
     * @return 用户信息
     */
    fun findByEmailVerificationToken(token: String): User?
    
    /**
     * 根据密码重置令牌查找用户
     * @param token 重置令牌
     * @return 用户信息
     */
    fun findByPasswordResetToken(token: String): User?
    
    /**
     * 更新邮箱验证状态
     * @param userId 用户ID
     */
    fun updateEmailVerificationStatus(userId: Long)
    
    /**
     * 设置邮箱验证令牌
     * @param userId 用户ID
     * @param token 验证令牌
     * @param expiresAt 过期时间
     */
    fun setEmailVerificationToken(userId: Long, token: String, expiresAt: LocalDateTime)
    
    /**
     * 设置密码重置令牌
     * @param userId 用户ID
     * @param token 重置令牌
     * @param expiresAt 过期时间
     */
    fun setPasswordResetToken(userId: Long, token: String, expiresAt: LocalDateTime)
    
    /**
     * 更新密码
     * @param userId 用户ID
     * @param password 新密码
     */
    fun updatePassword(userId: Long, password: String)
    
    /**
     * 更新最后登录时间和登录次数
     * @param userId 用户ID
     * @param lastLoginAt 最后登录时间
     */
//    fun updateLastLogin(userId: Long, lastLoginAt: LocalDateTime)
    
    /**
     * 根据用户名模糊搜索用户
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    @Select("SELECT * FROM users WHERE username LIKE CONCAT('%', #{keyword}, '%') AND status = 'ACTIVE'")
    fun findByUsernameContainingIgnoreCase(keyword: String): List<User>
    
    /**
     * 分页搜索用户
     * @param page 分页对象
     * @param keyword 搜索关键词
     * @return 分页用户列表
     */
    @Select("SELECT * FROM users WHERE (username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%') OR display_name LIKE CONCAT('%', #{keyword}, '%')) AND status = 'ACTIVE'")
    fun searchUsers(page: Page<User>, keyword: String): IPage<User>
    
    /**
     * 根据用户名或邮箱查找用户
     * @param username 用户名
     * @param email 邮箱
     * @return 用户信息
     */
    fun findByUsernameOrEmail(username: String, email: String): User?
    
    /**
     * 查找已验证邮箱的用户
     * @return 用户列表
     */
    @Select("SELECT * FROM users WHERE is_email_verified = true")
    fun findByIsEmailVerifiedTrue(): List<User>
    
    /**
     * 根据状态查找用户
     * @param status 用户状态
     * @return 用户列表
     */
    @Select("SELECT * FROM users WHERE status = #{status}")
    fun findByStatus(status: String): List<User>
    
    /**
     * 查找活跃用户
     * @param status 用户状态
     * @param lastLoginAt 最后登录时间
     * @return 用户列表
     */
    @Select("SELECT * FROM users WHERE status = #{status} AND last_login_at > #{lastLoginAt}")
    fun findByStatusAndLastLoginAtAfter(status: String, lastLoginAt: LocalDateTime): List<User>
    
    /**
     * 根据显示名称模糊搜索
     * @param displayName 显示名称
     * @return 用户列表
     */
    @Select("SELECT * FROM users WHERE display_name LIKE CONCAT('%', #{displayName}, '%')")
    fun findByDisplayNameContainingIgnoreCase(displayName: String): List<User>
    
    /**
     * 查找过期的邮箱验证令牌
     * @param dateTime 过期时间
     * @return 用户列表
     */
    @Select("SELECT * FROM users WHERE email_verification_expires_at < #{dateTime}")
    fun findByEmailVerificationExpiresAtBefore(dateTime: LocalDateTime): List<User>
    
    /**
     * 查找过期的密码重置令牌
     * @param dateTime 过期时间
     * @return 用户列表
     */
    @Select("SELECT * FROM users WHERE password_reset_expires_at < #{dateTime}")
    fun findByPasswordResetExpiresAtBefore(dateTime: LocalDateTime): List<User>
    
    /**
     * 统计指定状态的用户数量
     * @param status 用户状态
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE status = #{status}")
    fun countByStatus(status: String): Long
    
    /**
     * 统计时间范围内注册的用户数量
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE created_at >= #{startDate} AND created_at < #{endDate}")
    fun countByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): Long
}