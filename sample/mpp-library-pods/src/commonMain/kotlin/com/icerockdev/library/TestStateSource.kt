/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

class TestStateSource {
    val loading: UIState<String> = UIState.Loading
    val empty: UIState<String> = UIState.Empty
    val data: UIState<String> = UIState.Data(value = "test")
    val error: UIState<String> = UIState.Error(throwable = IllegalStateException())
}
