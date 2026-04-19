package io.github.fletchmckee.liquid.samples.app.platform

actual object DeepLinkHandler {
    private var pendingUrl: String? = null

    actual fun setPendingDeepLink(url: String) {
        pendingUrl = url
    }

    actual fun consumePendingDeepLink(): String? {
        val url = pendingUrl
        pendingUrl = null
        return url
    }
}
