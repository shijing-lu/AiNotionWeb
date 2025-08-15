package com.example.newainotionbackend.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.example.newainotionbackend.entity.Folder
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update

/**
 * 文件夹数据访问层
 * 提供文件夹相关的数据库操作方法
 */
@Mapper
interface FolderMapper : BaseMapper<Folder> {
    
    /**
     * 根据用户ID查询文件夹列表
     */
    @Select("""
        SELECT * FROM folders 
        WHERE user_id = #{userId} 
        AND is_archived = #{isArchived}
        ORDER BY is_pinned DESC, sort_order ASC, created_at DESC
    """)
    fun findByUserIdAndIsArchived(
        @Param("userId") userId: Long,
        @Param("isArchived") isArchived: Boolean
    ): List<Folder>
    
    /**
     * 根据父文件夹ID查询子文件夹
     */
    @Select("""
        SELECT * FROM folders 
        WHERE parent_id = #{parentId} 
        AND user_id = #{userId}
        AND is_archived = false
        ORDER BY is_pinned DESC, sort_order ASC, name ASC
    """)
    fun findByParentIdAndUserId(
        @Param("parentId") parentId: String?,
        @Param("userId") userId: Long
    ): List<Folder>
    
    /**
     * 根据父文件夹ID查询子文件夹（不限用户）
     */
    @Select("""
        SELECT * FROM folders 
        WHERE parent_id = #{parentId}
        ORDER BY sort_order ASC, name ASC
    """)
    fun findByParentId(@Param("parentId") parentId: String): List<Folder>
    
    /**
     * 根据用户ID和文件夹名称查询
     */
    @Select("""
        SELECT * FROM folders 
        WHERE user_id = #{userId} 
        AND name = #{name}
        AND parent_id = #{parentId}
        AND is_archived = false
    """)
    fun findByUserIdAndNameAndParentId(
        @Param("userId") userId: Long,
        @Param("name") name: String,
        @Param("parentId") parentId: String?
    ): Folder?
    
    /**
     * 根据用户ID分页查询文件夹
     */
    @Select("""
        SELECT * FROM folders 
        WHERE user_id = #{userId}
        AND is_archived = #{isArchived}
        ORDER BY is_pinned DESC, sort_order ASC, created_at DESC
    """)
    fun selectUserFoldersPage(
        page: Page<Folder>,
        @Param("userId") userId: Long,
        @Param("isArchived") isArchived: Boolean
    ): Page<Folder>
    
    /**
     * 搜索用户文件夹
     */
    @Select("""
        SELECT * FROM folders 
        WHERE user_id = #{userId}
        AND (name LIKE CONCAT('%', #{keyword}, '%') 
             OR description LIKE CONCAT('%', #{keyword}, '%'))
        AND is_archived = false
        ORDER BY is_pinned DESC, sort_order ASC, name ASC
    """)
    fun searchUserFoldersPage(
        page: Page<Folder>,
        @Param("userId") userId: Long,
        @Param("keyword") keyword: String
    ): Page<Folder>
    
    /**
     * 查询用户的根文件夹
     */
    @Select("""
        SELECT * FROM folders 
        WHERE user_id = #{userId}
        AND parent_id IS NULL
        AND is_archived = false
        ORDER BY is_pinned DESC, sort_order ASC, name ASC
    """)
    fun findRootFoldersByUserId(@Param("userId") userId: Long): List<Folder>
    
    /**
     * 查询用户收藏的文件夹
     */
    @Select("""
        SELECT * FROM folders 
        WHERE user_id = #{userId}
        AND is_pinned = true
        AND is_archived = false
        ORDER BY sort_order ASC, created_at DESC
    """)
    fun findPinnedFoldersByUserId(@Param("userId") userId: Long): List<Folder>
    
    /**
     * 统计用户文件夹数量
     */
    @Select("""
        SELECT COUNT(*) FROM folders 
        WHERE user_id = #{userId}
        AND is_archived = #{isArchived}
    """)
    fun countByUserIdAndIsArchived(
        @Param("userId") userId: Long,
        @Param("isArchived") isArchived: Boolean
    ): Long
    
    /**
     * 统计子文件夹数量
     */
    @Select("""
        SELECT COUNT(*) FROM folders 
        WHERE parent_id = #{parentId}
        AND is_archived = false
    """)
    fun countByParentId(@Param("parentId") parentId: String): Long
    
    /**
     * 更新文件夹统计信息
     */
    @Update("""
        UPDATE folders 
        SET document_count = #{documentCount},
            subfolder_count = #{subfolderCount},
            total_size = #{totalSize},
            updated_at = NOW()
        WHERE id = #{folderId}
    """)
    fun updateFolderStats(
        @Param("folderId") folderId: String,
        @Param("documentCount") documentCount: Long,
        @Param("subfolderCount") subfolderCount: Long,
        @Param("totalSize") totalSize: Long
    ): Int
    
    /**
     * 批量更新文件夹路径
     */
    @Update("""
        UPDATE folders 
        SET path = #{newPath},
            level = #{newLevel},
            updated_at = NOW()
        WHERE id = #{folderId}
    """)
    fun updateFolderPath(
        @Param("folderId") folderId: String,
        @Param("newPath") newPath: String,
        @Param("newLevel") newLevel: Int
    ): Int
    
    /**
     * 软删除文件夹（标记为已归档）
     */
    @Update("""
        UPDATE folders 
        SET is_archived = true,
            updated_at = NOW()
        WHERE id = #{folderId}
    """)
    fun softDeleteFolder(@Param("folderId") folderId: String): Int
    
    /**
     * 批量软删除文件夹
     */
    @Update("""
        <script>
        UPDATE folders 
        SET is_archived = true,
            updated_at = NOW()
        WHERE id IN
        <foreach collection="folderIds" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        </script>
    """)
    fun batchSoftDeleteFolders(@Param("folderIds") folderIds: List<String>): Int
}