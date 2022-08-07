/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("dev.icerock.moko.gradle.multiplatform.mobile")
    id("dev.icerock.mobile.multiplatform.ios-framework")
    id("dev.icerock.moko.gradle.detekt")
    id("dev.icerock.moko.kswift")
}

kswift {
    install(dev.icerock.moko.kswift.plugin.feature.PlatformExtensionFunctionsFeature)
    install(dev.icerock.moko.kswift.plugin.feature.SealedToSwiftEnumFeature)

    excludeLibrary("kotlinx-coroutines-core")

    projectPodspecName.set("MultiPlatformLibrary")
    iosDeploymentTarget.set("11.0")
}

dependencies {
    commonMainApi(libs.coroutines)

    commonMainApi(projects.kswiftRuntime)
}

framework {
    export(libs.coroutines)
    export(projects.kswiftRuntime)
}
