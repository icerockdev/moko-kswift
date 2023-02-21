package dev.icerock.moko.kswift.plugin.feature.associatedenum

import dev.icerock.moko.kswift.plugin.context.ClassContext
import dev.icerock.moko.kswift.plugin.context.kLibClasses
import dev.icerock.moko.kswift.plugin.getDeclaredTypeNameWithGenerics
import kotlinx.metadata.ClassName
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import java.util.Locale

fun buildEnumCases(
    kotlinFrameworkName: String,
    featureContext: ClassContext,
): List<AssociatedEnumCase> {
    val kmClass = featureContext.clazz
    return kmClass.sealedSubclasses.mapNotNull { sealedClassName ->
        val sealedClass: KmClass = featureContext.parentContext
            .fragment.classes.first { it.name == sealedClassName }

        if (Flag.IS_PUBLIC(sealedClass.flags).not()) return@mapNotNull null

        buildEnumCase(
            kotlinFrameworkName = kotlinFrameworkName,
            featureContext = featureContext,
            subclassName = sealedClassName,
            sealedCaseClass = sealedClass,
        )
    }
}

private fun buildEnumCase(
    kotlinFrameworkName: String,
    featureContext: ClassContext,
    subclassName: ClassName,
    sealedCaseClass: KmClass,
): AssociatedEnumCase {
    val kmClass = featureContext.clazz
    val name: String = if (subclassName.startsWith(kmClass.name)) {
        subclassName.removePrefix(kmClass.name).removePrefix(".")
    } else {
        subclassName.removePrefix(kmClass.name.substringBeforeLast("/")).removePrefix("/")
    }
    val decapitalizedName: String = name.decapitalize(Locale.ROOT)

    val isObject: Boolean = Flag.Class.IS_OBJECT(sealedCaseClass.flags)
    val caseArg = sealedCaseClass.getDeclaredTypeNameWithGenerics(
        kotlinFrameworkName = kotlinFrameworkName,
        classes = featureContext.kLibClasses,
    )

    return AssociatedEnumCase(
        frameworkName = kotlinFrameworkName,
        name = decapitalizedName,
        param = if (isObject) null else caseArg,
        initCheck = if (isObject) {
            "obj is $caseArg"
        } else {
            "let obj = obj as? $caseArg"
        },
        caseArg = caseArg,
        isObject = isObject,
        constructorParams = sealedCaseClass.constructors.first().valueParameters,
    )
}
