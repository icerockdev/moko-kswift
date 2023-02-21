package dev.icerock.moko.kswift.plugin.feature.associatedenum

import io.outfoxx.swiftpoet.ANY_OBJECT
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FLOAT32
import io.outfoxx.swiftpoet.FLOAT64
import io.outfoxx.swiftpoet.INT16
import io.outfoxx.swiftpoet.INT32
import io.outfoxx.swiftpoet.INT64
import io.outfoxx.swiftpoet.INT8
import io.outfoxx.swiftpoet.ParameterizedTypeName
import io.outfoxx.swiftpoet.UIN16
import io.outfoxx.swiftpoet.UINT32
import io.outfoxx.swiftpoet.UINT64
import io.outfoxx.swiftpoet.UINT8

internal val kotlinToSwiftTypeMap: Map<String, DeclaredTypeName> = mapOf(
    "kotlin/Any" to ANY_OBJECT,
    "kotlin/Boolean" to BOOL,
    "kotlin/Byte" to INT8,
    "kotlin/Double" to FLOAT64,
    "kotlin/Float" to FLOAT32,
    "kotlin/Int" to INT32,
    "kotlin/Long" to INT64,
    "kotlin/Short" to INT16,
    "kotlin/UByte" to UINT8,
    "kotlin/UInt" to UINT32,
    "kotlin/ULong" to UINT64,
    "kotlin/UShort" to UIN16,
)

internal val swiftTypeToKotlinMap: Map<DeclaredTypeName, String> = mapOf(
    ANY_OBJECT to "kotlin/Any",
    BOOL to "kotlin/Boolean",
    INT8 to "kotlin/Byte",
    FLOAT64 to "kotlin/Double",
    FLOAT32 to "kotlin/Float",
    INT32 to "kotlin/Int",
    INT64 to "kotlin/Long",
    INT16 to "kotlin/Short",
    UINT8 to "kotlin/UByte",
    UINT32 to "kotlin/UInt",
    UINT64 to "kotlin/ULong",
    UIN16 to "kotlin/UShort",
)

internal val swiftOptionalTypeToKotlinMap: Map<ParameterizedTypeName, String> =
    swiftTypeToKotlinMap.map { (swiftType, kotlinName) ->
        swiftType.wrapOptional() to kotlinName
    }
        .toMap()
