/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import kotlinx.metadata.KmClass
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmModuleFragment
import kotlinx.metadata.KmPackage
import kotlinx.metadata.klib.KlibModuleMetadata

sealed class FeatureContext {
    fun visit(action: FeatureContext.() -> Unit) {
        action()
        visitChildren(action)
    }

    protected abstract fun visitChildren(action: FeatureContext.() -> Unit)
}

abstract class ChildContext<T : FeatureContext> : FeatureContext() {
    abstract val parentContext: T
}

data class LibraryContext(
    val metadata: KlibModuleMetadata
) : FeatureContext() {
    override fun visitChildren(action: FeatureContext.() -> Unit) {
        metadata.annotations.forEach {
            // TODO
        }
        metadata.fragments.forEach { FragmentContext(this, it).visit(action) }
    }
}

data class FragmentContext(
    override val parentContext: LibraryContext,
    val fragment: KmModuleFragment
) : ChildContext<LibraryContext>() {
    override fun visitChildren(action: FeatureContext.() -> Unit) {
        fragment.pkg?.let { PackageContext(this, it).visit(action) }
        fragment.classes.forEach { ClassContext(this, it).visit(action) }
    }
}

data class PackageContext(
    override val parentContext: FragmentContext,
    val pkg: KmPackage
) : ChildContext<FragmentContext>() {
    override fun visitChildren(action: FeatureContext.() -> Unit) {
        pkg.functions.forEach { PackageFunctionContext(this, it).visit(action) }
    }
}

data class ClassContext(
    override val parentContext: FragmentContext,
    val clazz: KmClass
) : ChildContext<FragmentContext>() {
    override fun visitChildren(action: FeatureContext.() -> Unit) {
        // TODO
    }
}

data class PackageFunctionContext(
    override val parentContext: PackageContext,
    val func: KmFunction
) : ChildContext<PackageContext>() {
    override fun visitChildren(action: FeatureContext.() -> Unit) = Unit
}
