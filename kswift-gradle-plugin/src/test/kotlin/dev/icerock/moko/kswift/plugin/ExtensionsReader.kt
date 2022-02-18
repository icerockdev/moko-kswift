package dev.icerock.moko.kswift.plugin

import dev.icerock.moko.kswift.plugin.context.LibraryContext
import dev.icerock.moko.kswift.plugin.context.PackageFunctionContext
import dev.icerock.moko.kswift.plugin.feature.PackageFunctionReader
import io.outfoxx.swiftpoet.ExtensionSpec
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.klib.KlibModuleMetadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ExtensionsReader {
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
        val data: PackageFunctionReader.Data? = PackageFunctionReader.read(
            featureContext,
            "shared"
        )

        assertNotNull(data)

        val extensionSpec: ExtensionSpec = PackageFunctionReader.buildExtensionSpec(
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
            }
            """.trimIndent(),
            actual = extensionSpec.toString().trim()
        )
    }

    private fun testUILabelBindGenericAny(featureContext: PackageFunctionContext) {
        val data: PackageFunctionReader.Data? = PackageFunctionReader.read(
            featureContext,
            "shared"
        )

        assertNotNull(data)

        val extensionSpec: ExtensionSpec = PackageFunctionReader.buildExtensionSpec(
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
            }
            """.trimIndent(),
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
        val data: PackageFunctionReader.Data? = PackageFunctionReader.read(
            featureContext,
            "shared"
        )

        assertNotNull(data)

        val extensionSpec: ExtensionSpec = PackageFunctionReader.buildExtensionSpec(
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
              public func setEventHandler<T : UIKit.UIControl>(event: shared.ULong, lambda: @escaping (T) -> Swift.Void) -> shared.Closeable {
                return UIControlExtKt.setEventHandler(self, event: event, lambda: lambda as! @escaping (UIKit.UIControl) -> Swift.Void)
              }
            }
            """.trimIndent(),
            actual = extensionSpec.toString().trim()
        )
    }

    private fun testNotificationCenterSetEventHandler(featureContext: PackageFunctionContext) {
        val data: PackageFunctionReader.Data? = PackageFunctionReader.read(
            featureContext,
            "shared"
        )

        assertNotNull(data)

        val extensionSpec: ExtensionSpec = PackageFunctionReader.buildExtensionSpec(
            functionData = data,
            doc = "test doc"
        )

        assertEquals(
            expected =
            """
            public extension Foundation.NSNotificationCenter {
              /**
               * test doc
               */
              @discardableResult
              public func setEventHandler<T : Swift.AnyObject>(
                notification: Swift.String,
                ref: T,
                lambda: @escaping (T) -> Swift.Void
              ) -> shared.Closeable {
                return NSNotificationCenterExtKt.setEventHandler(self, notification: notification, ref: ref, lambda: lambda as! @escaping (Swift.AnyObject) -> Swift.Void)
              }
            }
            """.trimIndent(),
            actual = extensionSpec.toString().trim()
        )
    }
}
