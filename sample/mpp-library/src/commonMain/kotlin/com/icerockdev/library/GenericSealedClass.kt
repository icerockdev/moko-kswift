/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

/**
 * This sealed class was created to test alongside the `NonGenericSealedClass` type to ensure that kswift
 * creates the correct enum types for generic sealed classes with multiple type parameters.
 */
sealed class GenericSealedClass<out T, out U> {
    /** A test `object` with no parameters and hard-coded types. */
    object WithoutProperty : GenericSealedClass<Nothing, Nothing>()

    /**
     * A test `data class` with one parameter, one generic type, and one hard-coded type. Hard-codes
     * the second type parameter (U).
     */
    data class WithOnePropertyT<T>(val value: T) : GenericSealedClass<T, Nothing>()

    /**
     * A test `data class` with one parameter, one generic type, and one hard-coded type. Hard-codes
     * the first type parameter (T).
     */
    data class WithOnePropertyU<U>(val value: U) : GenericSealedClass<Nothing, U>()

    /** A test `data class` with two parameters and two generic types. */
    data class WithTwoProperties<T, U>(val value1: T, val value2: U) : GenericSealedClass<T, U>()
}

class TestGenericSealedClass {
    val withoutProperty: GenericSealedClass<String, String> = GenericSealedClass.WithoutProperty
    val withOnePropertyT: GenericSealedClass<String, String> = GenericSealedClass.WithOnePropertyT("test")
    val withOnePropertyU: GenericSealedClass<String, String> = GenericSealedClass.WithOnePropertyU("test")
    val withTwoProperties: GenericSealedClass<String, String> = GenericSealedClass.WithTwoProperties("test1", "test2")
}
