package com.github.drewchase.playarr.commonlib

import com.github.drewchase.playarr.commonlib.data.*
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request

class PlayarrClient(
    val playarrConnectionUrl: String,
    private val authToken: String? = null,
) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            if (!authToken.isNullOrBlank()) {
                requestBuilder.addHeader("Cookie", "plex_user_token=$authToken")
            }
            chain.proceed(requestBuilder.build())
        }
        .build()

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    // -- Server status --

    fun getServerInfo(): PlayarrServerInformationData {
        return get("/api/status", PlayarrServerInformationData::class.java)
    }

    // -- Dashboard hub endpoints --

    fun getContinueWatching(): List<PlexMediaItem> {
        return getList("/api/hubs/continue-watching")
    }

    fun getOnDeck(): List<PlexMediaItem> {
        return getList("/api/hubs/on-deck")
    }

    fun getRecentlyAdded(): List<PlexMediaItem> {
        return getList("/api/hubs/recently-added")
    }

    // -- Library endpoints --

    fun getLibraries(): List<PlexLibrary> {
        return getList("/api/libraries")
    }

    // -- Discover endpoints --

    fun getTrending(): DiscoverResults {
        return get("/api/discover/trending", DiscoverResults::class.java)
    }

    fun getUpcoming(): DiscoverResults {
        return get("/api/discover/upcoming", DiscoverResults::class.java)
    }

    // -- Auth/User --

    fun getUser(): PlexUser {
        return get("/api/auth/user", PlexUser::class.java)
    }

    // -- Search --

    fun search(query: String): List<SearchHub> {
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        return getList("/api/search?q=$encoded")
    }

    // -- Image URL builders --

    fun getThumbUrl(ratingKey: String): String {
        return "${playarrConnectionUrl}/api/media/$ratingKey/thumb"
    }

    fun getArtUrl(ratingKey: String): String {
        return "${playarrConnectionUrl}/api/media/$ratingKey/art"
    }

    fun getImageUrl(path: String, width: Int? = null, height: Int? = null): String {
        val encoded = java.net.URLEncoder.encode(path, "UTF-8")
        val params = buildString {
            append("path=$encoded")
            if (width != null) append("&width=$width")
            if (height != null) append("&height=$height")
        }
        return "${playarrConnectionUrl}/api/media/image?$params"
    }

    // -- Internal helpers --

    private fun <T> get(path: String, clazz: Class<T>): T {
        val request = Request.Builder().get().url("${playarrConnectionUrl}$path").build()
        val response = client.newCall(request).execute()
        val body = response.body.string()
        return gson.fromJson(body, clazz)
    }

    private inline fun <reified T> getList(path: String): List<T> {
        val request = Request.Builder().get().url("${playarrConnectionUrl}$path").build()
        val response = client.newCall(request).execute()
        val body = response.body.string()
        val type = TypeToken.getParameterized(List::class.java, T::class.java).type
        return gson.fromJson(body, type) ?: emptyList()
    }
}
