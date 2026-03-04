package com.github.drewchase.playarr.ui.theme

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme


@Preview(name = "1080p TV", device = "id:tv_1080p", showSystemUi = true)
@Preview(name = "4K TV", device = "id:tv_4k", showSystemUi = true)
annotation class TvPreviews

val ButtonShape = RoundedCornerShape(8.dp)


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayarrTVTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = darkColorScheme(
        primary = Primary,
        onPrimary = OnPrimary,
        secondary = Secondary,
        onSecondary = OnSecondary,
        background = Background,
        onBackground = OnBackground,
        surface = Surface,
        surfaceVariant = SurfaceVariant,
        onSurface = OnSurface,
        onSurfaceVariant = OnSurfaceVariant,
        border = Border,
        borderVariant = Border,
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayarrButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
    isPrimary: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = ButtonDefaults.shape(shape = ButtonShape),
        colors = if (isPrimary) {
            ButtonDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            ButtonDefaults.colors()
        },
        content = content,
    )
}
