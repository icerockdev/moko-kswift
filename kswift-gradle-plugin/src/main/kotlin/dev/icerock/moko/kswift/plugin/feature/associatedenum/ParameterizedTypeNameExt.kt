package dev.icerock.moko.kswift.plugin.feature.associatedenum

import io.outfoxx.swiftpoet.ParameterizedTypeName
import io.outfoxx.swiftpoet.TypeName

internal fun ParameterizedTypeName.toArrayCaster(
    paramName: String,
    optional: Boolean = false,
): String =
    "$paramName: "
        .plus(
            if (optional) "obj.$paramName != nil ? " else "",
        )
        .plus("obj.$paramName as! [")
        .plus(this.typeArguments[0].kotlinInteropTypeWithFallback)
        .plus("]")
        .plus(if (optional) " : nil" else "")

internal fun ParameterizedTypeName.toSetCaster(
    paramName: String,
    optional: Boolean = false,
): String =
    "$paramName: "
        .plus(
            if (optional) "obj.$paramName != nil ? " else "",
        )
        .plus("obj.$paramName as! Set<")
        .plus(this.typeArguments[0].kotlinInteropTypeWithFallback)
        .plus(">")
        .plus(if (optional) " : nil" else "")

internal fun ParameterizedTypeName.toDictionaryCaster(
    paramName: String,
    optional: Boolean = false,
): String =
    "$paramName: "
        .plus(
            if (optional) "obj.$paramName != nil ? " else "",
        )
        .plus("obj.$paramName as! [")
        .plus(this.typeArguments[0].kotlinInteropTypeWithFallback)
        .plus(" : ")
        .plus(this.typeArguments[1].kotlinInteropTypeWithFallback)
        .plus("]")
        .plus(if (optional) " : nil" else "")

internal fun TypeName.generateInitParameter(paramName: String): String {
    return "$paramName: "
        .plus(
            this.generateSwiftRetrieverForKotlinType(
                paramName = "obj.$paramName",
                isForTuple = false,
            ),
        )
}
