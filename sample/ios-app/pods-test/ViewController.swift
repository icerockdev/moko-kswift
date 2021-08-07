//
//  ViewController.swift
//  pods-test
//
//  Created by Aleksey Mikhailov on 07.08.2021.
//  Copyright © 2021 IceRock Development. All rights reserved.
//

import UIKit
import shared
import sharedSwift

class ViewController: UIViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let testSource = TestStateSource()
        let state = UIStateKs<NSString>(testSource.error)
        
        switch(state) {
        case .empty:
            print("empty state")
            break
        case .loading:
            print("loading state")
            break
        case .data(let obj):
            print("data state with \(obj.value ?? "nil")")
            break
        case .error(let obj):
            print("error state with \(obj.throwable)")
            break
        }
    }
}

