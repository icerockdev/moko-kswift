/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import dev.icerock.moko.mvvm.livedata.Closeable
import dev.icerock.moko.mvvm.livedata.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import platform.UIKit.UILabel
import dev.icerock.moko.mvvm.binding.bindText as originalBindText

fun UILabel.bindText(
    liveData: LiveData<String>,
    formatter: (String) -> String
): Closeable {
    return this.originalBindText(liveData, formatter)
}

fun UILabel.bindText(
    liveData: LiveData<String>
): Closeable {
    return this.originalBindText(liveData)
}

class CFlow<T>(private val stateFlow: StateFlow<T>) : StateFlow<T> by stateFlow

fun UILabel.bindText(coroutineScope: CoroutineScope, flow: CFlow<String>) {
    val label = this
    coroutineScope.launch {
        label.text = flow.value
        flow.collect { label.text = it }
    }
}
