/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.context

abstract class ChildContext<T : FeatureContext> : FeatureContext() {
    abstract val parentContext: T
}
