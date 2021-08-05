/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import dev.icerock.moko.kswift.plugin.context.FeatureContext
import kotlin.reflect.KClass

open class KSwiftExtension {
    internal val features: MutableMap<KClass<out FeatureContext>, List<ProcessorFeature<*>>> =
        mutableMapOf()

    fun <CTX : FeatureContext> install(
        featureContext: KClass<out CTX>,
        processorFeature: ProcessorFeature<CTX>
    ) {
        val currentList: List<ProcessorFeature<*>> = features[featureContext] ?: emptyList()
        features[featureContext] = currentList.plus(processorFeature)
    }

    inline fun <reified CTX : FeatureContext> install(processor: ProcessorFeature<CTX>) {
        install(CTX::class, processor)
    }
}
