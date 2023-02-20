package com.icerockdev.library.associatedenum

import com.icerockdev.library.associatedenum.HasChar
import com.icerockdev.library.associatedenum.HasEnum
import com.icerockdev.library.associatedenum.HasFunction
import com.icerockdev.library.associatedenum.HasInnerList
import com.icerockdev.library.associatedenum.HasInnerNullable
import com.icerockdev.library.associatedenum.HasNullableOuterList
import com.icerockdev.library.associatedenum.OwnEnum

object TestInstances {
    val hasChar = HasChar('a')
    val hasEnum = HasEnum(OwnEnum.A)
    val hasFunction = HasFunction { i, l, s -> "$i $l $s" }
    val hasNullableListNull = HasNullableOuterList(null)
    val hasNullableList = HasNullableOuterList(emptyList())
    val hasInnerList = HasInnerList(listOf(listOf(true, false), listOf(true)))
    val hasInnerNullable = HasInnerNullable(listOf(listOf(true, null), listOf(null)))
}
