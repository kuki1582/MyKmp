import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KmpSdk.shared.initialize(context: PlatformContext())
        #if DEBUG
        // デバッグビルドのときだけ、Store の Intent / Action / SideEffect を Xcode のコンソールに出す
        KmpSdk.shared.enableLogging()
        #endif
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
