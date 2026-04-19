package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * Cross-platform deep link handler.
 *
 * Native platforms (iOS via onOpenURL, Android via intent) write pending deep link URLs.
 * MainApp polls for pending links and routes them to the appropriate screen.
 *
 * Supported URL formats:
 * - klik://meeting/{sessionId}
 * - klik://task/{taskId}
 * - klik://person/{personId}
 * - klik://project/{projectId}
 * - klik://organization/{orgId}
 * - klik://screen/{routeName}
 * - https://hiklik.ai/app/meeting/{sessionId}
 * - https://hiklik.ai/app/task/{taskId}
 * - https://hiklik.ai/app/person/{personId}
 * - https://hiklik.ai/app/project/{projectId}
 * - https://hiklik.ai/app/organization/{orgId}
 */
expect object DeepLinkHandler {
    /**
     * Store a pending deep link URL received from the native layer.
     */
    fun setPendingDeepLink(url: String)

    /**
     * Consume and return the pending deep link URL, or null if none.
     */
    fun consumePendingDeepLink(): String?
}

/**
 * Parsed deep link navigation action.
 */
data class DeepLinkAction(
    val route: String,
    val entityId: String? = null,
    val entityType: String? = null
)

/** Whitelist of valid route names for klik://screen/{routeName} deep links. */
private val VALID_ROUTES = setOf(
    "today", "function", "growth", "explore", "archived",
    "pricing", "growth_tree", "privacy_settings", "account_security",
    "notification_settings", "notifications", "recording_consent",
    "biometric_consent"
)

/**
 * Parse a deep link URL into a navigation action.
 *
 * Returns null if the URL is not a recognized deep link.
 */
fun parseDeepLink(url: String): DeepLinkAction? {
    // Extract path segments from either klik:// or https://hiklik.ai/app/...
    val pathSegments = when {
        url.startsWith("klik://") -> {
            url.removePrefix("klik://").split("/").filter { it.isNotEmpty() }
        }
        url.contains("${ApiConfig.WEB_APP_DOMAIN}/app/") -> {
            val appPath = url.substringAfter("${ApiConfig.WEB_APP_DOMAIN}/app/")
            appPath.split("?").first().split("/").filter { it.isNotEmpty() }
        }
        else -> return null
    }

    if (pathSegments.isEmpty()) return null

    val type = pathSegments[0]
    val id = pathSegments.getOrNull(1)

    return when (type) {
        "meeting" -> DeepLinkAction(route = "today", entityId = id, entityType = "meeting")
        "task" -> DeepLinkAction(route = "function", entityId = id, entityType = "task")
        "person" -> DeepLinkAction(route = "growth", entityId = id, entityType = "person")
        "project" -> DeepLinkAction(route = "growth", entityId = id, entityType = "project")
        "organization" -> DeepLinkAction(route = "growth", entityId = id, entityType = "organization")
        "screen" -> {
            val route = id ?: "today"
            if (route in VALID_ROUTES) {
                DeepLinkAction(route = route)
            } else {
                KlikLogger.w("DeepLinkHandler", "Invalid deep link route: $route, falling back to today")
                DeepLinkAction(route = "today")
            }
        }
        "pricing" -> DeepLinkAction(route = "pricing")
        "privacy" -> DeepLinkAction(route = "privacy_settings")
        "notifications" -> DeepLinkAction(route = "notifications")
        "growth" -> DeepLinkAction(route = "growth_tree")
        else -> null
    }
}
