/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("dev.icerock.moko.gradle.publication")
    id("dev.icerock.moko.gradle.detekt")
    id("dev.icerock.moko.gradle.jvm")
    id("dev.icerock.moko.gradle.tests")
    id("com.gradle.plugin-publish") version ("0.15.0")
    id("java-gradle-plugin")
}

group = "dev.icerock.moko"
version = libs.versions.mokoKSwiftVersion.get()

dependencies {
    compileOnly(libs.kotlinGradlePluginNext)

    implementation(gradleKotlinDsl())
    implementation(libs.kotlinCompilerEmbeddable)

    api(libs.swiftPoet)
    api(libs.kotlinxMetadataKLib)

    testImplementation(libs.kotlinTestJUnit)
}

gradlePlugin {
    plugins {
        create("kswift") {
            id = "dev.icerock.moko.kswift"
            implementationClass = "dev.icerock.moko.kswift.plugin.KSwiftPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/icerockdev/moko-kswift"
    vcsUrl = "https://github.com/icerockdev/moko-kswift"
    description = "Swift-friendly api generator for Kotlin/Native frameworks"
    tags = listOf("moko-kswift", "moko", "kotlin", "kotlin-multiplatform", "codegen", "swift")

    plugins {
        getByName("kswift") {
            displayName = "MOKO KSwift generator plugin"
        }
    }

    mavenCoordinates {
        groupId = project.group as String
        artifactId = project.name
        version = project.version as String
    }
}
