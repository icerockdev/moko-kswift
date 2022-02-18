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
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.ParameterizedTypeName
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.TypeVariableName.Bound.Constraint
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmType
import kotlinx.metadata.KmValueParameter
import kotlinx.metadata.KmVariance
import kotlinx.metadata.klib.annotations
import kotlinx.metadata.klib.file
import kotlin.reflect.KClass

class PlatformExtensionFunctionsFeature(
    override val featureContext: KClass<PackageFunctionContext>,
    override val filter: Filter<PackageFunctionContext>
) : ProcessorFeature<PackageFunctionContext>() {
    @Suppress("ReturnCount")
    override fun doProcess(
        featureContext: PackageFunctionContext,
        processorContext: ProcessorContext
    ) {
        val kotlinFrameworkName: String = processorContext.framework.baseName

        val functionData: PackageFunctionReader.Data =
            PackageFunctionReader.read(featureContext, kotlinFrameworkName) ?: return

        val extensionSpec: ExtensionSpec = PackageFunctionReader.buildExtensionSpec(
            functionData = functionData,
            doc = "selector: ${featureContext.prefixedUniqueId}"
        )

        val fileSpecBuilder: FileSpec.Builder = processorContext.fileSpecBuilder

        fileSpecBuilder.addImport(functionData.classTypeName.moduleName)
        fileSpecBuilder.addImport(kotlinFrameworkName)
        fileSpecBuilder.addExtension(extensionSpec)
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

        @JvmStatic
        override val factory = Companion
    }
}

internal object PackageFunctionReader {
    private const val PLATFORM_CLASS_PARTS_COUNT = 3

    fun buildExtensionSpec(functionData: Data, doc: String): ExtensionSpec {
        return ExtensionSpec.builder(functionData.classTypeName.toSwift())
            .addFunction(
                FunctionSpec.builder(functionData.funcName)
                    .addDoc(doc + "\n")
                    .addAttribute(AttributeSpec.DISCARDABLE_RESULT)
                    .addModifiers(Modifier.PUBLIC)
                    .addTypeVariables(functionData.typeVariables)
                    .addModifiers(functionData.modifiers)
                    .addParameters(functionData.funcParams)
                    .returns(functionData.returnType)
                    .addCode(functionData.code)
                    .build()
            )
            .addModifiers(Modifier.PUBLIC)
            .build()
    }

    @Suppress("ReturnCount")
    fun read(context: PackageFunctionContext, moduleName: String): Data? {
        val func: KmFunction = context.func
        val receiver: KmType = func.receiverParameterType ?: return null

        val typeVariables: Map<Int, TypeVariableName> = buildTypeVariables(context, moduleName)

        val classTypeName: PlatformClassTypeName = buildClassTypeName(
            type = receiver,
            moduleName = moduleName,
            typeVariables = typeVariables
        ) ?: return null

        val funcName: String = func.name
        val fileName: String = func.file?.name ?: return null
        val swiftedClass: String = fileName.replace(".kt", "Kt")

        val funcParams: List<ParameterSpec> = buildFunctionParameters(
            featureContext = context,
            kotlinFrameworkName = moduleName,
            typeVariables = typeVariables
        )

        val modifiers: List<Modifier> = if (classTypeName is PlatformClassTypeName.Companion) {
            listOf(Modifier.CLASS)
        } else emptyList()

        val callParams: List<String> = func.valueParameters.map { param ->
            buildString {
                append(buildFunctionParameterName(param))
                append(": ")
                append(param.name)

                val type: KmType? = param.type
                if (type?.arguments?.any { it.type?.classifier is KmClassifier.TypeParameter } == true) {
                    append(" as! ")
                    val fullTypeWithoutTypeParameter: TypeName = type.toTypeName(
                        moduleName = moduleName,
                        isUsedInGenerics = false,
                        typeVariables = typeVariables,
                        removeTypeVariables = true
                    )
                    append(fullTypeWithoutTypeParameter.toString())
                }
            }
        }
        val callParamsLine: String = listOf("self").plus(callParams).joinToString(", ")

        return Data(
            classTypeName = classTypeName.typeName,
            funcName = funcName,
            typeVariables = typeVariables.values.toList(),
            funcParams = funcParams,
            modifiers = modifiers,
            returnType = func.returnType.toTypeName(moduleName, typeVariables = typeVariables),
            code = "return $swiftedClass.$funcName($callParamsLine)\n"
        )
    }

