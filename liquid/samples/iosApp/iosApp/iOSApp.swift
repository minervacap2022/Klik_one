import SwiftUI
import Foundation
import UIKit

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    handleUniversalLink(url)
                }
        }
    }

    /// Handle Universal Links and custom URL scheme deep links.
    ///
    /// Supported URL formats:
    /// - OAuth callback: https://hiklik.ai/integrations?success=true&provider=notion
    /// - Deep links: klik://meeting/123, klik://task/456, klik://person/789
    /// - Universal deep links: https://hiklik.ai/app/meeting/123
    private func handleUniversalLink(_ url: URL) {
        print("[iOSApp] Received URL: \(url.absoluteString)")

        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: true) else {
            print("[iOSApp] Failed to parse URL components")
            return
        }

        // Check if this is an OAuth callback
        if components.path.contains("integrations") {
            let queryItems = components.queryItems ?? []
            let provider = queryItems.first(where: { $0.name == "provider" })?.value
            let success = queryItems.first(where: { $0.name == "success" })?.value == "true"

            if let provider = provider {
                print("[iOSApp] OAuth callback - Provider: \(provider), Success: \(success)")

                let defaults = UserDefaults.standard
                defaults.set(provider, forKey: "oauth_callback_provider")
                defaults.set(success, forKey: "oauth_callback_success")
                defaults.set(Date().timeIntervalSince1970, forKey: "oauth_callback_timestamp")
                defaults.synchronize()

                NotificationCenter.default.post(
                    name: Notification.Name("OAuthCallbackReceived"),
                    object: nil,
                    userInfo: ["provider": provider, "success": success]
                )
            }
            return
        }

        // Handle deep links (klik:// scheme or hiklik.ai/app/ universal links)
        let isCustomScheme = url.scheme == "klik"
        let isAppUniversalLink = components.path.hasPrefix("/app/")

        if isCustomScheme || isAppUniversalLink {
            print("[iOSApp] Deep link detected: \(url.absoluteString)")
            let defaults = UserDefaults.standard
            defaults.set(url.absoluteString, forKey: "pending_deep_link")
            defaults.synchronize()
        }
    }
}

// MARK: - AppDelegate for Push Notifications

/**
 * AppDelegate for handling push notification lifecycle events.
 *
 * Required for APNs device token registration callbacks.
 */
class AppDelegate: NSObject, UIApplicationDelegate {
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        print("[AppDelegate] Application did finish launching")
        
        // Register for push notifications
        PushNotificationService.shared.registerForPushNotifications()
        
        return true
    }
    
    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        PushNotificationService.shared.handleDeviceToken(deviceToken)
    }
    
    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        PushNotificationService.shared.handleRegistrationError(error)
    }
}
