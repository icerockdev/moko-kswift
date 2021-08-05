/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.context

import kotlinx.metadata.KmClass

data class ClassContext(
    override val parentContext: FragmentContext,
    val clazz: KmClass
) : ChildContext<FragmentContext>() {
    override fun visitChildren(action: (FeatureContext) -> Unit) {
        // TODO
    }

    override fun toString(): String {
        return buildString {
            append("class name ")
            append(clazz.name)
            append(", parentContext ")
            append(parentContext.toString())
        }
    }

    override val uniqueId: String
        get() = buildString {
            append(parentContext.uniqueId)
            append("/")
            append(clazz.name)
        }
}
