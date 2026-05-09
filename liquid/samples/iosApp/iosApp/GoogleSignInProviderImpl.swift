import Foundation
import GoogleSignIn
import UIKit
import ComposeApp

/**
 * Swift implementation of the KMP GoogleSignInProvider interface.
 *
 * Wraps GIDSignIn (Google Identity Services iOS SDK). The SDK is configured with:
 *   clientID       = IOS_CLIENT_ID  — for the sign-in sheet UI
 *   serverClientID = WEB_CLIENT_ID  — forces idToken.aud == web_client_id so the backend
 *                                     verifies it identically to tokens from the web GIS flow.
 *
 * The implementing class is kept in Swift so the Google SDK dependency never touches KMP.
 * Kotlin calls through the GoogleSignInProvider protocol (ObjC-compatible interface).
 */
class GoogleSignInProviderImpl: NSObject, GoogleSignInProvider {

    // IOS_CLIENT_ID for the OAuth consent screen UI
    private static let iosClientId =
        "204420266683-91ic6ae5eh5696d8thrlhgsr1mcod6es.apps.googleusercontent.com"

    // WEB_CLIENT_ID: idToken.aud must equal this so the backend validates it unchanged
    private static let webClientId =
        "204420266683-umabjr4gvpuvdqdcsv71meqja50f2bfu.apps.googleusercontent.com"

    // Called once at startup by iOSApp.swift to configure GIDSignIn
    static func configure() {
        let config = GIDConfiguration(
            clientID: iosClientId,
            serverClientID: webClientId
        )
        GIDSignIn.sharedInstance.configuration = config
    }

    // Handle the Google redirect URL (called from iOSApp.swift onOpenURL)
    static func handle(_ url: URL) -> Bool {
        return GIDSignIn.sharedInstance.handle(url)
    }

    // MARK: - GoogleSignInProvider (KMP protocol)

    func startSignIn(handler: any GoogleSignInHandler) {
        // Find the top-most view controller at sign-in time to present the sheet
        guard let vc = Self.topViewController() else {
            handler.onFailure(message: "Could not find a view controller to present sign-in")
            return
        }

        GIDSignIn.sharedInstance.signIn(withPresenting: vc) { result, error in
            if let error = error {
                let nsError = error as NSError
                // GIDSignInError.canceled == 1
                if nsError.code == 1 {
                    handler.onCancelled()
                } else {
                    handler.onFailure(message: error.localizedDescription)
                }
                return
            }

            guard
                let user = result?.user,
                let idToken = user.idToken?.tokenString
            else {
                handler.onFailure(message: "Google Sign In returned no id token")
                return
            }

            let email = user.profile?.email
            let displayName = user.profile?.name
            handler.onSuccess(idToken: idToken, email: email, displayName: displayName)
        }
    }

    // Traverses the view controller hierarchy to find the currently presented one
    private static func topViewController() -> UIViewController? {
        guard let windowScene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first(where: { $0.activationState == .foregroundActive }),
              let root = windowScene.windows.first(where: { $0.isKeyWindow })?.rootViewController
        else { return nil }
        return findTop(root)
    }

    private static func findTop(_ vc: UIViewController) -> UIViewController {
        if let presented = vc.presentedViewController { return findTop(presented) }
        if let nav = vc as? UINavigationController, let top = nav.topViewController {
            return findTop(top)
        }
        if let tab = vc as? UITabBarController, let selected = tab.selectedViewController {
            return findTop(selected)
        }
        return vc
    }
}
