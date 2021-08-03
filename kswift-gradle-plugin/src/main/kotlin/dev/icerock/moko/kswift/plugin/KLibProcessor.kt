/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.VOID
import io.outfoxx.swiftpoet.parameterizedBy
import kotlinx.metadata.ClassName
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmModuleFragment
import kotlinx.metadata.KmPackage
import kotlinx.metadata.KmType
import kotlinx.metadata.klib.KlibModuleMetadata
import kotlinx.metadata.klib.file
import org.slf4j.Logger
import java.io.File

class KLibProcessor(
    private val kotlinFrameworkName: String,
    private val outputDir: File,
    private val library: File,
    private val logger: Logger
) {
    fun process() {
        val metadata: KlibModuleMetadata =
            KotlinMetadataLibraryProvider.readLibraryMetadata(library)

        logger.debug("metadata $metadata")

        metadata.annotations.forEach { annotation ->
            logger.debug("annotation metadata $annotation")
        }
        metadata.fragments.forEach { processFragment(it) }
    }

    private fun processFragment(fragment: KmModuleFragment) {
        logger.debug("fragment metadata $fragment")

        fragment.pkg?.let { processPackage(it) }
        fragment.classes.forEach { processClass(it) }
    }

    private fun processPackage(pkg: KmPackage) {
        logger.debug("pkg metadata $pkg")

        pkg.functions.forEach { processFunction(it) }
    }

    private fun processFunction(func: KmFunction) {
        processPlatformExtensionFunction(func)
    }

    private fun processPlatformExtensionFunction(func: KmFunction) {
        val receiver = func.receiverParameterType ?: return

        val classifier = receiver.classifier
        if (classifier !is KmClassifier.Class) return

        val receiverName: String = classifier.name
        val receiverParts: List<String> = receiverName.split("/")
        if (receiverParts.size < 3) return
        if (receiverParts[0] != "platform") return

        val frameworkName: String = receiverParts[1]
        val className: String = receiverParts[2]
        val funcName: String = func.name
        val fileName: String = func.file?.name ?: return
        val swiftedClass: String = fileName.replace(".kt", "Kt")
        val callParams: List<String> = func.valueParameters.map { param ->
            buildString {
                append(param.name)
                append(": ")
                append(param.name)
            }
        }
        val callParamsLine: String = callParams.joinToString(", ")

        val declaredType = DeclaredTypeName(moduleName = frameworkName, simpleName = className)
        val extensionSpec: ExtensionSpec = ExtensionSpec.builder(declaredType)
            .addFunction(
                FunctionSpec.builder(funcName)
                    .addParameters(func.valueParameters.map { param ->
                        val type = param.type?.toTypeName()
                            ?: throw IllegalArgumentException("extension $funcName have null type for $param")
                        ParameterSpec.builder(parameterName = param.name, type = type)
                            .build()
                    })
                    .returns(func.returnType.toTypeName())
                    .addCode("return $swiftedClass.$funcName(self, $callParamsLine)\n")
                    .build()
            )
            .addModifiers(Modifier.PUBLIC)
            .build()

        val fileSpec: FileSpec = FileSpec.builder(library.nameWithoutExtension)
            .addExtension(extensionSpec)
            .build()

        fileSpec.writeTo(outputDir)
    }

    private fun KmType.toTypeName(): TypeName {
        val classifier = classifier
        if (classifier !is KmClassifier.Class) {
            throw IllegalArgumentException("illegal type classifier $this $classifier")
        }

        return when (val classifierName = classifier.name) {
            "kotlin/String" -> DeclaredTypeName(moduleName = "Foundation", simpleName = "NSString")
            "kotlin/Int" -> DeclaredTypeName(moduleName = "Foundation", simpleName = "NSNumber")
            "kotlin/Unit" -> VOID
            else -> kotlinTypeToTypeName(classifierName)
        }
    }

    private fun KmType.kotlinTypeToTypeName(classifierName: ClassName): TypeName {
        val typeName = DeclaredTypeName(
            moduleName = kotlinFrameworkName,
            simpleName = classifierName.split("/").last()
        )
        if (this.arguments.isEmpty()) return typeName

        val arguments: List<TypeName> = this.arguments.mapNotNull { typeProj ->
            typeProj.type?.toTypeName()
        }
        return typeName.parameterizedBy(*arguments.toTypedArray())
    }

    private fun processClass(klass: KmClass) {
        logger.debug("class metadata $klass")
    }
}
