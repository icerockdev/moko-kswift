/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import io.outfoxx.swiftpoet.ANY_OBJECT
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.parameterizedBy
import kotlinx.metadata.ClassName
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass

fun KmClass.buildTypeVariableNames(
    kotlinFrameworkName: String,
) = this.typeParameters.map { typeParam ->
    val bounds: List<TypeVariableName.Bound> = typeParam.upperBounds
        .map { it.toTypeName(kotlinFrameworkName, isUsedInGenerics = true) }
        .map { TypeVariableName.Bound(it) }
        .ifEmpty { listOf(TypeVariableName.Bound(ANY_OBJECT)) }
    TypeVariableName.typeVariable(typeParam.name, bounds)
}

fun KmClass.getDeclaredTypeNameWithGenerics(
    kotlinFrameworkName: String,
    classes: List<KmClass>,
): TypeName {
    val typeVariables: List<TypeVariableName> = buildTypeVariableNames(kotlinFrameworkName)
    val haveGenerics: Boolean = typeVariables.isNotEmpty()
    val isInterface: Boolean = Flag.Class.IS_INTERFACE(flags)

    @Suppress("SpreadOperator")
    return getDeclaredTypeName(kotlinFrameworkName, classes)
        .let { type ->
            if (haveGenerics.not() || isInterface) {
                type
            } else {
                type.parameterizedBy(typeVariables)
            }
        }
}

fun KmClass.getDeclaredTypeName(
    kotlinFrameworkName: String,
    classes: List<KmClass>,
): DeclaredTypeName {
    return DeclaredTypeName(
        moduleName = kotlinFrameworkName,
        simpleName = getSimpleName(
            className = name,
            classes = classes,
        ),
    )
}

fun getSimpleName(className: ClassName, classes: List<KmClass>): String {
    val path = className.substringBeforeLast('/')
    val nameWithDots = className.substringAfterLast('/')
    val parts = nameWithDots.split(".")

    val partsInfo = parts.mapIndexed { idx, name ->
        val classInfo = classes.first {
            it.name == path + "/" + parts.take(idx + 1).joinToString(".")
        }
        val isInterface: Boolean = Flag.Class.IS_INTERFACE(classInfo.flags)
        val haveGenerics: Boolean = classInfo.typeParameters.isNotEmpty()
        val validForNesting = !isInterface && !haveGenerics
        validForNesting
    }

    var lastPartValid = partsInfo.first()
    val indexOfFirstDot: Int = partsInfo.drop(1).indexOfFirst { partValid ->
        val valid = lastPartValid && partValid
        if (!valid) lastPartValid = partValid
        valid
    }

    return if (indexOfFirstDot == -1) {
        nameWithDots.replace(".", "")
    } else {
        val left = parts.take(indexOfFirstDot + 1).joinToString("")
        val right = parts.drop(indexOfFirstDot + 1).joinToString("")
        "$left.$right"
    }
}
