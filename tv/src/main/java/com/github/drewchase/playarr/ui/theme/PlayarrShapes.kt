package com.github.drewchase.playarr.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class PlayarrShapes(
    val small: Shape,    // 2dp
    val medium: Shape,   // 4dp
    val large: Shape,    // 6dp
    val button: Shape,   // 2dp
    val full: Shape,     // 50% circle
)

val LocalPlayarrShapes = staticCompositionLocalOf {
    PlayarrShapes(
        small = RoundedCornerShape(2.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(6.dp),
        button = RoundedCornerShape(2.dp),
        full = RoundedCornerShape(50),
    )
}
