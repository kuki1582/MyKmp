import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KmpSdk.shared.initialize(context: PlatformContext())
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
