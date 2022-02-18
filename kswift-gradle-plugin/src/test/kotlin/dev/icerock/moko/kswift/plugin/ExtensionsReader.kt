package dev.icerock.moko.kswift.plugin

import dev.icerock.moko.kswift.plugin.context.LibraryContext
import dev.icerock.moko.kswift.plugin.context.PackageFunctionContext
import dev.icerock.moko.kswift.plugin.feature.PackageFunctionReader
import io.outfoxx.swiftpoet.ExtensionSpec
import kotlinx.metadata.klib.KlibModuleMetadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ExtensionsReader {
    @Test
    fun testExtensionsReading() {
        val metadata: KlibModuleMetadata = readKLib("mpp-library.klib")
        val libraryContext = LibraryContext(metadata)

        assertNotNull(libraryContext)

        libraryContext.visit { featureContext ->
            if (featureContext is PackageFunctionContext) {
                println("got package level function")

                if (featureContext.func.name == "bindGenericText") {
                    testUILabelBindGenericText(featureContext)
                }

                if(featureContext.func.name == "bindGenericAny") {
                    testUILabelBindGenericAny(featureContext)
                }
            }
        }
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
               * test doc */
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
               * test doc */
              public func bindGenericAny<T>(liveData: shared.LiveData<T>) -> shared.Closeable {
                return UILabelExtKt.bindGenericAny(self, liveData: liveData as! shared.LiveData<Swift.AnyObject>)
              }
            }
            """.trimIndent(),
            actual = extensionSpec.toString().trim()
        )
    }
}
