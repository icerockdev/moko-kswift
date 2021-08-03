/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("gradle-plugin-convention")
    id("detekt-convention")
    id("publication-convention")
}

group = "dev.icerock.moko"
version = libs.versions.mokoKSwiftVersion.get()

dependencies {
    implementation(gradleKotlinDsl())
    compileOnly(libs.kotlinGradlePlugin)
    implementation(libs.swiftPoet)
    implementation(libs.kotlinCompilerEmbeddable)
    implementation(files("libs/kotlinx-metadata-klib-1.5.20.jar"))
}

gradlePlugin {
    plugins {
        create("kswift") {
            id = "dev.icerock.moko.kswift"
            implementationClass = "dev.icerock.moko.kswift.plugin.KSwiftPlugin"
        }
    }
}

publishing.publications.register("mavenJava", MavenPublication::class) {
    from(components["java"])
}
