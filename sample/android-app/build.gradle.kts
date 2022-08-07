/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("dev.icerock.moko.gradle.android.application")
}

android {
    defaultConfig {
        applicationId = "dev.icerock.moko.samples.kswift"

        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(libs.appCompat)
    implementation(libs.material)

    implementation(projects.sample.mppLibrary)
}
