/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File

open class KSwiftPodspecTask : DefaultTask() {

    init {
        group = "cocoapods"
    }

    @get:Internal
    internal lateinit var linkTask: KotlinNativeLink

    @get:Internal
    internal lateinit var kSwiftExtension: KSwiftExtension

    private val projectPodspecName: String
        get() = kSwiftExtension.projectPodspecName.get()

    private val iosDeploymentTarget: String?
        get() = kSwiftExtension.iosDeploymentTarget.orNull

    private val moduleName: String
        get() = "${linkTask.baseName}Swift"

    private val podspecName: String
        get() = "${projectPodspecName}Swift"

    @get:Internal
    val outputCocoapodsDir: File
        get() = File(project.buildDir, "cocoapods/framework")

    @get:InputDirectory
    val frameworkDir: File
        get() = linkTask.outputFile.get()

    @get:OutputDirectory
    val outputDir: File
        get() = File(outputCocoapodsDir, moduleName)

    @get:OutputFile
    val outputPodspec: File
        get() = File(project.projectDir, "$podspecName.podspec")

    @TaskAction
    fun execute() {
        frameworkDir.copyRecursively(outputCocoapodsDir, true)

        val isStatic: Boolean = linkTask.isStaticFramework
        val iosDeploymentString: String = iosDeploymentTarget?.let {
            "spec.ios.deployment_target  = '$it'"
        }.orEmpty()

        @Suppress("MaxLineLength")
        outputPodspec.writeText(
            """
                Pod::Spec.new do |spec|
                    spec.name                     = '$podspecName'
                    spec.version                  = '1.0'
                    spec.homepage                 = 'Link to a Kotlin/Native module homepage'
                    spec.source                   = { :git => "Not Published", :tag => "Cocoapods/#{spec.name}/#{spec.version}" }
                    spec.authors                  = ''
                    spec.license                  = ''
                    spec.summary                  = 'Some description for a Kotlin/Native module'
                    spec.module_name              = "$moduleName"
                    
                    $iosDeploymentString
                    spec.static_framework         = $isStatic
                    spec.dependency '$projectPodspecName'
                    spec.source_files = "build/cocoapods/framework/$moduleName/**/*.{h,m,swift}"
                end
            """.trimIndent()
        )
    }
}
