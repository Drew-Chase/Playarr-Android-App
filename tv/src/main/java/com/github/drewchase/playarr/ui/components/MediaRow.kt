package com.github.drewchase.playarr.ui.components

import android.util.Log
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.github.drewchase.playarr.ui.theme.PlayarrTheme

private const val TAG = "PlayarrFocus"

/**
 * A titled horizontal row of media cards.
 * Used for "Continue Watching", "Recently Added", etc.
 * Skips rendering entirely if items is empty.
 */
@Composable
fun <T> MediaRow(
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit,
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier
            .onFocusChanged { focusState ->
                Log.d(TAG, "MediaRow($title): onFocusChanged — " +
                        "isFocused=${focusState.isFocused}, " +
                        "hasFocus=${focusState.hasFocus}")
            }
            .focusGroup()
    ) {
        PlayarrText(
            text = title,
            style = PlayarrTheme.typography.title,
            color = PlayarrTheme.colors.foreground,
            modifier = Modifier.padding(
                start = 48.dp,
                bottom = PlayarrTheme.spacing.lg,
            ),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(PlayarrTheme.spacing.xl),
        ) {
            items(items) { item ->
                itemContent(item)
            }
        }
    }
}
