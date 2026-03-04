package com.github.drewchase.playarr.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.github.drewchase.playarr.ui.theme.PlayarrTheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayarrText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = PlayarrTheme.typography.base,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
    )
}
