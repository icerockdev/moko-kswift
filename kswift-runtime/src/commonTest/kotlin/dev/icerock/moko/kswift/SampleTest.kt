/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift

import kotlin.test.Test
import kotlin.test.assertEquals

class SampleTest {
    @Test
    fun `equality test`() {
        assertEquals(expected = 4, actual = multiply(a = 2, b = 2))
    }
}
