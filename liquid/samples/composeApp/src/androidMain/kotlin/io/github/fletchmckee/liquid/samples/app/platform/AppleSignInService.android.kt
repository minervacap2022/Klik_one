package io.github.fletchmckee.liquid.samples.app.platform

/**
 * Android stub implementation of AppleSignInService.
 * Sign In with Apple is only natively supported on iOS.
 */
actual object AppleSignInService {
    
    /**
     * Android does not support native Sign In with Apple.
     */
    actual fun isSupported(): Boolean = false
    
    /**
     * Returns NotSupported on Android.
     */
    actual fun signIn(onResult: (AppleSignInResult) -> Unit) {
        onResult(AppleSignInResult.NotSupported)
    }
}
