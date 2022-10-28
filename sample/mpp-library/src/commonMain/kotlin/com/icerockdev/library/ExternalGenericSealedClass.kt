/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

/**
 * This sealed class was created to test alongside the [ExternalNonGenericSealedClass] type to ensure that kswift
 * creates the correct enum types for generic sealed classes with multiple type parameters.
 * Here, subclasses are defined out of the body of the parent class.
 * See [GenericSealedClass] to see an example of nested subclasses
 */
sealed class ExternalGenericSealedClass<out T, out U>

/** A test `object` with no parameters and hard-coded types. */
object ExternalGenericWithoutProperty : ExternalGenericSealedClass<Nothing, Nothing>()

/**
 * A test `data class` with one parameter, one generic type, and one hard-coded type. Hard-codes
 * the second type parameter (U).
 */
data class ExternalGenericWithOnePropertyT<T>(val value: T) :
    ExternalGenericSealedClass<T, Nothing>()

/**
 * A test `data class` with one parameter, one generic type, and one hard-coded type. Hard-codes
 * the first type parameter (T).
 */
data class ExternalGenericWithOnePropertyU<U>(val value: U) :
    ExternalGenericSealedClass<Nothing, U>()

/** A test `data class` with two parameters and two generic types. */
data class ExternalGenericWithTwoProperties<T, U>(val value1: T, val value2: U) :
    ExternalGenericSealedClass<T, U>()

class TestExternalGenericSealedClass {
    val withoutProperty: ExternalGenericSealedClass<String, String> = ExternalGenericWithoutProperty
    val withOnePropertyT: ExternalGenericSealedClass<String, String> =
        ExternalGenericWithOnePropertyT("test")
    val withOnePropertyU: ExternalGenericSealedClass<String, String> =
        ExternalGenericWithOnePropertyU("test")
    val withTwoProperties: ExternalGenericSealedClass<String, String> =
        ExternalGenericWithTwoProperties("test1", "test2")
}
