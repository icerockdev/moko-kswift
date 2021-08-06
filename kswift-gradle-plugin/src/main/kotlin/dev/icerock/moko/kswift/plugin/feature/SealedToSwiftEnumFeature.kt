/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.feature

import dev.icerock.moko.kswift.plugin.ProcessorContext
import dev.icerock.moko.kswift.plugin.ProcessorFeature
import dev.icerock.moko.kswift.plugin.context.ClassContext
import io.outfoxx.swiftpoet.TypeSpec
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import java.util.Locale

class SealedToSwiftEnumFeature(
    filter: Filter<ClassContext>
) : ProcessorFeature<ClassContext>(filter) {
    override fun doProcess(featureContext: ClassContext, processorContext: ProcessorContext) {
        if (featureContext.clazz.sealedSubclasses.isEmpty()) return

        val kmclass: KmClass = featureContext.clazz

        if (Flag.Class.IS_CLASS(kmclass.flags)) {
            // class generation logic
        } else if (Flag.Class.IS_INTERFACE(kmclass.flags)) {
            // interface generation logic
        } else {
            println("${kmclass.name} not class or interface - skip ${kmclass.flags}")
            return
        }

        val sealedCases = kmclass.sealedSubclasses.map { subclassName ->
            if (subclassName.startsWith(kmclass.name)) subclassName.removePrefix(kmclass.name).removePrefix(".")
            else subclassName
        }.map { it.decapitalize(Locale.ROOT) }

        val className: String = kmclass.name.substringAfterLast('/').replace('.', '_').plus("Ks")
        val enumType: TypeSpec = TypeSpec.enumBuilder(className)
            .addDoc("selector: ${featureContext.prefixedUniqueId}")
            .apply {
                sealedCases.forEach { addEnumCase(it) }
            }
            .build()

        processorContext.fileSpecBuilder.addType(enumType)
    }

    class Config {
        var filter: Filter<ClassContext> = Filter.Exclude(emptySet())
    }

    companion object : Factory<ClassContext, SealedToSwiftEnumFeature, Config> {
        override fun create(block: Config.() -> Unit): SealedToSwiftEnumFeature {
            val config = Config().apply(block)
            return SealedToSwiftEnumFeature(config.filter)
        }
    }
}
