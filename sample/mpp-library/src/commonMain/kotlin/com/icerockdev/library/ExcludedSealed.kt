/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import dev.icerock.moko.kswift.KSwiftExclude

@KSwiftExclude
sealed interface ExcludedSealed {
    object V1 : ExcludedSealed
    object V2 : ExcludedSealed
}
