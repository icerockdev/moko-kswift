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
