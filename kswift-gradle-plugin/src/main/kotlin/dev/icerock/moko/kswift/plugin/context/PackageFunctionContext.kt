/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.context

import kotlinx.metadata.KmFunction
import kotlinx.metadata.klib.uniqId

data class PackageFunctionContext(
    override val parentContext: PackageContext,
    val func: KmFunction
) : ChildContext<PackageContext>() {
    override fun visitChildren(action: (FeatureContext) -> Unit) = Unit

    override fun toString(): String {
        return buildString {
            append("package function ")
            append(func.name)
            append(", uniqId ")
            append(func.uniqId)
            append(", parentContext ")
            append(parentContext.toString())
        }
    }
}
