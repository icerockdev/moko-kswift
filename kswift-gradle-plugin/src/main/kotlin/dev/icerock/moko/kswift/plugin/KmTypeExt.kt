/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import io.outfoxx.swiftpoet.ANY_OBJECT
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.ParameterizedTypeName
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.VOID
import io.outfoxx.swiftpoet.parameterizedBy
import kotlinx.metadata.ClassName
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeParameter

@Suppress("ComplexMethod", "LongMethod")
fun KmType.toTypeName(
    moduleName: String,
    isUsedInGenerics: Boolean = false,
    typeParameters: List<KmTypeParameter>,
    classes: List<KmClass>
): TypeName {
    val className: ClassName = when (val cls = classifier) {
        is KmClassifier.TypeAlias -> cls.name
        is KmClassifier.Class -> cls.name
        is KmClassifier.TypeParameter -> {
            val typeParameter: KmTypeParameter = typeParameters[cls.id]
            return TypeVariableName.typeVariable(typeParameter.name)
        }
    }

    val type: TypeName = when (className) {
        "kotlin/String" -> if (isUsedInGenerics) {
            DeclaredTypeName(moduleName = "Foundation", simpleName = "NSString")
        } else {
            STRING
        }
        "kotlin/Int" -> DeclaredTypeName(moduleName = "Foundation", simpleName = "NSNumber")
        "kotlin/Boolean" -> if (isUsedInGenerics) {
            DeclaredTypeName(moduleName = moduleName, simpleName = "KotlinBoolean")
        } else {
            BOOL
        }
        "kotlin/Unit" -> VOID
        "kotlin/Function1" -> {
            val inputType: TypeName = arguments[0].type?.toTypeName(
                moduleName = moduleName,
                isUsedInGenerics = false,
                typeParameters = typeParameters,
                classes = classes
            )!!
            val outputType: TypeName = arguments[1].type?.toTypeName(
                moduleName = moduleName,
                isUsedInGenerics = false,
                typeParameters = typeParameters,
                classes = classes
            )!!
            FunctionTypeName.get(
                parameters = listOf(ParameterSpec.unnamed(inputType)),
                returnType = outputType
            )
        }
        "kotlin/Any" -> ANY_OBJECT
        else -> {
            if (className.startsWith("platform/")) {
                DeclaredTypeName.typeName(
                    className.split("/").drop(1).joinToString(".")
                ).toSwift()
            } else {
                kotlinTypeToTypeName(
                    moduleName = moduleName,
                    classifierName = className,
                    typeParameters = typeParameters,
                    classes = classes
                )
            }
        }
    }

    val isWithoutGenerics = classes.firstOrNull { it.name == className }?.let {
        Flag.Class.IS_INTERFACE(it.flags)
    } ?: false

    val usedType = if (isWithoutGenerics && type is ParameterizedTypeName) {
        type.rawType
    } else {
        type
    }

    return usedType
}

fun KmType.kotlinTypeToTypeName(
    moduleName: String,
    classifierName: ClassName,
    typeParameters: List<KmTypeParameter>,
    classes: List<KmClass>
): TypeName {
    val typeName = DeclaredTypeName(
        moduleName = moduleName,
        simpleName = classifierName.split("/").last()
    )
    if (this.arguments.isEmpty()) return typeName

    val arguments: List<TypeName> = this.arguments.mapNotNull { typeProj ->
        typeProj.type?.toTypeName(
            moduleName = moduleName,
            isUsedInGenerics = true,
            typeParameters = typeParameters,
            classes = classes
        )
    }
    @Suppress("SpreadOperator")
    return typeName.parameterizedBy(*arguments.toTypedArray())
}

fun DeclaredTypeName.toSwift(): DeclaredTypeName {
    return when {
        moduleName == "Foundation" && simpleName == "NSBundle" -> peerType("Bundle")
        else -> this
    }
}
