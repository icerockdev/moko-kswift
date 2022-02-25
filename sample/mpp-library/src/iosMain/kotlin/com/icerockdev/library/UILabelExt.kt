/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import platform.UIKit.UILabel

class LiveData<T>

interface Closeable

fun UILabel.bindText(
    liveData: LiveData<String>,
    formatter: (String) -> String
): Closeable {
    TODO()
}

fun UILabel.bindText(
    liveData: LiveData<String>
): Closeable {
    TODO()
}

class CFlow<T>(private val stateFlow: StateFlow<T>) : StateFlow<T> by stateFlow

fun UILabel.bindText(coroutineScope: CoroutineScope, flow: CFlow<String>) {
    val label = this
    coroutineScope.launch {
        label.text = flow.value
        flow.collect { label.text = it }
    }
}

fun <T : String?> UILabel.bindGenericText(liveData: LiveData<T>): Closeable {
    TODO()
}

fun <T> UILabel.bindGenericAny(liveData: LiveData<T>): Closeable {
    TODO()
}
