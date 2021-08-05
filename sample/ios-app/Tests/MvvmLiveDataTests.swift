//
//  MvvmLiveDataTests.swift
//  Tests
//
//  Created by Aleksey Mikhailov on 05.08.2021.
//  Copyright © 2021 IceRock Development. All rights reserved.
//

import XCTest
@testable import MultiPlatformLibrary
@testable import MultiPlatformLibrarySwift

class MvvmLiveDataTests: XCTestCase {

    private var label: UILabel!
    
    override func setUp() {
        self.label = UILabel()
        self.label.text = "empty"
    }

    func testLiveData() throws {
        let ld = MutableLiveData<NSString>(initialValue: "first")
        _ = label.bindText(liveData: ld)
        XCTAssertEqual(label.text, "first")
        ld.value = "second"
        XCTAssertEqual(label.text, "second")
    }
    
    func testViewModel() throws {
        let viewModel = TestViewModel()
        let emailText = UITextField()
        let passwordText = UITextField()
        let signInButton = UIButton()
        
        let _ = emailText.bindTextTwoWay(liveData: viewModel.login, formatter: { $0 }, reverseFormatter: { $0 })
        let _ = passwordText.bindTextTwoWay(liveData: viewModel.password, formatter: { $0 }, reverseFormatter: { $0 })
        let _ = signInButton.bindVisibility(liveData: viewModel.isFilled, inverted: false)
        
        XCTAssertEqual(emailText.text, "test")
        XCTAssertEqual(passwordText.text, "passwd")
        XCTAssertEqual(signInButton.isHidden, false)
    }
}
