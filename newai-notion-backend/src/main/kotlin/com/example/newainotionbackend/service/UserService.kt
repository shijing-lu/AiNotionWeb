package com.example.newainotionbackend.service

import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.example.newainotionbackend.entity.User
import com.example.newainotionbackend.entity.UserSettings
import com.example.newainotionbackend.entity.UserStatus
import com.example.newainotionbackend.mapper.UserMapper
import com.example.newainotionbackend.mapper.UserSettingsMapper
import com.example.newainotionbackend.util.EmailUtil
import com.example.newainotionbackend.util.PasswordUtil
import org.springframework.data.domain.Pageable
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class UserService(
    private val userMapper: UserMapper,
    private val userSettingsMapper: UserSettingsMapper,
    private val passwordUtil: PasswordUtil,
    private val emailUtil: EmailUtil
) : UserDetailsService {
    
    override fun loadUserByUsername(username: String): UserDetails {
        return userMapper.findByUsernameOrEmail(username, username)
            ?: throw UsernameNotFoundException("用户不存在: $username")
    }
    
    /**
     * 用户注册
     */
    fun registerUser(username: String, email: String, password: String, displayName: String? = null): User {
        // 检查用户名和邮箱是否已存在
        if (userMapper.existsByUsername(username)) {
            throw IllegalArgumentException("用户名已存在")
        }
        if (userMapper.existsByEmail(email)) {
            throw IllegalArgumentException("邮箱已存在")
        }
        
        // 验证密码有效性
        if (!passwordUtil.isPasswordValid(password)) {
            throw IllegalArgumentException("密码不能为空")
        }
        
        // 创建用户
        val user = User(
            username = username,
            email = email,
            password = passwordUtil.encodePassword(password),
            displayName = displayName ?: username,
            status = UserStatus.ACTIVE // 暂时直接激活，跳过邮箱验证
        )
        
        userMapper.insert(user)
        
        // 创建默认用户设置
        createDefaultUserSettings(user.id!!)
        
        // 暂时注释掉邮件发送功能，避免邮件配置问题
        // sendVerificationEmail(user)
        
        return user
    }
    
    /**
     * 用户登录
     */
    fun loginUser(usernameOrEmail: String, password: String): User {
        println("loginUser start")
        val user = userMapper.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
            ?: throw IllegalArgumentException("没有该用户")
        
        // 验证密码
//        logger.info("密码验证 - 输入密码: $password")
//        logger.info("密码验证 - 存储的哈希: ${user.password}")
        print(user.toString())
        val passwordMatches = passwordUtil.matches(password, user.password)
//        logger.info("密码验证结果: $passwordMatches")
        if (!passwordMatches) {
            throw IllegalArgumentException("用户名或密码错误")
        }
        
        if (user.status != UserStatus.ACTIVE) {
            throw IllegalArgumentException("账户未激活或已被禁用")
        }
        
        // 更新登录信息
//        userMapper.updateLastLogin(user.id!!, LocalDateTime.now())
        
        return user
    }
    
    /**
     * 验证邮箱
     */
    fun verifyEmail(token: String): Boolean {
        val user = userMapper.findByEmailVerificationToken(token)
            ?: throw IllegalArgumentException("无效的验证令牌")
//
//        if (user.emailVerificationExpiresAt?.isBefore(LocalDateTime.now()) == true) {
//            throw IllegalArgumentException("验证令牌已过期")
//        }
        
        userMapper.updateEmailVerificationStatus(user.id!!)
        
        // 如果用户状态是未激活，则激活用户
        if (user.status == UserStatus.INACTIVE) {
            val activatedUser = User(
                id = user.id,
                username = user.username,
                email = user.email,
                password = user.password,
                displayName = user.displayName,
                avatarUrl = user.avatarUrl,
                phoneNumber = user.phoneNumber,
                bio = user.bio,
                location = user.location,
                website = user.website,
                isEmailVerified = user.isEmailVerified,
                isPhoneVerified = user.isPhoneVerified,
                emailVerificationToken = user.emailVerificationToken,
//                emailVerificationExpiresAt = user.emailVerificationExpiresAt,
                passwordResetToken = user.passwordResetToken,
//                passwordResetExpiresAt = user.passwordResetExpiresAt,
//                lastLoginAt = user.lastLoginAt,
                loginCount = user.loginCount,
                status = UserStatus.ACTIVE,
                role = user.role,
//                createdAt = user.createdAt,
//                updatedAt = LocalDateTime.now()
            )
            userMapper.updateById(activatedUser)
        }
        
        return true
    }
    
    /**
     * 发送密码重置邮件
     */
    fun sendPasswordResetEmail(email: String) {
        val user = userMapper.findByEmail(email)
            ?: throw IllegalArgumentException("邮箱不存在")
        
        val resetToken = passwordUtil.generateRandomToken()
        val expiresAt = LocalDateTime.now().plusHours(1) // 1小时后过期
        
        userMapper.setPasswordResetToken(user.id!!, resetToken, expiresAt)
        
        emailUtil.sendPasswordResetEmail(user.email, user.username, resetToken, "http://localhost:3000")
    }
    
    /**
     * 重置密码
     */
    fun resetPassword(token: String, newPassword: String): Boolean {
        val user = userMapper.findByPasswordResetToken(token)
            ?: throw IllegalArgumentException("无效的重置令牌")
        
//        if (user.passwordResetExpiresAt?.isBefore(LocalDateTime.now()) == true) {
//            throw IllegalArgumentException("重置令牌已过期")
//        }
        
        if (!passwordUtil.isPasswordValid(newPassword)) {
            throw IllegalArgumentException("密码不能为空")
        }
        
        val encodedPassword = passwordUtil.encodePassword(newPassword)
        userMapper.updatePassword(user.id!!, encodedPassword)
        
        return true
    }
    
    /**
     * 修改密码
     */
    fun changePassword(userId: Long, oldPassword: String, newPassword: String): Boolean {
        val user = getUserById(userId)
        
        if (!passwordUtil.matches(oldPassword, user.password)) {
            throw IllegalArgumentException("原密码错误")
        }
        
        if (!passwordUtil.isPasswordValid(newPassword)) {
            throw IllegalArgumentException("新密码不能为空")
        }
        
        val encodedPassword = passwordUtil.encodePassword(newPassword)
        userMapper.updatePassword(userId, encodedPassword)
        
        return true
    }
    
    /**
     * 获取用户信息
     */
    fun getUserById(userId: Long): User {
        return userMapper.selectById(userId)
            ?: throw IllegalArgumentException("用户不存在")
    }
    
    /**
     * 更新用户信息
     */
    fun updateUser(userId: Long, updates: Map<String, Any?>): User {
        val user = getUserById(userId)
        
        val updatedUser = User(
            id = user.id,
            username = user.username,
            email = user.email,
            password = user.password,
            displayName = updates["displayName"] as? String ?: user.displayName,
            avatarUrl = user.avatarUrl,
            phoneNumber = updates["phoneNumber"] as? String ?: user.phoneNumber,
            bio = updates["bio"] as? String ?: user.bio,
            location = updates["location"] as? String ?: user.location,
            website = updates["website"] as? String ?: user.website,
            isEmailVerified = user.isEmailVerified,
            isPhoneVerified = user.isPhoneVerified,
            emailVerificationToken = user.emailVerificationToken,
//            emailVerificationExpiresAt = user.emailVerificationExpiresAt,
            passwordResetToken = user.passwordResetToken,
//            passwordResetExpiresAt = user.passwordResetExpiresAt,
//            lastLoginAt = user.lastLoginAt,
            loginCount = user.loginCount,
            status = user.status,
            role = user.role,
//            createdAt = user.createdAt,
//            updatedAt = LocalDateTime.now()
        )
        
        userMapper.updateById(updatedUser)
        return updatedUser
    }
    
    /**
     * 更新用户头像
     */
    fun updateUserAvatar(userId: Long, avatarUrl: String): User {
        val user = getUserById(userId)
        val updatedUser = User(
            id = user.id,
            username = user.username,
            email = user.email,
            password = user.password,
            displayName = user.displayName,
            avatarUrl = avatarUrl,
            phoneNumber = user.phoneNumber,
            bio = user.bio,
            location = user.location,
            website = user.website,
            isEmailVerified = user.isEmailVerified,
            isPhoneVerified = user.isPhoneVerified,
            emailVerificationToken = user.emailVerificationToken,
//            emailVerificationExpiresAt = user.emailVerificationExpiresAt,
            passwordResetToken = user.passwordResetToken,
//            passwordResetExpiresAt = user.passwordResetExpiresAt,
//            lastLoginAt = user.lastLoginAt,
            loginCount = user.loginCount,
            status = user.status,
            role = user.role,
//            createdAt = user.createdAt,
//            updatedAt = LocalDateTime.now()
        )
        userMapper.updateById(updatedUser)
        return updatedUser
    }
    
    /**
     * 获取用户设置
     */
    fun getUserSettings(userId: Long): UserSettings {
        return userSettingsMapper.findByUserId(userId)
            ?: createDefaultUserSettings(userId)
    }
    
    /**
     * 更新用户设置
     */
    fun updateUserSettings(userId: Long, updates: Map<String, Any?>): UserSettings {
        val settings = getUserSettings(userId)
        
        val updatedSettings = settings.copy(
            theme = updates["theme"] as? String ?: settings.theme,
            language = updates["language"] as? String ?: settings.language,
            timezone = updates["timezone"] as? String ?: settings.timezone,
            editorFontSize = updates["editorFontSize"] as? Int ?: settings.editorFontSize,
            editorFontFamily = updates["editorFontFamily"] as? String ?: settings.editorFontFamily,
            emailNotifications = updates["emailNotifications"] as? Boolean ?: settings.emailNotifications,
            pushNotifications = updates["pushNotifications"] as? Boolean ?: settings.pushNotifications,
            aiAutoComplete = updates["aiAutoComplete"] as? Boolean ?: settings.aiAutoComplete,
            updatedAt = LocalDateTime.now()
        )
        
        userSettingsMapper.updateById(updatedSettings)
        return updatedSettings
    }
    
    /**
     * 检查用户名是否可用
     */
    fun isUsernameAvailable(username: String): Boolean {
        return !userMapper.existsByUsername(username)
    }
    
    /**
     * 检查邮箱是否可用
     */
    fun isEmailAvailable(email: String): Boolean {
        return !userMapper.existsByEmail(email)
    }
    
    /**
     * 搜索用户
     */
    fun searchUsers(keyword: String, pageable: Pageable): IPage<User> {
        val page = Page<User>(pageable.pageNumber.toLong() + 1, pageable.pageSize.toLong())
        return userMapper.searchUsers(page, keyword)
    }
    
    /**
     * 删除用户
     */
    fun deleteUser(userId: Long) {
        val user = getUserById(userId)
        
        // 软删除：更新状态而不是物理删除
        val deletedUser = User(
            id = user.id,
            username = user.username,
            email = user.email,
            password = user.password,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl,
            phoneNumber = user.phoneNumber,
            bio = user.bio,
            location = user.location,
            website = user.website,
            isEmailVerified = user.isEmailVerified,
            isPhoneVerified = user.isPhoneVerified,
            emailVerificationToken = user.emailVerificationToken,
//            emailVerificationExpiresAt = user.emailVerificationExpiresAt,
            passwordResetToken = user.passwordResetToken,
//            passwordResetExpiresAt = user.passwordResetExpiresAt,
//            lastLoginAt = user.lastLoginAt,
            loginCount = user.loginCount,
            status = UserStatus.SUSPENDED,
            role = user.role,
//            createdAt = user.createdAt,
//            updatedAt = LocalDateTime.now()
        )
        
        userMapper.updateById(deletedUser)
        
        // 删除用户设置
        userSettingsMapper.deleteByUserId(userId)
    }
    
    /**
     * 创建默认用户设置
     */
    private fun createDefaultUserSettings(userId: Long): UserSettings {
        val defaultSettings = UserSettings(userId = userId)
        userSettingsMapper.insert(defaultSettings)
        return defaultSettings
    }
    
    /**
     * 发送验证邮件
     */
    private fun sendVerificationEmail(user: User) {
        val verificationToken = passwordUtil.generateRandomToken()
        val expiresAt = LocalDateTime.now().plusDays(1) // 24小时后过期
        
        userMapper.setEmailVerificationToken(user.id!!, verificationToken, expiresAt)
        
        emailUtil.sendVerificationEmail(
            user.email, 
            user.username, 
            verificationToken, 
            "http://localhost:3000"
        )
    }
}