/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("android-base-convention")
    id("detekt-convention")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform.android-manifest")
    id("dev.icerock.mobile.multiplatform.ios-framework")
    id("dev.icerock.moko.kswift")
}

kotlin {
    android()
    ios()
}

dependencies {
    commonMainApi(libs.coroutines)

    commonMainApi(projects.kswiftRuntime)
}

framework {
    export(projects.kswiftRuntime)
}

val syncMultiPlatformLibraryDebugFrameworkIosX64 by tasks

val podInstall = tasks.create("podInstall", Exec::class) {
    workingDir = File(projectDir.parent, "ios-app")
    commandLine = listOf("pod", "install")

    dependsOn(syncMultiPlatformLibraryDebugFrameworkIosX64)
}

val xcodeUnitTest = tasks.create("xcodeUnitTest", Exec::class) {
    group = JavaBasePlugin.VERIFICATION_GROUP

    workingDir = File(projectDir.parent, "ios-app")
    commandLine = listOf(
        "xcodebuild",
        "test",
        "-workspace", "ios-app.xcworkspace",
        "-scheme", "ios-app",
        "-destination", "platform=iOS Simulator,name=iPhone 12 mini"
    )

    dependsOn(podInstall)
}

tasks.getByName("check").dependsOn(xcodeUnitTest)
