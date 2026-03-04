package com.github.drewchase.playarr.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class PlayarrColors(
    val primary: Color,
    val primaryForeground: Color,
    val secondary: Color,
    val secondaryForeground: Color,
    val background: Color,
    val onBackground: Color,
    val content1: Color,
    val content2: Color,
    val content3: Color,
    val foreground: Color,
    val border: Color,
    val statusGreen: Color,
    val statusRed: Color,
    val statusOrange: Color,
    val statusBlue: Color,
    val statusYellow: Color,
    val disabledOpacity: Float,
)

val LocalPlayarrColors = staticCompositionLocalOf {
    PlayarrColors(
        primary = Color.Unspecified,
        primaryForeground = Color.Unspecified,
        secondary = Color.Unspecified,
        secondaryForeground = Color.Unspecified,
        background = Color.Unspecified,
        onBackground = Color.Unspecified,
        content1 = Color.Unspecified,
        content2 = Color.Unspecified,
        content3 = Color.Unspecified,
        foreground = Color.Unspecified,
        border = Color.Unspecified,
        statusGreen = Color.Unspecified,
        statusRed = Color.Unspecified,
        statusOrange = Color.Unspecified,
        statusBlue = Color.Unspecified,
        statusYellow = Color.Unspecified,
        disabledOpacity = 0.3f,
    )
}

fun playarrDarkColors(): PlayarrColors = PlayarrColors(
    primary = Primary,
    primaryForeground = PrimaryForeground,
    secondary = Secondary,
    secondaryForeground = SecondaryForeground,
    background = Background,
    onBackground = OnBackground,
    content1 = Content1,
    content2 = Content2,
    content3 = Content3,
    foreground = Foreground,
    border = Border,
    statusGreen = StatusGreen,
    statusRed = StatusRed,
    statusOrange = StatusOrange,
    statusBlue = StatusBlue,
    statusYellow = StatusYellow,
    disabledOpacity = 0.3f,
)
