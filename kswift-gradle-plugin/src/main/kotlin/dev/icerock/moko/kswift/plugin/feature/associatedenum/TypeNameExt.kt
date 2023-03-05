package dev.icerock.moko.kswift.plugin.feature.associatedenum

import dev.icerock.moko.kswift.plugin.hasGenerics
import dev.icerock.moko.kswift.plugin.isNullable
import io.outfoxx.swiftpoet.ARRAY
import io.outfoxx.swiftpoet.DICTIONARY
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ParameterizedTypeName
import io.outfoxx.swiftpoet.SET
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.parameterizedBy
import kotlinx.metadata.KmType

internal val TypeName.isCharacter: Boolean
    get() = this.name == "Swift.Character"

internal val TypeName.kotlinInteropTypeWithFallback: String
    get() = (
        this.firstTypeArgument?.kotlinInteropFromSwiftType
            ?: this.kotlinInteropFromSwiftType
            ?: this.name
        )
        .replace("?", "")

internal fun TypeName.generateKotlinConstructorIfNecessaryForParameter(paramName: String): String {
    return when {
        this.optional -> this.generateKotlinConstructorIfNecessary(paramName, false)
        else -> paramName
    }
}

internal fun TypeName.generateKotlinConstructorIfNecessary(
    paramName: String,
    isForTuple: Boolean = true,
): String {
    val unwrapped = this.firstTypeArgument
    return when {
        unwrapped != null -> unwrapped.generateKotlinConstructorForNullableType(paramName)
        this.optional && !isForTuple -> this.generateKotlinConstructorForNullableType(paramName)
        else -> generateKotlinConstructorForNonNullableType(paramName)
    }.let {
        if (!isForTuple) {
            it
        } else if (this == STRING) {
            it.replace(paramName, "$paramName as NSString")
        } else if (unwrapped == STRING) {
            it.replace("? $paramName :", "? $paramName! as NSString :")
        } else {
            it
        }
    }
}

internal fun List<TypeName>.stripInnerGenerics(): List<TypeName> = map { typeName ->
    (typeName as? ParameterizedTypeName)?.let {
        if (it.rawType.simpleName.contains("NS")) it.rawType else null
    } ?: typeName
}

internal fun TypeName.addGenericsAndOptional(
    kmType: KmType,
    moduleName: String,
    namingMode: NamingMode?,
    isOuterSwift: Boolean,
): TypeName {
    val isSwift = (this as? DeclaredTypeName)?.moduleName == "Swift"

    return if (this is DeclaredTypeName && kmType.hasGenerics) {
        val genericTypes = kmType.arguments.getTypes(
            moduleName = moduleName,
            namingMode = when {
                this.simpleName.startsWith("Kotlin") -> NamingMode.KOTLIN_NO_STRING
                this == ARRAY || this == SET || this == DICTIONARY -> NamingMode.KOTLIN
                namingMode != null -> namingMode
                isSwift -> NamingMode.SWIFT
                else -> NamingMode.OBJC
            },
            isOuterSwift = isSwift,
        )
        this.parameterizedBy(genericTypes)
    } else {
        this
    }.let {
        if (kmType.isNullable && isOuterSwift) it.makeOptional() else it
    }
}

private val TypeName.firstTypeArgument: TypeName?
    get() = (this as? ParameterizedTypeName)?.typeArguments?.first()

private val TypeName.kotlinInteropFromSwiftType: String?
    get() = swiftTypeToKotlinMap[this]?.replace("kotlin/", "Kotlin")

private val TypeName.swiftRetriever: String
    get() = (if (!this.optional) "!" else "?")
        .plus(".")
        .plus(
            this.name.split(".").last().lowercase()
                .replace("?", "")
                .let {
                    when (it) {
                        "float32" -> "float"
                        "float64" -> "double"
                        else -> it
                    }
                },
        )
        .plus("Value")

internal fun TypeName.generateSwiftRetrieverForKotlinType(
    paramName: String,
    isForTuple: Boolean = true,
): String =
    if (swiftTypeToKotlinMap.containsKey(this) || swiftOptionalTypeToKotlinMap.containsKey(this)) {
        paramName
            .plus(
                if (isForTuple || this.optional) {
                    this.swiftRetriever
                } else {
                    ""
                },
            )
    } else if (this == STRING) {
        "$paramName${if (isForTuple) "!" else ""} as String"
    } else if (this == STRING.wrapOptional()) {
        "$paramName != nil ? $paramName! as String : nil"
    } else {
        "$paramName${if (!this.optional && isForTuple) "!" else ""}"
    }

private fun TypeName.generateKotlinConstructorForNonNullableType(paramName: String): String {
    return this.kotlinInteropFromSwiftType?.plus("(value: $paramName)")
        ?: paramName
}

private fun TypeName.generateKotlinConstructorForNullableType(paramName: String): String {
    return "$paramName != nil ? "
        .plus(
            this.kotlinInteropFromSwiftType?.plus("(value: $paramName!)")
                ?: paramName,
        )
        .plus(" : nil")
}
