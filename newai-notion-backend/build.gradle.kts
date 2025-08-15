import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
	kotlin("jvm") version "1.9.20"
	kotlin("plugin.spring") version "1.9.20"
	kotlin("plugin.allopen") version "1.9.20"
	kotlin("plugin.noarg") version "1.9.20"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot Starters
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	
	// MyBatis-Plus - 更新到与Spring Boot 3.3.5兼容的版本
	implementation("com.baomidou:mybatis-plus-spring-boot3-starter:3.5.8")
//	implementation("com.baomidou:mybatis-plus-boot-starter:3.5.12")
	implementation("com.baomidou:mybatis-plus-generator:3.5.8")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	
	// Kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	
	// Database
	runtimeOnly("com.mysql:mysql-connector-j")
	
	// MongoDB
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	
	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
	
	// API Documentation
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
	
	// File Upload
	implementation("commons-io:commons-io:2.11.0")
	implementation("org.apache.tika:tika-core:2.9.1")
	
	// Image Processing
	implementation("net.coobird:thumbnailator:0.4.20")
	
	// HTTP Client (for AI API calls)
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	
	// Configuration Processor
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	
	// Development Tools
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	
	// Test Dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")
	testImplementation("org.testcontainers:mongodb")
	testImplementation("com.h2database:h2")
	testImplementation("io.mockk:mockk:1.13.8")
	testImplementation("com.ninja-squad:springmockk:4.0.2")
}

dependencyManagement {
	imports {
		mavenBom("org.testcontainers:testcontainers-bom:1.19.3")
	}
}

// Kotlin编译配置
tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

// AllOpen插件配置
allOpen {
	annotation("org.springframework.stereotype.Component")
	annotation("org.springframework.stereotype.Service")
	// Repository注解已移除，使用MyBatis-Plus Mapper
	annotation("org.springframework.web.bind.annotation.RestController")
}

// NoArg插件配置
noArg {
	// 为JPA实体类添加无参构造函数
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.Table")
}

// 测试配置
tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
		showStandardStreams = false
	}
}

// JAR构建配置
tasks.jar {
	enabled = false
	archiveClassifier = ""
}

// Spring Boot JAR配置
tasks.bootJar {
	enabled = true
	archiveClassifier = ""
	launchScript()
}