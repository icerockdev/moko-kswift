/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import dev.icerock.moko.kswift.plugin.context.FeatureContext
import io.outfoxx.swiftpoet.FileSpec
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

abstract class ProcessorFeature<CTX : FeatureContext>(private val filter: Filter<CTX>) {
    fun process(featureContext: CTX, processorContext: ProcessorContext) {
        if (filter.isShouldProcess(featureContext).not()) return

        doProcess(featureContext, processorContext)
    }

    protected abstract fun doProcess(featureContext: CTX, processorContext: ProcessorContext)

    sealed interface Filter<CTX : FeatureContext> {
        fun isShouldProcess(featureContext: CTX): Boolean

        data class Exclude<CTX : FeatureContext>(val names: Set<String>) : Filter<CTX> {
            override fun isShouldProcess(featureContext: CTX): Boolean {
                return names.contains(featureContext.prefixedUniqueId).not()
            }

            constructor(vararg names: String) : this(names.toSet())
        }

        data class Include<CTX : FeatureContext>(val names: Set<String>) : Filter<CTX> {
            override fun isShouldProcess(featureContext: CTX): Boolean {
                return names.contains(featureContext.prefixedUniqueId)
            }

            constructor(vararg names: String) : this(names.toSet())
        }
    }

    interface Factory<CTX : FeatureContext, F : ProcessorFeature<CTX>, Config> {
        fun create(block: Config.() -> Unit): F
    }
}

data class ProcessorContext(
    val fileSpecBuilder: FileSpec.Builder,
    val framework: Framework
)
