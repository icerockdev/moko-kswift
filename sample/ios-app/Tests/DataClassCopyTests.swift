/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import XCTest
@testable import MultiPlatformLibrary
@testable import MultiPlatformLibrarySwift

/**
 copy method needs to be generated
 */
class DataClassCopyTests: XCTestCase {
    
    func givenADataClassWhenUsingBetterCopyThenCheckThatProvidedArgumentsChangeRelatedPropertiesAndNotProvidedArgumentsPropertiesStayTheSame() throws {
        let dataClass = DataClass(
            stringValue: "aValue",
            optionalStringValue: nil,
            intValue: 0,
            optionalIntValue: nil,
            booleanValue: false,
            optionalBooleanValue: nil
            )
        let dataClassWithNewValues = dataClass.copy(stringValue: {"aNewValue"}, intValue: {1}, booleanValue: {true})
        XCTAssertEqual("aValue", dataClass.stringValue)
        XCTAssertEqual("aNewValue", dataClassWithNewValues.stringValue)
        XCTAssertEqual(dataClass.optionalStringValue, dataClassWithNewValues.optionalStringValue)
        XCTAssertEqual(0, dataClass.intValue)
        XCTAssertEqual(1, dataClassWithNewValues.intValue)
        XCTAssertEqual(dataClass.optionalIntValue, dataClassWithNewValues.optionalIntValue)
        XCTAssertFalse(dataClass.booleanValue)
        XCTAssertTrue(dataClassWithNewValues.booleanValue)
        XCTAssertEqual(dataClass.optionalBooleanValue, dataClassWithNewValues.optionalBooleanValue)
    }
    
    func givenADataClassWhenUsingBetterCopyThenCheckThatProvidedArgumentsChangeRelatedOptionalPropertiesAndNotProvidedArgumentsPropertiesStayTheSame() throws {
        let dataClass = DataClass(
            stringValue: "aValue",
            optionalStringValue: nil,
            intValue: 0,
            optionalIntValue: nil,
            booleanValue: false,
            optionalBooleanValue: nil
            )
        let dataClassWithNewValues = dataClass.copy(optionalStringValue: {"aNewOptionalValue"}, optionalIntValue: {1}, optionalBooleanValue: {true})
        XCTAssertEqual(dataClass.stringValue, dataClassWithNewValues.stringValue)
        XCTAssertEqual(nil, dataClass.optionalStringValue)
        XCTAssertEqual("aNewOptionalValue", dataClassWithNewValues.optionalStringValue)
        XCTAssertEqual(dataClass.intValue, dataClassWithNewValues.intValue)
        XCTAssertEqual(nil, dataClass.optionalIntValue)
        XCTAssertEqual(1, dataClassWithNewValues.optionalIntValue)
        XCTAssertEqual(dataClass.booleanValue, dataClassWithNewValues.booleanValue)
        XCTAssertNil(dataClass.optionalBooleanValue)
        XCTAssertTrue(dataClassWithNewValues.optionalBooleanValue!.boolValue)
    }
    
    func givenADataClassWhenUsingBetterCopyToSetAPropertyAsNilThenCheckThatIsPossibleToDoIt() throws {
        let dataClass = DataClass(
            stringValue: "aValue",
            optionalStringValue: "aNonOptionalValue",
            intValue: 0,
            optionalIntValue: 10,
            booleanValue: false,
            optionalBooleanValue: true
            )
        let dataClassWithNewValues = dataClass.copy(optionalStringValue: {nil}, optionalIntValue: {nil}, optionalBooleanValue: {nil})
        XCTAssertEqual(dataClass.stringValue, dataClassWithNewValues.stringValue)
        XCTAssertEqual("aNonOptionalValue", dataClass.optionalStringValue)
        XCTAssertNil(dataClassWithNewValues.optionalStringValue)
        XCTAssertEqual(dataClass.intValue, dataClassWithNewValues.intValue)
        XCTAssertEqual(10, dataClass.optionalIntValue)
        XCTAssertNil(dataClassWithNewValues.optionalIntValue)
        XCTAssertEqual(dataClass.booleanValue, dataClassWithNewValues.booleanValue)
        XCTAssertTrue(dataClass.optionalBooleanValue!.boolValue)
        XCTAssertNil(dataClassWithNewValues.optionalBooleanValue?.boolValue)
    }
    
}
