/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import dev.icerock.moko.kswift.plugin.context.FeatureContext
import dev.icerock.moko.kswift.plugin.context.LibraryContext
import dev.icerock.moko.kswift.plugin.feature.ProcessorContext
import dev.icerock.moko.kswift.plugin.feature.ProcessorFeature
import io.outfoxx.swiftpoet.FileSpec
import kotlinx.metadata.klib.KlibModuleMetadata
import org.gradle.api.logging.Logger
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import java.io.File
import kotlin.reflect.KClass

class KLibProcessor(
    private val logger: Logger,
    private val extension: KSwiftExtension
) {
    private val features: Map<KClass<out FeatureContext>, List<ProcessorFeature<*>>>
        get() = extension.features.groupBy { it.featureContext }

    fun processFeatureContext(library: File, outputDir: File, framework: Framework) {
        @Suppress("TooGenericExceptionCaught")
        val metadata: KlibModuleMetadata = try {
            KotlinMetadataLibraryProvider.readLibraryMetadata(library)
        } catch (exc: IllegalStateException) {
            logger.info("library can't be read", exc)
            return
        } catch (exc: Exception) {
            logger.error("can't parse metadata", exc)
            return
        }

        val fileSpecBuilder: FileSpec.Builder = FileSpec.builder(library.nameWithoutExtension)

        val processorContext = ProcessorContext(
            fileSpecBuilder = fileSpecBuilder,
            framework = framework
        )

        val libraryContext = LibraryContext(metadata)
        libraryContext.visit { featureContext ->
            logger.info("visit ${featureContext.prefixedUniqueId} - $featureContext")
            processFeatureContext(featureContext, processorContext)
        }

        val fileSpec: FileSpec = fileSpecBuilder.build()
        if (fileSpec.members.isNotEmpty()) {
            fileSpec.writeTo(outputDir)
        }
    }

    private fun <T : FeatureContext> processFeatureContext(
        featureContext: T,
        processorContext: ProcessorContext
    ) {
        val kclass: KClass<out T> = featureContext::class

        @Suppress("UNCHECKED_CAST")
        val processors: List<ProcessorFeature<T>> =
            features[kclass].orEmpty() as List<ProcessorFeature<T>>

        processors.forEach { featureProcessor ->
            @Suppress("TooGenericExceptionCaught")
            try {
                featureProcessor.process(featureContext, processorContext)
            } catch (exc: Exception) {
                logger.error("can't process context $featureContext", exc)
            }
        }
    }
}
