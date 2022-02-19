/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

import dev.icerock.moko.kswift.KSwiftExclude
import dev.icerock.moko.kswift.KSwiftOverrideName
import platform.UIKit.UIControl
import platform.UIKit.UIControlEvents
import platform.UIKit.UILabel

fun UILabel.fillByKotlin() {
    this.text = "filled by kotlin"
}

fun UILabel.fillByKotlin(text: String) {
    this.text = text
}

interface IDataProvider<T> {
    fun getData(): T
}

fun UILabel.fillByKotlin(
    @KSwiftOverrideName("provider_")
    provider: IDataProvider<String>
) {
    this.text = provider.getData()
}

class CDataProvider<T>(private val data: T) : IDataProvider<T> {
    override fun getData(): T = data
}

fun UILabel.fillByKotlin(provider: CDataProvider<String>) {
    this.text = provider.getData()
}

@KSwiftExclude
fun UILabel.excludedFun() = Unit

fun <T : UIControl> T.setEventHandler(
    event: UIControlEvents,
    lambda: (T) -> Unit
): Closeable {
    TODO()
}

/*
fun <T: Any> NSNotificationCenter.setEventHandler(
    notification: NSNotificationName,
    ref: T,
    lambda: T.() -> Unit
): Closeable {
    TODO()
}
*/
