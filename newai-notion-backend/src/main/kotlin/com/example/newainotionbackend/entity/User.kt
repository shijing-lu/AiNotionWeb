package com.example.newainotionbackend.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import net.sf.jsqlparser.util.validation.metadata.NamedObject
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@TableName("users")
class User(
    @TableId(value = "id", type = IdType.AUTO)
    val id: Long? = null,

    @TableField("avatar_url")
    val avatarUrl: String? = null,


    val bio: String? = null,

    @TableField("display_name")
    val displayName: String? = null,

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    val email: String,

    @TableField("email_verification_token")
    val emailVerificationToken: String? = null,

    @TableField("is_email_verified")
    val isEmailVerified: Boolean = false,

    @TableField("is_phone_verified")
    val isPhoneVerified: Boolean = false,

    val location: String? = null,

    @TableField("login_count")
    val loginCount: Long = 0,


    @NotBlank(message = "密码不能为空")
    @TableField("password_hash")
    private val password: String,



    @TableField("reset_token_password")
    val passwordResetToken: String? = null,

    @TableField("phone_number")
    val phoneNumber: String? = null,

    @TableField("role")
    val role: UserRole = UserRole.USER,


    @TableField("status")
    val status: UserStatus = UserStatus.ACTIVE,

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private val username: String,

    val website: String? = null,

) : UserDetails {
    
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${NamedObject.role.name}"))
    }

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = status != UserStatus.EXPIRED

    override fun isAccountNonLocked(): Boolean = status != UserStatus.LOCKED

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = status == UserStatus.ACTIVE
}

enum class UserStatus {
    ACTIVE,      // 活跃
    INACTIVE,    // 未激活
    LOCKED,      // 锁定
    SUSPENDED,   // 暂停
    EXPIRED      // 过期
}

enum class UserRole {
    USER,        // 普通用户
    PREMIUM,     // 高级用户
    ADMIN,       // 管理员
    SUPER_ADMIN  // 超级管理员
}