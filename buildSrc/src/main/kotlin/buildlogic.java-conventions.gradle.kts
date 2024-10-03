plugins {
    `java-library`
    // application
    `maven-publish`
    signing
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
    }
    mavenCentral()
}

dependencies {
}

group = "io.github.rxxy"
version = "2.2.0"

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}


tasks.withType<Jar> {
    destinationDirectory = file(layout.buildDirectory.get().dir("jar"))
}




// publishing {
//     repositories {
//         // maven {
//         //     name = 'localRepo'
//         //     url = "file://${buildDir}/repo"
//         // }
//         mavenLocal()
//     }
//     publications {
//         // myApp(MavenPublication) {
//         //     groupId = 'cn.hengyumo'
//         //     artifactId = 'my-app'
//         //     version = '0.0.1'
//         //
//         //     from components.java
//         // }
//     }
// }


// publishing {
//     repositories {
//         mavenLocal()
//     }
//     publications.create<MavenPublication>("maven") {
//         from(components["java"])
//         pom {
//             name.set("miya")
//             description.set("miya")
//             url.set("https://github.com/rxxy/miya")
//             licenses {
//                 license {
//                     name.set("The Apache License, Version 2.0")
//                     url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
//                 }
//             }
//             developers {
//                 developer {
//                     id.set("rxxy")
//                     name.set("rxxy")
//                     roles.add("Java Developer")
//                 }
//             }
//             scm {
//                 url = "https://github.com/miya-projects/miya-service"
//                 connection = "scm:git:https://git@github.com/xxx/yyy.git"
//                 developerConnection = "scm:git:git@github.com:xxx/yyy.git"
//             }
//         }
//     }
// }

// apply(from = "publish.gradle.kts")
