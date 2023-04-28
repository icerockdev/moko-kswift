/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.feature

import dev.icerock.moko.kswift.plugin.buildTypeVariableNames
import dev.icerock.moko.kswift.plugin.context.ClassContext
import dev.icerock.moko.kswift.plugin.context.kLibClasses
import dev.icerock.moko.kswift.plugin.getDeclaredTypeNameWithGenerics
import dev.icerock.moko.kswift.plugin.getPrimaryConstructor
import dev.icerock.moko.kswift.plugin.isDataClass
import dev.icerock.moko.kswift.plugin.shouldUseKotlinTypeWhenHandlingOptionalType
import dev.icerock.moko.kswift.plugin.toTypeName
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import kotlin.reflect.KClass

/**
 * Creates an improved copy method for data classes
 */

class DataClassCopyFeature(
    override val featureContext: KClass<ClassContext>,
    override val filter: Filter<ClassContext>
) : ProcessorFeature<ClassContext>() {

    @Suppress("ReturnCount")
    override fun doProcess(featureContext: ClassContext, processorContext: ProcessorContext) {

        val kotlinFrameworkName: String = processorContext.framework.baseName
        val kmClass: KmClass = featureContext.clazz

        if (Flag.IS_PUBLIC(kmClass.flags).not()) return

        if (kmClass.isDataClass().not()) return

        val typeVariables: List<TypeVariableName> =
            kmClass.buildTypeVariableNames(kotlinFrameworkName)

        // doesn't support generic data classes right now
        if (typeVariables.isNotEmpty()) return

        val functionReturnType = kmClass.getDeclaredTypeNameWithGenerics(
            kotlinFrameworkName,
            featureContext.kLibClasses
        )

        val constructorParameters = kmClass.getPrimaryConstructor().valueParameters

        val functionParameters = constructorParameters.mapNotNull { parameter ->

            val parameterType = parameter.type ?: return@mapNotNull null

            ParameterSpec.builder(
                parameterName = parameter.name,
                type = FunctionTypeName.get(
                    parameters = emptyList(),
                    returnType = parameterType.toTypeName(
                        moduleName = kotlinFrameworkName,
                        isUsedInGenerics = parameterType.shouldUseKotlinTypeWhenHandlingOptionalType()
                    ).run {
                        if (Flag.Type.IS_NULLABLE(parameterType.flags)) {
                            makeOptional()
                        } else {
                            this
                        }
                    }
                ).makeOptional(),
            ).defaultValue(
                codeBlock = CodeBlock.builder().add("nil").build()
            ).build()
        }
        val functionBody = constructorParameters.joinToString(separator = ",") { property ->
            property.name.run {
                "$this: ($this != nil) ? $this!() : self.$this"
            }
        }

        val copyFunction = FunctionSpec.builder("copy")
            .addParameters(functionParameters)
            .returns(functionReturnType)
            .addCode(
                CodeBlock.builder()
                    .addStatement("return %T($functionBody)", functionReturnType)
                    .build()
            )
            .build()

        val extensionSpec =
            ExtensionSpec.builder(TypeSpec.classBuilder(functionReturnType.name).build())
                .addFunction(copyFunction)
                .build()

        processorContext.fileSpecBuilder.addExtension(extensionSpec)
    }

    class Config : BaseConfig<ClassContext> {
        override var filter: Filter<ClassContext> = Filter.Exclude(emptySet())
    }

    companion object : Factory<ClassContext, DataClassCopyFeature, Config> {
        override fun create(block: Config.() -> Unit): DataClassCopyFeature {
            val config = Config().apply(block)
            return DataClassCopyFeature(featureContext, config.filter)
        }

        override val featureContext: KClass<ClassContext> = ClassContext::class

        @JvmStatic
        override val factory = Companion
    }
}
