[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Download](https://img.shields.io/maven-central/v/dev.icerock.moko/kswift-runtime) ](https://repo1.maven.org/maven2/dev/icerock/moko/kswift-runtime) ![kotlin-version](https://kotlin-version.aws.icerock.dev/kotlin-version?group=dev.icerock.moko&name=kswift-runtime)

# MOKO KSwift

KSwift it's gradle plugin for generation Swift-friendly API for Kotlin/Native framework.

## Kotlin sealed interface/class to Swift enum

![sealed classes compare](https://user-images.githubusercontent.com/5010169/128596479-688bce59-b224-402f-8f5b-8b32d33aa0b5.png)

## Kotlin extensions for K/N platform classes

![extensions compare](https://user-images.githubusercontent.com/5010169/128596630-28af4922-fcee-4eae-979b-0bad3722f0d2.png)

## Your own case

KSwift give you API for adding your own generator based on KLib metadata information.

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Samples](#samples)
- [Set Up Locally](#set-up-locally)
- [Contributing](#contributing)
- [License](#license)

## Features

- **API for extend logic for own cases** - just implement your own `ProcessorFeature`
- **Reading of all exported klibs** - you can generate swift additions to the api of external
  libraries
- **Kotlin sealed class/interface to Swift enum**
- **Kotlin extensions for platform classes to correct extensions** instead of additional class with
  static methods
- **Flexible filtration** - select what you want to generate and what not

## Requirements

- Gradle version 6.0+
- Kotlin 1.5.20

## Installation

root build.gradle

```groovy
buildscript {
    repositories {
        maven {
            gradlePluginPortal()
        }
    }
    dependencies {
        classpath("dev.icerock.moko:kswift-gradle-plugin:0.1.0")
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}
```

project build.gradle

```groovy
plugins {
    id("dev.icerock.moko.kswift")
}

dependencies {
    commonMainApi("dev.icerock.moko:kswift-runtime:0.1.0") // if you want use annotations
}
```

## Usage

...

## Samples

More examples can be found in the [sample directory](sample).

## Set Up Locally

- In [kswift directory](kswift) contains `kswift` library;
- In [sample directory](sample) contains samples on android, ios & mpp-library connected to apps.

## Contributing

All development (both new features and bug fixes) is performed in `develop` branch. This
way `master` sources always contain sources of the most recently released version. Please send PRs
with bug fixes to `develop` branch. Fixes to documentation in markdown files are an exception to
this rule. They are updated directly in `master`.

The `develop` branch is pushed to `master` during release.

More detailed guide for contributers see in [contributing guide](CONTRIBUTING.md).

## License

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