package dev.icerock.moko.kswift.plugin.feature

import dev.icerock.moko.kswift.plugin.buildTypeVariableNames
import dev.icerock.moko.kswift.plugin.context.ClassContext
import dev.icerock.moko.kswift.plugin.context.kLibClasses
import dev.icerock.moko.kswift.plugin.getDeclaredTypeNameWithGenerics
import dev.icerock.moko.kswift.plugin.getSimpleName
import dev.icerock.moko.kswift.plugin.objcNameToSwift
import io.outfoxx.swiftpoet.ANY_OBJECT
import io.outfoxx.swiftpoet.ARRAY
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DICTIONARY
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.EnumerationCaseSpec
import io.outfoxx.swiftpoet.FLOAT32
import io.outfoxx.swiftpoet.FLOAT64
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.INT16
import io.outfoxx.swiftpoet.INT32
import io.outfoxx.swiftpoet.INT64
import io.outfoxx.swiftpoet.INT8
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.ParameterizedTypeName
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.SET
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TupleTypeName
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.UIN16
import io.outfoxx.swiftpoet.UINT32
import io.outfoxx.swiftpoet.UINT64
import io.outfoxx.swiftpoet.UINT8
import io.outfoxx.swiftpoet.VOID
import io.outfoxx.swiftpoet.parameterizedBy
import kotlinx.metadata.ClassName
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeProjection
import org.gradle.configurationcache.extensions.capitalized
import java.util.Locale
import kotlin.reflect.KClass

