/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("android-base-convention")
    id("dev.icerock.mobile.multiplatform.android-manifest")
}

kotlin {
    android {
        publishLibraryVariants("release", "debug")
    }
    ios()
    iosSimulatorArm64()

    macosX64()
    macosArm64()
    tvos()
    watchos()

    jvm()
    js(BOTH) {
        nodejs()
        browser()
    }

    linuxArm64()
    linuxArm32Hfp()
    linuxMips32()
    linuxMipsel32()
    linuxX64()

    mingwX64()
    mingwX86()

    wasm32()
}
