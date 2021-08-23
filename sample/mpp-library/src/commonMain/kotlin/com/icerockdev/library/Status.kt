/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

sealed class Status<T : Any> {
    data class Success<T : Any>(val data: T) : Status<T>()
    data class Failure<T : Any>(val exception: Exception) : Status<T>()
}

val successStatus: Status<String> = Status.Success("hi!")
val failureStatus: Status<String> = Status.Failure(IllegalArgumentException("illegal"))
