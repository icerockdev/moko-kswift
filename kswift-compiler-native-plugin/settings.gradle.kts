/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "kswift-compiler-native-plugin"

pluginManagement {
    repositories {
        mavenCentral()
        google()

        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

includeBuild("../kswift-build-logic")
