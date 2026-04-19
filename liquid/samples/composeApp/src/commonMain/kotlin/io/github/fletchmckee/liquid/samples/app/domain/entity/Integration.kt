package io.github.fletchmckee.liquid.samples.app.domain.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an OAuth provider available for integration.
 * Maps to GET /api/auth/oauth/providers response.
 */
@Serializable
data class IntegrationProvider(
    @SerialName("provider_id")
    val providerId: String,
    @SerialName("display_name")
    val displayName: String,
    val configured: Boolean,
    @SerialName("credential_type")
    val credentialType: String
)

/**
 * Represents a user's stored OAuth credential.
 * Maps to GET /api/auth/oauth/credentials response.
 */
@Serializable
data class IntegrationCredential(
    val id: String,
    val name: String,
    @SerialName("credential_type")
    val credentialType: String,
    @SerialName("mcp_server_id")
    val mcpServerId: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

/**
 * Represents the connection status for a specific provider.
 * Maps to GET /api/auth/oauth/{provider}/status response.
 */
@Serializable
data class IntegrationStatus(
    val provider: String,
    @SerialName("display_name")
    val displayName: String,
    val configured: Boolean,
    val connected: Boolean,
    @SerialName("credential_id")
    val credentialId: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

/**
 * Response from GET /api/auth/oauth/{provider}/authorize endpoint.
 */
@Serializable
data class AuthorizationUrlResponse(
    @SerialName("authorization_url")
    val authorizationUrl: String,
    val state: String
)

/**
 * Combined integration info for UI display.
 * Merges provider info with user's connection status.
 */
data class IntegrationInfo(
    val providerId: String,
    val displayName: String,
    val configured: Boolean,
    val connected: Boolean,
    val credentialId: String? = null,
    val createdAt: String? = null
) {
    companion object {
        fun fromProviderAndCredentials(
            provider: IntegrationProvider,
            credentials: List<IntegrationCredential>
        ): IntegrationInfo {
            val credential = credentials.find { it.mcpServerId == provider.providerId }
            return IntegrationInfo(
                providerId = provider.providerId,
                displayName = provider.displayName,
                configured = provider.configured,
                connected = credential != null,
                credentialId = credential?.id,
                createdAt = credential?.createdAt
            )
        }
    }
}

/**
 * Predefined list of all supported integrations with their display info.
 */
object IntegrationProviders {
    // OAuth-based integrations (require web-based authorization flow)
    val oauthProviders = listOf(
        ProviderDisplayInfo("notion", "Notion", "N"),
        ProviderDisplayInfo("slack", "Slack", "S"),
        ProviderDisplayInfo("github", "GitHub", "G"),
        ProviderDisplayInfo("google", "Google", "G"),
        ProviderDisplayInfo("clickup", "ClickUp", "C"),
        ProviderDisplayInfo("monday", "Monday.com", "M"),
        ProviderDisplayInfo("asana", "Asana", "A"),
        ProviderDisplayInfo("atlassian", "Jira", "J"),
        ProviderDisplayInfo("linear", "Linear", "L"),
        ProviderDisplayInfo("microsoft", "Microsoft 365", "M")
    )

    // Apple native integrations (iOS only, require system permission dialogs)
    val appleNativeProviders = listOf(
        ProviderDisplayInfo("apple_calendar", "Apple Calendar", "📅"),
        ProviderDisplayInfo("apple_reminders", "Apple Reminders", "✓")
    )

    // All providers combined
    val all = oauthProviders + appleNativeProviders

    fun getDisplayInfo(providerId: String): ProviderDisplayInfo? {
        return all.find { it.id == providerId }
    }

    /**
     * Check if a provider is an Apple native integration.
     * Apple native integrations use iOS system permission dialogs instead of OAuth.
     */
    fun isAppleNativeProvider(providerId: String): Boolean {
        return appleNativeProviders.any { it.id == providerId }
    }

    /**
     * Apple Calendar provider ID constant
     */
    const val APPLE_CALENDAR = "apple_calendar"

    /**
     * Apple Reminders provider ID constant
     */
    const val APPLE_REMINDERS = "apple_reminders"
}

data class ProviderDisplayInfo(
    val id: String,
    val name: String,
    val initial: String
)
