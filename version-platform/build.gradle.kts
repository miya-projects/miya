plugins {
    id("java-platform")
    alias(libs.plugins.springboot) apply false
}

group = "io.github.rxxy"
version = "platform"

repositories {
    mavenCentral()
}

dependencies {
    javaPlatform.allowDependencies();
    api(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    constraints {
        api(libs.hutool)
        api(libs.org.springframework.boot.spring.boot.starter)
        api(libs.org.hibernate.validator.hibernate.validator)
        api(libs.org.modelmapper.modelmapper)

        api(libs.org.projectlombok.lombok)
        api(libs.org.apache.commons.commons.lang3)
        api(libs.jakarta.persistence.jakarta.persistence.api)
        api(libs.com.querydsl.querydsl.jpa)
        api(libs.org.hibernate.orm.hibernate.envers)
        api(libs.org.jxls.jxls.poi)
        api(libs.org.springdoc.springdoc.openapi.starter.webmvc.ui)
        api(libs.org.hibernate.orm.hibernate.jpamodelgen)
        api(libs.io.hypersistence.hypersistence.utils.hibernate.v62)
        api(libs.com.alibaba.druid.spring.boot.starter)


        api(libs.com.github.ben.manes.caffeine.caffeine)
        api(libs.jakarta.json.jakarta.json.api)
        api(libs.org.hibernate.validator.hibernate.validator.annotation.processor)

        api(libs.com.github.therapi.therapi.runtime.javadoc)
        api(libs.com.github.therapi.therapi.runtime.javadoc.scribe)

        api(libs.net.coobird.thumbnailator)
        api(libs.javax.mail.mail)

        api(libs.io.minio.minio)
        api(libs.co.elastic.clients.elasticsearch.java)
        api(libs.com.mysql.mysql.connector.j)
    }
}
