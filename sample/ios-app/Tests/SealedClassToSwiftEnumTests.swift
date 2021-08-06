//
//  SealedClassToSwiftEnumTests.swift
//  Tests
//
//  Created by Aleksey Mikhailov on 06.08.2021.
//  Copyright © 2021 IceRock Development. All rights reserved.
//

import XCTest
@testable import MultiPlatformLibrary
@testable import MultiPlatformLibrarySwift

// ===> NEED TO GENERATE
enum UIStateClassKs<T: AnyObject> {
    case loading
    case empty
    case data(obj: UIStateClassData<T>)
    case error(obj: UIStateClassError)
    
    init(_ obj: UIStateClass<T>) {
        if obj is UIStateClassLoading {
            self = .loading
        } else if obj is UIStateClassEmpty {
            self = .empty
        } else if let obj = obj as? UIStateClassData<T> {
            self = .data(obj: obj)
        } else if let obj = obj as? UIStateClassError {
            self = .error(obj: obj)
        } else {
            fatalError("UIStateClassKs not syncronized with UIStateClass class")
        }
    }
}

extension TestStateClassSource {
    var loadingKs: UIStateClassKs<NSString> {
        get {
            return UIStateClassKs<NSString>(self.loading)
        }
    }
    var emptyKs: UIStateClassKs<NSString> {
        get {
            return UIStateClassKs<NSString>(self.empty)
        }
    }
    var errorKs: UIStateClassKs<NSString> {
        get {
            return UIStateClassKs<NSString>(self.error)
        }
    }
    var dataKs: UIStateClassKs<NSString> {
        get {
            return UIStateClassKs<NSString>(self.data)
        }
    }
}
// <=== NEED TO GENERATE

class SealedClassToSwiftEnumTests: XCTestCase {
    private let testSource = TestStateClassSource()
    
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
