/*
 * This file was generated by the Gradle 'init' task.
 */

rootProject.name = "miya-parent"
include(":miya-system")
include(":miya-third-service")
include(":miya-common")
include(":miya-sms-service")
project(":miya-sms-service").projectDir = file("miya-third-service/miya-sms-service")

