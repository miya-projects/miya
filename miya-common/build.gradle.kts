plugins {
    id("buildlogic.java-conventions")
    // id("java-library")
}
description = "miya-common"

dependencies {
    // spring系列
    api(libs.org.springframework.boot.spring.boot.starter)
    api(libs.org.hibernate.validator.hibernate.validator)
    api(libs.org.modelmapper.modelmapper)
    api(libs.hutool)
    api(libs.org.projectlombok.lombok)
    api(libs.org.apache.commons.commons.lang3)


    compileOnly(libs.org.springframework.boot.spring.boot.starter.data.jpa)
    compileOnly(libs.org.springframework.boot.spring.boot.starter.web)

    // 持久化
    compileOnly(libs.jakarta.persistence.jakarta.persistence.api)
    compileOnly(variantOf(libs.com.querydsl.querydsl.jpa) {
        classifier("jakarta")
    })
    compileOnly(libs.org.hibernate.orm.hibernate.envers)

    // 工具类
    compileOnly(libs.org.jxls.jxls.poi)
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


