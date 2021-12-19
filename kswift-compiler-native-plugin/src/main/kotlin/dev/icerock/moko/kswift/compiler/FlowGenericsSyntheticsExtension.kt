/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.compiler

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertyGetterDescriptorImpl
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.resolve.lazy.descriptors.AbstractLazyMemberScope
import org.jetbrains.kotlin.resolve.lazy.descriptors.LazyClassMemberScope
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import java.util.concurrent.atomic.AtomicBoolean

class FlowGenericsSyntheticsExtension(
    private val log: MessageCollector
) : SyntheticResolveExtension {

    private val recursionGuard = AtomicBoolean(false)

    override fun getSyntheticPropertiesNames(thisDescriptor: ClassDescriptor): List<Name> {
        if (!recursionGuard.compareAndSet(false, true)) return emptyList()

        return thisDescriptor.getProperties()
            .also { recursionGuard.set(false) }
            .filterFlowProperties()
            .map { Name.identifier(it.name.identifier + "Ks") }
            .also { log.debug("synth names: $it") }
    }

    override fun generateSyntheticProperties(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: ArrayList<PropertyDescriptor>,
        result: MutableSet<PropertyDescriptor>
    ) {
        if (name.identifier.endsWith("Ks").not()) return

        val originalIdentifier: String = name.identifier.removeSuffix("Ks")
        val flowProperty: PropertyDescriptor = thisDescriptor.getProperties()
            .firstOrNull { it.name.identifier == originalIdentifier } ?: return

        result.add(flowProperty.createFlowSynthetic())

        log.debug("generated $result")
    }

    private fun ClassDescriptor.getProperties(): List<PropertyDescriptor> {
        return this.getDeclarations()
            .filterIsInstance<PropertyDescriptor>()
    }

    private fun Collection<PropertyDescriptor>.filterFlowProperties(): List<PropertyDescriptor> {
        return filter { propDesc ->
            if (!propDesc.visibility.isPublicAPI) return@filter false

            val fqName: FqName = propDesc.type.constructor.declarationDescriptor?.fqNameOrNull()
                ?: return@filter false

            when (fqName.asString()) {
                "kotlinx.coroutines.flow.MutableStateFlow" -> true
                else -> false
            }
        }
    }

    private fun PropertyDescriptor.createFlowSynthetic(): PropertyDescriptor {
        val property = PropertyDescriptorImpl.create(
            this.containingDeclaration,
            Annotations.EMPTY,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
            false,
            Name.identifier(name.identifier + "Ks"),
            CallableMemberDescriptor.Kind.SYNTHESIZED,
            SourceElement.NO_SOURCE,
            false,
            false,
            false,
            false,
            false,
            false
        )

        property.setType(
            this.type,
            this.typeParameters,
            this.dispatchReceiverParameter,
            this.extensionReceiverParameter
        )

        property.initialize(
            PropertyGetterDescriptorImpl(
                property,
                Annotations.EMPTY,
                Modality.FINAL,
                property.visibility,
                false,
                false,
                false,
                CallableMemberDescriptor.Kind.SYNTHESIZED,
                null,
                SourceElement.NO_SOURCE
            ).apply { initialize(property.type) },
            null
        )

        return property
    }

    // We need the user declared functions. Unfortunately there doesn't seem to be an official way for that.
    // Instead we'll use reflection to use the same code the compiler is using.
    // https://github.com/JetBrains/kotlin/blob/fe8f7cfcae3b33ba7ee5d06cd45e5e68f3c421a8/compiler/frontend/src/org/jetbrains/kotlin/resolve/lazy/descriptors/LazyClassMemberScope.kt#L64
    // thx https://github.com/rickclephas/KMP-NativeCoroutines
    @Suppress("UNCHECKED_CAST")
    private fun ClassDescriptor.getDeclarations(): List<DeclarationDescriptor> {
        val memberScope = unsubstitutedMemberScope
        if (memberScope !is LazyClassMemberScope) return emptyList()
        return AbstractLazyMemberScope::class.java.declaredMethods
            .first { it.name == "computeDescriptorsFromDeclaredElements" }
            .apply { isAccessible = true }
            .invoke(
                memberScope,
                DescriptorKindFilter.ALL,
                MemberScope.ALL_NAME_FILTER,
                NoLookupLocation.WHEN_GET_ALL_DESCRIPTORS
            ) as List<DeclarationDescriptor>
    }
}
