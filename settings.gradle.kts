/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

rootProject.name = "moko-kswift"

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

includeBuild("kswift-build-logic")
includeBuild("kswift-gradle-plugin")
includeBuild("kswift-compiler-native-plugin")

include(":kswift-runtime")
include(":sample:android-app")
include(":sample:mpp-library")
include(":sample:mpp-library:cflow")
include(":sample:mpp-library-pods")
