package com.github.drewchase.playarr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.github.drewchase.playarr.ui.theme.PlayarrTheme

@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(PlayarrTheme.shapes.full)
            .background(PlayarrTheme.colors.content3),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                .height(3.dp)
                .clip(PlayarrTheme.shapes.full)
                .background(PlayarrTheme.colors.primary),
        )
    }
}
