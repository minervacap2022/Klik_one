import UserNotifications
import UIKit

/**
 * Push Notification Service for Klik iOS App.
 *
 * Handles:
 * - APNs registration
 * - Device token management
 * - Foreground notification display
 * - Notification tap handling
 *
 * Device tokens are stored in UserDefaults for Kotlin access:
 * - Key: "apns_device_token" - The device token string
 * - Key: "apns_token_timestamp" - Unix timestamp when token was received
 *
 * Notification events are broadcast via NotificationCenter:
 * - "APNsTokenReceived" - When device token is received
 * - "PushNotificationTapped" - When user taps a notification
 */
final class PushNotificationService: NSObject {
    
    static let shared = PushNotificationService()
    
    // UserDefaults keys for Kotlin interop
    private let kDeviceTokenKey = "apns_device_token"
    private let kTokenTimestampKey = "apns_token_timestamp"
    
    // NotificationCenter names
    static let tokenReceivedNotification = Notification.Name("APNsTokenReceived")
    static let notificationTappedNotification = Notification.Name("PushNotificationTapped")
    
    private override init() {
        super.init()
    }
    
    // MARK: - Registration
    
    /**
     * Request notification permissions and register for remote notifications.
     *
     * Call this on app launch (in AppDelegate.didFinishLaunchingWithOptions).
     */
    func registerForPushNotifications() {
        print("[PushNotificationService] Requesting notification authorization")
        
        UNUserNotificationCenter.current().delegate = self
        
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .sound, .badge]
        ) { [weak self] granted, error in
            if let error = error {
                print("[PushNotificationService] Authorization error: \(error.localizedDescription)")
                return
            }
            
            guard granted else {
                print("[PushNotificationService] Notification permission denied")
                return
            }
            
            print("[PushNotificationService] Notification permission granted")
            
            DispatchQueue.main.async {
                UIApplication.shared.registerForRemoteNotifications()
            }
        }
    }
    
    // MARK: - Device Token Handling
    
    /**
     * Handle successful device token registration.
     *
     * Called from AppDelegate.didRegisterForRemoteNotificationsWithDeviceToken.
     *
     * - Parameter deviceToken: Raw device token data from APNs
     */
    func handleDeviceToken(_ deviceToken: Data) {
        let tokenString = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        
        print("[PushNotificationService] Device token received: \(tokenString.prefix(16))...")
        
        // Store in UserDefaults for Kotlin access
        let defaults = UserDefaults.standard
        defaults.set(tokenString, forKey: kDeviceTokenKey)
        defaults.set(Date().timeIntervalSince1970, forKey: kTokenTimestampKey)
        defaults.synchronize()
        
        // Broadcast token received event
        NotificationCenter.default.post(
            name: PushNotificationService.tokenReceivedNotification,
            object: nil,
            userInfo: ["token": tokenString]
        )
    }
    
    /**
     * Handle failed device token registration.
     *
     * Called from AppDelegate.didFailToRegisterForRemoteNotificationsWithError.
     *
     * - Parameter error: Registration error
     */
    func handleRegistrationError(_ error: Error) {
        print("[PushNotificationService] Registration failed: \(error.localizedDescription)")
    }
    
    // MARK: - Token Access
    
    /**
     * Get the stored device token.
     *
     * - Returns: Device token string, or nil if not registered
     */
    func getStoredToken() -> String? {
        return UserDefaults.standard.string(forKey: kDeviceTokenKey)
    }
    
    /**
     * Clear the stored device token.
     *
     * Call this on logout to prevent notifications to logged-out users.
     */
    func clearStoredToken() {
        let defaults = UserDefaults.standard
        defaults.removeObject(forKey: kDeviceTokenKey)
        defaults.removeObject(forKey: kTokenTimestampKey)
        defaults.synchronize()
        
        print("[PushNotificationService] Stored token cleared")
    }
}

// MARK: - UNUserNotificationCenterDelegate

extension PushNotificationService: UNUserNotificationCenterDelegate {
    
    /**
     * Handle notification when app is in foreground.
     *
     * Shows the notification as banner with sound.
     */
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        let userInfo = notification.request.content.userInfo
        print("[PushNotificationService] Notification received in foreground: \(userInfo)")
        
        // Show banner, play sound, update badge
        completionHandler([.banner, .sound, .badge])
    }
    
    /**
     * Handle notification tap.
     *
     * Broadcasts event for app navigation handling.
     */
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        print("[PushNotificationService] Notification tapped: \(userInfo)")
        
        // Extract notification data
        var notificationData: [String: Any] = [:]
        
        if let data = userInfo["data"] as? [String: Any] {
            notificationData = data
        }
        
        // Also include type and id from data if present
        if let type = (userInfo["data"] as? [String: Any])?["type"] as? String {
            notificationData["type"] = type
        }
        if let id = (userInfo["data"] as? [String: Any])?["id"] as? String {
            notificationData["id"] = id
        }
        
        // Broadcast tap event for navigation handling
        NotificationCenter.default.post(
            name: PushNotificationService.notificationTappedNotification,
            object: nil,
            userInfo: notificationData
        )
        
        completionHandler()
    }
}
