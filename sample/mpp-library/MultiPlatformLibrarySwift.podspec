Pod::Spec.new do |spec|
    spec.name                     = 'MultiPlatformLibrarySwift'
    spec.version                  = '0.1.0'
    spec.homepage                 = 'Link to a Kotlin/Native module homepage'
    spec.source                   = { :git => "Not Published", :tag => "Cocoapods/#{spec.name}/#{spec.version}" }
    spec.authors                  = 'IceRock Development'
    spec.license                  = ''
    spec.summary                  = 'Shared code between iOS and Android'

    spec.dependency 'MultiPlatformLibrary'

    spec.source_files = "build/cocoapods/framework/MultiPlatformLibrarySwift/**/*.{h,m,swift}"
end
