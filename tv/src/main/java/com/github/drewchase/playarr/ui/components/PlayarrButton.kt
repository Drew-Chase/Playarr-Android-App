package com.github.drewchase.playarr.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.github.drewchase.playarr.ui.theme.PlayarrTheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayarrButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = ButtonDefaults.shape(shape = PlayarrTheme.shapes.button),
        colors = if (isPrimary) {
            ButtonDefaults.colors(
                containerColor = PlayarrTheme.colors.primary,
                contentColor = PlayarrTheme.colors.primaryForeground,
                focusedContainerColor = PlayarrTheme.colors.primary,
                focusedContentColor = PlayarrTheme.colors.primaryForeground,
                pressedContainerColor = PlayarrTheme.colors.primary,
                pressedContentColor = PlayarrTheme.colors.primaryForeground,
            )
        } else {
            ButtonDefaults.colors(
                containerColor = PlayarrTheme.colors.content2,
                contentColor = PlayarrTheme.colors.foreground,
                focusedContainerColor = PlayarrTheme.colors.content3,
                focusedContentColor = PlayarrTheme.colors.foreground,
                pressedContainerColor = PlayarrTheme.colors.content3,
                pressedContentColor = PlayarrTheme.colors.foreground,
            )
        },
        content = content,
    )
}
