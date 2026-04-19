package io.github.fletchmckee.liquid.samples.app.platform

/**
 * JS stub implementation of AppleSignInService.
 * Sign In with Apple is only natively supported on iOS.
 */
actual object AppleSignInService {
    
    /**
     * JS does not support native Sign In with Apple.
     */
    actual fun isSupported(): Boolean = false
    
    /**
     * Returns NotSupported on JS.
     */
    actual fun signIn(onResult: (AppleSignInResult) -> Unit) {
        onResult(AppleSignInResult.NotSupported)
    }
}
