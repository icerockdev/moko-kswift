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
        classpath(":kswift-build-logic")
        classpath("dev.icerock.moko:kswift-gradle-plugin")
    }
}

allprojects {
    plugins.withId("org.gradle.maven-publish") {
        group = "dev.icerock.moko"
        version = libs.versions.mokoKSwiftVersion.get()
    }
}
