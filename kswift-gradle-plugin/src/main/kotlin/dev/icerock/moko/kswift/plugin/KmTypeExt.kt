/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import io.outfoxx.swiftpoet.ANY_OBJECT
import io.outfoxx.swiftpoet.ARRAY
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.DICTIONARY
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.INT32
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.SET
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.UINT64
import io.outfoxx.swiftpoet.VOID
import io.outfoxx.swiftpoet.parameterizedBy
import kotlinx.metadata.ClassName
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeProjection

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
            classifier.name.kotlinTypeNameToSwift(moduleName, isUsedInGenerics, arguments)
                ?: throw IllegalArgumentException("can't read type alias $this")
        }
        is KmClassifier.Class -> {
            val name: TypeName? =
                classifier.name.kotlinTypeNameToSwift(moduleName, isUsedInGenerics, arguments)
            return name ?: kotlinTypeToTypeName(
                moduleName,
                classifier.name,
                typeVariables,
                removeTypeVariables
            )
        }
    }
}

@Suppress("LongMethod", "ComplexMethod")
fun String.kotlinTypeNameToSwift(
    moduleName: String,
    isUsedInGenerics: Boolean,
    arguments: MutableList<KmTypeProjection>
): TypeName? {
    return when (this) {
        "kotlin/String" -> if (isUsedInGenerics) {
            DeclaredTypeName(moduleName = "Foundation", simpleName = "NSString")
        } else {
            STRING
        }
        "kotlin/Int" -> if (isUsedInGenerics) {
            DeclaredTypeName(moduleName = moduleName, simpleName = "KotlinInt")
        } else {
            INT32
        }
        "kotlin/Boolean" -> if (isUsedInGenerics) {
            DeclaredTypeName(moduleName = moduleName, simpleName = "KotlinBoolean")
        } else {
            BOOL
        }
        "kotlin/ULong" -> UINT64
        "kotlin/Unit" -> VOID
        "kotlin/Any" -> ANY_OBJECT
        "kotlin/collections/List" -> {
            arguments.first().type?.run {
                DeclaredTypeName.typeName(ARRAY.name).parameterizedBy(
                    this.toTypeName(
                        moduleName,
                        isUsedInGenerics = this.shouldUseKotlinTypeWhenHandlingCollections()
                    )
                )
            }
        }
        "kotlin/collections/Set" -> {
            arguments.first().type?.run {
                DeclaredTypeName.typeName(SET.name).parameterizedBy(
                    this.toTypeName(
                        moduleName,
                        isUsedInGenerics = this.shouldUseKotlinTypeWhenHandlingCollections()
                    )
                )
            }
        }
        "kotlin/collections/Map" -> {
            val firstArgumentType = arguments.first().type
            val secondArgumentType = arguments[1].type
            if (firstArgumentType != null && secondArgumentType != null) {
                DeclaredTypeName.typeName(DICTIONARY.name).parameterizedBy(
                    firstArgumentType.toTypeName(
                        moduleName,
                        isUsedInGenerics = firstArgumentType.shouldUseKotlinTypeWhenHandlingCollections()
                    ),
                    secondArgumentType.toTypeName(
                        moduleName,
                        isUsedInGenerics = secondArgumentType.shouldUseKotlinTypeWhenHandlingCollections()
                    )
                )
            } else {
                null
            }
        }
        else -> {
            if (this.startsWith("platform/")) {
                val withoutCompanion: String = this.removeSuffix(".Companion")
                val moduleAndClass: List<String> = withoutCompanion.split("/").drop(1)
                val module: String = moduleAndClass[0]
                val className: String = moduleAndClass[1]

                DeclaredTypeName.typeName(
                    listOf(module, className).joinToString(".")
                ).objcNameToSwift()
            } else if (this.startsWith("kotlin/Function")) {
                null
            } else if (this.startsWith("kotlin/") && this.count { it == '/' } == 1) {
                DeclaredTypeName(
                    moduleName = moduleName,
                    simpleName = "Kotlin" + this.split("/").last()
                )
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

    @Suppress("UnsafeCallOnNullableType")
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

fun KmType.shouldUseKotlinTypeWhenHandlingOptionalType(): Boolean =
    if (classifier.toString().contains("kotlin/String")) {
        false
    } else {
        Flag.Type.IS_NULLABLE(flags)
    }

fun KmType.shouldUseKotlinTypeWhenHandlingCollections(): Boolean =
    !classifier.toString().contains("kotlin/String")
