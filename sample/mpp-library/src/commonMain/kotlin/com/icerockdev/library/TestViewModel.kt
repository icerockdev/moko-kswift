/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import dev.icerock.moko.kswift.KSwiftExclude
import dev.icerock.moko.kswift.KSwiftInclude
import dev.icerock.moko.mvvm.ResourceState
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.mvvm.livedata.mediatorOf
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

class TestViewModel {
    val login: MutableLiveData<String> = MutableLiveData(initialValue = "test")
    val password: MutableLiveData<String> = MutableLiveData(initialValue = "passwd")

    val isFilled: LiveData<Boolean> = mediatorOf(login, password) { login, password ->
        login.isNotBlank() && password.isNotBlank()
    }

    var state: ResourceState<String, StringDesc> = ResourceState.Empty()

    fun changeState() {
        state = when (state) {
            is ResourceState.Empty -> ResourceState.Loading()
            is ResourceState.Loading -> ResourceState.Success("data")
            is ResourceState.Success -> ResourceState.Failed("broke".desc())
            is ResourceState.Failed -> ResourceState.Empty()
        }
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

@KSwiftExclude
sealed interface ExcludedSealed {
    object V1: ExcludedSealed
    object V2: ExcludedSealed
}
