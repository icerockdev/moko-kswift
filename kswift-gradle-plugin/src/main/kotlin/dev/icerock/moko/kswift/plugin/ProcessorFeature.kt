/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import io.outfoxx.swiftpoet.FileSpec
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

interface ProcessorFeature<CTX : FeatureContext> {
    fun process(featureContext: CTX, processorContext: ProcessorContext)
}

data class ProcessorContext(
    val fileSpecBuilder: FileSpec.Builder,
    val framework: Framework
)
