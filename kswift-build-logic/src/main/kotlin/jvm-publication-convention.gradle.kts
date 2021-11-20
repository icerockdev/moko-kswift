/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("publication-convention")
}

publishing.publications.register("mavenJava", MavenPublication::class) {
    from(components["java"])
}
