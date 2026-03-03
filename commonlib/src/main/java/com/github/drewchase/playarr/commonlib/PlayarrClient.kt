package com.github.drewchase.playarr.commonlib

import com.github.drewchase.playarr.commonlib.data.PlayarrServerInformationData
import com.google.gson.FieldNamingPolicy

class PlayarrClient(val playarrConnectionUrl: String) {
    private val client: okhttp3.OkHttpClient = okhttp3.OkHttpClient()
    private val gson: com.google.gson.Gson = com.google.gson.GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()

    fun getServerInfo(): PlayarrServerInformationData {
        println("Fetching server info from $playarrConnectionUrl")
        val request = okhttp3.Request.Builder().get().url("${this.playarrConnectionUrl}/api/status")
        val response = this.client.newCall(request.build()).execute()
        return gson.fromJson(response.body.string(), PlayarrServerInformationData::class.java)
    }
}