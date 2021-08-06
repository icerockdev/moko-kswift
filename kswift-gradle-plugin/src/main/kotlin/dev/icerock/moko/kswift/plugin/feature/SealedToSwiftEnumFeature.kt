/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.feature

import dev.icerock.moko.kswift.plugin.ProcessorContext
import dev.icerock.moko.kswift.plugin.ProcessorFeature
import dev.icerock.moko.kswift.plugin.context.ClassContext

class SealedToSwiftEnumFeature(
    filter: Filter<ClassContext>
) : ProcessorFeature<ClassContext>(filter) {
    override fun doProcess(featureContext: ClassContext, processorContext: ProcessorContext) {
        if (featureContext.clazz.sealedSubclasses.isEmpty()) return

        println(featureContext)
    }

    class Config {
        var filter: Filter<ClassContext> = Filter.Exclude(emptySet())
    }

    companion object : Factory<ClassContext, SealedToSwiftEnumFeature, Config> {
        override fun create(block: Config.() -> Unit): SealedToSwiftEnumFeature {
            val config = Config().apply(block)
            return SealedToSwiftEnumFeature(config.filter)
        }
    }
}
