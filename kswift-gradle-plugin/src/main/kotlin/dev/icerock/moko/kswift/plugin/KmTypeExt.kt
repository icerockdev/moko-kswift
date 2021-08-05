/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.VOID
import io.outfoxx.swiftpoet.parameterizedBy
import kotlinx.metadata.ClassName
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType

fun KmType.toTypeName(moduleName: String, isUsedInGenerics: Boolean = false): TypeName {
    val classifier = classifier
    if (classifier !is KmClassifier.Class) {
        throw IllegalArgumentException("illegal type classifier $this $classifier")
    }

    return when (val classifierName = classifier.name) {
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
        "kotlin/Unit" -> VOID
        "kotlin/Function1" -> {
            val inputType: TypeName = arguments[0].type?.toTypeName(moduleName, false)!!
            val outputType: TypeName = arguments[1].type?.toTypeName(moduleName, false)!!
            FunctionTypeName.get(
                parameters = listOf(ParameterSpec.unnamed(inputType)),
                returnType = outputType
            ).makeEscaping()
        }
        else -> kotlinTypeToTypeName(moduleName, classifierName)
    }
}

fun KmType.kotlinTypeToTypeName(
    moduleName: String,
    classifierName: ClassName
): TypeName {
    val typeName = DeclaredTypeName(
        moduleName = moduleName,
        simpleName = classifierName.split("/").last()
    )
    if (this.arguments.isEmpty()) return typeName

    val arguments: List<TypeName> = this.arguments.mapNotNull { typeProj ->
        typeProj.type?.toTypeName(moduleName = moduleName, isUsedInGenerics = true)
    }
    return typeName.parameterizedBy(*arguments.toTypedArray())
}
