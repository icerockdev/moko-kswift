package dev.icerock.moko.kswift.plugin.feature.associatedenum

import io.outfoxx.swiftpoet.TupleTypeName

internal fun TupleTypeName.generateTuple(paramName: String): String =
    if (this.types.size == 2) {
        "$paramName: ("
            .plus(
                this.types[0].second
                    .generateSwiftRetrieverForKotlinType(
                        "obj.$paramName.first",
                    ),
            )
            .plus(", ")
            .plus(
                this.types[1].second
                    .generateSwiftRetrieverForKotlinType(
                        "obj.$paramName.second",
                    ),
            )
            .plus(")")
    } else {
        "$paramName: ("
            .plus(
                this.types[0].second
                    .generateSwiftRetrieverForKotlinType(
                        "obj.$paramName.first",
                    ),
            )
            .plus(", ")
            .plus(
                this.types[1].second
                    .generateSwiftRetrieverForKotlinType(
                        "obj.$paramName.second",
                    ),
            )
            .plus(", ")
            .plus(
                this.types[2].second
                    .generateSwiftRetrieverForKotlinType(
                        "obj.$paramName.third",
                    ),
            )
            .plus(")")
    }
