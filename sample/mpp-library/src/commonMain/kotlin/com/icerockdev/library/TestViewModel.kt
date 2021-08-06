/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.mvvm.livedata.mediatorOf

class TestViewModel {
    val login: MutableLiveData<String> = MutableLiveData(initialValue = "test")
    val password: MutableLiveData<String> = MutableLiveData(initialValue = "passwd")

    val isFilled: LiveData<Boolean> = mediatorOf(login, password) { login, password ->
        login.isNotBlank() && password.isNotBlank()
    }

    sealed interface ScreenState {
        object Idle : ScreenState
        object ReadyToSend : ScreenState
        object WaitResponse : ScreenState
        object Authorized : ScreenState
    }

    sealed class ScreenStateClass {
        object Idle : ScreenStateClass()
        object ReadyToSend : ScreenStateClass()
        object WaitResponse : ScreenStateClass()
        object Authorized : ScreenStateClass()
    }
}
