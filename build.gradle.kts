/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }

    dependencies {
        classpath(libs.mobileMultiplatformGradlePlugin)
        classpath(libs.kotlinGradlePlugin)
        classpath(libs.mokoGradlePlugin)
        classpath(libs.androidGradlePlugin)
        classpath(libs.detektGradlePlugin)
        classpath("dev.icerock.moko:kswift-gradle-plugin")
    }
}

apply(plugin = "dev.icerock.moko.gradle.publication.nexus")
val mokoVersion = libs.versions.mokoKSwiftVersion.get()
allprojects {
    group = "dev.icerock.moko"
    version = mokoVersion
}

// temporary fix for Apple Silicon (remove after 1.6.20 update)
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
}
