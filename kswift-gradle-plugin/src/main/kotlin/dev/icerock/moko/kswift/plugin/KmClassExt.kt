/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import kotlinx.metadata.ClassName
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass

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
