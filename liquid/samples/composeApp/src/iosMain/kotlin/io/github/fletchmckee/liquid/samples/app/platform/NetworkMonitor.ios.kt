package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_get_status
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

actual object NetworkMonitor {
    private var isNetworkConnected = true
    private var monitor: platform.Network.nw_path_monitor_t? = null

    actual fun isConnected(): Boolean = isNetworkConnected

    actual fun startMonitoring() {
        val m = nw_path_monitor_create() ?: return
        monitor = m

        nw_path_monitor_set_update_handler(m) { path ->
            if (path != null) {
                val status = nw_path_get_status(path)
                val wasConnected = isNetworkConnected
                isNetworkConnected = status == nw_path_status_satisfied
                if (wasConnected != isNetworkConnected) {
                    KlikLogger.i("NetworkMonitor", "Connectivity changed: connected=$isNetworkConnected")
                }
            }
        }

        nw_path_monitor_set_queue(m, dispatch_get_main_queue())
        nw_path_monitor_start(m)
        KlikLogger.i("NetworkMonitor", "Started network monitoring")
    }

    actual fun stopMonitoring() {
        monitor?.let { nw_path_monitor_cancel(it) }
        monitor = null
        KlikLogger.i("NetworkMonitor", "Stopped network monitoring")
    }
}
