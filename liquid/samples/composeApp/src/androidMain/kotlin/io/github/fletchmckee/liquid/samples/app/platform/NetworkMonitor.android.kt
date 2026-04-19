package io.github.fletchmckee.liquid.samples.app.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import io.github.fletchmckee.liquid.samples.app.data.storage.ApplicationContextProvider
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

actual object NetworkMonitor {
    private var isNetworkConnected = true
    private var callback: ConnectivityManager.NetworkCallback? = null

    actual fun isConnected(): Boolean = isNetworkConnected

    actual fun startMonitoring() {
        val context = try {
            ApplicationContextProvider.context
        } catch (e: UninitializedPropertyAccessException) {
            KlikLogger.e("NetworkMonitor", "Android context not initialized")
            return
        }

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Check initial state
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        isNetworkConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val wasConnected = isNetworkConnected
                isNetworkConnected = true
                if (!wasConnected) {
                    KlikLogger.i("NetworkMonitor", "Network available")
                }
            }

            override fun onLost(network: Network) {
                isNetworkConnected = false
                KlikLogger.i("NetworkMonitor", "Network lost")
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)
        callback = networkCallback
        KlikLogger.i("NetworkMonitor", "Started network monitoring")
    }

    actual fun stopMonitoring() {
        val context = try {
            ApplicationContextProvider.context
        } catch (e: UninitializedPropertyAccessException) {
            return
        }

        callback?.let {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(it)
        }
        callback = null
        KlikLogger.i("NetworkMonitor", "Stopped network monitoring")
    }
}
