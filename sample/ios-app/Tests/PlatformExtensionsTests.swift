/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import XCTest
@testable import MultiPlatformLibrary
@testable import MultiPlatformLibrarySwift

class PlatformExtensionsTests: XCTestCase {
    
    private var label: UILabel!
    
    override func setUp() {
        self.label = UILabel()
        self.label.text = "empty"
    }

    func testClassProviderExtension() throws {
        let classProvider: CDataProvider<NSString> = CDataProvider(data: "data")
        label.fillByKotlin(provider: classProvider)
        
        XCTAssertEqual(label.text, "data")
    }
    
    func testExtensionWithoutArgs() throws {
        label.fillByKotlin()
        
        XCTAssertEqual(label.text, "filled by kotlin")
    }
    
    func testExtensionWithArg() throws {
        label.fillByKotlin(text: "test")
        
        XCTAssertEqual(label.text, "test")
    }
    
    func testInterfaceProviderExtension() throws {
        let interfaceProvider: IDataProvider = TestProvider()
        label.fillByKotlin(provider: interfaceProvider)
        
        XCTAssertEqual(label.text, "test interface")
    }
}

class TestProvider: NSObject, IDataProvider {
    func getData() -> Any? {
        return "test interface"
    }
}
