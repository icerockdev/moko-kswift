/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import kotlinx.metadata.Flag
import kotlinx.metadata.KmConstructor

fun KmConstructor.isPrimary(): Boolean = Flag.Constructor.IS_SECONDARY(flags).not()

fun List<KmConstructor>.getPrimaryConstructor(): KmConstructor = single { constructor -> constructor.isPrimary() }
