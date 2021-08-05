/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import io.outfoxx.swiftpoet.DeclaredTypeName
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
            DeclaredTypeName(moduleName = "Swift", simpleName = "String")
        }
        "kotlin/Int" -> DeclaredTypeName(moduleName = "Foundation", simpleName = "NSNumber")
        "kotlin/Unit" -> VOID
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
