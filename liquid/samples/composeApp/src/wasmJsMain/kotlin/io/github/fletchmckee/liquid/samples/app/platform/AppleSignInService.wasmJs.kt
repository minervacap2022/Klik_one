package io.github.fletchmckee.liquid.samples.app.platform

/**
 * WasmJS stub implementation of AppleSignInService.
 * Sign In with Apple is only natively supported on iOS.
 */
actual object AppleSignInService {
    
    /**
     * WasmJS does not support native Sign In with Apple.
     */
    actual fun isSupported(): Boolean = false
    
    /**
     * Returns NotSupported on WasmJS.
     */
    actual fun signIn(onResult: (AppleSignInResult) -> Unit) {
        onResult(AppleSignInResult.NotSupported)
    }
}
