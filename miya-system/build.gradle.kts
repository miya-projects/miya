plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(libs.org.springframework.boot.spring.boot.starter)
    api(libs.org.springframework.boot.spring.boot.starter.web)
    api(libs.org.springframework.boot.spring.boot.starter.cache)
    api(libs.org.springframework.retry.spring.retry)
    api(libs.org.springframework.boot.spring.boot.configuration.processor)

    api(libs.javax.mail.mail)
    api(project(":miya-common"))
    api(project(":miya-sms-service"))

    api(libs.com.google.guava.guava)
    api(libs.net.coobird.thumbnailator)
    api(libs.jakarta.json.jakarta.json.api)
    api(libs.org.jxls.jxls.poi)
    api(libs.com.fasterxml.jackson.core.jackson.databind)
    compileOnly(libs.org.hibernate.validator.hibernate.validator.annotation.processor)

    runtimeOnly(libs.com.mysql.mysql.connector.j)
    api(libs.co.elastic.clients.elasticsearch.java)
    api(libs.com.github.therapi.therapi.runtime.javadoc)
    compileOnly(libs.io.minio.minio)
    api(variantOf(libs.com.querydsl.querydsl.jpa) {
        classifier("jakarta")
    })
    api(libs.org.springdoc.springdoc.openapi.starter.webmvc.ui)



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
