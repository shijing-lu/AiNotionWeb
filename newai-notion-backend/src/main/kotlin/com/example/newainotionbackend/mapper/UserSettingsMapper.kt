package com.example.newainotionbackend.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.example.newainotionbackend.entity.UserSettings
import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

/**
 * 用户设置数据访问层
 * 提供用户设置相关的数据库操作
 */
@Mapper
interface UserSettingsMapper : BaseMapper<UserSettings> {
    
    /**
     * 根据用户ID查找设置
     * @param userId 用户ID
     * @return 用户设置信息，如果不存在则返回null
     */
    @Select("SELECT * FROM user_settings WHERE user_id = #{userId}")
    fun findByUserId(userId: Long): UserSettings?
    
    /**
     * 检查用户设置是否存在
     * @param userId 用户ID
     * @return 如果存在返回true，否则返回false
     */
    @Select("SELECT COUNT(1) FROM user_settings WHERE user_id = #{userId}")
    fun existsByUserId(userId: Long): Boolean
    
    /**
     * 根据用户ID删除设置
     * @param userId 用户ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM user_settings WHERE user_id = #{userId}")
    fun deleteByUserId(userId: Long): Int
}