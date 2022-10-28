/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import XCTest
@testable import MultiPlatformLibrary
@testable import MultiPlatformLibrarySwift

// ===> NEED TO GENERATE
extension ExternalNonGenericSealedClass {
    var withoutPropertyKs: ExternalNonGenericSealedClassKs {
        get {
            return ExternalNonGenericSealedClassKs(ExternalNonGenericWithoutProperty())
        }
    }
    var withPropertyKs: ExternalNonGenericSealedClassKs {
        get {
            return ExternalNonGenericSealedClassKs(ExternalNonGenericWithProperty(value: "test"))
        }
    }
}
// <=== NEED TO GENERATE

class ExternalNonGenericSealedClassToSwiftEnumTests: XCTestCase {
    private let testSource = ExternalNonGenericSealedClass()

    func testWithoutProperty() throws {
        if case .externalNonGenericWithoutProperty = testSource.withoutPropertyKs { } else { XCTFail() }
        XCTAssertEqual(testSource.withoutPropertyKs.sealed, ExternalNonGenericWithoutProperty())
    }

    func testWithProperty() throws {
        if case .externalNonGenericWithProperty(let data) = testSource.withPropertyKs {
            XCTAssertEqual(data.value, "test")
        } else { XCTFail() }
        XCTAssertEqual(testSource.withPropertyKs.sealed, ExternalNonGenericWithProperty(value: "test"))
    }
}
