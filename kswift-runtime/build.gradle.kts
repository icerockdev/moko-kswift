/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("multiplatform-library-convention")
    id("dev.icerock.mobile.multiplatform.android-manifest")
    id("javadoc-stub-convention")
    id("publication-convention")
}

tasks.getByName("publishToMavenLocal").dependsOn(
    gradle.includedBuild("kswift-gradle-plugin").task(":publishToMavenLocal")
)
