package com.example.newainotionbackend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import org.springframework.core.convert.converter.Converter

/**
 * MongoDB配置类
 * 配置MongoDB相关设置和转换器
 * @author AI Assistant
 */
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = ["com.example.newainotionbackend.repository"])
class MongoConfig {
    
    /**
     * 自定义转换器配置
     * 处理LocalDateTime与Date之间的转换
     */
    @Bean
    fun customConversions(): MongoCustomConversions {
        return MongoCustomConversions(
            listOf(
                LocalDateTimeToDateConverter(),
                DateToLocalDateTimeConverter()
            )
        )
    }
    
    /**
     * LocalDateTime转Date转换器
     */
    class LocalDateTimeToDateConverter : Converter<LocalDateTime, Date> {
        override fun convert(source: LocalDateTime): Date {
            return Date.from(source.atZone(ZoneId.systemDefault()).toInstant())
        }
    }
    
    /**
     * Date转LocalDateTime转换器
     */
    class DateToLocalDateTimeConverter : Converter<Date, LocalDateTime> {
        override fun convert(source: Date): LocalDateTime {
            return source.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        }
    }
}