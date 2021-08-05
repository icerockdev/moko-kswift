/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.context

import kotlinx.metadata.KmAnnotation

data class LibraryAnnotationContext(
    override val parentContext: LibraryContext,
    val annotation: KmAnnotation
) : ChildContext<LibraryContext>() {
    override fun visitChildren(action: (FeatureContext) -> Unit) = Unit

    override fun toString(): String {
        return buildString {
            append("annotation ")
            append(annotation.className)
            append(", arguments ")
            append(annotation.arguments)
            append(", parentContext ")
            append(parentContext.toString())
        }
    }

    override val uniqueId: String
        get() = buildString {
            append(parentContext.uniqueId)
            append('/')
            append(annotation.className)
        }
}
