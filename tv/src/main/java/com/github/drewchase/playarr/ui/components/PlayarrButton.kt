package com.github.drewchase.playarr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.github.drewchase.playarr.ui.theme.PlayarrTheme

enum class PlayarrButtonStyle {
    PRIMARY, SECONDARY, OUTLINE
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayarrButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    style: PlayarrButtonStyle = if (isPrimary) PlayarrButtonStyle.PRIMARY else PlayarrButtonStyle.SECONDARY,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = ButtonDefaults.shape(shape = PlayarrTheme.shapes.button),
        border = when (style) {
            PlayarrButtonStyle.OUTLINE -> ButtonDefaults.border(
                border = Border(
                    border = BorderStroke(1.5.dp, PlayarrTheme.colors.foreground.copy(alpha = 0.5f)),
                    shape = PlayarrTheme.shapes.button,
                ),
                focusedBorder = Border(
                    border = BorderStroke(1.5.dp, PlayarrTheme.colors.foreground),
                    shape = PlayarrTheme.shapes.button,
                ),
                pressedBorder = Border(
                    border = BorderStroke(1.5.dp, PlayarrTheme.colors.foreground),
                    shape = PlayarrTheme.shapes.button,
                ),
            )
            else -> ButtonDefaults.border()
        },
        colors = when (style) {
            PlayarrButtonStyle.PRIMARY -> ButtonDefaults.colors(
                containerColor = PlayarrTheme.colors.primary,
                contentColor = PlayarrTheme.colors.primaryForeground,
                focusedContainerColor = PlayarrTheme.colors.primary,
                focusedContentColor = PlayarrTheme.colors.primaryForeground,
                pressedContainerColor = PlayarrTheme.colors.primary,
                pressedContentColor = PlayarrTheme.colors.primaryForeground,
            )
            PlayarrButtonStyle.SECONDARY -> ButtonDefaults.colors(
                containerColor = PlayarrTheme.colors.content2,
                contentColor = PlayarrTheme.colors.foreground,
                focusedContainerColor = PlayarrTheme.colors.content3,
                focusedContentColor = PlayarrTheme.colors.foreground,
                pressedContainerColor = PlayarrTheme.colors.content3,
                pressedContentColor = PlayarrTheme.colors.foreground,
            )
            PlayarrButtonStyle.OUTLINE -> ButtonDefaults.colors(
                containerColor = Color.Transparent,
                contentColor = PlayarrTheme.colors.foreground,
                focusedContainerColor = PlayarrTheme.colors.foreground.copy(alpha = 0.15f),
                focusedContentColor = PlayarrTheme.colors.foreground,
                pressedContainerColor = PlayarrTheme.colors.foreground.copy(alpha = 0.2f),
                pressedContentColor = PlayarrTheme.colors.foreground,
            )
        },
        content = content,
    )
}
