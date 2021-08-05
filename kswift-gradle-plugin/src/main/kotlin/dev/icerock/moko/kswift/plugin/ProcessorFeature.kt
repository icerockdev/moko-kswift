/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import dev.icerock.moko.kswift.plugin.context.FeatureContext
import io.outfoxx.swiftpoet.FileSpec
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

abstract class ProcessorFeature<CTX : FeatureContext>(config: Builder.() -> Unit) {
    protected val excludeNames: Set<String>

    init {
        val excludes = mutableSetOf<String>()
        val builder = Builder(excludes)
        builder.config()

        excludeNames = excludes.toSet()
    }

    fun process(featureContext: CTX, processorContext: ProcessorContext) {
        if (excludeNames.contains(featureContext.prefixedUniqueId)) return

        doProcess(featureContext, processorContext)
    }

    protected abstract fun doProcess(featureContext: CTX, processorContext: ProcessorContext)

    class Builder(private val excludes: MutableSet<String>) {
        fun exclude(name: String) {
            excludes.add(name)
        }
    }
}

data class ProcessorContext(
    val fileSpecBuilder: FileSpec.Builder,
    val framework: Framework
)
