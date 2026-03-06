package com.github.drewchase.playarr

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.github.drewchase.playarr.commonlib.PlayarrClient
import fi.iki.elonen.NanoHTTPD
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Inet4Address

class SetupServer(private val context: Context, private val clientId: String) :
    NanoHTTPD(null, 65267) {

    var onSetupComplete: ((serverUrl: String, authToken: String) -> Unit)? = null
    var pinCode: String? = null
    var pinId: Long? = null

    private val httpClient = OkHttpClient()

    override fun serve(session: IHTTPSession?): Response {
        val uri = session?.uri ?: "/"
        val method = session?.method ?: Method.GET

        return when {
            method == Method.GET && uri == "/api/pin" -> handleGetPin()
            method == Method.POST && uri == "/api/setup" && session != null -> handleSetupSubmit(
                session
            )

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
        val rawToken = extractJsonField(body, "authToken")

        if (serverUrl.isNullOrBlank()) {
            return jsonError("Please enter a server URL.")
        }
        if (rawToken.isNullOrBlank()) {
            return jsonError("Please link your Plex account first.")
        }

        // Resolve the composite token: "{userId}:{plexTvToken}:{serverAccessToken}"
        // The Playarr server expects this format in the plex_user_token cookie.
        val compositeToken = try {
            resolveCompositeToken(rawToken)
        } catch (_: Exception) {
            // Fall back to raw token if we can't resolve
            rawToken
        }

        // Validate the server URL by trying to fetch server info
        return try {
            val client = PlayarrClient(serverUrl, compositeToken)
            val info = client.getServerInfo()
            onSetupComplete?.invoke(serverUrl, compositeToken)
            newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                """{"success": true, "serverName": "${info.version}"}"""
            )
        } catch (e: Exception) {
            jsonError("Could not connect to Playarr server at $serverUrl. Please check the URL and try again.")
        }
    }

    /**
     * Resolve a raw plex.tv auth token into the composite format expected by the
     * Playarr server: "{userId}:{plexTvToken}:{plexTvToken}".
     *
     * Fetches the user's Plex account info from plex.tv to obtain the user ID.
     * For admin users the plex.tv token IS the server token; for shared users the
     * Playarr server will resolve a server-specific token when needed.
     */
    private fun resolveCompositeToken(rawToken: String): String {
        val request = Request.Builder()
            .url("https://plex.tv/api/v2/user")
            .get()
            .header("Accept", "application/json")
            .header("X-Plex-Token", rawToken)
            .header("X-Plex-Product", "Playarr")
            .header("X-Plex-Client-Identifier", clientId)
            .build()
        val response = httpClient.newCall(request).execute()
        val body = response.body?.string()
            ?: throw Exception("Failed to resolve composite token: no response body")
        val userId = extractJsonLongField(body, "id") ?: 0
        return "$userId:$rawToken:$rawToken"
    }

    private fun jsonError(message: String): Response {
        val escaped = message.replace("\"", "\\\"")
        return newFixedLengthResponse(
            Response.Status.BAD_REQUEST,
            "application/json",
            """{"success": false, "error": "$escaped"}"""
        )
    }

    private fun extractJsonField(json: String, field: String): String? {
        val regex = """"$field"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(json)?.groupValues?.get(1)
    }

    private fun extractJsonLongField(json: String, field: String): Long? {
        val regex = """"$field"\s*:\s*(\d+)""".toRegex()
        return regex.find(json)?.groupValues?.get(1)?.toLongOrNull()
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
