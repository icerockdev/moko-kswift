/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import dev.icerock.moko.kswift.plugin.context.FeatureContext
import dev.icerock.moko.kswift.plugin.feature.BaseConfig
import dev.icerock.moko.kswift.plugin.feature.ProcessorFeature
import org.gradle.api.DomainObjectSet
import org.gradle.api.provider.Property

abstract class KSwiftExtension {
    abstract val features: DomainObjectSet<ProcessorFeature<*>>

    abstract val excludedLibs: DomainObjectSet<String>
    abstract val includedLibs: DomainObjectSet<String>

    abstract val projectPodspecName: Property<String>
    abstract val iosDeploymentTarget: Property<String>

    fun <CTX : FeatureContext, Config : BaseConfig<CTX>> install(
        featureFactory: ProcessorFeature.Factory<CTX, *, Config>
    ) {
        features.add(featureFactory.create { })
    }

    fun <CTX : FeatureContext, Config : BaseConfig<CTX>> install(
        featureFactory: ProcessorFeature.Factory<CTX, *, Config>,
        config: Config.() -> Unit
    ) {
        features.add(featureFactory.create(config))
    }

    fun includeLibrary(libraryName: String) {
        includedLibs.add(libraryName)
    }

    fun excludeLibrary(libraryName: String) {
        excludedLibs.add(libraryName)
    }
}
