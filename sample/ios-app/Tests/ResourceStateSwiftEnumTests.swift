/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import XCTest
@testable import MultiPlatformLibrary
@testable import MultiPlatformLibrarySwift

// ===> NEED TO GENERATE
extension TestViewModel {
    var stateKs: ResourceStateKs<NSString, StringDesc> {
        get {
            return ResourceStateKs<NSString, StringDesc>(self.state)
        }
    }
}
// <=== NEED TO GENERATE

class ResourceStateSwiftEnumTests: XCTestCase {
    private let testSource = TestViewModel()
    
    func testStates() throws {
        if case .empty(_) = testSource.stateKs { } else { XCTFail() }
        
        testSource.changeState()
        
        if case .loading(_) = testSource.stateKs { } else { XCTFail() }
        
        testSource.changeState()
        
        if case .success(let obj) = testSource.stateKs {
            XCTAssertEqual(obj.data, "data")
        } else { XCTFail() }
        
        testSource.changeState()
        
        if case .failed(let obj) = testSource.stateKs {
            XCTAssertEqual(obj.error?.localized(), "broke")
        } else { XCTFail() }
    }
}
