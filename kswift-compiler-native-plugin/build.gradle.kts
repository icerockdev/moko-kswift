/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("jvm-convention")
    id("jvm-publication-convention")
    id("kapt-convention")
}

group = "dev.icerock.moko"
version = libs.versions.mokoKSwiftVersion.get()

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler")

    compileOnly(libs.autoService)
    kapt(libs.autoService)
}

tasks.compileKotlin.configure {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}
