//
//  Tests.swift
//  Tests
//
//  Created by Aleksey Mikhailov on 04.08.2021.
//  Copyright © 2021 IceRock Development. All rights reserved.
//

import XCTest
@testable import MultiPlatformLibrary
@testable import MultiPlatformLibrarySwift

class PlatformExtensionsTests: XCTestCase {

    func testClassProviderExtension() throws {
        let label = UILabel()
        label.text = "empty"
        
        let classProvider: CDataProvider<NSString> = CDataProvider(data: "data")
        label.fillByKotlin(provider: classProvider)
        
        XCTAssertEqual(label.text, "data", "label should contain filled data")
    }
}
