/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.feature

import dev.icerock.moko.kswift.plugin.context.FeatureContext

interface BaseConfig<CTX : FeatureContext> {
    val filter: Filter<CTX>

    fun excludeFilter(vararg names: String): Filter.Exclude<CTX> {
        return Filter.Exclude(names.toSet())
    }

    fun includeFilter(vararg names: String): Filter.Include<CTX> {
        return Filter.Include(names.toSet())
    }
}
