Pod::Spec.new do |spec|
    spec.name                     = 'MultiPlatformLibrarySwift'
    spec.version                  = '1.0'
    spec.homepage                 = 'Link to a Kotlin/Native module homepage'
    spec.source                   = { :git => "Not Published", :tag => "Cocoapods/#{spec.name}/#{spec.version}" }
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Some description for a Kotlin/Native module'
    spec.module_name              = "MultiPlatformLibrarySwift"

    spec.static_framework         = false
    spec.dependency 'MultiPlatformLibrary'
    spec.source_files = "build/cocoapods/framework/MultiPlatformLibrarySwift/**/*.{h,m,swift}"
end