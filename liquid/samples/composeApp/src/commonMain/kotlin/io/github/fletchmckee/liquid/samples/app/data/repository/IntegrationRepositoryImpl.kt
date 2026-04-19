// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import io.github.fletchmckee.liquid.samples.app.data.network.HttpClient
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.domain.entity.AuthorizationUrlResponse
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationCredential
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationInfo
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationProvider
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationProviders
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationStatus
import io.github.fletchmckee.liquid.samples.app.domain.repository.AuthRepository
import io.github.fletchmckee.liquid.samples.app.platform.AppleIntegrationService
import io.github.fletchmckee.liquid.samples.app.platform.ApplePermissionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Error response from API when authentication fails.
 */
@Serializable
private data class ApiErrorResponse(
    val detail: String
)

/**
 * Implementation of IntegrationRepository.
 * Handles OAuth integration operations via KK_auth service endpoints.
 * Uses HttpClient which handles automatic token refresh on 401.
 */
class IntegrationRepositoryImpl(
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : IntegrationRepository {

    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Check if response is an error object instead of expected data.
     * API errors return {"detail":"..."} which cannot be parsed as arrays.
     */
    private fun isErrorResponse(responseText: String): String? {
        val trimmed = responseText.trim()
        // Detect plain-text error responses (e.g. "Internal Server Error" from 500)
        if (!trimmed.startsWith("[") && !trimmed.startsWith("{")) {
            return trimmed.take(200)
        }
        if (trimmed.startsWith("{") && trimmed.contains("\"detail\"")) {
            return try {
                val error = json.decodeFromString<ApiErrorResponse>(trimmed)
                error.detail
            } catch (e: Exception) {
                "Failed to parse error response"
            }
        }
        return null
    }

    // Cached credentials for quick access
    private val _credentials = MutableStateFlow<List<IntegrationCredential>>(emptyList())
    val credentials: StateFlow<List<IntegrationCredential>> = _credentials.asStateFlow()

    // Cached providers for quick access
    private val _providers = MutableStateFlow<List<IntegrationProvider>>(emptyList())
    val providers: StateFlow<List<IntegrationProvider>> = _providers.asStateFlow()

    /**
     * Get list of all available OAuth providers.
     * Calls GET /api/auth/oauth/providers
     */
    override suspend fun getProviders(): Result<List<IntegrationProvider>> {
        return try {
            if (!CurrentUser.isLoggedIn) {
                return Result.failure(Exception("Not authenticated"))
            }

            val url = ApiConfig.OAuth.providersUrl
            KlikLogger.d("IntegrationRepository", "GET $url")

            val responseText = HttpClient.getUrl(url)
                ?: return Result.failure(Exception("Empty response from providers endpoint"))

            // Check for error response before parsing as array
            val errorDetail = isErrorResponse(responseText)
            if (errorDetail != null) {
                KlikLogger.e("IntegrationRepository", "getProviders API error: $errorDetail")
                return Result.failure(Exception("API error: $errorDetail"))
            }

            val providers = json.decodeFromString<List<IntegrationProvider>>(responseText)
            _providers.value = providers

            KlikLogger.i("IntegrationRepository", "Got ${providers.size} providers")
            Result.success(providers)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            KlikLogger.e("IntegrationRepository", "getProviders error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get list of user's OAuth credentials.
     * Calls GET /api/auth/oauth/credentials
     */
    override suspend fun getUserCredentials(): Result<List<IntegrationCredential>> {
        return try {
            if (!CurrentUser.isLoggedIn) {
                return Result.failure(Exception("Not authenticated"))
            }

            val url = ApiConfig.OAuth.credentialsUrl
            KlikLogger.d("IntegrationRepository", "GET $url")

            val responseText = HttpClient.getUrl(url)
                ?: return Result.failure(Exception("Empty response from credentials endpoint"))

            // Check for error response before parsing as array
            val errorDetail = isErrorResponse(responseText)
            if (errorDetail != null) {
                KlikLogger.e("IntegrationRepository", "getUserCredentials API error: $errorDetail")
                return Result.failure(Exception("API error: $errorDetail"))
            }

            val credentials = json.decodeFromString<List<IntegrationCredential>>(responseText)
            _credentials.value = credentials

            KlikLogger.i("IntegrationRepository", "Got ${credentials.size} credentials")
            Result.success(credentials)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            KlikLogger.e("IntegrationRepository", "getUserCredentials error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get connection status for a specific provider.
     * Calls GET /api/auth/oauth/{provider}/status
     */
    override suspend fun getStatus(provider: String): Result<IntegrationStatus> {
        return try {
            if (!CurrentUser.isLoggedIn) {
                return Result.failure(Exception("Not authenticated"))
            }

            val url = ApiConfig.OAuth.statusUrl(provider)
            KlikLogger.d("IntegrationRepository", "GET $url")

            val responseText = HttpClient.getUrl(url)
                ?: return Result.failure(Exception("Empty response from status endpoint"))

            // Check for error response before parsing
            val errorDetail = isErrorResponse(responseText)
            if (errorDetail != null) {
                KlikLogger.e("IntegrationRepository", "getStatus API error: $errorDetail")
                return Result.failure(Exception("API error: $errorDetail"))
            }

            val status = json.decodeFromString<IntegrationStatus>(responseText)

            KlikLogger.i("IntegrationRepository", "Provider $provider: connected=${status.connected}")
            Result.success(status)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            KlikLogger.e("IntegrationRepository", "getStatus error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get authorization URL to start OAuth flow.
     * Calls GET /api/auth/oauth/{provider}/authorize
     */
    override suspend fun getAuthorizationUrl(provider: String): Result<AuthorizationUrlResponse> {
        return try {
            if (!CurrentUser.isLoggedIn) {
                return Result.failure(Exception("Not authenticated"))
            }

            val url = ApiConfig.OAuth.authorizeUrl(provider)
            KlikLogger.d("IntegrationRepository", "GET $url")

            val responseText = HttpClient.getUrl(url)
                ?: return Result.failure(Exception("Empty response from authorize endpoint"))

            // Check for error response before parsing
            val errorDetail = isErrorResponse(responseText)
            if (errorDetail != null) {
                KlikLogger.e("IntegrationRepository", "getAuthorizationUrl API error: $errorDetail")
                return Result.failure(Exception("API error: $errorDetail"))
            }

            val response = json.decodeFromString<AuthorizationUrlResponse>(responseText)

            KlikLogger.i("IntegrationRepository", "Got authorization URL for $provider")
            Result.success(response)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            KlikLogger.e("IntegrationRepository", "getAuthorizationUrl error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Disconnect an OAuth provider.
     * Calls DELETE /api/auth/oauth/{provider}/disconnect
     */
    override suspend fun disconnect(provider: String): Result<Unit> {
        return try {
            if (!CurrentUser.isLoggedIn) {
                return Result.failure(Exception("Not authenticated"))
            }

            val url = ApiConfig.OAuth.disconnectUrl(provider)
            KlikLogger.d("IntegrationRepository", "DELETE $url")

            HttpClient.deleteUrl(url)
                ?: return Result.failure(Exception("Empty response from disconnect endpoint"))

            // Refresh credentials after disconnect
            getUserCredentials()

            KlikLogger.i("IntegrationRepository", "Disconnected $provider")
            Result.success(Unit)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            KlikLogger.e("IntegrationRepository", "disconnect error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get combined integration info (providers + user credentials).
     * Convenience method that merges provider list with credential status.
     */
    override suspend fun getIntegrationInfoList(): Result<List<IntegrationInfo>> {
        return try {
            // Fetch both providers and credentials
            val providersResult = getProviders()
            val credentialsResult = getUserCredentials()

            if (providersResult.isFailure) {
                return Result.failure(providersResult.exceptionOrNull() ?: Exception("Failed to get providers"))
            }

            if (credentialsResult.isFailure) {
                return Result.failure(credentialsResult.exceptionOrNull() ?: Exception("Failed to get credentials"))
            }

            val providers = providersResult.getOrThrow()
            val credentials = credentialsResult.getOrThrow()

            // Merge providers with credentials
            val integrationInfoList = providers.map { provider ->
                IntegrationInfo.fromProviderAndCredentials(provider, credentials)
            }

            KlikLogger.i("IntegrationRepository", "Built ${integrationInfoList.size} integration info items")
            Result.success(integrationInfoList)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            KlikLogger.e("IntegrationRepository", "getIntegrationInfoList error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Refresh integration status after OAuth callback.
     * Re-fetches credentials to update connection status.
     */
    override suspend fun refreshIntegrations() {
        try {
            getUserCredentials()
            KlikLogger.i("IntegrationRepository", "Refreshed integrations")
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            KlikLogger.e("IntegrationRepository", "refreshIntegrations error: ${e.message}", e)
        }
    }

    /**
     * Check if a specific provider is connected.
     */
    fun isProviderConnected(providerId: String): Boolean {
        return _credentials.value.any { it.mcpServerId == providerId }
    }

    /**
     * Get list of providers that are not yet connected.
     */
    fun getUnconnectedProviders(): List<IntegrationProvider> {
        val connectedProviderIds = _credentials.value.map { it.mcpServerId }.toSet()
        return _providers.value.filter { provider ->
            provider.configured && provider.providerId !in connectedProviderIds
        }
    }

    // ==================== Apple Native Integration Methods ====================

    /**
     * Check Apple native integration permissions (Calendar, Reminders).
     * Returns a map of provider_id to permission status.
     * Only meaningful on iOS; returns empty map on other platforms.
     */
    override fun checkAppleNativePermissions(): Map<String, ApplePermissionStatus> {
        if (!AppleIntegrationService.isSupported()) {
            return emptyMap()
        }

        return mapOf(
            IntegrationProviders.APPLE_CALENDAR to AppleIntegrationService.checkCalendarPermission(),
            IntegrationProviders.APPLE_REMINDERS to AppleIntegrationService.checkRemindersPermission()
        )
    }

    /**
     * Request permission for an Apple native integration.
     * Shows iOS system permission dialog.
     */
    override fun requestAppleNativePermission(providerId: String, onResult: (Boolean) -> Unit) {
        if (!AppleIntegrationService.isSupported()) {
            KlikLogger.w("IntegrationRepository", "Apple integrations not supported on this platform")
            onResult(false)
            return
        }

        when (providerId) {
            IntegrationProviders.APPLE_CALENDAR -> {
                KlikLogger.i("IntegrationRepository", "Requesting Apple Calendar permission")
                AppleIntegrationService.requestCalendarPermission(onResult)
            }
            IntegrationProviders.APPLE_REMINDERS -> {
                KlikLogger.i("IntegrationRepository", "Requesting Apple Reminders permission")
                AppleIntegrationService.requestRemindersPermission(onResult)
            }
            else -> {
                KlikLogger.w("IntegrationRepository", "Unknown Apple native provider: $providerId")
                onResult(false)
            }
        }
    }

    /**
     * Sync Apple native integration permission status to backend.
     * Creates/updates oauth_credentials entry for the native provider.
     * USER-SPECIFIC: Always includes userId in request body for explicit tracking.
     */
    override suspend fun syncAppleNativeCredential(providerId: String, isGranted: Boolean): Result<Unit> {
        return try {
            // Validate user is logged in
            if (!CurrentUser.isLoggedIn) {
                val error = "Cannot sync Apple credential: User not authenticated"
                KlikLogger.e("IntegrationRepository", error)
                return Result.failure(Exception(error))
            }

            val userId = CurrentUser.userId
            if (userId == null) {
                val error = "Cannot sync Apple credential: User ID is null despite being logged in"
                KlikLogger.e("IntegrationRepository", error)
                return Result.failure(Exception(error))
            }

            // Validate providerId
            if (!IntegrationProviders.isAppleNativeProvider(providerId)) {
                val error = "Invalid Apple native provider ID: $providerId"
                KlikLogger.e("IntegrationRepository", error)
                return Result.failure(Exception(error))
            }

            val url = ApiConfig.OAuth.nativeSyncUrl
            KlikLogger.d("IntegrationRepository", "POST $url for provider=$providerId, user=$userId, granted=$isGranted")

            val requestBody = NativeCredentialSyncRequest(
                userId = userId,
                providerId = providerId,
                isGranted = isGranted,
                deviceId = CurrentUser.deviceId,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )

            val responseText = HttpClient.postUrl(url, json.encodeToString(NativeCredentialSyncRequest.serializer(), requestBody))
            if (responseText == null) {
                val error = "Empty response from native sync endpoint for provider=$providerId, user=$userId"
                KlikLogger.e("IntegrationRepository", error)
                return Result.failure(Exception(error))
            }

            KlikLogger.i("IntegrationRepository", "Successfully synced native credential: provider=$providerId, user=$userId, granted=$isGranted")

            // Refresh credentials to include the new native credential
            getUserCredentials()

            Result.success(Unit)
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            KlikLogger.e("IntegrationRepository", "Failed syncing Apple native credential: providerId=$providerId, error=${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get IntegrationInfo list for Apple native providers only.
     * Checks local permission status without making network calls.
     * STRICT: Only returns integrations for which we have explicit permission status.
     */
    override fun getAppleNativeIntegrations(): List<IntegrationInfo> {
        if (!AppleIntegrationService.isSupported()) {
            KlikLogger.w("IntegrationRepository", "Apple integrations not supported on this platform")
            return emptyList()
        }

        val permissions = checkAppleNativePermissions()
        KlikLogger.i("IntegrationRepository", "Apple native permissions: $permissions")

        return IntegrationProviders.appleNativeProviders.mapNotNull { provider ->
            val status = permissions[provider.id]
            if (status == null) {
                KlikLogger.w("IntegrationRepository", "No permission status found for provider=${provider.id}")
                null // Don't include providers without explicit status
            } else {
                IntegrationInfo(
                    providerId = provider.id,
                    displayName = provider.name,
                    configured = true, // Always configured on iOS
                    connected = status == ApplePermissionStatus.GRANTED,
                    credentialId = null, // Native integrations use system permissions, not credential IDs
                    createdAt = null
                )
            }
        }
    }
}

/**
 * Request body for syncing native integration credentials to backend.
 * USER-SPECIFIC: Always includes userId for explicit user tracking.
 */
@Serializable
private data class NativeCredentialSyncRequest(
    @kotlinx.serialization.SerialName("user_id")
    val userId: String,
    @kotlinx.serialization.SerialName("provider_id")
    val providerId: String,
    @kotlinx.serialization.SerialName("is_granted")
    val isGranted: Boolean,
    @kotlinx.serialization.SerialName("device_id")
    val deviceId: String?,
    val timestamp: Long
)
