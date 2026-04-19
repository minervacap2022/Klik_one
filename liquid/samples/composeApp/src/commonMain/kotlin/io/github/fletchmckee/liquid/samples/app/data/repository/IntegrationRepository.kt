package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.domain.entity.AuthorizationUrlResponse
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationCredential
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationInfo
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationProvider
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationStatus
import io.github.fletchmckee.liquid.samples.app.platform.ApplePermissionStatus

/**
 * Repository interface for OAuth integrations.
 * Handles communication with KK_auth OAuth endpoints.
 * Also handles Apple native integrations (Calendar, Reminders) on iOS.
 */
interface IntegrationRepository {
    /**
     * Get list of all available OAuth providers.
     * Calls GET /api/auth/oauth/providers
     */
    suspend fun getProviders(): Result<List<IntegrationProvider>>

    /**
     * Get list of user's OAuth credentials.
     * Calls GET /api/auth/oauth/credentials
     */
    suspend fun getUserCredentials(): Result<List<IntegrationCredential>>

    /**
     * Get connection status for a specific provider.
     * Calls GET /api/auth/oauth/{provider}/status
     */
    suspend fun getStatus(provider: String): Result<IntegrationStatus>

    /**
     * Get authorization URL to start OAuth flow.
     * Calls GET /api/auth/oauth/{provider}/authorize
     */
    suspend fun getAuthorizationUrl(provider: String): Result<AuthorizationUrlResponse>

    /**
     * Disconnect an OAuth provider.
     * Calls DELETE /api/auth/oauth/{provider}/disconnect
     */
    suspend fun disconnect(provider: String): Result<Unit>

    /**
     * Get combined integration info (providers + user credentials).
     * Convenience method that merges provider list with credential status.
     * Includes Apple native integrations on iOS.
     */
    suspend fun getIntegrationInfoList(): Result<List<IntegrationInfo>>

    /**
     * Refresh integration status after OAuth callback.
     * Re-fetches credentials to update connection status.
     */
    suspend fun refreshIntegrations()

    // ==================== Apple Native Integration Methods ====================

    /**
     * Check Apple native integration permissions (Calendar, Reminders).
     * Returns a map of provider_id to permission status.
     * Only meaningful on iOS; returns empty map on other platforms.
     */
    fun checkAppleNativePermissions(): Map<String, ApplePermissionStatus>

    /**
     * Request permission for an Apple native integration.
     * Shows iOS system permission dialog.
     * @param providerId Either "apple_calendar" or "apple_reminders"
     * @param onResult Callback with true if permission was granted
     */
    fun requestAppleNativePermission(providerId: String, onResult: (Boolean) -> Unit)

    /**
     * Sync Apple native integration permission status to backend.
     * Creates/updates oauth_credentials entry for the native provider.
     * @param providerId Either "apple_calendar" or "apple_reminders"
     * @param isGranted Whether permission was granted
     */
    suspend fun syncAppleNativeCredential(providerId: String, isGranted: Boolean): Result<Unit>

    /**
     * Get IntegrationInfo list for Apple native providers only.
     * Checks local permission status without making network calls.
     */
    fun getAppleNativeIntegrations(): List<IntegrationInfo>
}
