/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "kswift-gradle-plugin"

pluginManagement {
    repositories {
        mavenCentral()
        google()

        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if(requested.id.id.startsWith("dev.icerock.moko.gradle")) {
                // FIXME use single source of truth
                useModule("dev.icerock.moko:moko-gradle-plugin:0.2.0")
            }
        }
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
