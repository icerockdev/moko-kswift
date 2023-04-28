@file:Suppress("MaxLineLength")

/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import dev.icerock.moko.kswift.plugin.context.LibraryContext
import dev.icerock.moko.kswift.plugin.context.PackageFunctionContext
import dev.icerock.moko.kswift.plugin.feature.PlatformExtensionFunctionsFeature
import io.outfoxx.swiftpoet.ExtensionSpec
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.klib.KlibModuleMetadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PlatformExtensionsTest {
    @Test
    fun testBindExtensions() {
        val metadata: KlibModuleMetadata = readKLib("mpp-library.klib")
        val libraryContext = LibraryContext(metadata)

        assertNotNull(libraryContext)

        var checkedCount = 0

        libraryContext.visit { featureContext ->
            if (featureContext is PackageFunctionContext) {
                if (featureContext.func.name == "bindGenericText") {
                    testUILabelBindGenericText(featureContext)
                    checkedCount++
                }

                if (featureContext.func.name == "bindGenericAny") {
                    testUILabelBindGenericAny(featureContext)
                    checkedCount++
                }
            }
        }

        assertEquals(expected = 2, actual = checkedCount)
    }

    private fun testUILabelBindGenericText(featureContext: PackageFunctionContext) {
        val data: PlatformExtensionFunctionsFeature.Data? =
            PlatformExtensionFunctionsFeature.Processing.read(
                context = featureContext,
                moduleName = "shared"
            )

        assertNotNull(data)

        val extensionSpec: ExtensionSpec =
            PlatformExtensionFunctionsFeature.Processing.buildExtensionSpec(
                functionData = data,
                doc = "test doc"
            )

        assertEquals(
            expected =
            """
    public extension UIKit.UILabel {
    
      /**
       * test doc
       */
      @discardableResult
      public func bindGenericText<T : Foundation.NSString>(liveData: shared.LiveData<T>) -> shared.Closeable {
        return UILabelExtKt.bindGenericText(self, liveData: liveData as! shared.LiveData<Foundation.NSString>)
      }
    
    }"""
                .trimIndent(),
            actual = extensionSpec.toString().trim()
        )
    }

    private fun testUILabelBindGenericAny(featureContext: PackageFunctionContext) {
        val data: PlatformExtensionFunctionsFeature.Data? =
            PlatformExtensionFunctionsFeature.Processing.read(
                context = featureContext,
                moduleName = "shared"
            )

        assertNotNull(data)

        val extensionSpec: ExtensionSpec =
            PlatformExtensionFunctionsFeature.Processing.buildExtensionSpec(
                functionData = data,
                doc = "test doc"
            )

        assertEquals(
            expected =
            """
    public extension UIKit.UILabel {
    
      /**
       * test doc
       */
      @discardableResult
      public func bindGenericAny<T>(liveData: shared.LiveData<T>) -> shared.Closeable {
        return UILabelExtKt.bindGenericAny(self, liveData: liveData as! shared.LiveData<Swift.AnyObject>)
      }
    
    }"""
                .trimIndent(),
            actual = extensionSpec.toString().trim()
        )
    }

    @Test
    fun testSetHandlerExtensions() {
        val metadata: KlibModuleMetadata = readKLib("mvvm-livedata.klib")
        val libraryContext = LibraryContext(metadata)

        assertNotNull(libraryContext)

        var checkedCount = 0

        libraryContext.visit { featureContext ->
            if (featureContext is PackageFunctionContext) {
                if (
                    featureContext.func.receiverParameterType?.classifier is KmClassifier.TypeParameter &&
                    featureContext.func.name == "setEventHandler"
                ) {
                    testUIControlSetEventHandler(featureContext)
                    checkedCount++
                } else if (featureContext.func.name == "setEventHandler") {
                    testNotificationCenterSetEventHandler(featureContext)
                    checkedCount++
                }
            }
        }

        assertEquals(expected = 2, actual = checkedCount)
    }

    private fun testUIControlSetEventHandler(featureContext: PackageFunctionContext) {
        val data: PlatformExtensionFunctionsFeature.Data? =
            PlatformExtensionFunctionsFeature.Processing.read(
                context = featureContext,
                moduleName = "shared"
            )

        assertNotNull(data)

        val extensionSpec: ExtensionSpec =
            PlatformExtensionFunctionsFeature.Processing.buildExtensionSpec(
                functionData = data,
                doc = "test doc"
            )

        assertEquals(
            expected =
            """
    public extension UIKit.UIControl {
    
      /**
       * test doc
       */
      @discardableResult
      public func setEventHandler<T : UIKit.UIControl>(event: Swift.UInt64, lambda: @escaping (T) -> Swift.Void) -> shared.Closeable {
        return UIControlExtKt.setEventHandler(self, event: event, lambda: lambda as! (UIKit.UIControl) -> Swift.Void)
      }
    
    }"""
                .trimIndent(),
            actual = extensionSpec.toString().trim()
        )
    }

    private fun testNotificationCenterSetEventHandler(featureContext: PackageFunctionContext) {
        val data: PlatformExtensionFunctionsFeature.Data? =
            PlatformExtensionFunctionsFeature.Processing.read(
                context = featureContext,
                moduleName = "shared"
            )

        assertNotNull(data)

        val extensionSpec: ExtensionSpec =
            PlatformExtensionFunctionsFeature.Processing.buildExtensionSpec(
                functionData = data,
                doc = "test doc"
            )

        assertEquals(
            expected =
            """
    public extension Foundation.NotificationCenter {
    
      /**
       * test doc
       */
      @discardableResult
      public func setEventHandler<T : Swift.AnyObject>(
        notification: Swift.String,
        ref: T,
        lambda: @escaping (T) -> Swift.Void
      ) -> shared.Closeable {
        return NSNotificationCenterExtKt.setEventHandler(self, notification: notification, ref: ref, lambda: lambda as! (Swift.AnyObject) -> Swift.Void)
      }
    
    }"""
                .trimIndent(),
            actual = extensionSpec.toString().trim()
        )
    }
}
