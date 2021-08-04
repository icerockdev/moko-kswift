/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin.feature

import dev.icerock.moko.kswift.plugin.PackageFunctionContext
import dev.icerock.moko.kswift.plugin.ProcessorContext
import dev.icerock.moko.kswift.plugin.ProcessorFeature
import dev.icerock.moko.kswift.plugin.toTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmFunction
import kotlinx.metadata.klib.file

class PlatformExtensionFunctionsFeature : ProcessorFeature<PackageFunctionContext> {
    override fun process(
        featureContext: PackageFunctionContext,
        processorContext: ProcessorContext
    ) {
        val func: KmFunction = featureContext.func
        val kotlinFrameworkName: String = processorContext.framework.baseName

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
                    .addModifiers(Modifier.PUBLIC)
                    .addParameters(func.valueParameters.map { param ->
                        val type = param.type?.toTypeName(kotlinFrameworkName)
                            ?: throw IllegalArgumentException("extension $funcName have null type for $param")
                        ParameterSpec.builder(parameterName = param.name, type = type)
                            .build()
                    })
                    .returns(func.returnType.toTypeName(kotlinFrameworkName))
                    .addCode("return $swiftedClass.$funcName(self, $callParamsLine)\n")
                    .build()
            )
            .addModifiers(Modifier.PUBLIC)
            .build()

        val fileSpecBuilder: FileSpec.Builder = processorContext.fileSpecBuilder

        fileSpecBuilder.addImport(declaredType.moduleName)
        fileSpecBuilder.addExtension(extensionSpec)
    }
}
