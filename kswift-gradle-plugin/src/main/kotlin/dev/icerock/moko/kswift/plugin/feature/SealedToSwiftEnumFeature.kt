/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.feature

import dev.icerock.moko.kswift.plugin.ProcessorContext
import dev.icerock.moko.kswift.plugin.ProcessorFeature
import dev.icerock.moko.kswift.plugin.context.ClassContext
import dev.icerock.moko.kswift.plugin.toTypeName
import io.outfoxx.swiftpoet.ANY_OBJECT
import io.outfoxx.swiftpoet.AttributeSpec
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.EnumerationCaseSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.parameterizedBy
import kotlinx.metadata.ClassName
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import java.util.Locale

class SealedToSwiftEnumFeature(
    filter: Filter<ClassContext>
) : ProcessorFeature<ClassContext>(filter) {
    override fun doProcess(featureContext: ClassContext, processorContext: ProcessorContext) {
        if (featureContext.clazz.sealedSubclasses.isEmpty()) return

        val kotlinFrameworkName: String = processorContext.framework.baseName
        val kmclass: KmClass = featureContext.clazz

        val isSealedInterface: Boolean = if (Flag.Class.IS_CLASS(kmclass.flags)) {
            false
        } else if (Flag.Class.IS_INTERFACE(kmclass.flags)) {
            true
        } else {
            println("${kmclass.name} not class or interface - skip ${kmclass.flags}")
            return
        }
        val sealedCases: List<EnumCase> = buildEnumCases(featureContext)
        val typeVariables: List<TypeVariableName> =
            buildTypeVariableNames(kmclass, kotlinFrameworkName)

        val originalClassName: String = kmclass.name.getSimpleName(featureContext)
        val className: String = originalClassName.replace(".", "").plus("Ks")
        val enumType: TypeSpec = TypeSpec.enumBuilder(className)
            .addDoc("selector: ${featureContext.prefixedUniqueId}")
            .apply {
                typeVariables.forEach { addTypeVariable(it) }
                sealedCases.forEach { addEnumCase(it.enumCaseSpec) }
            }
            .addFunction(
                buildEnumConstructor(
                    featureContext = featureContext,
                    kotlinFrameworkName = kotlinFrameworkName,
                    typeVariables = typeVariables,
                    isSealedInterface = isSealedInterface,
                    sealedCases = sealedCases,
                    className = className,
                    originalClassName = originalClassName
                )
            )
            .build()

        processorContext.fileSpecBuilder.addType(enumType)
    }

    private fun buildEnumConstructor(
        featureContext: ClassContext,
        kotlinFrameworkName: String,
        typeVariables: List<TypeVariableName>,
        isSealedInterface: Boolean,
        sealedCases: List<EnumCase>,
        className: String,
        originalClassName: String
    ): FunctionSpec {
        val kmclass = featureContext.clazz
        return FunctionSpec.builder("init")
            .addParameter(
                label = "_",
                name = "obj",
                type = kmclass.getDeclaredTypeName(kotlinFrameworkName, featureContext)
                    .let { type ->
                        if (typeVariables.isEmpty() || isSealedInterface) type
                        else type.parameterizedBy(*typeVariables.toTypedArray())
                    }
            )
            .addCode(
                CodeBlock.builder()
                    .apply {
                        sealedCases.forEachIndexed { index, enumCase ->
                            buildString {
                                if (index != 0) append("} else ")
                                append("if ")
                                append(enumCase.initCheck)
                                append(" {")
                                append('\n')
                            }.also { add(it) }
                            indent()
                            buildString {
                                append("self = .")
                                append(enumCase.name)
                                append(enumCase.initBlock)
                                append('\n')
                            }.also { add(it) }
                            unindent()
                        }
                        add("} else {\n")
                        indent()
                        add("fatalError(\"$className not syncronized with $originalClassName class\")\n")
                        unindent()
                        add("}\n")
                    }
                    .build()
            )
            .build()
    }

    private fun buildEnumCases(featureContext: ClassContext): List<EnumCase> {
        val kmclass = featureContext.clazz
        return kmclass.sealedSubclasses.map { sealedClassName ->
            val sealedClass: KmClass = featureContext.parentContext
                .fragment.classes.first { it.name == sealedClassName }
            buildEnumCase(featureContext, sealedClassName, sealedClass)
        }
    }

    private fun buildEnumCase(
        featureContext: ClassContext,
        subclassName: ClassName,
        sealedClass: KmClass
    ): EnumCase {
        val kmclass = featureContext.clazz
        val enumCaseClassName: String = subclassName.getSimpleName(featureContext)
        val name: String = if (subclassName.startsWith(kmclass.name)) {
            subclassName.removePrefix(kmclass.name).removePrefix(".")
        } else subclassName
        val decapitalizedName: String = name.decapitalize(Locale.ROOT)

        val types: String = sealedClass.typeParameters.map {
            it.name
        }.ifNotEmpty { joinToString(",", "<", ">") }.orEmpty()

        return EnumCase(
            name = decapitalizedName,
            params = emptyList(),
            initCheck = "obj is $enumCaseClassName$types",
            initBlock = ""
        )
    }

    private fun buildTypeVariableNames(
        kmclass: KmClass,
        kotlinFrameworkName: String
    ) = kmclass.typeParameters.map { typeParam ->
        val bounds: List<TypeVariableName.Bound> = typeParam.upperBounds
            .map { it.toTypeName(kotlinFrameworkName, isUsedInGenerics = true) }
            .map { TypeVariableName.Bound(it) }
            .ifEmpty { listOf(TypeVariableName.Bound(ANY_OBJECT)) }
        TypeVariableName.typeVariable(typeParam.name, bounds)
    }

    private fun KmClass.getDeclaredTypeName(
        kotlinFrameworkName: String,
        featureContext: ClassContext
    ): DeclaredTypeName {
        return DeclaredTypeName(
            moduleName = kotlinFrameworkName,
            simpleName = name.getSimpleName(featureContext)
        )
    }

    private fun String.getSimpleName(featureContext: ClassContext): String {
        return dev.icerock.moko.kswift.plugin.getSimpleName(
            className = this,
            classes = featureContext.parentContext.fragment.classes
        )
    }

    data class EnumCase(
        val name: String,
        val params: List<DeclaredTypeName>,
        val initCheck: String,
        val initBlock: String
    ) {
        val enumCaseSpec: EnumerationCaseSpec
            get() = EnumerationCaseSpec.builder(name)
                .apply {
                    params.forEach { addAttribute(AttributeSpec.builder(it).build()) }
                }
                .build()
    }

    class Config {
        var filter: Filter<ClassContext> = Filter.Exclude(emptySet())
    }

    companion object : Factory<ClassContext, SealedToSwiftEnumFeature, Config> {
        override fun create(block: Config.() -> Unit): SealedToSwiftEnumFeature {
            val config = Config().apply(block)
            return SealedToSwiftEnumFeature(config.filter)
        }
    }
}
