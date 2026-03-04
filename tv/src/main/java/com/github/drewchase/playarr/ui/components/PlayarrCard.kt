package com.github.drewchase.playarr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.github.drewchase.playarr.ui.theme.PlayarrTheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayarrCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp,
    height: Dp,
    content: @Composable () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(width)
            .height(height),
        shape = CardDefaults.shape(shape = PlayarrTheme.shapes.medium),
        colors = CardDefaults.colors(
            containerColor = PlayarrTheme.colors.content1,
            contentColor = PlayarrTheme.colors.foreground,
            focusedContainerColor = PlayarrTheme.colors.content2,
            focusedContentColor = PlayarrTheme.colors.foreground,
        ),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, PlayarrTheme.colors.primary),
                shape = PlayarrTheme.shapes.medium,
            ),
        ),
    ) {
        content()
    }
}
