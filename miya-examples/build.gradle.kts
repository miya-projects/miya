import com.google.cloud.tools.jib.api.buildplan.ImageFormat
import org.springframework.boot.gradle.tasks.bundling.BootJar


plugins {
    `java-library`
    id("org.springframework.boot") version "3.3.1"
    // 3.4.3 在windows下执行jibDockerBuild会卡住
    id("com.google.cloud.tools.jib") version ("3.4.2")
    // id("io.spring.dependency-management") version ("3.2.3")
}
// apply(plugin = "io.spring.dependency-management")


group = "io.github.rxxy"
version = "1.0"
description = "miya-examples"
java.sourceCompatibility = JavaVersion.VERSION_17


tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.named<BootJar>("bootJar") {
    archiveClassifier.set("boot")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
    }
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation(libs.io.github.rxxy.miya.system)
    // implementation(libs.org.springframework.boot.spring.boot.starter)

    // implementation(libs.org.springframework.boot.spring.boot.starter.data.jpa)
    // implementation(libs.org.springframework.boot.spring.boot.starter.web)
    // implementation(libs.org.springframework.boot.spring.boot.starter.cache)
    implementation(libs.org.springframework.boot.spring.boot.starter.websocket)

    api(libs.jakarta.persistence.jakarta.persistence.api)
    api(libs.com.alibaba.druid.spring.boot.starter)
    api(variantOf(libs.com.querydsl.querydsl.jpa) {
        classifier("jakarta")
    })
    api(libs.org.hibernate.orm.hibernate.envers)
    api(libs.io.hypersistence.hypersistence.utils.hibernate.v62)

    implementation(libs.org.springdoc.springdoc.openapi.starter.webmvc.ui)

    implementation(libs.org.apache.commons.commons.lang3)
    implementation(libs.org.apache.commons.commons.collections4)
    implementation(libs.cn.hutool.hutool.all)
    implementation(libs.com.aliyun.oss.aliyun.sdk.oss)
    implementation(libs.io.minio.minio)
    compileOnly(libs.org.projectlombok.lombok)


    annotationProcessor("org.projectlombok:lombok:1.18.32")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")
    annotationProcessor("com.github.therapi:therapi-runtime-javadoc-scribe:0.15.0")
    annotationProcessor("org.hibernate.orm:hibernate-jpamodelgen:6.3.1.Final")



}

// 不排除会报错，且只有jib打包后跑容器会报错
// Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
configurations {
    implementation {
        exclude(group = "commons-logging", module = "commons-logging")
    }
}


jib {
    from.image = "registry.cn-hangzhou.aliyuncs.com/rxxy/java:17-jdk"
    to {
        image = "registry.cn-hangzhou.aliyuncs.com/rxxy/miya-examples:latest"
        auth {
            username = System.getenv("REGISTRY_USERNAME")
            password = System.getenv("REGISTRY_PASSWORD")
        }
    }
    container {
        mainClass = "com.teamytd.Application"
        ports = listOf("8080")
        labels = mapOf("app" to "miya-examples")
        environment = mapOf(
                "TZ" to "Asia/Shanghai",
                "JDK_JAVA_OPTIONS" to "-Xms512m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/miya-examples-dump.hprof -Dspring.profiles.active=local"
        )
        format = ImageFormat.OCI
    }
}

// tasks.register("helloTask") {
//     group = "Application"
//     description = "Runs this project as a JVM application."
//     doLast {
//         println("doLast")
//         System.setProperty("myEnvVar", "123")
//     }
// }

tasks.withType<Jar> {
    destinationDirectory = file(layout.buildDirectory.get().dir("jar"))
    manifest {
        attributes(
                "Main-Class" to "com.teamytd.Application"
        )
    }
}



