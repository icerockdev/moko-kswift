/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import XCTest
@testable import MultiPlatformLibrary
@testable import MultiPlatformLibrarySwift

// ===> NEED TO GENERATE
extension TestExternalGenericSealedClass {
    var withoutPropertyKs: ExternalGenericSealedClassKs<NSString, NSString> {
        get {
            return ExternalGenericSealedClassKs(self.withoutProperty)
        }
    }
    var withOnePropertyTKs: ExternalGenericSealedClassKs<NSString, NSString> {
        get {
            return ExternalGenericSealedClassKs(self.withOnePropertyT)
        }
    }
    var withOnePropertyUKs: ExternalGenericSealedClassKs<NSString, NSString> {
        get {
            return ExternalGenericSealedClassKs(self.withOnePropertyU)
        }
    }
    var withTwoPropertiesKs: ExternalGenericSealedClassKs<NSString, NSString> {
        get {
            return ExternalGenericSealedClassKs(self.withTwoProperties)
        }
    }
}
// <=== NEED TO GENERATE

class ExternalGenericSealedClassToSwiftEnumTests: XCTestCase {
    private let testSource = TestExternalGenericSealedClass()

    func testWithoutProperty() throws {
        if case .externalGenericWithoutProperty = testSource.withoutPropertyKs { } else { XCTFail() }
        XCTAssertEqual(testSource.withoutPropertyKs.sealed, ExternalGenericWithoutProperty())
    }

    func testWithOnePropertyT() throws {
        if case .externalGenericWithOnePropertyT(let data) = testSource.withOnePropertyTKs {
            XCTAssertEqual(data.value, "test")
        } else { XCTFail() }
        XCTAssertEqual(testSource.withOnePropertyTKs.sealed, ExternalGenericWithOnePropertyT<NSString>(value: "test"))
    }

    func testWithOnePropertyU() throws {
        if case .externalGenericWithOnePropertyU(let data) = testSource.withOnePropertyUKs {
            XCTAssertEqual(data.value, "test")
        } else { XCTFail() }
        XCTAssertEqual(testSource.withOnePropertyUKs.sealed, ExternalGenericWithOnePropertyU<NSString>(value: "test"))
    }

    func testWithTwoProperties() throws {
        if case .externalGenericWithTwoProperties(let data) = testSource.withTwoPropertiesKs {
            XCTAssertEqual(data.value1, "test1")
            XCTAssertEqual(data.value2, "test2")
        } else { XCTFail() }
        XCTAssertEqual(testSource.withTwoPropertiesKs.sealed, ExternalGenericWithTwoProperties<NSString, NSString>(value1: "test1", value2: "test2"))
    }
}
