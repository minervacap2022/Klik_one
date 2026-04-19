package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * JVM/Desktop stub implementation of ShareService.
 */
actual object ShareService {

    actual fun share(text: String, subject: String?) {
        // Desktop sharing not implemented
        KlikLogger.i("ShareService", "Share (JVM): ${subject ?: "No subject"} - $text")
    }
}
