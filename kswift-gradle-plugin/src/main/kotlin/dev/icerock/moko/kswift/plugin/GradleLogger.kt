/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger

class GradleLogger(
    private val logger: Logger
) : org.jetbrains.kotlin.util.Logger {
    override fun error(message: String) {
        logger.error(message)
    }

    override fun fatal(message: String): Nothing {
        logger.error(message)
        throw GradleException(message)
    }

    override fun log(message: String) {
        logger.lifecycle(message)
    }

    override fun warning(message: String) {
        logger.warn(message)
    }
}
