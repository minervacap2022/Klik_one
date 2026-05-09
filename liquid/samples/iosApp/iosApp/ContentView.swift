import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let vc = MainViewControllerKt.MainViewController()
        // Register the Google Sign-In provider with the KMP singleton.
        // GoogleSignInProviderImpl finds the top VC at sign-in time so no reference is stored here.
        GoogleSignInService.shared.setProvider(provider: GoogleSignInProviderImpl())
        return vc
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
