package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * WASM JS stub implementation of ShareService.
 */
actual object ShareService {

    actual fun share(text: String, subject: String?) {
        // WASM sharing not implemented
        KlikLogger.i("ShareService", "Share (WASM): ${subject ?: "No subject"} - $text")
    }
}
