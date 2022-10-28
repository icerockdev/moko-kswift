/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

/**
 * This sealed class was created to test alongside the [ExternalGenericSealedClass] type to ensure that
 * kswift creates the correct enum types for both generic and non-generic sealed classes.
 * Here, subclasses are defined out of the body of the parent class.
 * See [NonGenericSealedClass] to see an example of nested subclasses
 */
sealed class ExternalNonGenericSealedClass

/** A test `object` that has no property. */
object ExternalNonGenericWithoutProperty : ExternalNonGenericSealedClass()

/** A test `data class` with an associated value. */
data class ExternalNonGenericWithProperty(val value: String) : ExternalNonGenericSealedClass()
