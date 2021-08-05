/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.context

import kotlinx.metadata.KmPackage
import kotlinx.metadata.klib.fqName

data class PackageContext(
    override val parentContext: FragmentContext,
    val pkg: KmPackage
) : ChildContext<FragmentContext>() {
    override fun visitChildren(action: (FeatureContext) -> Unit) {
        pkg.functions.forEach { PackageFunctionContext(this, it).visit(action) }
    }

    override fun toString(): String {
        return buildString {
            append("package name ")
            append(pkg.fqName)
            append(", parentContext ")
            append(parentContext.toString())
        }
    }
}
