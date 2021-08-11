/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.feature

import dev.icerock.moko.kswift.plugin.KSwiftRuntimeAnnotations
import dev.icerock.moko.kswift.plugin.context.PackageFunctionContext
import dev.icerock.moko.kswift.plugin.context.classes
import dev.icerock.moko.kswift.plugin.findByClassName
import dev.icerock.moko.kswift.plugin.getStringArgument
import dev.icerock.moko.kswift.plugin.toSwift
import dev.icerock.moko.kswift.plugin.toTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.ParameterizedTypeName
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmType
import kotlinx.metadata.KmValueParameter
import kotlinx.metadata.klib.annotations
import kotlinx.metadata.klib.file

class PlatformExtensionFunctionsFeature(
    filter: Filter<PackageFunctionContext>
) : ProcessorFeature<PackageFunctionContext>(filter) {
    @Suppress("ReturnCount")
    override fun doProcess(
        featureContext: PackageFunctionContext,
        processorContext: ProcessorContext
    ) {
        val func: KmFunction = featureContext.func
        val kotlinFrameworkName: String = processorContext.framework.baseName

        val receiver = func.receiverParameterType ?: return

        val classifier = receiver.classifier
        val classTypeName = buildClassTypeName(classifier) ?: return

        val funcName: String = func.name
        val fileName: String = func.file?.name ?: return
        val swiftedClass: String = fileName.replace(".kt", "Kt")
        val callParams: List<String> = func.valueParameters.map { param ->
            buildString {
                append(buildFunctionParameterName(param))
                append(": ")
                append(param.name)
            }
        }
        val callParamsLine: String = listOf("self").plus(callParams).joinToString(", ")

        val funcParams: List<ParameterSpec> =
            buildFunctionParameters(featureContext, kotlinFrameworkName)

        val extensionSpec: ExtensionSpec = ExtensionSpec.builder(classTypeName.typeName.toSwift())
            .addFunction(
                FunctionSpec.builder(funcName)
                    .addDoc("selector: ${featureContext.prefixedUniqueId}")
                    .addModifiers(Modifier.PUBLIC)
                    .apply {
                        if (classTypeName is PlatformClassTypeName.Companion) {
                            addModifiers(Modifier.CLASS)
                        }
                    }
                    .addParameters(funcParams)
                    .returns(func.returnType.toTypeName(kotlinFrameworkName))
                    .addCode("return $swiftedClass.$funcName($callParamsLine)\n")
                    .build()
            )
            .addModifiers(Modifier.PUBLIC)
            .build()

        val fileSpecBuilder: FileSpec.Builder = processorContext.fileSpecBuilder

        fileSpecBuilder.addImport(classTypeName.typeName.moduleName)
        fileSpecBuilder.addImport(kotlinFrameworkName)
        fileSpecBuilder.addExtension(extensionSpec)
    }

    @Suppress("ReturnCount")
    private fun buildClassTypeName(classifier: KmClassifier): PlatformClassTypeName? {
        if (classifier !is KmClassifier.Class) return null

        val receiverName: String = classifier.name
        val receiverParts: List<String> = receiverName.split("/")
        if (receiverParts.size < PLATFORM_CLASS_PARTS_COUNT) return null
        if (receiverParts[0] != "platform") return null

        val frameworkName: String = receiverParts[1]
        val className: String = receiverParts[2]

        val (simpleName, isCompanion) = if (className.endsWith(".Companion")) {
            className.removeSuffix(".Companion") to true
        } else {
            className to false
        }

        val declaredTypeName = DeclaredTypeName(moduleName = frameworkName, simpleName = simpleName)

        return if (isCompanion) PlatformClassTypeName.Companion(declaredTypeName)
        else PlatformClassTypeName.Normal(declaredTypeName)
    }

    sealed interface PlatformClassTypeName {
        val typeName: DeclaredTypeName

        data class Normal(override val typeName: DeclaredTypeName) : PlatformClassTypeName

        data class Companion(override val typeName: DeclaredTypeName) : PlatformClassTypeName
    }

    private fun buildFunctionParameters(
        featureContext: PackageFunctionContext,
        kotlinFrameworkName: String
    ): List<ParameterSpec> {
        val func: KmFunction = featureContext.func
        val classes: List<KmClass> = featureContext.classes
        return func.valueParameters.map { param ->
            val paramType: KmType = param.type
                ?: throw IllegalArgumentException("extension ${func.name} have null type for $param")
            val type = paramType.toTypeName(kotlinFrameworkName)

            val paramTypeClassifier = paramType.classifier
            if (paramTypeClassifier !is KmClassifier.Class) {
                throw IllegalArgumentException("extension ${func.name} have not class param $param")
            }

            val paramClassName = paramTypeClassifier.name

            val isWithoutGenerics = classes.firstOrNull { it.name == paramClassName }?.let {
                Flag.Class.IS_INTERFACE(it.flags)
            } ?: false

            val usedType = if (isWithoutGenerics && type is ParameterizedTypeName) {
                type.rawType
            } else {
                type
            }

            ParameterSpec.builder(parameterName = param.name, type = usedType)
                .build()
        }
    }

    private fun buildFunctionParameterName(param: KmValueParameter): String {
        return param.annotations
            .findByClassName(KSwiftRuntimeAnnotations.KSWIFT_OVERRIDE_NAME)
            ?.getStringArgument("newParamName") ?: param.name
    }

    class Config {
        var filter: Filter<PackageFunctionContext> = Filter.Exclude(emptySet())
    }

    companion object : Factory<PackageFunctionContext, PlatformExtensionFunctionsFeature, Config> {
        override fun create(block: Config.() -> Unit): PlatformExtensionFunctionsFeature {
            val config = Config().apply(block)
            return PlatformExtensionFunctionsFeature(config.filter)
        }

        private const val PLATFORM_CLASS_PARTS_COUNT = 3
    }
}
