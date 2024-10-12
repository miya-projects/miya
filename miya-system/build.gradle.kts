plugins {
    id("buildlogic.java-conventions")
    alias(libs.plugins.springboot) apply false
}

dependencies {
    implementation(platform(project(":version-platform")))
    testImplementation(platform(project(":version-platform")))

    api(project(":miya-common"))
    api(project(":miya-sms-service"))

    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-cache")


    runtimeOnly("com.mysql:mysql-connector-j")
    api("com.alibaba:druid-spring-boot-starter")
    api("org.hibernate.orm:hibernate-envers")
    api("io.hypersistence:hypersistence-utils-hibernate-62")

    api("org.springframework.retry:spring-retry")
    api("org.springframework.boot:spring-boot-configuration-processor")
    api("org.springframework.security:spring-security-crypto")



    api("com.github.ben-manes.caffeine:caffeine")
    api("jakarta.json:jakarta.json-api")
    // compileOnly("org.hibernate.validator:hibernate-validator-annotation-processor")

    // 三方工具包
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    api("com.github.therapi:therapi-runtime-javadoc")
    // api(libs.com.google.guava.guava)
    api("net.coobird:thumbnailator")
    api("javax.mail:mail")
    api("org.jxls:jxls-poi")
    api(variantOf(libs.com.querydsl.querydsl.jpa) {
        classifier("jakarta")
    })


    // 外部服务包
    compileOnly("io.minio:minio")
    api("co.elastic.clients:elasticsearch-java")

    annotationProcessor(platform(project(":version-platform")))
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor(variantOf(libs.com.querydsl.querydsl.apt) {
        classifier("jakarta")
    })
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("org.hibernate.orm:hibernate-jpamodelgen")
    annotationProcessor("org.hibernate.validator:hibernate-validator-annotation-processor")
    annotationProcessor("com.github.therapi:therapi-runtime-javadoc-scribe")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")



    // 测试依赖
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")

    testImplementation("junit:junit")
    testCompileOnly("org.projectlombok:lombok")

    testAnnotationProcessor(platform(project(":version-platform")))
    testAnnotationProcessor("org.projectlombok:lombok")
    testAnnotationProcessor(variantOf(libs.com.querydsl.querydsl.apt) {
        classifier("jakarta")
    })
}


description = "miya-system"

tasks.withType<Test> {
    enabled = false
}
