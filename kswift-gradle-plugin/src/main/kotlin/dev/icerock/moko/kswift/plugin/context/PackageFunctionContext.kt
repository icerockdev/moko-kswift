/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.context

import kotlinx.metadata.KmAnnotation
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmType
import kotlinx.metadata.KmValueParameter
import kotlinx.metadata.klib.annotations
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

    override val uniqueId: String
        get() = buildString {
            append(parentContext.uniqueId)
            append('/')
            append(func.receiverParameterType?.classifier)
            append('/')
            append(func.name)
            append('/')
            append(func.valueParameters.joinToString(",") { it.uniqueId })
        }

    private val KmValueParameter.uniqueId: String
        get() = buildString {
            append(name)
            append(':')
            type?.also { type ->
                append(type.uniqueId)
            }
        }

    private val KmType.uniqueId: String
        get() = buildString {
            append(classifier)
            if (arguments.isNotEmpty()) {
                val args = arguments
                    .mapNotNull { it.type?.uniqueId }
                    .joinToString(separator = ",", prefix = "<", postfix = ">")
                append(args)
            }
        }

    override val annotations: List<KmAnnotation>
        get() = func.annotations
}
