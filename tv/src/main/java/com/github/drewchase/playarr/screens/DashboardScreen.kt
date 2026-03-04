package com.github.drewchase.playarr.screens

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.github.drewchase.playarr.ui.components.PlayarrText
import com.github.drewchase.playarr.ui.theme.PlayarrTheme
import com.github.drewchase.playarr.ui.theme.TvPreviews

class DashboardScreen {
    @RequiresApi(Build.VERSION_CODES.M)
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @OptIn(ExperimentalTvMaterial3Api::class)
    @TvPreviews
    @Composable
    fun View() {
        PlayarrTheme{
            PlayarrText("Dashboard")
        }
    }
}