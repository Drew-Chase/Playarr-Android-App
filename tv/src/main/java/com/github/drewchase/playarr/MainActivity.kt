package com.github.drewchase.playarr

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.github.drewchase.playarr.screens.DashboardScreen
import com.github.drewchase.playarr.screens.DetailScreen
import com.github.drewchase.playarr.ui.theme.PlayarrTheme
import com.github.drewchase.playarr.screens.WelcomeScreen

sealed interface AppScreen {
    data object Dashboard : AppScreen
    data class Detail(val ratingKey: String, val mediaType: String) : AppScreen
}

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    @androidx.annotation.RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AppConfiguration(this)
        val setupComplete = mutableStateOf(config.isSetupComplete)
        val currentScreen = mutableStateOf<AppScreen>(AppScreen.Dashboard)
        setContent {
            PlayarrTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape,
                )
                {
                    if (!setupComplete.value) {
                        WelcomeScreen(onSetupComplete = {
                            setupComplete.value = true
                        }).View()
                    } else {
                        when (val screen = currentScreen.value) {
                            is AppScreen.Dashboard -> DashboardScreen().View(
                                onSignOut = {
                                    config.serverUrl = null
                                    config.authToken = null
                                    setupComplete.value = false
                                },
                                onItemClick = { item ->
                                    currentScreen.value = AppScreen.Detail(
                                        ratingKey = item.ratingKey,
                                        mediaType = item.type,
                                    )
                                },
                            )
                            is AppScreen.Detail -> DetailScreen(
                                ratingKey = screen.ratingKey,
                                mediaType = screen.mediaType,
                            ).View(
                                onBack = { currentScreen.value = AppScreen.Dashboard },
                                onItemClick = { item ->
                                    currentScreen.value = AppScreen.Detail(
                                        ratingKey = item.ratingKey,
                                        mediaType = item.type,
                                    )
                                },
                                onSignOut = {
                                    config.serverUrl = null
                                    config.authToken = null
                                    setupComplete.value = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
