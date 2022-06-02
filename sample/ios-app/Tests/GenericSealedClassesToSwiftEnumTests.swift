/*
 * Copyright 2022 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import XCTest
@testable import MultiPlatformLibrary
@testable import MultiPlatformLibrarySwift

// ===> NEED TO GENERATE
extension TestGenericSealedClass {
    var withoutPropertyKs: GenericSealedClassKs<NSString, NSString> {
        get {
            return GenericSealedClassKs(self.withoutProperty)
        }
    }
    var withOnePropertyTKs: GenericSealedClassKs<NSString, NSString> {
        get {
            return GenericSealedClassKs(self.withOnePropertyT)
        }
    }
    var withOnePropertyUKs: GenericSealedClassKs<NSString, NSString> {
        get {
            return GenericSealedClassKs(self.withOnePropertyU)
        }
    }
    var withTwoPropertiesKs: GenericSealedClassKs<NSString, NSString> {
        get {
            return GenericSealedClassKs(self.withTwoProperties)
        }
    }
}
// <=== NEED TO GENERATE

class GenericSealedClassToSwiftEnumTests: XCTestCase {
    private let testSource = TestGenericSealedClass()

    func testWithoutProperty() throws {
        if case .withoutProperty = testSource.withoutPropertyKs { } else { XCTFail() }
        XCTAssertEqual(testSource.withoutPropertyKs.sealed, GenericSealedClassWithoutProperty())
    }

    func testWithOnePropertyT() throws {
        if case .withOnePropertyT(let data) = testSource.withOnePropertyTKs {
            XCTAssertEqual(data.value, "test")
        } else { XCTFail() }
        XCTAssertEqual(testSource.withOnePropertyTKs.sealed, GenericSealedClassWithOnePropertyT<NSString>(value: "test"))
    }

    func testWithOnePropertyU() throws {
        if case .withOnePropertyU(let data) = testSource.withOnePropertyUKs {
            XCTAssertEqual(data.value, "test")
        } else { XCTFail() }
        XCTAssertEqual(testSource.withOnePropertyUKs.sealed, GenericSealedClassWithOnePropertyU<NSString>(value: "test"))
    }

    func testWithTwoProperties() throws {
        if case .withTwoProperties(let data) = testSource.withTwoPropertiesKs {
            XCTAssertEqual(data.value1, "test1")
            XCTAssertEqual(data.value2, "test2")
        } else { XCTFail() }
        XCTAssertEqual(testSource.withTwoPropertiesKs.sealed, GenericSealedClassWithTwoProperties<NSString, NSString>(value1: "test1", value2: "test2"))
    }
}
