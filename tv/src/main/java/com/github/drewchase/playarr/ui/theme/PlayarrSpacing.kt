package com.github.drewchase.playarr.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class PlayarrSpacing(
    val xxs: Dp,   // 2dp
    val xs: Dp,    // 4dp
    val sm: Dp,    // 6dp
    val md: Dp,    // 8dp
    val lg: Dp,    // 12dp
    val xl: Dp,    // 16dp
    val xxl: Dp,   // 24dp
    val xxxl: Dp,  // 32dp
)

val LocalPlayarrSpacing = staticCompositionLocalOf {
    PlayarrSpacing(
        xxs = 2.dp,
        xs = 4.dp,
        sm = 6.dp,
        md = 8.dp,
        lg = 12.dp,
        xl = 16.dp,
        xxl = 24.dp,
        xxxl = 32.dp,
    )
}
