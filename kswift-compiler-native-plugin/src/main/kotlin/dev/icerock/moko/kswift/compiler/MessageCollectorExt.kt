/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.compiler

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

@Suppress("NOTHING_TO_INLINE")
internal inline fun MessageCollector.debug(message: String) {
    report(CompilerMessageSeverity.WARNING, message)
}
