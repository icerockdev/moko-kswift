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

kswift {
    install(dev.icerock.moko.kswift.plugin.feature.PlatformExtensionFunctionsFeature) {
        filter = excludeFilter(
            "PackageFunctionContext/dev.icerock.moko:mvvm-livedata/dev.icerock.moko.mvvm.binding/Class(name=platform/UIKit/UILabel)/bindText/liveData:Class(name=dev/icerock/moko/mvvm/livedata/LiveData)<Class(name=kotlin/String)>,formatter:Class(name=kotlin/Function1)<Class(name=kotlin/String),Class(name=kotlin/String)>",
            "PackageFunctionContext/dev.icerock.moko:mvvm-livedata/dev.icerock.moko.mvvm.binding/Class(name=platform/UIKit/UITextField)/bindText/liveData:Class(name=dev/icerock/moko/mvvm/livedata/LiveData)<Class(name=kotlin/String)>,formatter:Class(name=kotlin/Function1)<Class(name=kotlin/String),Class(name=kotlin/String)>",
            "PackageFunctionContext/dev.icerock.moko:mvvm-livedata/dev.icerock.moko.mvvm.binding/Class(name=platform/UIKit/UITextView)/bindText/liveData:Class(name=dev/icerock/moko/mvvm/livedata/LiveData)<Class(name=kotlin/String)>,formatter:Class(name=kotlin/Function1)<Class(name=kotlin/String),Class(name=kotlin/String)>",
            "PackageFunctionContext/dev.icerock.moko:mvvm-livedata/dev.icerock.moko.mvvm.binding/Class(name=platform/UIKit/UIButton)/bindTitle/liveData:Class(name=dev/icerock/moko/mvvm/livedata/LiveData)<Class(name=kotlin/String)>"
        )
    }
    install(dev.icerock.moko.kswift.plugin.feature.SealedToSwiftEnumFeature) {
    }
}

dependencies {
    commonMainApi(libs.coroutines)

    commonMainApi(libs.mokoMvvmCore)
    commonMainApi(libs.mokoMvvmLiveData)
    commonMainApi(libs.mokoMvvmState)
    commonMainApi(libs.mokoResources)

    commonMainApi(projects.kswiftRuntime)
}

framework {
    export(projects.kswiftRuntime)
    export(libs.mokoMvvmCore)
    export(libs.mokoMvvmLiveData)
    export(libs.mokoMvvmState)
    export(libs.mokoResources)
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
