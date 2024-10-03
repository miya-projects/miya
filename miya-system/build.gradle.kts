plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(libs.org.springframework.boot.spring.boot.starter)
    api(libs.org.springframework.boot.spring.boot.starter.web)
    api(libs.org.springframework.boot.spring.boot.starter.data.jpa)
    api(libs.org.springframework.boot.spring.boot.starter.cache)

    api(libs.com.alibaba.druid.spring.boot.starter)
    api(libs.org.hibernate.orm.hibernate.envers)

    api(libs.io.hypersistence.hypersistence.utils.hibernate.v62)
    api(libs.org.springframework.retry.spring.retry)
    api(libs.org.springframework.boot.spring.boot.configuration.processor)
    api(libs.org.springframework.security.spring.security.crypto)

    api(project(":miya-common"))
    api(project(":miya-sms-service"))

    api(libs.com.github.ben.manes.caffeine.caffeine)

    api(libs.jakarta.json.jakarta.json.api)
    compileOnly(libs.org.hibernate.validator.hibernate.validator.annotation.processor)

    // 三方工具包
    api(libs.org.springdoc.springdoc.openapi.starter.webmvc.ui)
    api(libs.com.github.therapi.therapi.runtime.javadoc)
    // api(libs.com.google.guava.guava)
    api(libs.net.coobird.thumbnailator)
    api(libs.javax.mail.mail)
    api(libs.org.jxls.jxls.poi)
    api(variantOf(libs.com.querydsl.querydsl.jpa) {
        classifier("jakarta")
    })



    // 外部服务包
    compileOnly(libs.io.minio.minio)
    api(libs.co.elastic.clients.elasticsearch.java)
    runtimeOnly(libs.com.mysql.mysql.connector.j)


    annotationProcessor(libs.org.projectlombok.lombok)
    annotationProcessor(variantOf(libs.com.querydsl.querydsl.apt) {
        classifier("jakarta")
    })
    annotationProcessor(libs.jakarta.persistence.jakarta.persistence.api)
    annotationProcessor(libs.org.hibernate.orm.hibernate.jpamodelgen)
    annotationProcessor(libs.com.github.therapi.therapi.runtime.javadoc.scribe)



    // 测试依赖
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.data.jpa)

    testImplementation(libs.junit.junit)
    testCompileOnly(libs.org.projectlombok.lombok)

    testAnnotationProcessor(libs.org.projectlombok.lombok)
    testAnnotationProcessor(variantOf(libs.com.querydsl.querydsl.apt) {
        classifier("jakarta")
    })
    testAnnotationProcessor(libs.jakarta.persistence.jakarta.persistence.api)

}


description = "miya-system"

tasks.withType<Test> {
    enabled = false
}
