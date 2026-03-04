package com.github.drewchase.playarr.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class PlayarrTypography(
    // Size scale matching the web app
    val xs: TextStyle,       // 12sp
    val sm: TextStyle,       // 14sp
    val base: TextStyle,     // 16sp
    val lg: TextStyle,       // 18sp
    val xl: TextStyle,       // 20sp
    val xxl: TextStyle,      // 24sp
    val xxxl: TextStyle,     // 36sp
    val xxxxl: TextStyle,    // 48sp
    val xxxxxl: TextStyle,   // 60sp

    // Semantic aliases
    val body: TextStyle,
    val bodySmall: TextStyle,
    val label: TextStyle,
    val title: TextStyle,
    val titleLarge: TextStyle,
    val headline: TextStyle,
    val hero: TextStyle,
)

val LocalPlayarrTypography = staticCompositionLocalOf {
    PlayarrTypography(
        xs = TextStyle.Default,
        sm = TextStyle.Default,
        base = TextStyle.Default,
        lg = TextStyle.Default,
        xl = TextStyle.Default,
        xxl = TextStyle.Default,
        xxxl = TextStyle.Default,
        xxxxl = TextStyle.Default,
        xxxxxl = TextStyle.Default,
        body = TextStyle.Default,
        bodySmall = TextStyle.Default,
        label = TextStyle.Default,
        title = TextStyle.Default,
        titleLarge = TextStyle.Default,
        headline = TextStyle.Default,
        hero = TextStyle.Default,
    )
}

fun playarrTypography(): PlayarrTypography {
    val fontFamily = FontFamily.Default // Roboto is Android's default

    return PlayarrTypography(
        xs = TextStyle(fontFamily = fontFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal),
        sm = TextStyle(fontFamily = fontFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal),
        base = TextStyle(fontFamily = fontFamily, fontSize = 16.sp, fontWeight = FontWeight.Normal),
        lg = TextStyle(fontFamily = fontFamily, fontSize = 18.sp, fontWeight = FontWeight.Normal),
        xl = TextStyle(fontFamily = fontFamily, fontSize = 20.sp, fontWeight = FontWeight.Normal),
        xxl = TextStyle(fontFamily = fontFamily, fontSize = 24.sp, fontWeight = FontWeight.Normal),
        xxxl = TextStyle(fontFamily = fontFamily, fontSize = 36.sp, fontWeight = FontWeight.Normal),
        xxxxl = TextStyle(fontFamily = fontFamily, fontSize = 48.sp, fontWeight = FontWeight.Normal),
        xxxxxl = TextStyle(fontFamily = fontFamily, fontSize = 60.sp, fontWeight = FontWeight.Normal),
        body = TextStyle(fontFamily = fontFamily, fontSize = 16.sp, fontWeight = FontWeight.Normal),
        bodySmall = TextStyle(fontFamily = fontFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal),
        label = TextStyle(fontFamily = fontFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium),
        title = TextStyle(fontFamily = fontFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold),
        titleLarge = TextStyle(fontFamily = fontFamily, fontSize = 36.sp, fontWeight = FontWeight.Bold),
        headline = TextStyle(fontFamily = fontFamily, fontSize = 48.sp, fontWeight = FontWeight.Bold),
        hero = TextStyle(fontFamily = fontFamily, fontSize = 60.sp, fontWeight = FontWeight.Bold),
    )
}
