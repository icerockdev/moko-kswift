/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import kotlinx.metadata.KmClass
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KmClassExtKtTest {
    private lateinit var classes: List<KmClass>

    @BeforeTest
    fun setup() {
        val klib = readKLib("mpp-library.klib")
        classes = klib.fragments.flatMap { it.classes }
    }

    @Test
    fun `simple class name`() {
        val output = getSimpleName(
            className = "com/icerockdev/library/TestViewModel",
            classes = classes
        )
        assertEquals(expected = "TestViewModel", actual = output)
    }

    @Test
    fun `nested class name`() {
        val output = getSimpleName(
            className = "com/icerockdev/library/TestViewModel.ScreenStateClass",
            classes = classes
        )
        assertEquals(expected = "TestViewModel.ScreenStateClass", actual = output)
    }

    @Test
    fun `nested sealed class name`() {
        val output = getSimpleName(
            className = "com/icerockdev/library/TestViewModel.ScreenStateClass.Authorized",
            classes = classes
        )
        assertEquals(expected = "TestViewModel.ScreenStateClassAuthorized", actual = output)
    }

    @Test
    fun `nested interface name`() {
        val output = getSimpleName(
            className = "com/icerockdev/library/TestViewModel.ScreenState",
            classes = classes
        )
        assertEquals(expected = "TestViewModelScreenState", actual = output)
    }

    @Test
    fun `nested interface sealed name`() {
        val output = getSimpleName(
            className = "com/icerockdev/library/TestViewModel.ScreenState.Authorized",
            classes = classes
        )
        assertEquals(expected = "TestViewModelScreenStateAuthorized", actual = output)
    }
}
