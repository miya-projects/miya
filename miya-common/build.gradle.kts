plugins {
    id("buildlogic.java-conventions")
    // id("java-library")
}
description = "miya-common"

dependencies {
    // spring系列
    // api("org.springframework.boot:spring-boot-starter-data-jpa")
    api(libs.org.springframework.boot.spring.boot.starter.data.jpa)
    api(libs.org.springframework.boot.spring.boot.starter.web)
    api(libs.org.springframework.security.spring.security.crypto)
    api(libs.org.springframework.spring.context.support)
    compileOnly(libs.org.springframework.spring.tx)

    // 持久化
    api(libs.jakarta.persistence.jakarta.persistence.api)
    api(libs.com.alibaba.druid.spring.boot.starter)
    api(variantOf(libs.com.querydsl.querydsl.jpa) {
        classifier("jakarta")
    })
    api(libs.org.hibernate.orm.hibernate.envers)
    api(libs.io.hypersistence.hypersistence.utils.hibernate.v62)

    // 工具类
    api(libs.io.jsonwebtoken.jjwt)
    api(libs.org.hibernate.validator.hibernate.validator)
    api(libs.com.fasterxml.jackson.module.jackson.module.jakarta.xmlbind.annotations)
    api(libs.org.modelmapper.modelmapper)
    api(libs.org.jxls.jxls.poi)
    api(libs.com.github.ben.manes.caffeine.caffeine)

    compileOnly(libs.org.springdoc.springdoc.openapi.starter.webmvc.ui)
    // compileOnly(libs.com.github.xiaoymin.knife4j.openapi3.ui)


    // 注解处理器
    annotationProcessor(libs.org.projectlombok.lombok)
    annotationProcessor(variantOf(libs.com.querydsl.querydsl.apt) {
        classifier("jakarta")
    })
    annotationProcessor(libs.jakarta.persistence.jakarta.persistence.api)
    annotationProcessor(libs.org.hibernate.orm.hibernate.jpamodelgen)


}


