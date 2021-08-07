/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.context

import kotlinx.metadata.KmAnnotation
import kotlinx.metadata.klib.KlibModuleMetadata

data class LibraryContext(
    val metadata: KlibModuleMetadata
) : FeatureContext() {
    override fun visitChildren(action: (FeatureContext) -> Unit) {
        metadata.fragments.forEach { FragmentContext(this, it).visit(action) }
    }

    override fun toString(): String {
        return buildString {
            append("metadata ")
            append(metadata.name)
            append(", fragments count ")
            append(metadata.fragments.size)
            append(", annotations count ")
            append(metadata.annotations.size)
        }
    }

    override val uniqueId: String
        get() = metadata.name.removeSurrounding("<", ">")

    override val annotations: List<KmAnnotation>
        get() = metadata.annotations
}