class SealedToSwiftAssociatedEnumFeature(
    override val featureContext: KClass<ClassContext>,
    override val filter: Filter<ClassContext>,
) : ProcessorFeature<ClassContext>() {

    @Suppress("ReturnCount")
    override fun doProcess(featureContext: ClassContext, processorContext: ProcessorContext) {
        if (featureContext.clazz.sealedSubclasses.isEmpty()) return

        val kotlinFrameworkName: String = processorContext.framework.baseName
        val kmClass: KmClass = featureContext.clazz

        if (Flag.IS_PUBLIC(kmClass.flags).not()) return

        val originalClassName: String = getSimpleName(kmClass.name, featureContext.kLibClasses)

        println("Generating enum for sealed class $originalClassName")

        val sealedCases: List<EnumCase> = buildEnumCases(kotlinFrameworkName, featureContext)
        if (sealedCases.isEmpty()) return

        val typeVariables: List<TypeVariableName> =
            kmClass.buildTypeVariableNames(kotlinFrameworkName)

        val className: String = originalClassName.replace(".", "").plus("Ks")
        val enumType: TypeSpec = TypeSpec.enumBuilder(className)
            .addDoc("selector: ${featureContext.prefixedUniqueId}")
            .apply {
                typeVariables.forEach { addTypeVariable(it) }
                sealedCases.forEach { addEnumCase(it.enumCaseSpec) }
            }
            .addModifiers(Modifier.PUBLIC)
            .addFunction(
                buildEnumConstructor(
                    featureContext = featureContext,
                    kotlinFrameworkName = kotlinFrameworkName,
                    sealedCases = sealedCases,
                    className = className,
                    originalClassName = originalClassName,
                ),
            )
            .addProperty(
                buildSealedProperty(
                    featureContext = featureContext,
                    kotlinFrameworkName = kotlinFrameworkName,
                    sealedCases = sealedCases,
                ),
            )
            .build()

        processorContext.fileSpecBuilder.addType(enumType)
    }

    private fun buildEnumConstructor(
        featureContext: ClassContext,
        kotlinFrameworkName: String,
        sealedCases: List<EnumCase>,
        className: String,
        originalClassName: String,
    ): FunctionSpec {
        return FunctionSpec.builder("init")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(
                label = "_",
                name = "obj",
                type = featureContext.clazz.getDeclaredTypeNameWithGenerics(
                    kotlinFrameworkName = kotlinFrameworkName,
                    classes = featureContext.kLibClasses,
                ),
            )
            .addCode(
                CodeBlock.builder()
                    .apply {
                        sealedCases.forEachIndexed { index, enumCase ->
                            buildString {
                                if (index != 0) append("} else ")
                                append("if ")
                                append(enumCase.initCheck)
                                append(" {")
                                append('\n')
                            }.also { add(it) }
                            indent()
                            buildString {
                                append("self = .")
                                append(enumCase.name)
                                append(enumCase.initBlock)
                                append('\n')
                            }.also { add(it) }
                            unindent()
                        }
                        add("} else {\n")
                        indent()
                        add(
                            "fatalError(\"$className not synchronized with $originalClassName class\")\n",
                        )
                        unindent()
                        add("}\n")
                    }
                    .build(),
            )
            .build()
    }

    private fun buildEnumCases(
        kotlinFrameworkName: String,
        featureContext: ClassContext,
    ): List<EnumCase> {
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
    ): EnumCase {
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

        return EnumCase(
            name = decapitalizedName,
            param = if (isObject) null else caseArg,
            initCheck = if (isObject) {
                "obj is $caseArg"
            } else {
                "let obj = obj as? $caseArg"
            },
            caseArg = caseArg,
            isObject = isObject,
            explodedParams = sealedCaseClass.constructors.first().valueParameters.map {
                Pair(
                    it.name,
                    it.type?.kotlinPrimitiveTypeNameToSwift(kotlinFrameworkName)
                        ?: DeclaredTypeName.typeName("Swift.FailedToGetReturnType"),
                )
            },
        )
    }

    private fun buildSealedProperty(
        featureContext: ClassContext,
        kotlinFrameworkName: String,
        sealedCases: List<EnumCase>,
    ): PropertySpec {
        val returnType: TypeName = featureContext.clazz.getDeclaredTypeNameWithGenerics(
            kotlinFrameworkName = kotlinFrameworkName,
            classes = featureContext.kLibClasses,
        )
        return PropertySpec.builder("sealed", type = returnType)
            .addModifiers(Modifier.PUBLIC)
            .getter(
                FunctionSpec
                    .getterBuilder()
                    .addCode(buildSealedPropertyBody(sealedCases))
                    .build(),
            ).build()
    }

    private fun buildSealedPropertyBody(
        sealedCases: List<EnumCase>,
    ): CodeBlock = CodeBlock.builder().apply {
        add("switch self {\n")
        sealedCases.forEach { enumCase ->
            buildString {
                append("case .")
                append(enumCase.name)
                append(enumCase.caseBlock)
                append(":\n")
            }.also { add(it) }
            indent()
            addSealedCaseReturnCode(enumCase)
            unindent()
        }
        add("}\n")
    }.build()

    private fun CodeBlock.Builder.addSealedCaseReturnCode(
        enumCase: EnumCase,
    ) {
        val parameters = enumCase.swiftToKotlinConstructor()
        add("return ${enumCase.name.capitalized()}($parameters)\n")
    }

    data class EnumCase(
        val name: String,
        val param: TypeName?,
        val initCheck: String,
        val caseArg: TypeName,
        val isObject: Boolean,
        val explodedParams: List<Pair<String, TypeName>>,
    ) {
        val initBlock: String = if (isObject) {
            ""
        } else {
            "("
                .plus(
                    explodedParams.joinToString(",\n") {
                        val tupleType = it.second as? TupleTypeName
                        val paramType = (it.second as? ParameterizedTypeName)
                        when {
                            tupleType != null -> tupleType.generateTuple(it.first)
                            it.second.isCharacter -> "${it.first}: Character(UnicodeScalar(obj.${it.first})!)"
                            paramType?.rawType == DICTIONARY -> paramType.toDictionaryCaster(
                                it.first,
                            )

                            paramType?.rawType == SET -> paramType.toSetCaster(it.first)
                            paramType?.rawType == ARRAY -> paramType.toArrayCaster(it.first)
                            paramType?.optional == true -> {
                                val unwrapped = paramType.unwrapOptional()
                                when ((unwrapped as? ParameterizedTypeName)?.rawType) {
                                    DICTIONARY -> paramType.toDictionaryCaster(it.first, true)
                                    SET -> paramType.toSetCaster(it.first, true)
                                    ARRAY -> paramType.toArrayCaster(it.first, true)
                                    else -> paramType.generateInitParameter(it.first)
                                }
                            }

                            else -> it.second.generateInitParameter(it.first)
                        }
                    },
                )
                .plus(")")
        }

        val caseBlock = if (isObject) {
            ""
        } else {
            "(" + explodedParams.joinToString { "let ${it.first}" } + ")"
        }
        val enumCaseSpec: EnumerationCaseSpec
            get() {
                return if (param == null) {
                    EnumerationCaseSpec.builder(name).build()
                } else if (explodedParams.isNotEmpty()) {
                    val stripGenericsFromObjC = explodedParams.map { param ->
                        (param.second as? ParameterizedTypeName)?.let {
                            if (it.rawType.moduleName != "Swift") {
                                param.first to it.rawType.parameterizedBy(
                                    *it.typeArguments.stripInnerGenerics().toTypedArray(),
                                )
                            } else {
                                null
                            }
                        } ?: param
                    }
                    EnumerationCaseSpec.builder(
                        name = name,
                        type = TupleTypeName.of(*stripGenericsFromObjC.toTypedArray()),
                    ).build()
                } else {
                    EnumerationCaseSpec.builder(name, param).build()
                }
            }
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

private fun ParameterizedTypeName.toArrayCaster(
    paramName: String,
    optional: Boolean = false,
): String =
    "$paramName: "
        .plus(
            if (optional) "obj.$paramName != nil ? " else "",
        )
        .plus("obj.$paramName as! [")
        .plus(this.typeArguments[0].getKotlinInteropTypeWithFallback())
        .plus("]")
        .plus(if (optional) " : nil" else "")

private fun ParameterizedTypeName.toSetCaster(
    paramName: String,
    optional: Boolean = false,
): String =
    "$paramName: "
        .plus(
            if (optional) "obj.$paramName != nil ? " else "",
        )
        .plus("obj.$paramName as! Set<")
        .plus(this.typeArguments[0].getKotlinInteropTypeWithFallback())
        .plus(">")
        .plus(if (optional) " : nil" else "")

private fun ParameterizedTypeName.toDictionaryCaster(
    paramName: String,
    optional: Boolean = false,
): String =
    "$paramName: "
        .plus(
            if (optional) "obj.$paramName != nil ? " else "",
        )
        .plus("obj.$paramName as! [")
        .plus(this.typeArguments[0].getKotlinInteropTypeWithFallback())
        .plus(" : ")
        .plus(this.typeArguments[1].getKotlinInteropTypeWithFallback())
        .plus("]")
        .plus(if (optional) " : nil" else "")

private fun TupleTypeName.generateTuple(paramName: String): String =
    if (this.types.size == 2) {
        "$paramName: ("
            .plus(
                this.types[0].second
                    .generateSwiftRetrieverForKotlinType(
                        "obj.$paramName.first",
                    ),
            )
            .plus(", ")
            .plus(
                this.types[1].second
                    .generateSwiftRetrieverForKotlinType(
                        "obj.$paramName.second",
                    ),
            )
            .plus(")")
    } else {
        "$paramName: ("
            .plus(
                this.types[0].second
                    .generateSwiftRetrieverForKotlinType(
                        "obj.$paramName.first",
                    ),
            )
            .plus(", ")
            .plus(
                this.types[1].second
                    .generateSwiftRetrieverForKotlinType(
                        "obj.$paramName.second",
                    ),
            )
            .plus(", ")
            .plus(
                this.types[2].second
                    .generateSwiftRetrieverForKotlinType(
                        "obj.$paramName.third",
                    ),
            )
            .plus(")")
    }

private val TypeName.firstTypeArgument: TypeName?
    get() = (this as? ParameterizedTypeName)?.let {
        it.typeArguments.first()
    }

private fun TypeName.getKotlinInteropTypeWithFallback(): String {
    return (
        this.firstTypeArgument?.getKotlinInteropFromSwiftType()
            ?: this.getKotlinInteropFromSwiftType()
            ?: this.name
        )
        .replace("?", "")
}

private fun SealedToSwiftAssociatedEnumFeature.EnumCase.swiftToKotlinConstructor(): String =
    explodedParams.joinToString { (paramName, paramType) ->
        "$paramName: " + when {
            paramType.isCharacter -> "$paramName.utf16.first!"
            paramType is TupleTypeName -> {
                if (paramType.types.size == 2) {
                    val first = paramType.types[0]
                    val firstType = first.second
                    val second = paramType.types[1]
                    val secondType = second.second

                    "KotlinPair<"
                        .plus(firstType.getKotlinInteropTypeWithFallback().toNSString())
                        .plus(", ")
                        .plus(secondType.getKotlinInteropTypeWithFallback().toNSString())
                        .plus(">(first: ")
                        .plus(
                            firstType.generateKotlinConstructorIfNecessary("$paramName.0"),
                        )
                        .plus(", second: ")
                        .plus(
                            secondType.generateKotlinConstructorIfNecessary("$paramName.1"),
                        )
                        .plus(")")
                } else if (paramType.types.size == 3) {
                    val first = paramType.types[0]
                    val firstType = first.second
                    val second = paramType.types[1]
                    val secondType = second.second
                    val third = paramType.types[2]
                    val thirdType = third.second
                    "KotlinTriple<"
                        .plus(firstType.getKotlinInteropTypeWithFallback().toNSString())
                        .plus(", ")
                        .plus(secondType.getKotlinInteropTypeWithFallback().toNSString())
                        .plus(", ")
                        .plus(thirdType.getKotlinInteropTypeWithFallback().toNSString())
                        .plus(">(first: ")
                        .plus(
                            firstType.generateKotlinConstructorIfNecessary("$paramName.0"),
                        )
                        .plus(", second: ")
                        .plus(
                            secondType.generateKotlinConstructorIfNecessary("$paramName.1"),
                        )
                        .plus(", third: ")
                        .plus(
                            thirdType.generateKotlinConstructorIfNecessary("$paramName.2"),
                        )
                        .plus(")")
                } else {
                    "unknown tuple type"
                }
            }

            else -> paramType.generateKotlinConstructorIfNecessaryForParameter(paramName)
        }
    }

private fun String.toNSString(): String =
    if (this == "String" || this == "Swift.String") {
        "NSString"
    } else {
        this
    }

private fun TypeName.generateKotlinConstructorIfNecessaryForParameter(paramName: String): String {
    return when {
        this.optional -> this.generateKotlinConstructorIfNecessary(paramName, false)
        else -> paramName
    }
}

private fun TypeName.generateKotlinConstructorIfNecessary(
    paramName: String,
    isForTuple: Boolean = true,
): String {
    val unwrapped = this.firstTypeArgument
    return when {
        unwrapped != null -> unwrapped.generateKotlinConstructorForNullableType(paramName)
        this.optional && !isForTuple -> this.generateKotlinConstructorForNullableType(paramName)
        else -> generateKotlinConstructorForNonNullableType(paramName)
    }.let {
        if (!isForTuple) {
            it
        } else if (this == STRING) {
            it.replace(paramName, "$paramName as NSString")
        } else if (unwrapped == STRING) {
            it.replace("? $paramName :", "? $paramName! as NSString :")
        } else {
            it
        }
    }
}

private fun TypeName.getKotlinInteropFromSwiftType(): String? =
    swiftTypeToKotlinMap[this]?.replace("kotlin/", "Kotlin")

private val TypeName.swiftRetriver: String
    get() = (if (!this.optional) "!" else "?")
        .plus(".")
        .plus(
            this.name.split(".").last().lowercase()
                .replace("?", "")
                .let {
                    when (it) {
                        "float32" -> "float"
                        "float64" -> "double"
                        else -> it
                    }
                },
        )
        .plus("Value")

private fun TypeName.generateSwiftRetrieverForKotlinType(
    paramName: String,
    isForTuple: Boolean = true,
): String =
    if (swiftTypeToKotlinMap.containsKey(this) || swiftOptionalTypeToKotlinMap.containsKey(this)) {
        "$paramName"
            .plus(
                if (isForTuple || this.optional) {
                    this.swiftRetriver
                } else {
                    ""
                },
            )
    } else if (this == STRING) {
        "$paramName${if (isForTuple) "!" else ""} as String"
    } else if (this == STRING.wrapOptional()) {
        "$paramName != nil ? $paramName! as String : nil"
    } else {
        "$paramName${if (!this.optional && isForTuple) "!" else ""}"
    }

private fun TypeName.generateKotlinConstructorForNonNullableType(paramName: String): String {
    return this.getKotlinInteropFromSwiftType()?.plus("(value: $paramName)")
        ?: paramName
}

private fun TypeName.generateKotlinConstructorForNullableType(paramName: String): String {
    return "$paramName != nil ? "
        .plus(
            this.getKotlinInteropFromSwiftType()?.plus("(value: $paramName!)")
                ?: paramName,
        )
        .plus(" : nil")
}

private fun List<TypeName>.stripInnerGenerics(): List<TypeName> = map {
    (it as? ParameterizedTypeName)?.let {
        if (it.rawType.simpleName.contains("NS")) it.rawType else null
    } ?: it
}

private val kotlinToSwiftTypeMap: Map<String, DeclaredTypeName> = mapOf(
    "kotlin/Any" to ANY_OBJECT,
    "kotlin/Boolean" to BOOL,
    "kotlin/Byte" to INT8,
    "kotlin/Double" to FLOAT64,
    "kotlin/Float" to FLOAT32,
    "kotlin/Int" to INT32,
    "kotlin/Long" to INT64,
    "kotlin/Short" to INT16,
    "kotlin/UByte" to UINT8,
    "kotlin/UInt" to UINT32,
    "kotlin/ULong" to UINT64,
    "kotlin/UShort" to UIN16,
)

val swiftTypeToKotlinMap: Map<DeclaredTypeName, String> = mapOf(
    ANY_OBJECT to "kotlin/Any",
    BOOL to "kotlin/Boolean",
    INT8 to "kotlin/Byte",
    FLOAT64 to "kotlin/Double",
    FLOAT32 to "kotlin/Float",
    INT32 to "kotlin/Int",
    INT64 to "kotlin/Long",
    INT16 to "kotlin/Short",
    UINT8 to "kotlin/UByte",
    UINT32 to "kotlin/UInt",
    UINT64 to "kotlin/ULong",
    UIN16 to "kotlin/UShort",
)

val swiftOptionalTypeToKotlinMap: Map<ParameterizedTypeName, String> =
    swiftTypeToKotlinMap.map { (swiftType, kotlinName) ->
        swiftType.wrapOptional() to kotlinName
    }
        .toMap()

private fun String.kotlinPrimitiveTypeNameToSwift(
    moduleName: String,
    arguments: List<KmTypeProjection>,
): TypeName {
    require(this.startsWith("kotlin/"))
    return when (this) {
        "kotlin/Char" -> DeclaredTypeName.typeName("Swift.Character")
        "kotlin/Comparable" -> DeclaredTypeName.typeName("Swift.Comparable")
        "kotlin/Pair" -> arguments.generateTupleType(moduleName)
        "kotlin/Result" -> ANY_OBJECT
        "kotlin/String" -> STRING
        "kotlin/Triple" -> arguments.generateTupleType(moduleName)
        "kotlin/Throwable" -> DeclaredTypeName(
            moduleName = moduleName,
            simpleName = "KotlinThrowable",
        )

        "kotlin/Unit" -> VOID
        "kotlin/collections/List" -> ARRAY
        "kotlin/collections/Map" -> DICTIONARY
        "kotlin/collections/Set" -> SET
        else -> {
            if (this.startsWith("kotlin/Function")) {
                val typedArgs = arguments.getTypes(moduleName, NamingMode.KOTLIN, false)
                val types = typedArgs.map { ParameterSpec.unnamed(it) }.dropLast(1)
                FunctionTypeName.get(types, typedArgs.last())
            } else {
                kotlinToSwiftTypeMap[this] ?: this.kotlinInteropName(moduleName)
            }
        }
    }
}

private fun List<KmTypeProjection>.generateTupleType(moduleName: String): TupleTypeName =
    TupleTypeName.of(
        *this
            .map { projection ->
                (projection.type?.kotlinTypeNameToInner(moduleName, NamingMode.SWIFT, true) ?: ANY_OBJECT)
                    .let {
                        if (projection.type?.isNullable == true && !it.optional) {
                            it.wrapOptional()
                        } else {
                            it
                        }
                    }
            }
            .map { "" to it }
            .toTypedArray(),
    )

private fun KmType.kotlinPrimitiveTypeNameToSwift(moduleName: String): TypeName? {
    val typeName = this.nameAsString

    return when {
        typeName == null -> null
        typeName.startsWith("kotlin/") ->
            typeName.kotlinPrimitiveTypeNameToSwift(moduleName, this.arguments)

        else -> getDeclaredTypeNameFromNonPrimitive(typeName, moduleName)
    }?.addGenericsAndOptional(
        kmType = this,
        moduleName = moduleName,
        namingMode = null,
        isOuterSwift = true,
    )
}

private val NSSTRING = DeclaredTypeName(moduleName = "Foundation", simpleName = "NSString")

private fun String.kotlinPrimitiveTypeNameToObjectiveC(moduleName: String): DeclaredTypeName {
    require(this.startsWith("kotlin/"))
    return when (this) {
        "kotlin/Any" -> ANY_OBJECT
        "kotlin/Boolean" -> DeclaredTypeName(moduleName = moduleName, simpleName = "KotlinBoolean")
        "kotlin/Pair" -> DeclaredTypeName(moduleName = moduleName, simpleName = "KotlinPair")
        "kotlin/Result" -> ANY_OBJECT
        "kotlin/String" -> NSSTRING
        "kotlin/Short" -> DeclaredTypeName(moduleName = "Foundation", simpleName = "NSNumber")
        "kotlin/Triple" -> DeclaredTypeName(moduleName = moduleName, simpleName = "KotlinTriple")
        "kotlin/collections/Map" -> DeclaredTypeName(
            moduleName = "Foundation",
            simpleName = "NSDictionary",
        )

        "kotlin/collections/Set" -> DeclaredTypeName(
            moduleName = "Foundation",
            simpleName = "NSSet",
        )

        "kotlin/collections/List" -> DeclaredTypeName(
            moduleName = "Foundation",
            simpleName = "NSArray",
        )

        else -> this.kotlinInteropName(moduleName)
    }
}

private fun String.kotlinInteropName(moduleName: String) = DeclaredTypeName(
    moduleName = moduleName,
    simpleName = "Kotlin" + this.split("/").last(),
)

private fun getDeclaredTypeNameFromNonPrimitive(
    typeName: String,
    moduleName: String,
) = if (typeName.startsWith("platform/")) {
    val withoutCompanion: String = typeName.removeSuffix(".Companion")
    val moduleAndClass: List<String> = withoutCompanion.split("/").drop(1)
    val module: String = moduleAndClass[0]
    val className: String = moduleAndClass[1]

    DeclaredTypeName.typeName(
        listOf(module, className).joinToString("."),
    ).objcNameToSwift()
} else {
    // take type after final slash and generate declared type assuming module name
    val simpleName: String = typeName.split("/").last()
    DeclaredTypeName(
        moduleName = moduleName,
        simpleName = simpleName,
    )
}

private fun TypeName.addGenericsAndOptional(
    kmType: KmType,
    moduleName: String,
    namingMode: NamingMode?,
    isOuterSwift: Boolean,
): TypeName {
    val isSwift = (this as? DeclaredTypeName)?.moduleName == "Swift"

    return if (this is DeclaredTypeName && kmType.hasGenerics) {
        val genericTypes = kmType.arguments.getTypes(
            moduleName = moduleName,
            namingMode = when {
                this.simpleName.startsWith("Kotlin") -> NamingMode.KOTLIN_NO_STRING
                this == ARRAY || this == SET || this == DICTIONARY -> NamingMode.KOTLIN
                namingMode != null -> namingMode
                isSwift -> NamingMode.SWIFT
                else -> NamingMode.OBJC
            },
            isOuterSwift = isSwift,
        )
        this.parameterizedBy(*genericTypes.toTypedArray())
    } else {
        this
    }.let {
        if (kmType.isNullable && isOuterSwift) it.makeOptional() else it
    }
}

enum class NamingMode { KOTLIN, KOTLIN_NO_STRING, SWIFT, OBJC }

private fun List<KmTypeProjection>.getTypes(
    moduleName: String,
    namingMode: NamingMode,
    isOuterSwift: Boolean,
): List<TypeName> = this.map {
    it.type?.kotlinTypeNameToInner(moduleName, namingMode, isOuterSwift) ?: ANY_OBJECT
}

private fun KmType.kotlinTypeNameToInner(
    moduleName: String,
    namingMode: NamingMode,
    isOuterSwift: Boolean,
): TypeName? {
    val typeName = this.nameAsString
    return when {
        typeName == null -> null
        typeName.startsWith("kotlin/") -> {
            when (namingMode) {
                NamingMode.KOTLIN -> typeName.kotlinPrimitiveTypeNameToKotlinInterop(moduleName)
                NamingMode.SWIFT -> typeName.kotlinPrimitiveTypeNameToSwift(moduleName, arguments)
                NamingMode.OBJC -> typeName.kotlinPrimitiveTypeNameToObjectiveC(moduleName)
                NamingMode.KOTLIN_NO_STRING ->
                    typeName
                        .kotlinPrimitiveTypeNameToKotlinInterop(moduleName)
                        .let { if (it == STRING) NSSTRING else it }
            }
        }

        else -> getDeclaredTypeNameFromNonPrimitive(typeName, moduleName)
    }?.addGenericsAndOptional(
        kmType = this,
        moduleName = moduleName,
        namingMode = namingMode,
        isOuterSwift = isOuterSwift,
    )
}

private fun String.kotlinPrimitiveTypeNameToKotlinInterop(moduleName: String): TypeName {
    require(this.startsWith("kotlin/"))
    return when (this) {
        "kotlin/String" -> STRING
        "kotlin/collections/List" -> ARRAY
        "kotlin/collections/Map" -> DICTIONARY
        "kotlin/collections/Set" -> SET
        else -> this.kotlinInteropName(moduleName)
    }
}

val TypeName.isCharacter: Boolean
    get() = this.name == "Swift.Character"

private val KmType.isNullable: Boolean
    get() = Flag.Type.IS_NULLABLE(flags)

private val KmType.hasGenerics: Boolean
    get() = this.arguments.isNotEmpty()

private val KmType.nameAsString: String?
    get() {
        val classifier = this.classifier
        return when (classifier) {
            is KmClassifier.Class -> classifier.name
            is KmClassifier.TypeParameter -> null
            is KmClassifier.TypeAlias -> classifier.name
        }
    }

private fun TypeName.generateInitParameter(paramName: String): String {
    return "$paramName: "
        .plus(
            this.generateSwiftRetrieverForKotlinType(
                paramName = "obj.$paramName",
                isForTuple = false,
            ),
        )
}
