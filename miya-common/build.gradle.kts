plugins {
    id("buildlogic.java-conventions")
    // id("java-library")
    alias(libs.plugins.springboot) apply false
}
description = "miya-common"

dependencies {
    implementation(platform(project(":version-platform")))
    annotationProcessor(platform(project(":version-platform")))
    // spring系列
    api("org.springframework.boot:spring-boot-starter")
    api("org.hibernate.validator:hibernate-validator")
    api("org.modelmapper:modelmapper")
    api("cn.hutool:hutool-all")
    api("org.projectlombok:lombok")
    api("org.apache.commons:commons-lang3")

    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.springframework.boot:spring-boot-starter-web")

    // 持久化
    compileOnly(variantOf(libs.com.querydsl.querydsl.jpa) {
        classifier("jakarta")
    })
    compileOnly("org.hibernate.orm:hibernate-envers")

    // 工具类
    compileOnly("org.jxls:jxls-poi")
    compileOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    // 注解处理器
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor(variantOf(libs.com.querydsl.querydsl.apt) {
        classifier("jakarta")
    })
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("org.hibernate.orm:hibernate-jpamodelgen")

}


