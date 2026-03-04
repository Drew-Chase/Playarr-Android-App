package com.github.drewchase.playarr

import android.content.Context
import androidx.core.content.edit

class AppConfiguration(private val context: Context) {
    private val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
    var serverUrl: String?
        get() = prefs.getString("server_url", null)
        set(value) = prefs.edit { putString("server_url", value) }
    var authToken: String?
        get() = prefs.getString("auth_token", null)
        set(value) = prefs.edit { putString("auth_token", value) }
    val isSetupComplete: Boolean
        get() = serverUrl != null && authToken != null

}