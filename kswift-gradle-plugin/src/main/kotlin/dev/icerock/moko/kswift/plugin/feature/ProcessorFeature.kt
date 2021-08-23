/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.feature

import dev.icerock.moko.kswift.plugin.context.FeatureContext
import io.outfoxx.swiftpoet.FileSpec
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import kotlin.reflect.KClass

abstract class ProcessorFeature<CTX : FeatureContext> {
    abstract val featureContext: KClass<CTX>
    abstract val filter: Filter<CTX>

    fun process(featureContext: CTX, processorContext: ProcessorContext) {
        if (filter.isShouldProcess(featureContext).not()) return

        doProcess(featureContext, processorContext)
    }

    protected abstract fun doProcess(featureContext: CTX, processorContext: ProcessorContext)

    interface Factory<CTX : FeatureContext, F : ProcessorFeature<CTX>, Config : BaseConfig<CTX>> {
        fun create(block: Config.() -> Unit): F

        val featureContext: KClass<CTX>

        val factory: Factory<CTX, F, Config>
    }
}

data class ProcessorContext(
    val fileSpecBuilder: FileSpec.Builder,
    val framework: Framework
)
