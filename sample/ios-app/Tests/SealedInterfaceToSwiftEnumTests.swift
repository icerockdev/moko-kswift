//
//  SealedToSwiftEnumTests.swift
//  Tests
//
//  Created by Aleksey Mikhailov on 06.08.2021.
//  Copyright © 2021 IceRock Development. All rights reserved.
//

import XCTest
@testable import MultiPlatformLibrary
@testable import MultiPlatformLibrarySwift

// ===> NEED TO GENERATE
enum UIStateKs<T: AnyObject> {
    case loading
    case empty
    case data(obj: UIStateData<T>)
    case error(obj: UIStateError)
    
    init(_ obj: UIState) {
        if obj is UIStateLoading {
            self = .loading
        } else if obj is UIStateEmpty {
            self = .empty
        } else if let obj = obj as? UIStateData<T> {
            self = .data(obj: obj)
        } else if let obj = obj as? UIStateError {
            self = .error(obj: obj)
        } else {
            fatalError("UIStateKs not syncronized with UIState class")
        }
    }
}

extension TestStateSource {
    var loadingKs: UIStateKs<NSString> {
        get {
            return UIStateKs<NSString>(self.loading)
        }
    }
    var emptyKs: UIStateKs<NSString> {
        get {
            return UIStateKs<NSString>(self.empty)
        }
    }
    var errorKs: UIStateKs<NSString> {
        get {
            return UIStateKs<NSString>(self.error)
        }
    }
    var dataKs: UIStateKs<NSString> {
        get {
            return UIStateKs<NSString>(self.data)
        }
    }
}
// <=== NEED TO GENERATE

class SealedToSwiftEnumTests: XCTestCase {
    private let testSource = TestStateSource()
    
    func testLoadingState() throws {
        if case .loading = testSource.loadingKs { } else { XCTFail() }
    }
    
    func testEmptyState() throws {
        if case .empty = testSource.emptyKs { } else { XCTFail() }
    }
    
    func testDataState() throws {
        if case .data(let data) = testSource.dataKs {
            XCTAssertEqual(data.value, "test")
        } else { XCTFail() }
    }
    
    func testErrorState() throws {
        if case .error(let error) = testSource.errorKs {
            XCTAssertTrue(error.throwable is KotlinIllegalStateException)
        } else { XCTFail() }
    }
}
