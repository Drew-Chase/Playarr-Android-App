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
import com.github.drewchase.playarr.ui.theme.PlayarrTheme
import com.github.drewchase.playarr.screens.WelcomeScreen

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    @androidx.annotation.RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AppConfiguration(this)
        val setupComplete = mutableStateOf(config.isSetupComplete)
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
                        DashboardScreen().View()
                    }
                }
            }
        }
    }
}
