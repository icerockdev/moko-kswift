package dev.icerock.moko.kswift.plugin.feature.associatedenum

import io.outfoxx.swiftpoet.ARRAY
import io.outfoxx.swiftpoet.DICTIONARY
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.EnumerationCaseSpec
import io.outfoxx.swiftpoet.ParameterizedTypeName
import io.outfoxx.swiftpoet.SET
import io.outfoxx.swiftpoet.TupleTypeName
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.parameterizedBy
import kotlinx.metadata.KmTypeParameter
import kotlinx.metadata.KmValueParameter

private const val PAIR = 2
private const val TRIPLE = 3

data class AssociatedEnumCase(
    val frameworkName: String,
    val name: String,
    val param: TypeName?,
    val initCheck: String,
    val caseArg: TypeName,
    val isObject: Boolean,
    val constructorParams: List<KmValueParameter>,
    val typeParameters: List<KmTypeParameter>,
) {
    private val explodedParams: List<Pair<String, TypeName>> = constructorParams.map {
        Pair(
            it.name,
            it.type?.kotlinTypeToSwiftTypeName(frameworkName, typeParameters)
                ?: DeclaredTypeName.typeName("Swift.FailedToGetReturnType"),
        )
    }

    internal val initBlock: String = if (isObject) {
        ""
    } else {
        "("
            .plus(
                explodedParams.joinToString(",\n") {
                    val tupleType = it.second as? TupleTypeName
                    val paramType = (it.second as? ParameterizedTypeName)
                    when {
                        tupleType != null -> tupleType.generateTuple(it.first)
                        it.second.isCharacter -> {
                            "${it.first}: Character(UnicodeScalar(obj.${it.first})!)"
                        }
                        paramType?.rawType == DICTIONARY -> {
                            paramType.toDictionaryCaster(it.first)
                        }

                        paramType?.rawType == SET -> paramType.toSetCaster(it.first)
                        paramType?.rawType == ARRAY -> paramType.toArrayCaster(it.first)
                        paramType?.optional == true -> {
                            val unwrapped = paramType.unwrapOptional()
                            when ((unwrapped as? ParameterizedTypeName)?.rawType) {
                                DICTIONARY -> {
                                    unwrapped.toDictionaryCaster(it.first, true)
                                }
                                SET -> paramType.toSetCaster(it.first, true)
                                ARRAY -> paramType.toArrayCaster(it.first, true)
                                else -> paramType.generateInitParameter(it.first)
                            }
                        }

                        else -> it.second.generateInitParameter(it.first)
                    }
                },
            )
            .plus(")")
    }

    internal val caseBlock = if (isObject) {
        ""
    } else {
        "(" + explodedParams.joinToString { "let ${it.first}" } + ")"
    }

    internal val enumCaseSpec: EnumerationCaseSpec
        get() {
            return if (param == null) {
                EnumerationCaseSpec.builder(name).build()
            } else if (explodedParams.isNotEmpty()) {
                val stripGenericsFromObjC = explodedParams.map { param ->
                    (param.second as? ParameterizedTypeName)?.let {
                        if (it.rawType.moduleName != "Swift") {
                            param.first to it.rawType.parameterizedBy(
                                it.typeArguments.stripInnerGenerics(),
                            )
                        } else {
                            null
                        }
                    } ?: param
                }
                EnumerationCaseSpec.builder(
                    name = name,
                    type = TupleTypeName.of(stripGenericsFromObjC),
                ).build()
            } else {
                EnumerationCaseSpec.builder(name, param).build()
            }
        }

    internal val swiftToKotlinConstructor: String = explodedParams
        .joinToString { (paramName, paramType) ->
            "$paramName: " + when {
                paramType.isCharacter -> "$paramName.utf16.first!"
                paramType is TupleTypeName -> {
                    when (paramType.types.size) {
                        PAIR -> {
                            val first = paramType.types[0]
                            val firstType = first.second
                            val second = paramType.types[1]
                            val secondType = second.second

                            "KotlinPair<"
                                .plus(firstType.kotlinInteropTypeWithFallback.toNSString())
                                .plus(", ")
                                .plus(secondType.kotlinInteropTypeWithFallback.toNSString())
                                .plus(">(first: ")
                                .plus(
                                    firstType.generateKotlinConstructorIfNecessary("$paramName.0"),
                                )
                                .plus(", second: ")
                                .plus(
                                    secondType.generateKotlinConstructorIfNecessary("$paramName.1"),
                                )
                                .plus(")")
                        }
                        TRIPLE -> {
                            val first = paramType.types[0]
                            val firstType = first.second
                            val second = paramType.types[1]
                            val secondType = second.second
                            val third = paramType.types[2]
                            val thirdType = third.second
                            "KotlinTriple<"
                                .plus(firstType.kotlinInteropTypeWithFallback.toNSString())
                                .plus(", ")
                                .plus(secondType.kotlinInteropTypeWithFallback.toNSString())
                                .plus(", ")
                                .plus(thirdType.kotlinInteropTypeWithFallback.toNSString())
                                .plus(">(first: ")
                                .plus(
                                    firstType.generateKotlinConstructorIfNecessary("$paramName.0"),
                                )
                                .plus(", second: ")
                                .plus(
                                    secondType.generateKotlinConstructorIfNecessary("$paramName.1"),
                                )
                                .plus(", third: ")
                                .plus(
                                    thirdType.generateKotlinConstructorIfNecessary("$paramName.2"),
                                )
                                .plus(")")
                        }
                        else -> {
                            "unknown tuple type"
                        }
                    }
                }

                else -> paramType.generateKotlinConstructorIfNecessaryForParameter(paramName)
            }
        }
}

private fun String.toNSString(): String =
    if (this == "String" || this == "Swift.String") {
        "NSString"
    } else {
        this
    }
