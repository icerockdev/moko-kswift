/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.feature

import dev.icerock.moko.kswift.plugin.KSwiftRuntimeAnnotations
import dev.icerock.moko.kswift.plugin.context.FeatureContext
import dev.icerock.moko.kswift.plugin.findByClassName

sealed interface Filter<CTX : FeatureContext> {
    fun isShouldProcess(featureContext: CTX): Boolean

    data class Exclude<CTX : FeatureContext>(val names: Set<String>) : Filter<CTX> {
        override fun isShouldProcess(featureContext: CTX): Boolean {
            val isExcludedByAnnotation: Boolean = featureContext.annotations
                .findByClassName(KSwiftRuntimeAnnotations.KSWIFT_EXCLUDE) != null
            val isExcludedByNames: Boolean = names.contains(featureContext.prefixedUniqueId)

            return !isExcludedByAnnotation && !isExcludedByNames
        }
    }

    data class Include<CTX : FeatureContext>(val names: Set<String>) : Filter<CTX> {
        override fun isShouldProcess(featureContext: CTX): Boolean {
            val isIncludedByAnnotation: Boolean = featureContext.annotations
                .findByClassName(KSwiftRuntimeAnnotations.KSWIFT_INCLUDE) != null
            val isIncludedByNames: Boolean = names.contains(featureContext.prefixedUniqueId)

            return isIncludedByAnnotation || isIncludedByNames
        }
    }
}
