/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import XCTest
@testable import MultiPlatformLibrary
@testable import MultiPlatformLibrarySwift

class AnyObjectSealedClassToEnum: XCTestCase {
    
    func testSuccessState() throws {
        if case .success(let value) = StatusKs(StatusKt.successStatus) {
            XCTAssertEqual(value.data, "hi!")
        } else { XCTFail() }
    }
    
    func testFailureState() throws {
        if case .failure(let value) = StatusKs(StatusKt.failureStatus) {
            XCTAssertTrue(value.exception is KotlinRuntimeException)
        } else { XCTFail() }
    }
}
