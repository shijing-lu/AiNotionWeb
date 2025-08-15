package com.example.newainotionbackend.util

import net.coobird.thumbnailator.Thumbnails
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class FileUtil {
    
    @Value("\${file.upload.path}")
    private lateinit var uploadPath: String
    
    @Value("\${file.upload.max-size}")
    private var maxFileSize: Long = 0
    
    @Value("\${file.upload.allowed-types}")
    private lateinit var allowedTypes: String
    
    private val tika = Tika()
    
    /**
     * 上传文件
     */
    fun uploadFile(file: MultipartFile, userId: Long, folder: String = "documents"): FileUploadResult {
        // 验证文件
        validateFile(file)
        
        // 生成文件名
        val originalFilename = file.originalFilename ?: throw IllegalArgumentException("文件名不能为空")
        val extension = getFileExtension(originalFilename)
        val newFilename = generateUniqueFilename(extension)
        
        // 创建目录结构
        val userFolder = createUserFolder(userId, folder)
        val filePath = userFolder.resolve(newFilename)
        
        try {
            // 保存文件
            Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            
            // 如果是图片，生成缩略图
            val thumbnailPath = if (isImageFile(extension)) {
                generateThumbnail(filePath, userFolder)
            } else null
            
            return FileUploadResult(
                success = true,
                filename = newFilename,
                originalFilename = originalFilename,
                filePath = filePath.toString(),
                thumbnailPath = thumbnailPath,
                fileSize = file.size,
                mimeType = file.contentType ?: detectMimeType(filePath),
                url = generateFileUrl(userId, folder, newFilename)
            )
        } catch (e: IOException) {
            throw RuntimeException("文件上传失败: ${e.message}", e)
        }
    }
    
    /**
     * 删除文件
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            val path = Paths.get(filePath)
            Files.deleteIfExists(path)
            
            // 删除缩略图
            val thumbnailPath = getThumbnailPath(path)
            if (Files.exists(thumbnailPath)) {
                Files.deleteIfExists(thumbnailPath)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取文件信息
     */
    fun getFileInfo(filePath: String): FileInfo? {
        return try {
            val path = Paths.get(filePath)
            if (!Files.exists(path)) return null
            
            val file = path.toFile()
            FileInfo(
                name = file.name,
                size = file.length(),
                mimeType = detectMimeType(path),
                lastModified = Files.getLastModifiedTime(path).toInstant(),
                isImage = isImageFile(getFileExtension(file.name))
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 验证文件
     */
    private fun validateFile(file: MultipartFile) {
        // 检查文件是否为空
        if (file.isEmpty) {
            throw IllegalArgumentException("文件不能为空")
        }
        
        // 检查文件大小
        if (file.size > maxFileSize) {
            throw IllegalArgumentException("文件大小超过限制（${maxFileSize / 1024 / 1024}MB）")
        }
        
        // 检查文件类型
        val extension = getFileExtension(file.originalFilename ?: "")
        val allowedExtensions = allowedTypes.split(",").map { it.trim().lowercase() }
        if (extension.lowercase() !in allowedExtensions) {
            throw IllegalArgumentException("不支持的文件类型：$extension")
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private fun getFileExtension(filename: String): String {
        return filename.substringAfterLast('.', "")
    }
    
    /**
     * 生成唯一文件名
     */
    private fun generateUniqueFilename(extension: String): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8)
        return "${timestamp}_${uuid}.$extension"
    }
    
    /**
     * 创建用户文件夹
     */
    private fun createUserFolder(userId: Long, folder: String): Path {
        val userFolder = Paths.get(uploadPath, "user_$userId", folder)
        Files.createDirectories(userFolder)
        return userFolder
    }
    
    /**
     * 检测MIME类型
     */
    private fun detectMimeType(filePath: Path): String {
        return try {
            tika.detect(filePath)
        } catch (e: Exception) {
            "application/octet-stream"
        }
    }
    
    /**
     * 判断是否为图片文件
     */
    private fun isImageFile(extension: String): Boolean {
        val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        return extension.lowercase() in imageExtensions
    }
    
    /**
     * 生成缩略图
     */
    private fun generateThumbnail(imagePath: Path, userFolder: Path): String? {
        return try {
            val thumbnailFolder = userFolder.resolve("thumbnails")
            Files.createDirectories(thumbnailFolder)
            
            val thumbnailName = "thumb_${imagePath.fileName}"
            val thumbnailPath = thumbnailFolder.resolve(thumbnailName)
            
            Thumbnails.of(imagePath.toFile())
                .size(300, 300)
                .keepAspectRatio(true)
                .toFile(thumbnailPath.toFile())
            
            thumbnailPath.toString()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取缩略图路径
     */
    private fun getThumbnailPath(imagePath: Path): Path {
        val parent = imagePath.parent
        val thumbnailFolder = parent.resolve("thumbnails")
        val thumbnailName = "thumb_${imagePath.fileName}"
        return thumbnailFolder.resolve(thumbnailName)
    }
    
    /**
     * 生成文件访问URL
     */
    private fun generateFileUrl(userId: Long, folder: String, filename: String): String {
        return "/api/files/user_$userId/$folder/$filename"
    }
    
    /**
     * 清理过期的临时文件
     */
    fun cleanupTempFiles() {
        // 实现清理逻辑
    }
}

data class FileUploadResult(
    val success: Boolean,
    val filename: String? = null,
    val originalFilename: String? = null,
    val filePath: String? = null,
    val thumbnailPath: String? = null,
    val fileSize: Long = 0,
    val mimeType: String? = null,
    val url: String? = null,
    val error: String? = null
)

data class FileInfo(
    val name: String,
    val size: Long,
    val mimeType: String,
    val lastModified: java.time.Instant,
    val isImage: Boolean
)