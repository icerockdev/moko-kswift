/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("android-base-convention")
    id("detekt-convention")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform.android-manifest")
    id("dev.icerock.moko.kswift-flow")
}

kotlin {
    android()
    ios()
}

dependencies {
    commonMainApi(libs.coroutines)

    commonMainApi(libs.mokoMvvmCore)

    commonMainApi(projects.kswiftRuntime)
}
