/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

@Suppress("unused")
open class KSwiftPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<KSwiftExtension>("kswift")
        extension.projectPodspecName = target.name.replace('-', '_')

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
        registerPodspecTask(linkTask, kSwiftExtension)
    }

    private fun registerPodspecTask(
        linkTask: KotlinNativeLink,
        kSwiftExtension: KSwiftExtension
    ) {
        val project: Project = linkTask.project
        val frameworkName: String = linkTask.baseName
        val podspecTaskName = "kSwift${frameworkName}Podspec"

        if (project.tasks.findByName(podspecTaskName) != null) return

        project.tasks.create(podspecTaskName, KSwiftPodspecTask::class) {
            it.linkTask = linkTask
            it.kSwiftExtension = kSwiftExtension
            it.dependsOn(linkTask)
        }
    }
}
