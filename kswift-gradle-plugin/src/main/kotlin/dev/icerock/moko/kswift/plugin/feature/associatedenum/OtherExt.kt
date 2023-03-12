package dev.icerock.moko.kswift.plugin.feature.associatedenum

import dev.icerock.moko.kswift.plugin.isNullable
import dev.icerock.moko.kswift.plugin.objcNameToSwift
import io.outfoxx.swiftpoet.ANY_OBJECT
import io.outfoxx.swiftpoet.ARRAY
import io.outfoxx.swiftpoet.DICTIONARY
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.SET
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.VOID
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeParameter
import kotlinx.metadata.KmTypeProjection

private val NSSTRING = DeclaredTypeName(moduleName = "Foundation", simpleName = "NSString")

internal fun KmType.kotlinTypeNameToInner(
    moduleName: String,
    namingMode: NamingMode,
    isOuterSwift: Boolean,
    typeParameters: List<KmTypeParameter>,
): TypeName? {
    val typeName = this.nameAsString(typeParameters)
    return when {
        typeName == null -> null
        typeName.startsWith("kotlin/") -> {
            kotlinPrimitiveToTypeNameWithNamingMode(
                namingMode = namingMode,
                typeName = typeName,
                moduleName = moduleName,
                typeParameters = typeParameters,
            )
        }
        else -> getDeclaredTypeNameFromNonPrimitive(typeName, moduleName)
    }?.addGenericsAndOptional(
        kmType = this,
        moduleName = moduleName,
        namingMode = namingMode,
        isOuterSwift = isOuterSwift,
        typeParameters = typeParameters,
    )
}

private fun KmType.kotlinPrimitiveToTypeNameWithNamingMode(
    namingMode: NamingMode,
    typeName: String,
    moduleName: String,
    typeParameters: List<KmTypeParameter>,
) = when (namingMode) {
    NamingMode.KOTLIN -> typeName.kotlinPrimitiveTypeNameToKotlinInterop(moduleName)
    NamingMode.SWIFT -> typeName.kotlinPrimitiveTypeNameToSwift(moduleName, arguments, typeParameters)
    NamingMode.OBJC -> typeName.kotlinPrimitiveTypeNameToObjectiveC(moduleName)
    NamingMode.KOTLIN_NO_STRING ->
        typeName
            .kotlinPrimitiveTypeNameToKotlinInterop(moduleName)
            .let { if (it == STRING) NSSTRING else it }
}

private fun String.kotlinPrimitiveTypeNameToSwift(
    moduleName: String,
    arguments: List<KmTypeProjection>,
    typeParameters: List<KmTypeParameter>,
): TypeName {
    require(this.startsWith("kotlin/"))
    return when (this) {
        "kotlin/Char" -> DeclaredTypeName.typeName("Swift.Character")
        "kotlin/Comparable" -> DeclaredTypeName.typeName("Swift.Comparable")
        "kotlin/Pair" -> arguments.generateTupleType(moduleName, typeParameters)
        "kotlin/Result" -> ANY_OBJECT
        "kotlin/String" -> STRING
        "kotlin/Triple" -> arguments.generateTupleType(moduleName, typeParameters)
        "kotlin/Throwable" -> DeclaredTypeName(
            moduleName = moduleName,
            simpleName = "KotlinThrowable",
        )

        "kotlin/Unit" -> VOID
        "kotlin/collections/List" -> ARRAY
        "kotlin/collections/Map" -> DICTIONARY
        "kotlin/collections/Set" -> SET
        else -> unknownKotlinPrimitiveTypeToSwift(arguments, moduleName, typeParameters)
    }
}

private fun String.unknownKotlinPrimitiveTypeToSwift(
    arguments: List<KmTypeProjection>,
    moduleName: String,
    typeParameters: List<KmTypeParameter>,
) = if (this.startsWith("kotlin/Function")) {
    val typedArgs = arguments.getTypes(moduleName, NamingMode.KOTLIN, false, typeParameters)
    val types = typedArgs.map { ParameterSpec.unnamed(it) }.dropLast(1)
    FunctionTypeName.get(types, typedArgs.last())
} else {
    kotlinToSwiftTypeMap[this] ?: this.kotlinInteropName(moduleName)
}

