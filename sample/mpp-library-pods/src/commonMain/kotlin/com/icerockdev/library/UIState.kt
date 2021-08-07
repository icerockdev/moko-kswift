/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

sealed interface UIState<out T> {
    object Loading : UIState<Nothing>
    object Empty : UIState<Nothing>
    data class Data<T>(val value: T) : UIState<T>
    data class Error(val throwable: Throwable) : UIState<Nothing>
}
