package dev.icerock.moko.kswift.plugin.feature.associatedenum

import dev.icerock.moko.kswift.plugin.isNullable
import io.outfoxx.swiftpoet.ANY_OBJECT
import io.outfoxx.swiftpoet.TupleTypeName
import io.outfoxx.swiftpoet.TypeName
import kotlinx.metadata.KmTypeParameter
import kotlinx.metadata.KmTypeProjection

internal fun List<KmTypeProjection>.generateTupleType(
    moduleName: String,
    typeParameters: List<KmTypeParameter>,
): TupleTypeName =
    TupleTypeName(
        this.map { projection ->
            (projection.type?.kotlinTypeNameToInner(
                moduleName = moduleName,
                namingMode = NamingMode.SWIFT,
                isOuterSwift = true,
                typeParameters = typeParameters,
            ) ?: ANY_OBJECT)
                .let {
                    if (projection.type?.isNullable == true && !it.optional) {
                        it.wrapOptional()
                    } else {
                        it
                    }
                }
        }
            .map { "" to it },
    )

internal fun List<KmTypeProjection>.getTypes(
    moduleName: String,
    namingMode: NamingMode,
    isOuterSwift: Boolean,
    typeParameters: List<KmTypeParameter>,
): List<TypeName> = this.map {
    it.type?.kotlinTypeNameToInner(moduleName, namingMode, isOuterSwift, typeParameters) ?: ANY_OBJECT
}
