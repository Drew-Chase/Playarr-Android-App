package com.github.drewchase.playarr.commonlib

import com.github.drewchase.playarr.commonlib.data.CreateWatchPartyRequest
import com.github.drewchase.playarr.commonlib.data.DiscoverResults
import com.github.drewchase.playarr.commonlib.data.PlayarrServerInformationData
import com.github.drewchase.playarr.commonlib.data.PlexLibrary
import com.github.drewchase.playarr.commonlib.data.PlexMediaItem
import com.github.drewchase.playarr.commonlib.data.PlexServerUser
import com.github.drewchase.playarr.commonlib.data.PlexUser
import com.github.drewchase.playarr.commonlib.data.SearchHub
import com.github.drewchase.playarr.commonlib.data.WatchRoom
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

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

    private val gsonCamelCase = GsonBuilder().create()

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

    // -- Watch Party --

    fun createWatchParty(request: CreateWatchPartyRequest): WatchRoom {
        return post("/api/watch-party/rooms", request, WatchRoom::class.java)
    }

    fun listWatchPartyRooms(): List<WatchRoom> {
        return getList("/api/watch-party/rooms")
    }

    fun getWatchPartyRoom(roomId: String): WatchRoom {
        return get("/api/watch-party/rooms/$roomId", WatchRoom::class.java)
    }

    fun deleteWatchPartyRoom(roomId: String): Boolean {
        return delete("/api/watch-party/rooms/$roomId")
    }

    fun joinByInviteCode(code: String): WatchRoom {
        return get("/api/watch-party/join/$code", WatchRoom::class.java)
    }

    fun kickUser(roomId: String, userId: Long, reason: String? = null): Boolean {
        val body = mapOf("user_id" to userId, "reason" to reason)
        post("/api/watch-party/rooms/$roomId/kick", body, Map::class.java)
        return true
    }

    // -- Plex Users --

    fun getPlexUsers(): List<PlexServerUser> {
        return getList("/api/plex/users")
    }

    // -- Media detail endpoints --

    fun getMediaDetail(ratingKey: String): PlexMediaItem {
        return get("/api/media/$ratingKey", PlexMediaItem::class.java)
    }

    fun getMediaChildren(ratingKey: String): List<PlexMediaItem> {
        return getList("/api/media/$ratingKey/children")
    }

    fun getRelatedMedia(ratingKey: String): List<PlexMediaItem> {
        return getList("/api/media/$ratingKey/related")
    }

    fun getOnDeckForMedia(ratingKey: String): PlexMediaItem? {
        return try {
            get("/api/media/$ratingKey/onDeck", PlexMediaItem::class.java)
        } catch (_: Exception) {
            null
        }
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

    private fun <T> post(path: String, requestBody: Any, clazz: Class<T>): T {
        val json = gsonCamelCase.toJson(requestBody)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url("${playarrConnectionUrl}$path")
            .post(json.toRequestBody(mediaType))
            .build()
        val response = client.newCall(request).execute()
        val body = response.body.string()
        return gson.fromJson(body, clazz)
    }

    private fun delete(path: String): Boolean {
        val request = Request.Builder()
            .url("${playarrConnectionUrl}$path")
            .delete()
            .build()
        val response = client.newCall(request).execute()
        return response.isSuccessful
    }
}
