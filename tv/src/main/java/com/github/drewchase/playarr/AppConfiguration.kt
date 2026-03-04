package com.github.drewchase.playarr

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

class AppConfiguration(private val context: Context) {
    private val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
    var serverUrl: String?
        get() = prefs.getString("server_url", null)
        set(value) = prefs.edit { putString("server_url", value) }
    var authToken: String?
        get() = prefs.getString("auth_token", null)
        set(value) = prefs.edit { putString("auth_token", value) }
    val plexClientId: String
        get() {
            val existing = prefs.getString("plex_client_id", null)
            if (existing != null) return existing
            val newId = UUID.randomUUID().toString()
            prefs.edit { putString("plex_client_id", newId) }
            return newId
        }
    val isSetupComplete: Boolean
        get() = serverUrl != null && authToken != null
}