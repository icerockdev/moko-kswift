/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.compiler

import com.google.auto.service.AutoService
import com.intellij.mock.MockProject
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

@AutoService(ComponentRegistrar::class)
open class FlowGenericsComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        val messageCollector: MessageCollector =
            configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        IrGenerationExtension.registerExtension(
            project,
            FlowGenericsIrGenerationExtension(messageCollector)
        )
        SyntheticResolveExtension.registerExtension(
            project,
            FlowGenericsSyntheticsExtension(messageCollector)
        )
    }
}
