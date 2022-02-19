/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.feature

import dev.icerock.moko.kswift.plugin.KSwiftRuntimeAnnotations
import dev.icerock.moko.kswift.plugin.context.PackageFunctionContext
import dev.icerock.moko.kswift.plugin.context.classes
import dev.icerock.moko.kswift.plugin.findByClassName
import dev.icerock.moko.kswift.plugin.getStringArgument
import dev.icerock.moko.kswift.plugin.objcNameToSwift
import dev.icerock.moko.kswift.plugin.toTypeName
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.AttributeSpec.Companion.ESCAPING
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
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

        val functionData: Data = Processing.read(
            context = featureContext,
            moduleName = kotlinFrameworkName
        ) ?: return

        val extensionSpec: ExtensionSpec = Processing.buildExtensionSpec(
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

    internal sealed interface PlatformClassTypeName {
        val typeName: DeclaredTypeName

        data class Normal(override val typeName: DeclaredTypeName) : PlatformClassTypeName

        data class Companion(override val typeName: DeclaredTypeName) : PlatformClassTypeName
    }

    internal data class Data(
        val classTypeName: DeclaredTypeName,
        val funcName: String,
        val typeVariables: List<TypeVariableName>,
        val funcParams: List<ParameterSpec>,
        val modifiers: List<Modifier>,
        val returnType: TypeName,
        val code: String
    )

    companion object : Factory<PackageFunctionContext, PlatformExtensionFunctionsFeature, Config> {
        override fun create(block: Config.() -> Unit): PlatformExtensionFunctionsFeature {
            val config = Config().apply(block)
            return PlatformExtensionFunctionsFeature(featureContext, config.filter)
        }

        override val featureContext: KClass<PackageFunctionContext> = PackageFunctionContext::class

        @JvmStatic
        override val factory = Companion
    }

    internal object Processing {

        fun buildExtensionSpec(functionData: Data, doc: String): ExtensionSpec {
            return ExtensionSpec.builder(functionData.classTypeName.objcNameToSwift())
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

            val fileName: String = func.file?.name ?: return null
            val swiftedClass: String = fileName.replace(".kt", "Kt")

            val typeVariables: Map<Int, TypeVariableName> = buildTypeVariables(context, moduleName)

            val classTypeName: PlatformClassTypeName = buildClassTypeName(
                type = receiver,
                context = context,
                moduleName = moduleName,
                typeVariables = typeVariables
            ) ?: return null

            val funcName: String = func.name

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
            context: PackageFunctionContext,
            moduleName: String,
            typeVariables: Map<Int, TypeVariableName>
        ): PlatformClassTypeName? {
            val isCompanion: Boolean = (type.classifier as? KmClassifier.Class)
                ?.name?.endsWith(".Companion") == true

            val typeName: TypeName = type.toTypeName(
                moduleName = moduleName,
                typeVariables = typeVariables,
                removeTypeVariables = true
            )

            val declaredTypeName: DeclaredTypeName = typeName as? DeclaredTypeName ?: return null

            if (declaredTypeName.moduleName != moduleName) {
                return if (isCompanion) PlatformClassTypeName.Companion(declaredTypeName)
                else PlatformClassTypeName.Normal(declaredTypeName)
            }

            // extensions for kotlin classes will be available out of box
            if (isCompanion) return null

            // generate extensions only for interfaces
            val className = (type.classifier as? KmClassifier.Class)?.name
                ?: return PlatformClassTypeName.Normal(declaredTypeName)

            val clazz: KmClass = context.classes.firstOrNull { it.name == className }
                ?: return PlatformClassTypeName.Normal(declaredTypeName)

            val isInterface: Boolean = Flag.Class.IS_INTERFACE(clazz.flags)
            return if (!isInterface) null
            else PlatformClassTypeName.Normal(declaredTypeName)
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

                        val isWithoutGenerics =
                            classes.firstOrNull { it.name == paramClassName }?.let {
                                Flag.Class.IS_INTERFACE(it.flags)
                            } ?: false

                        if (isWithoutGenerics) type.rawType
                        else type
                    }
                    else -> type
                }

                ParameterSpec.builder(parameterName = param.name, type = usedType)
                    .apply { if (type is FunctionTypeName) addAttribute(ESCAPING) }
                    .build()
            }
        }

        private fun buildFunctionParameterName(param: KmValueParameter): String {
            return param.annotations
                .findByClassName(KSwiftRuntimeAnnotations.KSWIFT_OVERRIDE_NAME)
                ?.getStringArgument("newParamName") ?: param.name
        }
    }
}
