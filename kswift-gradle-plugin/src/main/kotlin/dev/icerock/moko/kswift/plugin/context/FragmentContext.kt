/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.context

import kotlinx.metadata.KmAnnotation
import kotlinx.metadata.KmModuleFragment
import kotlinx.metadata.klib.fqName
import kotlinx.metadata.klib.moduleFragmentFiles

data class FragmentContext(
    override val parentContext: LibraryContext,
    val fragment: KmModuleFragment
) : ChildContext<LibraryContext>() {
    override fun visitChildren(action: (FeatureContext) -> Unit) {
        fragment.pkg?.let { PackageContext(this, it).visit(action) }
        fragment.classes.forEach { ClassContext(this, it).visit(action) }
    }

    override fun toString(): String {
        return buildString {
            append("fragment files ")
            append(fragment.moduleFragmentFiles)
            append(", parentContext ")
            append(parentContext.toString())
        }
    }

    override val uniqueId: String
        get() = parentContext.uniqueId

    override val annotations: List<KmAnnotation>
        get() = emptyList()
}
