/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import dev.icerock.moko.mvvm.livedata.Closeable
import dev.icerock.moko.mvvm.livedata.LiveData
import platform.UIKit.UILabel
import dev.icerock.moko.mvvm.binding.bindText as originalBindText

fun UILabel.bindText(
    liveData: LiveData<String>,
    formatter: (String) -> String
): Closeable {
    return this.originalBindText(liveData, formatter)
}
