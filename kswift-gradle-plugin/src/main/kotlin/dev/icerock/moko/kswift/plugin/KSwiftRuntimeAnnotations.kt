/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

enum class KSwiftRuntimeAnnotations(val className: String) {
    KSWIFT_INCLUDE("dev/icerock/moko/kswift/KSwiftInclude"),
    KSWIFT_EXCLUDE("dev/icerock/moko/kswift/KSwiftExclude"),
    KSWIFT_OVERRIDE_NAME("dev/icerock/moko/kswift/KSwiftOverrideName")
}
