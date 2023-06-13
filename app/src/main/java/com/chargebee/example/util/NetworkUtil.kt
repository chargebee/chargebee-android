package com.chargebee.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log

class NetworkUtil(context: Context, callback: NetworkListener) :
    ConnectivityManager.NetworkCallback() {
    private var mNetworkRequest: NetworkRequest? = null
    private var mConnectivityManager: ConnectivityManager? = null
    private var callback: NetworkListener

    interface NetworkListener {
        fun onNetworkConnectionAvailable()
        fun onNetworkConnectionLost()
    }

    init {
        mNetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        mConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        this.callback = callback
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        Log.i(javaClass.simpleName, "Connected to network");
        callback.onNetworkConnectionAvailable()

    }

    override fun onLost(network: Network) {
        super.onLost(network)
        Log.i(javaClass.simpleName, "Lost network connection");
        callback.onNetworkConnectionLost()
    }

    fun registerCallbackEvents() {
        Log.i(javaClass.simpleName, "Register callback events to trigger network connectivity")
        mConnectivityManager!!.registerNetworkCallback(mNetworkRequest!!, this)
    }
}