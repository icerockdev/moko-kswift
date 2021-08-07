/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File

@Suppress("unused")
open class KSwiftPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<KSwiftExtension>("kswift")
        val processor = KLibProcessor(
            logger = target.logger,
            extension = extension
        )

        target.plugins
            .withType<KotlinMultiplatformPluginWrapper>()
            .configureEach { pluginWrapper ->
                val multiplatformExtension = target.extensions
                    .getByType(pluginWrapper.projectExtensionClass.java)

                applyToKotlinMultiplatform(multiplatformExtension, processor, extension)
            }
    }

    private fun applyToKotlinMultiplatform(
        extension: KotlinMultiplatformExtension,
        processor: KLibProcessor,
        kSwiftExtension: KSwiftExtension
    ) {
        extension.targets
            .withType<KotlinNativeTarget>()
            .matching { it.konanTarget.family.isAppleFamily }
            .configureEach { applyToAppleTarget(it, processor, kSwiftExtension) }
    }

    private fun applyToAppleTarget(
        target: KotlinNativeTarget,
        processor: KLibProcessor,
        kSwiftExtension: KSwiftExtension
    ) {
        target.binaries
            .withType<Framework>()
            .configureEach { applyToAppleFramework(it, processor, kSwiftExtension) }
    }

    private fun applyToAppleFramework(
        framework: Framework,
        processor: KLibProcessor,
        kSwiftExtension: KSwiftExtension
    ) {
        val linkTask: KotlinNativeLink = framework.linkTask
        linkTask.doLast(PostProcessLinkTask(framework, processor, kSwiftExtension))
    }

    private class PostProcessLinkTask(
        private val framework: Framework,
        private val processor: KLibProcessor,
        private val kSwiftExtension: KSwiftExtension,
    ) : Action<Task> {

        override fun execute(task: Task) {
            val linkTask: KotlinNativeLink = task as KotlinNativeLink

            val kotlinFrameworkName = framework.baseName
            val swiftFrameworkName = "${kotlinFrameworkName}Swift"
            val outputDir = File(framework.outputDirectory, swiftFrameworkName)
            outputDir.deleteRecursively()

            linkTask.exportLibraries
                .plus(linkTask.intermediateLibrary.get())
                .filter { file ->
                    val name = file.nameWithoutExtension
                    if (kSwiftExtension.includedLibs.isNotEmpty()) {
                        if (kSwiftExtension.includedLibs.contains(name).not()) return@filter false
                    }
                    kSwiftExtension.excludedLibs.contains(name).not()
                }
                .forEach { library ->
                    processor.processFeatureContext(library, outputDir, framework)
                }
        }
    }
}
