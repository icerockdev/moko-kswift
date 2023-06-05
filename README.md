![moko-kswift](https://user-images.githubusercontent.com/5010169/128708262-81403e12-4ddb-4ff6-8d9d-feda2fd95bf3.png)  
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Download](https://img.shields.io/maven-central/v/dev.icerock.moko/kswift-runtime) ](https://repo1.maven.org/maven2/dev/icerock/moko/kswift-runtime) ![kotlin-version](https://kotlin-version.aws.icerock.dev/kotlin-version?group=dev.icerock.moko&name=kswift-runtime)

# MOKO KSwift

KSwift it's gradle plugin for generation Swift-friendly API for Kotlin/Native framework.

## Kotlin sealed interface/class to Swift enum

![sealed classes compare](https://user-images.githubusercontent.com/5010169/128596479-688bce59-b224-402f-8f5b-8b32d33aa0b5.png)

## Kotlin extensions for K/N platform classes

![extensions compare](https://user-images.githubusercontent.com/5010169/128596630-28af4922-fcee-4eae-979b-0bad3722f0d2.png)

## Your own case

KSwift give you API for adding your own generator based on KLib metadata information.

# Posts

- [How to implement Swift-friendly API with Kotlin Multiplatform Mobile](https://medium.com/icerock/how-to-implement-swift-friendly-api-with-kotlin-multiplatform-mobile-e68521a63b6d)

# Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [FAQ](#faq)
- [Samples](#samples)
- [Set Up Locally](#set-up-locally)
- [Contributing](#contributing)
- [License](#license)

# Features

- **API for extend logic for own cases** - just implement your own `ProcessorFeature`
- **Reading of all exported klibs** - you can generate swift additions to the api of external
  libraries
- **Kotlin sealed class/interface to Swift enum**
- **Kotlin extensions for platform classes to correct extensions** instead of additional class with
  static methods
- **Flexible filtration** - select what you want to generate and what not

# Requirements

- Gradle version 6.0+
- Kotlin 1.6.10

# Installation

## Plugin

### Using legacy plugin application

root `build.gradle`

```groovy
buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("dev.icerock.moko:kswift-gradle-plugin:0.6.1")
    }
}
```

project where framework compiles `build.gradle`

```groovy
plugins {
    id("dev.icerock.moko.kswift")
}
```

### Using the plugins DSL

`settings.gradle`

```groovy
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
```

project where framework compiles `build.gradle`

```groovy
plugins {
    id("dev.icerock.moko.kswift") version "0.6.1"
}
```

## Runtime library

root `build.gradle`

```groovy
allprojects {
    repositories {
        mavenCentral()
    }
}
```

project `build.gradle`

```groovy
dependencies {
    commonMainApi("dev.icerock.moko:kswift-runtime:0.6.1") // if you want use annotations
}
```

# Usage

## Xcode configuration

The Swift code generated from this plugin is not automatically included in the shared framework you might have.

You have 2 options to use it in your iOS project:
- Xcode direct file integration
- CocoaPods integration

### Xcode direct file integration

You can directly import the generated file in your Xcode project like it's a file you have written on your own.

To do so:
- open the Xcode project
- right click on "iosApp"
- choose "Add files to iOSApp"
- add the file from the generated folder (you might need to read the FAQ to know where the generated folder is)
- you are now good to go!

### CocoaPods integration

After you have added the moko-kswift plugin to your shared module and synced your project, a new Gradle task should appear with name `kSwiftXXXXXPodspec` where `XXXXX` is the name of your shared module (so your task might be named `kSwiftsharedPodspec`).

- Run the task doing `./gradlew kSwiftsharedPodspec` from the root of your project.
  This will generate a new podspec file, `XXXXXSwift.podspec`, where `XXXXX` is still the name of your shared module (so e.g. `sharedSwift.podspec`)

- Now edit the `Podfile` inside the iOS project adding this line
  `pod 'sharedSwift', :path => '../shared'`
  just after the one already there for the already available shared module
  `pod 'shared', :path => '../shared'`

- Now run `pod install` from the `iosApp` folder so the new framework is linked to your project.

- Whenever you need a Swift file generated from moko-kswift just import the generated module (e.g. `import sharedSwift`) and you are good to go!

## Sealed classes/interfaces to Swift enum

Enable feature in project `build.gradle`:

kotlin:
```kotlin
kswift {
    install(dev.icerock.moko.kswift.plugin.feature.SealedToSwiftEnumFeature)
}
```

groovy:
```groovy
kswift {
    install(dev.icerock.moko.kswift.plugin.feature.SealedToSwiftEnumFeature.factory)
}
```

That's all - after this setup all sealed classes and sealed interfaces will be parsed by plugin and
plugin will generate Swift enums for this classes.

For example if you have in your kotlin code:

```kotlin
sealed interface UIState<out T> {
    object Loading : UIState<Nothing>
    object Empty : UIState<Nothing>
    data class Data<T>(val value: T) : UIState<T>
    data class Error(val throwable: Throwable) : UIState<Nothing>
}
```

Then plugin will generate source code:

```swift
/**
 * selector: ClassContext/moko-kswift.sample:mpp-library-pods/com/icerockdev/library/UIState */
public enum UIStateKs<T : AnyObject> {

  case loading
  case empty
  case data(UIStateData<T>)
  case error(UIStateError)

  public init(_ obj: UIState) {
    if obj is shared.UIStateLoading {
      self = .loading
    } else if obj is shared.UIStateEmpty {
      self = .empty
    } else if let obj = obj as? shared.UIStateData<T> {
      self = .data(obj)
    } else if let obj = obj as? shared.UIStateError {
      self = .error(obj)
    } else {
      fatalError("UIStateKs not syncronized with UIState class")
    }
  }

}
```

For each generated entry in comment generated `selector` - value of this selector can be used for
filter. By default all entries generated. But if generated code invalid (please report issue in this
case) you can disable generation of this particular entry:

kotlin:
```kotlin
kswift {
    install(dev.icerock.moko.kswift.plugin.feature.SealedToSwiftEnumFeature) {
        filter = excludeFilter("ClassContext/moko-kswift.sample:mpp-library-pods/com/icerockdev/library/UIState")
    }
}
```

groovy:
```groovy
kswift {
    install(dev.icerock.moko.kswift.plugin.feature.SealedToSwiftEnumFeature.factory) {
        it.filter = it.excludeFilter("ClassContext/moko-kswift.sample:mpp-library-pods/com/icerockdev/library/UIState")
    }
}
```

As alternative you can use `includeFilter` to explicit setup each required for generation entries:

kotlin:
```kotlin
kswift {
    install(dev.icerock.moko.kswift.plugin.feature.SealedToSwiftEnumFeature) {
        filter = includeFilter("ClassContext/moko-kswift.sample:mpp-library-pods/com/icerockdev/library/UIState")
    }
}
```

groovy:
```groovy
kswift {
    install(dev.icerock.moko.kswift.plugin.feature.SealedToSwiftEnumFeature.factory) {
        it.filter = it.includeFilter("ClassContext/moko-kswift.sample:mpp-library-pods/com/icerockdev/library/UIState")
    }
}
```

## Extensions from platform classes

Enable feature in project `build.gradle`:

kotlin:
```kotlin
kswift {
    install(dev.icerock.moko.kswift.plugin.feature.PlatformExtensionFunctionsFeature)
}
```

groovy:
```groovy
kswift {
    install(dev.icerock.moko.kswift.plugin.feature.PlatformExtensionFunctionsFeature.factory)
}
```

That's all - after this setup all extension functions for classes from `platform.*` package will be
correct swift code.

For example if you have in your kotlin code:

```kotlin
class CFlow<T>(private val stateFlow: StateFlow<T>) : StateFlow<T> by stateFlow

fun UILabel.bindText(coroutineScope: CoroutineScope, flow: CFlow<String>) {
    val label = this
    coroutineScope.launch {
        label.text = flow.value
        flow.collect { label.text = it }
    }
}
```

Then plugin will generate source code:

```swift
public extension UIKit.UILabel {
  /**
   * selector: PackageFunctionContext/moko-kswift.sample:mpp-library/com.icerockdev.library/Class(name=platform/UIKit/UILabel)/bindText/coroutineScope:Class(name=kotlinx/coroutines/CoroutineScope),flow:Class(name=com/icerockdev/library/CFlow)<Class(name=kotlin/String)> */
  public func bindText(coroutineScope: CoroutineScope, flow: CFlow<NSString>) {
    return UILabelExtKt.bindText(self, coroutineScope: coroutineScope, flow: flow)
  }
}
```

Selector from comment can be used for filters as in first example.

## Implementation of own generator

First create `buildSrc`, if you don't. `build.gradle` will contains:

```groovy
plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()

    maven("https://jitpack.io")
}

dependencies {
    implementation("com.android.tools.build:gradle:7.0.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
    implementation("dev.icerock.moko:kswift-gradle-plugin:0.2.0")
}
```

Then in `buildSrc/src/main/kotlin` create `MyKSwiftGenerator`:

```kotlin
import dev.icerock.moko.kswift.plugin.context.ClassContext
import dev.icerock.moko.kswift.plugin.feature.ProcessorContext
import dev.icerock.moko.kswift.plugin.feature.ProcessorFeature
import dev.icerock.moko.kswift.plugin.feature.BaseConfig
import dev.icerock.moko.kswift.plugin.feature.Filter
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FileSpec
import kotlin.reflect.KClass


class MyKSwiftGenerator(
    override val featureContext: KClass<ClassContext>,
    override val filter: Filter<ClassContext>
) : ProcessorFeature<ClassContext>() {
    override fun doProcess(featureContext: ClassContext, processorContext: ProcessorContext) {
        val fileSpec: FileSpec.Builder = processorContext.fileSpecBuilder
        val frameworkName: String = processorContext.framework.baseName

        val classSimpleName = featureContext.clazz.name.substringAfterLast('/')

        fileSpec.addExtension(
            ExtensionSpec
                .builder(
                    DeclaredTypeName.typeName("$frameworkName.$classSimpleName")
                )
                .build()
        )
    }

    class Config : BaseConfig<ClassContext> {
        override var filter: Filter<ClassContext> = Filter.Exclude(emptySet())
    }

    companion object : Factory<ClassContext, MyKSwiftGenerator, Config> {
        override fun create(block: Config.() -> Unit): MyKSwiftGenerator {
            val config = Config().apply(block)
            return MyKSwiftGenerator(featureContext, config.filter)
        }

        override val featureContext: KClass<ClassContext> = ClassContext::class

        @JvmStatic
        override val factory = Companion
    }
}
```

in this example will be generated swift extension for each class in kotlin module. You can select
required `Context` to got required info from klib metadata.

last step - enable feature in gradle:

kotlin:
```kotlin
kswift {
    install(MyKSwiftGenerator)
}
```

groovy:
```groovy
kswift {
    install(MyKSwiftGenerator.factory)
}
```

## Set iOS deployment target for podspec

kotlin:
```kotlin
kswift {
    iosDeploymentTarget.set("11.0")
}
```

groovy:
```groovy
kswift {
    iosDeploymentTarget = "11.0"
}
```

# FAQ

## Where destination directory for all generated sources?

Swift source code generates in same directory where compiles Kotlin/Native framework. In common case
it directory
`build/bin/{iosArm64 || iosX64}/{debugFramework || releaseFramework}/{frameworkName}Swift`.

Kotlin/Native cocoapods plugin (and also mobile-multiplatform cocoapods plugin by IceRock) will move
this sources into fixed directory - `build/cocoapods/framework/{frameworkName}Swift`.

## How to exclude generation of entries from some libraries?

```groovy
kswift {
    excludeLibrary("{libraryName}")
}
```

## How to generate entries only from specific libraries?

```groovy
kswift {
    includeLibrary("{libraryName1}")
    includeLibrary("{libraryName2}")
}
```

# Samples

More examples can be found in the [sample directory](sample).

# Set Up Locally

Clone project and just open it. Gradle plugin attached to sample by gradle composite build, so you
will see changes at each gradle build.

```bash
# clone repo
git clone git@github.com:icerockdev/moko-kswift.git
cd moko-kswift 
# generate podspec files for cocopods intergration. with integration will be generated swift files for cocoapod
./gradlew kSwiftmpp_library_podsPodspec
./gradlew kSwiftMultiPlatformLibraryPodspec
# go to ios dir
cd sample/ios-app
# install pods
pod install
# now we can open xcworkspace and build ios project
open ios-app.xcworkspace
# or run xcodebuild
xcodebuild -scheme ios-app -workspace ios-app.xcworkspace test -destination "platform=iOS Simulator,name=iPhone 12 mini"
xcodebuild -scheme pods-test -workspace ios-app.xcworkspace test -destination "platform=iOS Simulator,name=iPhone 12 mini"
```

# Contributing

All development (both new features and bug fixes) is performed in `develop` branch. This
way `master` sources always contain sources of the most recently released version. Please send PRs
with bug fixes to `develop` branch. Fixes to documentation in markdown files are an exception to
this rule. They are updated directly in `master`.

The `develop` branch is pushed to `master` during release.

More detailed guide for contributers see in [contributing guide](CONTRIBUTING.md).

# License

    Copyright 2021 IceRock MAG Inc
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