internal fun KmType.kotlinTypeToSwiftTypeName(
    moduleName: String,
    typeParameters: List<KmTypeParameter>,
): TypeName? {
    val typeName = this.nameAsString(typeParameters)

    return when {
        typeName == null -> null
        typeName.startsWith("kotlin/") ->
            typeName.kotlinPrimitiveTypeNameToSwift(moduleName, this.arguments, typeParameters)

        else -> getDeclaredTypeNameFromNonPrimitive(typeName, moduleName)
    }?.addGenericsAndOptional(
        kmType = this,
        moduleName = moduleName,
        namingMode = null,
        isOuterSwift = true,
        typeParameters = typeParameters,
    )
}

private fun KmType.nameAsString(typeParameters: List<KmTypeParameter>): String? =
    when (val classifier = this.classifier) {
        is KmClassifier.Class -> classifier.name
        is KmClassifier.TypeParameter -> {
            typeParameters.recursivelyResolveToName(classifier.id)
        }
        is KmClassifier.TypeAlias -> classifier.name
    }

private fun List<KmTypeParameter>.recursivelyResolveToName(id: Int): String? {
    return try {
        this[id].name + if (this[id].upperBounds.firstOrNull()?.isNullable != false) "?" else ""
    } catch (e: IndexOutOfBoundsException) {
        (id - this.size).takeIf { it >= 0 }?.let { indexInParent ->
            this.recursivelyResolveToName(indexInParent)
        }
    }
}

private fun String.kotlinPrimitiveTypeNameToKotlinInterop(moduleName: String): TypeName {
    require(this.startsWith("kotlin/"))
    return when (this) {
        "kotlin/String" -> STRING
        "kotlin/collections/List" -> ARRAY
        "kotlin/collections/Map" -> DICTIONARY
        "kotlin/collections/Set" -> SET
        else -> this.kotlinInteropName(moduleName)
    }
}

private fun String.kotlinInteropName(moduleName: String) = DeclaredTypeName(
    moduleName = moduleName,
    simpleName = "Kotlin" + this.split("/").last(),
)

private fun String.kotlinPrimitiveTypeNameToObjectiveC(moduleName: String): DeclaredTypeName {
    require(this.startsWith("kotlin/"))
    return when (this) {
        "kotlin/Any" -> ANY_OBJECT
        "kotlin/Boolean" -> DeclaredTypeName(moduleName = moduleName, simpleName = "KotlinBoolean")
        "kotlin/Pair" -> DeclaredTypeName(moduleName = moduleName, simpleName = "KotlinPair")
        "kotlin/Result" -> ANY_OBJECT
        "kotlin/String" -> NSSTRING
        "kotlin/Short" -> DeclaredTypeName(moduleName = "Foundation", simpleName = "NSNumber")
        "kotlin/Triple" -> DeclaredTypeName(moduleName = moduleName, simpleName = "KotlinTriple")
        "kotlin/collections/Map" -> DeclaredTypeName(
            moduleName = "Foundation",
            simpleName = "NSDictionary",
        )

        "kotlin/collections/Set" -> DeclaredTypeName(
            moduleName = "Foundation",
            simpleName = "NSSet",
        )

        "kotlin/collections/List" -> DeclaredTypeName(
            moduleName = "Foundation",
            simpleName = "NSArray",
        )

        else -> this.kotlinInteropName(moduleName)
    }
}

private fun getDeclaredTypeNameFromNonPrimitive(
    typeName: String,
    moduleName: String,
) = if (typeName.startsWith("platform/")) {
    val withoutCompanion: String = typeName.removeSuffix(".Companion")
    val moduleAndClass: List<String> = withoutCompanion.split("/").drop(1)
    val module: String = moduleAndClass[0]
    val className: String = moduleAndClass[1]

    DeclaredTypeName.typeName(
        listOf(module, className).joinToString("."),
    ).objcNameToSwift()
} else {
    // take type after final slash and generate declared type assuming module name
    val simpleName: String = typeName.split("/").last()
    DeclaredTypeName(
        moduleName = moduleName,
        simpleName = simpleName,
    )
}
