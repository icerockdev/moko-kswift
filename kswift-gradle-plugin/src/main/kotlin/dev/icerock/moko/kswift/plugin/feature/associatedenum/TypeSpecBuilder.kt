package dev.icerock.moko.kswift.plugin.feature.associatedenum

import dev.icerock.moko.kswift.plugin.context.ClassContext
import dev.icerock.moko.kswift.plugin.context.kLibClasses
import dev.icerock.moko.kswift.plugin.getDeclaredTypeNameWithGenerics
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
import org.gradle.configurationcache.extensions.capitalized

@Suppress("LongParameterList")
fun buildTypeSpec(
    featureContext: ClassContext,
    typeVariables: List<TypeVariableName>,
    sealedCases: List<AssociatedEnumCase>,
    kotlinFrameworkName: String,
    originalClassName: String,
): TypeSpec {
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
                originalClassName = originalClassName,
            ),
        )
        .addProperty(
            buildSealedProperty(
                featureContext = featureContext,
                kotlinFrameworkName = kotlinFrameworkName,
                sealedCases = sealedCases,
            ),
        )
        .build()
    return enumType
}

private fun buildEnumConstructor(
    featureContext: ClassContext,
    kotlinFrameworkName: String,
    sealedCases: List<AssociatedEnumCase>,
    className: String,
    originalClassName: String,
): FunctionSpec {
    return FunctionSpec.builder("init")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(
            label = "_",
            name = "obj",
            type = featureContext.clazz.getDeclaredTypeNameWithGenerics(
                kotlinFrameworkName = kotlinFrameworkName,
                classes = featureContext.kLibClasses,
            ),
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
                    add("fatalError(\"$className not synchronized with $originalClassName class\")\n")
                    unindent()
                    add("}\n")
                }
                .build(),
        )
        .build()
}

private fun buildSealedProperty(
    featureContext: ClassContext,
    kotlinFrameworkName: String,
    sealedCases: List<AssociatedEnumCase>,
): PropertySpec {
    val returnType: TypeName = featureContext.clazz.getDeclaredTypeNameWithGenerics(
        kotlinFrameworkName = kotlinFrameworkName,
        classes = featureContext.kLibClasses,
    )
    return PropertySpec.builder("sealed", type = returnType)
        .addModifiers(Modifier.PUBLIC)
        .getter(
            FunctionSpec
                .getterBuilder()
                .addCode(buildSealedPropertyBody(sealedCases))
                .build(),
        ).build()
}

private fun buildSealedPropertyBody(sealedCases: List<AssociatedEnumCase>): CodeBlock = CodeBlock
    .builder().apply {
        add("switch self {\n")
        sealedCases.forEach { enumCase ->
            buildString {
                append("case .")
                append(enumCase.name)
                append(enumCase.caseBlock)
                append(":\n")
            }.also { add(it) }
            indent()
            addSealedCaseReturnCode(enumCase)
            unindent()
        }
        add("}\n")
    }
    .build()

private fun CodeBlock.Builder.addSealedCaseReturnCode(enumCase: AssociatedEnumCase) {
    val parameters = enumCase.swiftToKotlinConstructor
    add("return ${enumCase.name.capitalized()}($parameters)\n")
}
