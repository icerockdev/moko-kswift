/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.associatedenum

import org.jetbrains.kotlin.kotlinx.collections.immutable.ImmutableList

@Suppress("LongLine")
sealed interface TestingSealed
data class HasChar(val mychar: Char) : TestingSealed
data class HasEnum(val myenum: OwnEnum) : TestingSealed
data class HasFunction(val myfunc: (Int, List<Boolean>, String) -> String) : TestingSealed
data class HasNullableOuterList(val innerList: List<List<Boolean>>?) : TestingSealed
data class HasImmutableList(val innerList: ImmutableList<String>) : TestingSealed
data class HasInnerList(val innerList: List<List<Boolean>>) : TestingSealed
data class HasNullableInnerList(val innerList: List<List<Boolean>?>) : TestingSealed
data class HasInnerNullable(val innerList: List<List<Boolean?>>) : TestingSealed
data class HasListInt(val hasGeneric: List<Int>) : TestingSealed
data class HasListIntNullable(val hasGeneric: List<Int?>) : TestingSealed
data class HasListString(val hasGeneric: List<String>) : TestingSealed
data class HasListStringOuterNullable(val hasGeneric: List<String>?) : TestingSealed
data class HasListStringNullable(val hasGeneric: List<String?>) : TestingSealed
data class HasListOwn(val hasGeneric: List<OwnClass>) : TestingSealed
data class HasMap(val map: Map<String, Int>) : TestingSealed
data class HasMapNullableParams(val map: Map<String, Int?>) : TestingSealed
data class HasMapNullableOuter(val map: Map<String, Int>?) : TestingSealed
data class HasMultipleOwnParams(val p1: OwnClass, val p2: OwnClass?) : TestingSealed
data class HasNestedGeneric(val nested: List<Pair<String, Int?>>) : TestingSealed
data class HasSomeNullables(
    val myint: Int,
    val myintopt: Int?,
    val uintnotoptional: UInt,
    val uintoptional: UInt?,
    val mybool: Boolean,
    val optbool: Boolean?,
) : TestingSealed
data class HasOtherNullables(
    val mystring: String,
    val optstring: String?,
    val myfloat: Float,
    val optfloat: Float?,
    val mydouble: Double,
    val optdouble: Double?,
) : TestingSealed
data class HasOwnClass(val ownClass: OwnClass) : TestingSealed
data class HasOwnClassWithGeneric(val ownClassWithGeneric: OwnHasGeneric<String>) : TestingSealed
data class HasOwnClassWithGenericAny(val ownClassWithGeneric: OwnHasGeneric<Any>) : TestingSealed
data class HasOwnClassWithGenericEnum(val ownClassWithGeneric: OwnHasGeneric<Any>) : TestingSealed
data class HasOwnClassWithGenericInnerMap(val ownClassWithGeneric: OwnHasGeneric<Map<String, Int?>>) :
    TestingSealed
data class HasOwnClassWithGenericInnerPair(
    val ownClassWithGeneric: OwnHasGeneric<Pair<String, Int?>>,
) : TestingSealed
data class HasOwnClassWithGenericInnerSet(val ownClassWithGeneric: OwnHasGeneric<Set<Int?>>) :
    TestingSealed
data class HasOwnClassWithGenericNested(val ownClassWithGeneric: OwnHasGeneric<OwnHasGeneric<Int>>) :
    TestingSealed
data class HasOwnClassWithGenericNullable(val ownClassWithGeneric: OwnHasGeneric<UInt?>) :
    TestingSealed
data class HasOwnClassWithGenericThrowable(val ownClassWithGeneric: OwnHasGeneric<Throwable>) :
    TestingSealed
data class HasOwnClassWithGenericWildcard(val ownClassWithGeneric: OwnHasGeneric<*>) : TestingSealed
data class HasPairGeneric(val pair: Pair<UByte, OwnClass?>) : TestingSealed
data class HasPairBool(val pair: Pair<Boolean, Boolean?>) : TestingSealed
data class HasPairString(val pair: Pair<String, String?>) : TestingSealed
data class HasPairFloat(val pair: Pair<Float, Float?>) : TestingSealed
data class HasSet(val myset: Set<OwnClass>) : TestingSealed
data class HasSetNullableOuter(val myset: Set<Int?>?) : TestingSealed
data class HasSetNullableInt(val myset: Set<Int?>) : TestingSealed
data class HasSetString(val myset: Set<String>) : TestingSealed
data class HasSetStringNullable(val myset: Set<String?>) : TestingSealed
data class HasThrowable(val throwable: Throwable) : TestingSealed
data class HasTriple(val triple: Triple<Float, Int?, OwnClass>) : TestingSealed {
    val anotherProp = "anotherProp"
}
object JustAnObj : TestingSealed

data class OwnClass(val whatever: String)

class OwnHasGeneric<T> {
    val value: T? = null
}

enum class OwnEnum {
    A, B, C
}

sealed class LoadingState<T, P : Any> {
    class Loading<T, P : Any> : LoadingState<T, P>()
    data class Success<T, P : Any>(val payload: T) : LoadingState<T, P>()
    data class Other<T, P : Any>(val otherPayload: P) : LoadingState<T, P>()
    data class ErrorOnLoad<T, P : Any>(val error: String) : LoadingState<T, P>()
}
