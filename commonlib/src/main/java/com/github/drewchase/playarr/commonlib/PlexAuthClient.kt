package com.github.drewchase.playarr.commonlib

import com.github.drewchase.playarr.commonlib.data.PlexPinData
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class PlexAuthClient(private val clientId: String) {
    private val client = OkHttpClient()
    private val gson = Gson()

    private fun plexHeaders(): Map<String, String> = mapOf(
        "X-Plex-Client-Identifier" to clientId,
        "X-Plex-Product" to "Playarr",
        "X-Plex-Version" to "1.0.0",
        "X-Plex-Platform" to "Android",
        "X-Plex-Device" to "Android TV",
        "X-Plex-Device-Name" to "Playarr TV",
        "Accept" to "application/json",
    )

    fun createPin(): PlexPinData {
        val body = FormBody.Builder()
            .add("strong", "false")
            .build()

        val requestBuilder = Request.Builder()
            .url("https://plex.tv/api/v2/pins")
            .post(body)

        plexHeaders().forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        val response = client.newCall(requestBuilder.build()).execute()
        val responseBody = response.body.string()
        return gson.fromJson(responseBody, PlexPinData::class.java)
    }

    fun checkPin(pinId: Long): PlexPinData {
        val requestBuilder = Request.Builder()
            .url("https://plex.tv/api/v2/pins/$pinId")
            .get()

        plexHeaders().forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        val response = client.newCall(requestBuilder.build()).execute()
        val responseBody = response.body.string()
        return gson.fromJson(responseBody, PlexPinData::class.java)
    }
}
