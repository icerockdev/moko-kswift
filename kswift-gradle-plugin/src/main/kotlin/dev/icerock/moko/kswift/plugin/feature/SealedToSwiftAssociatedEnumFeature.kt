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
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
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

        val sealedCases: List<AssociatedEnumCase> = buildEnumCases(kotlinFrameworkName, featureContext)
        if (sealedCases.isEmpty()) {
            logger.warn("No public subclasses found for sealed class $originalClassName")
            return
        } else {
            logger.lifecycle("Generating enum for sealed class $originalClassName (${sealedCases.size} public subclasses)")
        }

        val enumType: TypeSpec = buildTypeSpec(
            featureContext = featureContext,
            typeVariables = kmClass.buildTypeVariableNames(kotlinFrameworkName),
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

        val logger: Logger = Logging.getLogger("SealedToSwiftAssociatedEnumFeature")
    }
}
