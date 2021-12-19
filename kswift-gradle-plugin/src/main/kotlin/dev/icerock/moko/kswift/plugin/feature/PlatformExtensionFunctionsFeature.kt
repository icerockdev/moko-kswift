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
import io.outfoxx.swiftpoet.AttributeSpec.Companion.ESCAPING
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmType
import kotlinx.metadata.KmValueParameter
import kotlinx.metadata.klib.annotations
import kotlinx.metadata.klib.file
import kotlin.reflect.KClass

class PlatformExtensionFunctionsFeature(
    override val featureContext: KClass<PackageFunctionContext>,
    override val filter: Filter<PackageFunctionContext>
) : ProcessorFeature<PackageFunctionContext>() {
    @Suppress("ReturnCount", "LongMethod")
    override fun doProcess(
        featureContext: PackageFunctionContext,
        processorContext: ProcessorContext
    ) {
        val func: KmFunction = featureContext.func
        val kotlinFrameworkName: String = processorContext.framework.baseName
        val classes: List<KmClass> = featureContext.classes

        val receiver = func.receiverParameterType ?: return

        val classifier = receiver.classifier
        val classTypeName = buildClassTypeName(classifier) ?: return

        val funcName: String = func.name
        val fileName: String = func.file?.name ?: return
        val swiftedClass: String = fileName.replace(".kt", "Kt")

        val funcParams: List<ParameterSpec> =
            buildFunctionParameters(featureContext, kotlinFrameworkName, classes)

        val callParams: List<String> = func.valueParameters.mapIndexed { index, param ->
            buildString {
                append(buildFunctionParameterName(param))
                append(": ")
                append(param.name)

                if (param.type?.classifier is KmClassifier.TypeParameter) {
                    append(" as Any")
                }
                val parameterSpec: ParameterSpec = funcParams[index]
                val parameterType: TypeName = parameterSpec.type
                if (parameterType is FunctionTypeName) {
                    val lambdaCast = buildLambdaCast(parameterType)
                    if (lambdaCast != null) {
                        append(" as! $lambdaCast")
                    }
                }
            }
        }
        val callParamsLine: String = listOf("self").plus(callParams).joinToString(", ")

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
                    .addTypeVariables(
                        func.typeParameters.map { typeParameter ->
                            TypeVariableName.typeVariable(typeParameter.name)
                        }
                    )
                    .returns(
                        func.returnType.toTypeName(
                            moduleName = kotlinFrameworkName,
                            typeParameters = func.typeParameters,
                            classes = classes
                        )
                    )
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

    private fun buildLambdaCast(parameterType: FunctionTypeName): String? {
        val hasParamsWithGenerics = parameterType.parameters.any { it.type is TypeVariableName }
        val hasResultGeneric = parameterType.returnType is TypeVariableName

        if (!hasParamsWithGenerics && !hasResultGeneric) return null

        val params: List<String> = parameterType.parameters.map { parameter ->
            if (parameter.type is TypeVariableName) "Any"
            else parameter.type.toString()
        }
        val result: String = if (hasResultGeneric) "Any" else parameterType.returnType.toString()

        return buildString {
            append("(")
            append(params.joinToString())
            append(") -> ")
            append(result)
        }
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

        val swiftedName: String = if (frameworkName == "Foundation") simpleName.removePrefix("NS")
        else simpleName

        val declaredTypeName =
            DeclaredTypeName(moduleName = frameworkName, simpleName = swiftedName)

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
        kotlinFrameworkName: String,
        classes: List<KmClass>
    ): List<ParameterSpec> {
        val func: KmFunction = featureContext.func
        return func.valueParameters.map { param ->
            val paramType: KmType = param.type
                ?: throw IllegalArgumentException("extension ${func.name} have null type for $param")
            val type = paramType.toTypeName(
                moduleName = kotlinFrameworkName,
                typeParameters = func.typeParameters,
                classes = classes
            )

            ParameterSpec.builder(parameterName = param.name, type = type)
                .apply { if (type is FunctionTypeName) addAttribute(ESCAPING) }
                .build()
        }
    }

    private fun buildFunctionParameterName(param: KmValueParameter): String {
        return param.annotations
            .findByClassName(KSwiftRuntimeAnnotations.KSWIFT_OVERRIDE_NAME)
            ?.getStringArgument("newParamName") ?: param.name
    }

    class Config : BaseConfig<PackageFunctionContext> {
        override var filter: Filter<PackageFunctionContext> = Filter.Exclude(emptySet())
    }

    companion object : Factory<PackageFunctionContext, PlatformExtensionFunctionsFeature, Config> {
        override fun create(block: Config.() -> Unit): PlatformExtensionFunctionsFeature {
            val config = Config().apply(block)
            return PlatformExtensionFunctionsFeature(featureContext, config.filter)
        }

        override val featureContext: KClass<PackageFunctionContext> = PackageFunctionContext::class

        private const val PLATFORM_CLASS_PARTS_COUNT = 3

        @JvmStatic
        override val factory = Companion
    }
}
