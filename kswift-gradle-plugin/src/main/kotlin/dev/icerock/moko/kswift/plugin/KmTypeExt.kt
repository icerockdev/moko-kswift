/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import io.outfoxx.swiftpoet.ANY_OBJECT
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.UINT64
import io.outfoxx.swiftpoet.VOID
import io.outfoxx.swiftpoet.parameterizedBy
import kotlinx.metadata.ClassName
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType

@Suppress("ReturnCount")
fun KmType.toTypeName(
    moduleName: String,
    isUsedInGenerics: Boolean = false,
    typeVariables: Map<Int, TypeVariableName> = emptyMap(),
    removeTypeVariables: Boolean = false
): TypeName {
    return when (val classifier = classifier) {
        is KmClassifier.TypeParameter -> {
            val typeVariable: TypeVariableName? = typeVariables[classifier.id]
            if (typeVariable != null) {
                return if (!removeTypeVariables) typeVariable
                else typeVariable.bounds.firstOrNull()?.type ?: ANY_OBJECT
            } else throw IllegalArgumentException("can't read type parameter $this without type variables list")
        }
        is KmClassifier.TypeAlias -> {
            classifier.name.kotlinTypeNameToSwift(moduleName, isUsedInGenerics)
                ?: throw IllegalArgumentException("can't read type alias $this")
        }
        is KmClassifier.Class -> {
            val name: TypeName? =
                classifier.name.kotlinTypeNameToSwift(moduleName, isUsedInGenerics)
            return name ?: kotlinTypeToTypeName(
                moduleName,
                classifier.name,
                typeVariables,
                removeTypeVariables
            )
        }
    }
}

fun String.kotlinTypeNameToSwift(moduleName: String, isUsedInGenerics: Boolean): TypeName? {
    return when (this) {
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
        "kotlin/ULong" -> UINT64
        "kotlin/Unit" -> VOID
        "kotlin/Any" -> ANY_OBJECT
        else -> {
            if (this.startsWith("platform/")) {
                val moduleAndClass: List<String> = this.split("/").drop(1)
                val module: String = moduleAndClass[0]
                val className: String = moduleAndClass[1]

                DeclaredTypeName.typeName(
                    listOf(module, className).joinToString(".")
                ).objcNameToSwift()
            } else null
        }
    }
}

fun KmType.kotlinTypeToTypeName(
    moduleName: String,
    classifierName: ClassName,
    typeVariables: Map<Int, TypeVariableName>,
    removeTypeVariables: Boolean
): TypeName {
    val typeName = DeclaredTypeName(
        moduleName = moduleName,
        simpleName = classifierName.split("/").last()
    )
    if (this.arguments.isEmpty()) return typeName

    return when (classifierName) {
        "kotlin/Function1" -> {
            val inputType: TypeName = arguments[0].type?.toTypeName(
                moduleName = moduleName,
                isUsedInGenerics = false,
                typeVariables = typeVariables,
                removeTypeVariables = removeTypeVariables
            )!!
            val outputType: TypeName = arguments[1].type?.toTypeName(
                moduleName = moduleName,
                isUsedInGenerics = false,
                typeVariables = typeVariables,
                removeTypeVariables = removeTypeVariables
            )!!
            FunctionTypeName.get(
                parameters = listOf(ParameterSpec.unnamed(inputType)),
                returnType = outputType
            )
        }
        else -> {
            val arguments: List<TypeName> = this.arguments.mapNotNull { typeProj ->
                typeProj.type?.toTypeName(
                    moduleName = moduleName,
                    isUsedInGenerics = true,
                    typeVariables = typeVariables,
                    removeTypeVariables = removeTypeVariables
                )
            }
            @Suppress("SpreadOperator")
            typeName.parameterizedBy(*arguments.toTypedArray())
        }
    }
}

fun DeclaredTypeName.objcNameToSwift(): DeclaredTypeName {
    return when (moduleName) {
        "Foundation" -> peerType(simpleName.removePrefix("NS"))
        else -> this
    }
}
