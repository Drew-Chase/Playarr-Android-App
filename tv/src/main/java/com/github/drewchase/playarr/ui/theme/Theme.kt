package com.github.drewchase.playarr.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme


@Preview(name = "1080p TV", device = "id:tv_1080p", showSystemUi = true)
@Preview(name = "4K TV", device = "id:tv_4k", showSystemUi = true)
annotation class TvPreviews

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayarrTheme(
    colors: PlayarrColors = playarrDarkColors(),
    typography: PlayarrTypography = playarrTypography(),
    shapes: PlayarrShapes = PlayarrShapes(
        small = RoundedCornerShape(2.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(6.dp),
        button = RoundedCornerShape(8.dp),
        full = RoundedCornerShape(50),
    ),
    spacing: PlayarrSpacing = PlayarrSpacing(
        xxs = 2.dp, xs = 4.dp, sm = 6.dp, md = 8.dp,
        lg = 12.dp, xl = 16.dp, xxl = 24.dp, xxxl = 32.dp,
    ),
    content: @Composable () -> Unit,
) {
    // Bridge PlayarrColors into MaterialTheme so tv-material3
    // components (Button, Surface, Text) inherit correct colors
    val materialColors = darkColorScheme(
        primary = colors.primary,
        onPrimary = colors.primaryForeground,
        secondary = colors.secondary,
        onSecondary = colors.secondaryForeground,
        background = colors.background,
        onBackground = colors.onBackground,
        surface = colors.content1,
        surfaceVariant = colors.content2,
        onSurface = colors.foreground,
        onSurfaceVariant = colors.foreground,
        border = colors.border,
        borderVariant = colors.border,
    )

    CompositionLocalProvider(
        LocalPlayarrColors provides colors,
        LocalPlayarrTypography provides typography,
        LocalPlayarrShapes provides shapes,
        LocalPlayarrSpacing provides spacing,
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content,
        )
    }
}

object PlayarrTheme {
    val colors: PlayarrColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPlayarrColors.current

    val typography: PlayarrTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalPlayarrTypography.current

    val shapes: PlayarrShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalPlayarrShapes.current

    val spacing: PlayarrSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalPlayarrSpacing.current
}
