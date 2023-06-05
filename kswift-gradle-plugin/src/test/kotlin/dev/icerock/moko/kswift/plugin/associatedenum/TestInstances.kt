/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.associatedenum

object TestInstances {
    val hasChar = HasChar('a')
    val hasEnum = HasEnum(OwnEnum.A)
    val hasFunction = HasFunction { i, l, s -> "$i $l $s" }
    val hasNullableListNull = HasNullableOuterList(null)
    val hasNullableList = HasNullableOuterList(emptyList())
    val hasInnerList = HasInnerList(listOf(listOf(true, false), listOf(true)))
    val hasInnerNullable = HasInnerNullable(listOf(listOf(true, null), listOf(null)))
}
