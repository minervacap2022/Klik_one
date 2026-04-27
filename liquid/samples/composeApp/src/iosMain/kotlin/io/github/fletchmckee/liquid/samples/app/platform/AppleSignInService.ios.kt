// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.darwin.NSObject

/**
 * iOS implementation of AppleSignInService using AuthenticationServices framework.
 *
 * Requirements:
 * - iOS 13.0+
 * - Sign In with Apple capability in entitlements
 * - App ID configured with Sign In with Apple in Apple Developer Console
 */
actual object AppleSignInService {

  private var pendingCallback: ((AppleSignInResult) -> Unit)? = null
  private var delegateHolder: AppleSignInDelegate? = null

  /**
   * iOS supports Sign In with Apple (iOS 13+).
   */
  actual fun isSupported(): Boolean = true

  /**
   * Initiate Sign In with Apple using ASAuthorizationController.
   * Requests full name and email scopes.
   */
  actual fun signIn(onResult: (AppleSignInResult) -> Unit) {
    pendingCallback = onResult

    // Create Apple ID provider and request
    val appleIDProvider = ASAuthorizationAppleIDProvider()
    val request = appleIDProvider.createRequest()

    // Request user's full name and email
    request.requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)

    // Create and configure authorization controller
    val controller = ASAuthorizationController(authorizationRequests = listOf(request))

    // Create and hold delegate reference
    val delegate = AppleSignInDelegate { result ->
      pendingCallback?.invoke(result)
      pendingCallback = null
      delegateHolder = null
    }
    delegateHolder = delegate

    controller.delegate = delegate
    controller.presentationContextProvider = delegate

    // Start the authorization flow
    controller.performRequests()
  }
}

/**
 * Delegate class handling ASAuthorizationController callbacks.
 * Implements both delegate and presentation context provider protocols.
 */
@OptIn(ExperimentalForeignApi::class)
private class AppleSignInDelegate(
  private val onComplete: (AppleSignInResult) -> Unit,
) : NSObject(),
  ASAuthorizationControllerDelegateProtocol,
  ASAuthorizationControllerPresentationContextProvidingProtocol {

  /**
   * Called when authorization completes successfully.
   */
  @ObjCSignatureOverride
  override fun authorizationController(
    controller: ASAuthorizationController,
    didCompleteWithAuthorization: ASAuthorization,
  ) {
    val credential = didCompleteWithAuthorization.credential

    if (credential is ASAuthorizationAppleIDCredential) {
      // Extract identity token
      val identityTokenData = credential.identityToken
      val identityToken = identityTokenData?.toUtf8String()

      if (identityToken == null) {
        onComplete(AppleSignInResult.Error("Failed to extract identity token"))
        return
      }

      // Extract authorization code
      val authCodeData = credential.authorizationCode
      val authorizationCode = authCodeData?.toUtf8String()

      if (authorizationCode == null) {
        onComplete(AppleSignInResult.Error("Failed to extract authorization code"))
        return
      }

      // Extract user info
      val userId = credential.user
      val email = credential.email

      // Extract full name components
      val nameComponents = credential.fullName
      val fullName = if (nameComponents != null) {
        listOfNotNull(
          nameComponents.givenName,
          nameComponents.familyName,
        ).joinToString(" ").takeIf { it.isNotBlank() }
      } else {
        null
      }

      // Determine if email is real or relay
      // Apple relay emails end with @privaterelay.appleid.com
      val isRealEmail = email?.let { !it.contains("privaterelay.appleid.com") } ?: true

      val result = AppleSignInCredential(
        identityToken = identityToken,
        authorizationCode = authorizationCode,
        userId = userId,
        email = email,
        fullName = fullName,
        isRealEmail = isRealEmail,
      )

      KlikLogger.i("AppleSignInService", "Sign in successful: userId=$userId, email=$email, fullName=$fullName")
      onComplete(AppleSignInResult.Success(result))
    } else {
      onComplete(AppleSignInResult.Error("Unexpected credential type: ${credential::class.simpleName}"))
    }
  }

  /**
   * Called when authorization fails.
   */
  @ObjCSignatureOverride
  override fun authorizationController(
    controller: ASAuthorizationController,
    didCompleteWithError: NSError,
  ) {
    val errorCode = didCompleteWithError.code
    val errorDomain = didCompleteWithError.domain

    // ASAuthorizationError codes:
    // 1000 = canceled
    // 1001 = invalid response
    // 1002 = not handled
    // 1003 = failed
    // 1004 = not interactive

    if (errorCode == 1000L) {
      // User cancelled
      KlikLogger.i("AppleSignInService", "User cancelled sign in")
      onComplete(AppleSignInResult.Cancelled)
    } else {
      val errorMessage = didCompleteWithError.localizedDescription
      KlikLogger.e("AppleSignInService", "Sign in failed: code=$errorCode, domain=$errorDomain, message=$errorMessage")
      onComplete(AppleSignInResult.Error(errorMessage))
    }
  }

  /**
   * Provide presentation anchor for the authorization UI.
   */
  override fun presentationAnchorForAuthorizationController(
    controller: ASAuthorizationController,
  ): ASPresentationAnchor {
    // Return the key window as the presentation anchor
    return UIApplication.sharedApplication.keyWindow
      ?: UIApplication.sharedApplication.windows.firstOrNull() as? ASPresentationAnchor
      ?: throw IllegalStateException("No window available for presentation")
  }
}

/**
 * Extension to convert NSData to UTF-8 String.
 */
@OptIn(ExperimentalForeignApi::class)
private fun NSData.toUtf8String(): String? = NSString.create(data = this, encoding = NSUTF8StringEncoding)?.toString()
