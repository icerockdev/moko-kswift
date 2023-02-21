package dev.icerock.moko.kswift.plugin.feature

import dev.icerock.moko.kswift.plugin.buildTypeVariableNames
import dev.icerock.moko.kswift.plugin.context.ClassContext
import dev.icerock.moko.kswift.plugin.context.kLibClasses
import dev.icerock.moko.kswift.plugin.feature.associatedenum.AssociatedEnumCase
import dev.icerock.moko.kswift.plugin.feature.associatedenum.buildEnumCases
import dev.icerock.moko.kswift.plugin.feature.associatedenum.buildTypeSpec
import dev.icerock.moko.kswift.plugin.getSimpleName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import kotlin.reflect.KClass

class SealedToSwiftAssociatedEnumFeature(
    override val featureContext: KClass<ClassContext>,
    override val filter: Filter<ClassContext>,
) : ProcessorFeature<ClassContext>() {

    @Suppress("ReturnCount")
    override fun doProcess(featureContext: ClassContext, processorContext: ProcessorContext) {
        val kotlinFrameworkName: String = processorContext.framework.baseName

        doProcess(
            featureContext = featureContext,
            fileSpecBuilder = processorContext.fileSpecBuilder,
            kotlinFrameworkName = kotlinFrameworkName,
        )
    }

    fun doProcess(
        featureContext: ClassContext,
        fileSpecBuilder: FileSpec.Builder,
        kotlinFrameworkName: String,
    ) {
        val kmClass: KmClass = featureContext.clazz

        if (Flag.IS_PUBLIC(kmClass.flags).not()) return

        val originalClassName: String = getSimpleName(kmClass.name, featureContext.kLibClasses)

        if (featureContext.clazz.sealedSubclasses.isEmpty()) return

        println("Generating enum for sealed class $originalClassName")

        val sealedCases: List<AssociatedEnumCase> = buildEnumCases(kotlinFrameworkName, featureContext)
        if (sealedCases.isEmpty()) return

        val typeVariables: List<TypeVariableName> =
            kmClass.buildTypeVariableNames(kotlinFrameworkName)

        val className: String = originalClassName.replace(".", "").plus("Ks")
        val enumType: TypeSpec = buildTypeSpec(
            className = className,
            featureContext = featureContext,
            typeVariables = typeVariables,
            sealedCases = sealedCases,
            kotlinFrameworkName = kotlinFrameworkName,
            originalClassName = originalClassName,
        )

        fileSpecBuilder.addType(enumType)
    }

    class Config : BaseConfig<ClassContext> {
        override var filter: Filter<ClassContext> = Filter.Exclude(emptySet())
    }

    companion object : Factory<ClassContext, SealedToSwiftAssociatedEnumFeature, Config> {
        override fun create(block: Config.() -> Unit): SealedToSwiftAssociatedEnumFeature {
            val config = Config().apply(block)
            return SealedToSwiftAssociatedEnumFeature(featureContext, config.filter)
        }

        override val featureContext: KClass<ClassContext> = ClassContext::class

        @JvmStatic
        override val factory = Companion
    }
}