    private fun buildTypeVariables(
        context: PackageFunctionContext,
        moduleName: String
    ): Map<Int, TypeVariableName> {
        val func: KmFunction = context.func

        val resultMap: MutableMap<Int, TypeVariableName> = mutableMapOf()

        func.typeParameters.forEach { typeParam ->
            when (typeParam.variance) {
                KmVariance.INVARIANT -> TypeVariableName.typeVariable(
                    typeParam.name,
                    typeParam.upperBounds.map { type ->
                        TypeVariableName.bound(
                            Constraint.CONFORMS_TO,
                            type.toTypeName(
                                moduleName = moduleName,
                                isUsedInGenerics = true,
                                typeVariables = resultMap
                            )
                        )
                    }
                ).also { resultMap[typeParam.id] = it }
                KmVariance.IN -> TODO()
                KmVariance.OUT -> TODO()
            }
        }

        return resultMap
    }

    @Suppress("ReturnCount")
    private fun buildClassTypeName(
        type: KmType,
        moduleName: String,
        typeVariables: Map<Int, TypeVariableName>
    ): PlatformClassTypeName? {
        val classifier: KmClassifier = type.classifier

        if (classifier is KmClassifier.Class) {
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

            val declaredTypeName = DeclaredTypeName(
                moduleName = frameworkName,
                simpleName = simpleName
            )

            return if (isCompanion) PlatformClassTypeName.Companion(declaredTypeName)
            else PlatformClassTypeName.Normal(declaredTypeName)
        }

        val typeName: TypeName = type.toTypeName(
            moduleName = moduleName,
            typeVariables = typeVariables,
            removeTypeVariables = true
        )

        val declaredTypeName: DeclaredTypeName? = typeName as? DeclaredTypeName
        return declaredTypeName?.let { PlatformClassTypeName.Normal(it) }
    }

    private fun buildFunctionParameters(
        featureContext: PackageFunctionContext,
        kotlinFrameworkName: String,
        typeVariables: Map<Int, TypeVariableName>
    ): List<ParameterSpec> {
        val func: KmFunction = featureContext.func
        val classes: List<KmClass> = featureContext.classes
        return func.valueParameters.map { param ->
            val paramType: KmType = param.type
                ?: throw IllegalArgumentException("extension ${func.name} have null type for $param")
            val type: TypeName = paramType.toTypeName(
                kotlinFrameworkName,
                typeVariables = typeVariables
            )

            val usedType: TypeName = when (type) {
                is ParameterizedTypeName -> {
                    val paramTypeClassifier = paramType.classifier
                    if (paramTypeClassifier !is KmClassifier.Class) {
                        throw IllegalArgumentException("extension ${func.name} have not class param $param")
                    }

                    val paramClassName = paramTypeClassifier.name

                    val isWithoutGenerics = classes.firstOrNull { it.name == paramClassName }?.let {
                        Flag.Class.IS_INTERFACE(it.flags)
                    } ?: false

                    if (isWithoutGenerics) type.rawType
                    else type
                }
                else -> type
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

    sealed interface PlatformClassTypeName {
        val typeName: DeclaredTypeName

        data class Normal(override val typeName: DeclaredTypeName) : PlatformClassTypeName

        data class Companion(override val typeName: DeclaredTypeName) : PlatformClassTypeName
    }

    data class Data(
        val classTypeName: DeclaredTypeName,
        val funcName: String,
        val typeVariables: List<TypeVariableName>,
        val funcParams: List<ParameterSpec>,
        val modifiers: List<Modifier>,
        val returnType: TypeName,
        val code: String
    )
}
