/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.passTypeArgumentsFrom
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.properties

class FlowGenericsIrGenerationExtension(
    private val log: MessageCollector
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val properties: Sequence<IrProperty> = sequence {
            moduleFragment.files.forEach { searchGeneratedProperties(it) }
        }
        properties.forEach { generateForProperty(it, pluginContext) }
    }

    private suspend fun SequenceScope<IrProperty>.searchGeneratedProperties(
        file: IrFile
    ) {
        return file.declarations.forEach { searchGeneratedProperties(it) }
    }

    private suspend fun SequenceScope<IrProperty>.searchGeneratedProperties(
        declaration: IrDeclaration
    ) {
        when (declaration) {
            is IrProperty -> yield(declaration)
            is IrClass -> searchGeneratedProperties(declaration)
            else -> return
        }
    }

    private suspend fun SequenceScope<IrProperty>.searchGeneratedProperties(
        clazz: IrClass
    ) {
        clazz.declarations.forEach { searchGeneratedProperties(it) }
    }

    private fun generateForProperty(
        property: IrProperty,
        pluginContext: IrPluginContext
    ) {
        if (property.name.identifier.endsWith("Ks").not()) {
            return
        }

        val getter: IrSimpleFunction? = property.getter
        if (getter == null) {
            log.debug("skip $property - getter is null")
            return
        }

        val parentClass: IrClass? = property.parentClassOrNull
        if (parentClass == null) {
            log.debug("skip $property - class is null")
            return
        }

        val originalPropertyIdentifier: String = property.name.identifier.removeSuffix("Ks")
        val originalProperty: IrProperty? = parentClass.properties.firstOrNull {
            it.name.identifier == originalPropertyIdentifier
        }
        if (originalProperty == null) {
            log.debug("skip $property - original not found")
            return
        }
        val originalGetter = originalProperty.getter
        if (originalGetter == null) {
            log.debug("skip $property - original getter is null")
            return
        }

        getter.body = DeclarationIrBuilder(pluginContext, getter.symbol).irBlockBody {
            +irReturn(callOriginalFunction(getter, originalGetter))
        }
        log.debug("body for ${property.name} added")
    }

    private fun IrBuilderWithScope.callOriginalFunction(
        function: IrFunction,
        originalFunction: IrFunction
    ) = irCall(originalFunction).apply {
        dispatchReceiver = function.dispatchReceiverParameter?.let { irGet(it) }
        extensionReceiver = function.extensionReceiverParameter?.let { irGet(it) }
        passTypeArgumentsFrom(function)
        function.valueParameters.forEachIndexed { index, parameter ->
            putValueArgument(index, irGet(parameter))
        }
    }
}
