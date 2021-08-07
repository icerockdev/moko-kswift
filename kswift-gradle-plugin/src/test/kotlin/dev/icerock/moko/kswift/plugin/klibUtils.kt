/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.kswift.plugin

import kotlinx.metadata.klib.KlibModuleMetadata
import java.io.File
import java.io.InputStream

fun readKLib(name: String): KlibModuleMetadata {
    val classLoader = KmClassExtKtTest::class.java.classLoader
    val inputStream: InputStream = classLoader.getResourceAsStream(name)
    val tempFile: File = File.createTempFile("kswift-test", ".klib")
    tempFile.outputStream().use { output ->
        inputStream.use { input ->
            input.copyTo(output)
        }
    }
    return KotlinMetadataLibraryProvider.readLibraryMetadata(tempFile).also {
        tempFile.delete()
    }
}
