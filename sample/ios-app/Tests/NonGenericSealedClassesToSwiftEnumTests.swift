/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import XCTest
@testable import MultiPlatformLibrary
@testable import MultiPlatformLibrarySwift

// ===> NEED TO GENERATE
extension NonGenericSealedClass {
    var withoutPropertyKs: NonGenericSealedClassKs {
        get {
            return NonGenericSealedClassKs(WithoutProperty())
        }
    }
    var withPropertyKs: NonGenericSealedClassKs {
        get {
            return NonGenericSealedClassKs(WithProperty(value: "test"))
        }
    }
}
// <=== NEED TO GENERATE

class NonGenericSealedClassToSwiftEnumTests: XCTestCase {
    private let testSource = NonGenericSealedClass()

    func testWithoutProperty() throws {
        if case .withoutProperty = testSource.withoutPropertyKs { } else { XCTFail() }
        XCTAssertEqual(testSource.withoutPropertyKs.sealed, NonGenericSealedClass.WithoutProperty())
    }

    func testWithProperty() throws {
        if case .withProperty(let data) = testSource.withPropertyKs {
            XCTAssertEqual(data.value, "test")
        } else { XCTFail() }
        XCTAssertEqual(testSource.withPropertyKs.sealed, NonGenericSealedClass.WithProperty(value: "test"))
    }
}
