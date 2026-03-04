package com.github.drewchase.playarr.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.github.drewchase.playarr.ui.theme.PlayarrButton
import com.github.drewchase.playarr.ui.theme.PlayarrTVTheme
import com.github.drewchase.playarr.ui.theme.TvPreviews

class WelcomeScreen {

    @OptIn(ExperimentalTvMaterial3Api::class)
    @TvPreviews
    @Composable
    fun View() {
        PlayarrTVTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .border(width = 4.dp, color = MaterialTheme.colorScheme.primary),
            )
            {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Welcome to",
                        fontSize = TextUnit(32f, TextUnitType.Sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Playarr",
                        fontSize = TextUnit(48f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterHorizontally
                        ),
                    ) {
                        PlayarrButton(
                            onClick = { },
                            content = {
                                Text("Get Started", fontSize = TextUnit(16f, TextUnitType.Sp))
                            },
                            isPrimary = true
                        )
                        PlayarrButton(
                            onClick = { },
                            content = {
                                Text("Cancel", fontSize = TextUnit(16f, TextUnitType.Sp))
                            },
                        )
                    }
                }
            }
        }
    }
}