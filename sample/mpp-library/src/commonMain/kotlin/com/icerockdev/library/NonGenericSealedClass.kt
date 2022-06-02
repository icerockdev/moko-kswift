/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

/**
 * This sealed class was created to test alongside the `GenericSealedClass<T, U>` type to ensure that
 * kswift creates the correct enum types for both generic and non-generic sealed classes.
 */
sealed class NonGenericSealedClass {
    /** A test `object` that has no property. */
    object WithoutProperty : NonGenericSealedClass()

    /** A test `data class` with an associated value. */
    data class WithProperty(val value: String) : NonGenericSealedClass()
}
