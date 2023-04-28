/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

data class DataClassWithCollections(
    val stringValues: List<String>,
    val optionalStringValues: List<String>?,
    val intValues: Set<Int>,
    val optionalIntValues: Set<Int>?,
    val booleanValues: Map<Int, Boolean>,
    val optionalBooleanValues: Map<Int, Boolean>?
)
