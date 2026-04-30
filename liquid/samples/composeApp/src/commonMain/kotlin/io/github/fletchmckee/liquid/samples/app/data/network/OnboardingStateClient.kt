// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Onboarding + consent completion state held by KK_auth (port 8833) on the
 * `users` row. The server is the source of truth; the client SecureStorage
 * cache is a write-through optimisation only.
 *
 * NULL = not completed. ISO-8601 timestamps when set. Reinstalls and new
 * devices read the same values back.
 */
@Serializable
data class AuthOnboardingState(
  val onboarding_completed_at: String? = null,
  val recording_consent_at: String? = null,
  val biometric_consent_at: String? = null,
) {
  val onboardingComplete: Boolean get() = !onboarding_completed_at.isNullOrBlank()
  val recordingConsent: Boolean get() = !recording_consent_at.isNullOrBlank()
  val biometricConsent: Boolean get() = !biometric_consent_at.isNullOrBlank()
}

@Serializable
private data class AuthMeRaw(
  val onboarding_completed_at: String? = null,
  val recording_consent_at: String? = null,
  val biometric_consent_at: String? = null,
)

@Serializable
private data class OnboardingPatchBody(
  val onboarding_completed: Boolean? = null,
  val recording_consent: Boolean? = null,
  val biometric_consent: Boolean? = null,
)

object OnboardingStateClient {
  private val json = Json { ignoreUnknownKeys = true }

  /**
   * GET /api/auth/me — returns the three timestamps for the current JWT user.
   * Throws if the call fails; callers that need a soft-degrade should catch.
   */
  suspend fun fetch(): AuthOnboardingState {
    val url = "${ApiConfig.AUTH_BASE_URL}/me"
    KlikLogger.d("OnboardingState", "GET $url")
    val body = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch onboarding state: empty body")
    val raw = json.decodeFromString(AuthMeRaw.serializer(), body)
    return AuthOnboardingState(
      onboarding_completed_at = raw.onboarding_completed_at,
      recording_consent_at = raw.recording_consent_at,
      biometric_consent_at = raw.biometric_consent_at,
    )
  }

  /**
   * PATCH /api/auth/users/me/onboarding — flips one or more flags to NOW().
   * Forward-only on the server: nothing un-sets.
   */
  suspend fun patch(
    onboardingCompleted: Boolean? = null,
    recordingConsent: Boolean? = null,
    biometricConsent: Boolean? = null,
  ): AuthOnboardingState {
    val url = "${ApiConfig.AUTH_BASE_URL}/users/me/onboarding"
    val payload = json.encodeToString(
      OnboardingPatchBody.serializer(),
      OnboardingPatchBody(
        onboarding_completed = onboardingCompleted,
        recording_consent = recordingConsent,
        biometric_consent = biometricConsent,
      ),
    )
    KlikLogger.d("OnboardingState", "PATCH $url $payload")
    val body = HttpClient.patchUrl(url, payload)
      ?: throw IllegalStateException("Failed to patch onboarding state: empty body")
    val raw = json.decodeFromString(AuthMeRaw.serializer(), body)
    return AuthOnboardingState(
      onboarding_completed_at = raw.onboarding_completed_at,
      recording_consent_at = raw.recording_consent_at,
      biometric_consent_at = raw.biometric_consent_at,
    )
  }
}
