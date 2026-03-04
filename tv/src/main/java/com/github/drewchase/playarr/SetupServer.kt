package com.github.drewchase.playarr

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import fi.iki.elonen.NanoHTTPD
import java.net.Inet4Address

class SetupServer(private val context: Context, private val clientId: String) :
    NanoHTTPD(null, 65267) {

    var onSetupComplete: ((serverUrl: String, authToken: String) -> Unit)? = null
    var pinCode: String? = null
    var pinId: Long? = null

    override fun serve(session: IHTTPSession?): Response {
        val uri = session?.uri ?: "/"
        val method = session?.method ?: Method.GET

        return when {
            method == Method.GET && uri == "/api/pin" -> handleGetPin()
            method == Method.POST && uri == "/api/setup" && session != null -> handleSetupSubmit(session)
            else -> serveSetupPage()
        }
    }

    private fun handleGetPin(): Response {
        val code = pinCode
        val id = pinId
        if (code != null && id != null) {
            return newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                """{"clientId": "$clientId", "pinId": $id, "code": "$code"}"""
            )
        }
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            """{"clientId": "$clientId", "pinId": null, "code": null}"""
        )
    }

    private fun serveSetupPage(): Response {
        return try {
            val html = context.resources.openRawResource(
                context.resources.getIdentifier("index", "raw", context.packageName)
            ).bufferedReader().readText()
            newFixedLengthResponse(Response.Status.OK, "text/html", html)
        } catch (e: Exception) {
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "text/plain",
                "Failed to load setup page: ${e.message}"
            )
        }
    }

    private fun handleSetupSubmit(session: IHTTPSession): Response {
        val files = HashMap<String, String>()
        session.parseBody(files)
        val body = files["postData"] ?: ""

        val serverUrl = extractJsonField(body, "serverUrl")
        val authToken = extractJsonField(body, "authToken")

        if (serverUrl != null && authToken != null) {
            onSetupComplete?.invoke(serverUrl, authToken)
            return newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                """{"success": true}"""
            )
        }

        return newFixedLengthResponse(
            Response.Status.BAD_REQUEST,
            "application/json",
            """{"success": false, "error": "Missing serverUrl or authToken"}"""
        )
    }

    private fun extractJsonField(json: String, field: String): String? {
        val regex = """"$field"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(json)?.groupValues?.get(1)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getConnectionUrl(): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val linkProperties = connectivityManager.getLinkProperties(network)
        val localIp = linkProperties?.linkAddresses
            ?.firstOrNull { it.address is Inet4Address }
            ?.address?.hostAddress

        return "${localIp}:65267"
    }
}
