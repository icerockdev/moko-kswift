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

class TestStateSource {
    val loading: UIState<String> = UIState.Loading
    val empty: UIState<String> = UIState.Empty
    val data: UIState<String> = UIState.Data(value = "test")
    val error: UIState<String> = UIState.Error(throwable = IllegalStateException("error"))
}

sealed class UIStateClass<out T> {
    object Loading : UIStateClass<Nothing>()
    object Empty : UIStateClass<Nothing>()
    data class Data<T>(val value: T) : UIStateClass<T>()
    data class Error(val throwable: Throwable) : UIStateClass<Nothing>()
}

class TestStateClassSource {
    val loading: UIStateClass<String> = UIStateClass.Loading
    val empty: UIStateClass<String> = UIStateClass.Empty
    val data: UIStateClass<String> = UIStateClass.Data(value = "test")
    val error: UIStateClass<String> = UIStateClass.Error(throwable = IllegalStateException("error"))
}
