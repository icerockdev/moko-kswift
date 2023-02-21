package dev.icerock.moko.kswift.plugin.feature.associatedenum

import dev.icerock.moko.kswift.plugin.isNullable
import io.outfoxx.swiftpoet.ANY_OBJECT
import io.outfoxx.swiftpoet.TupleTypeName
import io.outfoxx.swiftpoet.TypeName
import kotlinx.metadata.KmTypeProjection

internal fun List<KmTypeProjection>.generateTupleType(moduleName: String): TupleTypeName =
    TupleTypeName.of(
        *this
            .map { projection ->
                (projection.type?.kotlinTypeNameToInner(moduleName, NamingMode.SWIFT, true) ?: ANY_OBJECT)
                    .let {
                        if (projection.type?.isNullable == true && !it.optional) {
                            it.wrapOptional()
                        } else {
                            it
                        }
                    }
            }
            .map { "" to it }
            .toTypedArray(),
    )

internal fun List<KmTypeProjection>.getTypes(
    moduleName: String,
    namingMode: NamingMode,
    isOuterSwift: Boolean,
): List<TypeName> = this.map {
    it.type?.kotlinTypeNameToInner(moduleName, namingMode, isOuterSwift) ?: ANY_OBJECT
}
