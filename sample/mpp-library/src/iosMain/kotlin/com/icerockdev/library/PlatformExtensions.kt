/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.library

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

fun UILabel.fillByKotlin(provider: IDataProvider<String>) {
    this.text = provider.getData()
}

class CDataProvider<T>(private val data: T): IDataProvider<T> {
    override fun getData(): T = data
}

fun UILabel.fillByKotlin(provider: CDataProvider<String>) {
    this.text = provider.getData()
}
