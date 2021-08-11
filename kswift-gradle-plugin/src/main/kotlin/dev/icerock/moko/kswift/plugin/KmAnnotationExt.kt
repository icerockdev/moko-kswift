/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import kotlinx.metadata.KmAnnotation
import kotlinx.metadata.KmAnnotationArgument

fun List<KmAnnotation>.findByClassName(annotation: KSwiftRuntimeAnnotations): KmAnnotation? {
    return findByClassName(annotation.className)
}

fun List<KmAnnotation>.findByClassName(className: String): KmAnnotation? {
    return firstOrNull { it.className == className }
}

fun KmAnnotation.getStringArgument(name: String): String? {
    return (arguments[name] as? KmAnnotationArgument.StringValue)?.value
}
