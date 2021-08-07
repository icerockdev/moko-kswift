/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift

@Retention(AnnotationRetention.BINARY)
annotation class KSwiftInclude

@Retention(AnnotationRetention.BINARY)
annotation class KSwiftExclude

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class KSwiftOverrideName(val newParamName: String)

/**
 * Annotation for type parameters that lost generics in ObjC (interfaces),
 * will be removed later when plugin will can detect by type that it's interface type
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class KSwiftWithoutGenerics
