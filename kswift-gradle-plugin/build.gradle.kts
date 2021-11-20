/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("gradle-plugin-convention")
    id("detekt-convention")
    id("jvm-publication-convention")
    id("com.github.gmazzo.buildconfig") version ("3.0.2")
    id("com.gradle.plugin-publish") version ("0.15.0")
    id("java-gradle-plugin")
}

group = "dev.icerock.moko"
version = libs.versions.mokoKSwiftVersion.get()

dependencies {
    compileOnly(libs.kotlinGradlePlugin)

    implementation(gradleKotlinDsl())
    implementation(libs.kotlinCompilerEmbeddable)

    api(libs.swiftPoet)
    api(libs.kotlinxMetadataKLib)

    testImplementation(libs.kotlinTestJUnit)
}

buildConfig {
    sourceSets.getByName("main") {
        buildConfig {
            buildConfigField("String", "compilerPluginVersion", "\"${project.version}\"")
        }
    }
}

tasks.withType<Test>().configureEach {
    testLogging {
        showStandardStreams = true
    }
}

gradlePlugin {
    plugins {
        create("kswift") {
            id = "dev.icerock.moko.kswift"
            implementationClass = "dev.icerock.moko.kswift.plugin.KSwiftPlugin"
        }
        create("kswift-flow") {
            id = "dev.icerock.moko.kswift-flow"
            implementationClass = "dev.icerock.moko.kswift.plugin.KSwiftFlowGradleSubplugin"
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
