package com.github.drewchase.playarr

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import fi.iki.elonen.NanoHTTPD

class SetupServer(private val context: Context) : NanoHTTPD(null, 65267) {
    override fun serve(session: IHTTPSession?): Response? {
        val message =
            "<html><body><h1>Playarr Setup</h1><p>Playarr is a Plex server manager for Android.</p></body></html>"

        return newFixedLengthResponse(message)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getConnectionUrl(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val linkProperties = connectivityManager.getLinkProperties(network)
        val localIp = linkProperties?.linkAddresses?.first()?.address?.hostAddress
        val port = this.listeningPort

        return "${localIp}:${port}"
    }

}