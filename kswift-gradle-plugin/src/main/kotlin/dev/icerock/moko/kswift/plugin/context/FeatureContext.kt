/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.context

abstract class FeatureContext {
    fun visit(action: (FeatureContext) -> Unit) {
        action(this)
        visitChildren(action)
    }

    protected abstract fun visitChildren(action: (FeatureContext) -> Unit)
}
