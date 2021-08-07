/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.feature

import dev.icerock.moko.kswift.plugin.buildTypeVariableNames
import dev.icerock.moko.kswift.plugin.context.ClassContext
import dev.icerock.moko.kswift.plugin.context.kLibClasses
import dev.icerock.moko.kswift.plugin.getDeclaredTypeNameWithGenerics
import dev.icerock.moko.kswift.plugin.getSimpleName
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.EnumerationCaseSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
import kotlinx.metadata.ClassName
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import java.util.Locale

class SealedToSwiftEnumFeature(
    filter: Filter<ClassContext>
) : ProcessorFeature<ClassContext>(filter) {
    override fun doProcess(featureContext: ClassContext, processorContext: ProcessorContext) {
        if (featureContext.clazz.sealedSubclasses.isEmpty()) return

        val kotlinFrameworkName: String = processorContext.framework.baseName
        val kmClass: KmClass = featureContext.clazz

        val sealedCases: List<EnumCase> = buildEnumCases(kotlinFrameworkName, featureContext)
        val typeVariables: List<TypeVariableName> =
            kmClass.buildTypeVariableNames(kotlinFrameworkName)

        val originalClassName: String = getSimpleName(kmClass.name, featureContext.kLibClasses)
        val className: String = originalClassName.replace(".", "").plus("Ks")
        val enumType: TypeSpec = TypeSpec.enumBuilder(className)
            .addDoc("selector: ${featureContext.prefixedUniqueId}")
            .apply {
                typeVariables.forEach { addTypeVariable(it) }
                sealedCases.forEach { addEnumCase(it.enumCaseSpec) }
            }
            .addModifiers(Modifier.PUBLIC)
            .addFunction(
                buildEnumConstructor(
                    featureContext = featureContext,
                    kotlinFrameworkName = kotlinFrameworkName,
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
        sealedCases: List<EnumCase>,
        className: String,
        originalClassName: String
    ): FunctionSpec {
        return FunctionSpec.builder("init")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(
                label = "_",
                name = "obj",
                type = featureContext.clazz.getDeclaredTypeNameWithGenerics(
                    kotlinFrameworkName = kotlinFrameworkName,
                    classes = featureContext.kLibClasses
                )
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

    private fun buildEnumCases(
        kotlinFrameworkName: String,
        featureContext: ClassContext
    ): List<EnumCase> {
        val kmClass = featureContext.clazz
        return kmClass.sealedSubclasses.map { sealedClassName ->
            val sealedClass: KmClass = featureContext.parentContext
                .fragment.classes.first { it.name == sealedClassName }
            buildEnumCase(kotlinFrameworkName, featureContext, sealedClassName, sealedClass)
        }
    }

    private fun buildEnumCase(
        kotlinFrameworkName: String,
        featureContext: ClassContext,
        subclassName: ClassName,
        sealedCaseClass: KmClass
    ): EnumCase {
        val kmClass = featureContext.clazz
        val name: String = if (subclassName.startsWith(kmClass.name)) {
            subclassName.removePrefix(kmClass.name).removePrefix(".")
        } else subclassName
        val decapitalizedName: String = name.decapitalize(Locale.ROOT)

        val isObject: Boolean = Flag.Class.IS_OBJECT(sealedCaseClass.flags)
        val caseArg = sealedCaseClass.getDeclaredTypeNameWithGenerics(
            kotlinFrameworkName = kotlinFrameworkName,
            classes = featureContext.kLibClasses
        )

        return EnumCase(
            name = decapitalizedName,
            param = if (isObject) null else caseArg,
            initCheck = if (isObject) {
                "obj is $caseArg"
            } else {
                "let obj = obj as? $caseArg"
            },
            initBlock = if (isObject) "" else "(obj)"
        )
    }

    data class EnumCase(
        val name: String,
        val param: TypeName?,
        val initCheck: String,
        val initBlock: String
    ) {
        val enumCaseSpec: EnumerationCaseSpec
            get() {
                return if (param == null) {
                    EnumerationCaseSpec.builder(name)
                } else {
                    EnumerationCaseSpec.builder(name, param)
                }.build()
            }
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
