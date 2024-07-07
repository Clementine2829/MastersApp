package co.za.clementine.mastersapp.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    var networkStateListener: NetworkStateListener? = null

    fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                networkStateListener?.onNetworkAvailable()
            }

            override fun onLost(network: Network) {
                networkStateListener?.onNetworkLost()
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    fun unregisterNetworkCallback() {
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
    }

    interface NetworkStateListener {
        fun onNetworkAvailable()
        fun onNetworkLost()
    }
}
