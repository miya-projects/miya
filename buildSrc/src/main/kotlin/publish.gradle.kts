
//
// plugins {
//     `maven-publish`
//     signing
// }
//


//
// tasks.withType<Javadoc>() {
//     options.encoding = "UTF-8"
//     options.quiet()
//     isFailOnError = false
//     enabled = false
// }
//
// val sourcesJar by tasks.registering(Jar::class) {
//     group = "build"
//     from(sourceSets["main"].allJava)
//     archiveClassifier = "sources"
//     // dependsOn(tasks.named("sourceJar"))
// }
//
// val javadocJar by tasks.registering(Jar::class) {
//     group = "build"
//     from(tasks.withType<Javadoc>())
//     archiveClassifier = "javadoc"
// }





// repositories {
//     maven {
//         name = "oss"
//         url = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
//         credentials {
//             username = "oss_sonatype_maven_username"
//             password = "oss_sonatype_maven_password"
//         }
//     }
//     // maven {
//     //     name = 'oss-snapshot'
//     //     url = oss_sonatype_snapshot_maven_url
//     //     credentials {
//     //         username = oss_sonatype_maven_username
//     //         password = oss_sonatype_maven_password
//     //     }
//     // }
// }
//
// publishing {
//     publications.create<MavenPublication>("maven") {
//         from(components["java"])
//         artifact(sourcesJar)
//         artifact(javadocJar)
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
//
// // tasks.withType<>("signing") {
// //
// // }
// // SigningExtension
//
// signing {
//     sign(publishing.publications["maven"])
//     // sign publishing.publications.mavenJava
// }
