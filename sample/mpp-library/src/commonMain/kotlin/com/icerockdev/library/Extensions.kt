/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

interface SomeInterface

fun SomeInterface.extensionOnInterface() {
    TODO()
}

val String.extensionPropertyOnString: String get() = "123"

fun String.extensionFunctionOnString() = Unit

fun List<String>.extensionOnList(): String = joinToString()

class SomeClass {
    fun test() = Unit

    companion object
}

fun SomeClass.Companion.extensionOnCompanion() = Unit

fun SomeClass.extensionOnKotlinClass() = Unit

fun IntProgression.Companion.extensionOnKotlinCompanion() = Unit
