/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library.cflow

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TestViewModel : ViewModel() {
    val login: MutableStateFlow<String> = MutableStateFlow(value = "test")
    val password: MutableStateFlow<String> = MutableStateFlow(value = "passwd")

    val isFilled: StateFlow<Boolean> = combine(login, password) { login, password ->
        login.isNotBlank() && password.isNotBlank()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)
}
