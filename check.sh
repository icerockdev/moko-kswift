#
# Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
#

./gradlew -p kswift-gradle-plugin build &&
./gradlew build syncMultiPlatformLibraryDebugFrameworkIosX64 &&
cd sample/ios-app && set -o pipefail && xcodebuild -scheme ios-app -workspace ios-app.xcworkspace test -destination "platform=iOS Simulator,name=iPhone 12 mini" CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO | xcpretty
