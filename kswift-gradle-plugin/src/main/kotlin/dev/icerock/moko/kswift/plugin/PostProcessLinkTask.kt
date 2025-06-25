/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import org.gradle.api.Action
import org.gradle.api.Task
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File

internal class PostProcessLinkTask(
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

        linkTask.inputs.files
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
