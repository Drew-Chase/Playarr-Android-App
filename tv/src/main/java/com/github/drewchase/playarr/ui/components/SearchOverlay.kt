package com.github.drewchase.playarr.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import com.github.drewchase.playarr.commonlib.PlayarrClient
import com.github.drewchase.playarr.commonlib.data.PlexMediaItem
import com.github.drewchase.playarr.ui.theme.PlayarrTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Full-screen search overlay with text input and results grid.
 */
@Composable
fun SearchOverlay(
    client: PlayarrClient,
    imageLoader: ImageLoader,
    onDismiss: () -> Unit,
    onItemClick: (PlexMediaItem) -> Unit,
) {
    val query = remember { mutableStateOf("") }
    val results = remember { mutableStateOf<List<PlexMediaItem>>(emptyList()) }
    val focusRequester = remember { FocusRequester() }

    BackHandler(onBack = onDismiss)

    // Debounced search
    LaunchedEffect(query.value) {
        if (query.value.length < 2) {
            results.value = emptyList()
            return@LaunchedEffect
        }
        delay(400)
        val searchResults = withContext(Dispatchers.IO) {
            try {
                val hubs = client.search(query.value)
                hubs.flatMap { it.metadata ?: emptyList() }
            } catch (_: Exception) {
                emptyList()
            }
        }
        results.value = searchResults
    }

    // Auto-focus the search field
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayarrTheme.colors.background.copy(alpha = 0.95f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
        ) {
            // Search input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        PlayarrTheme.colors.content2,
                        shape = PlayarrTheme.shapes.medium,
                    )
                    .padding(16.dp),
            ) {
                if (query.value.isEmpty()) {
                    PlayarrText(
                        text = "Search movies, shows...",
                        style = PlayarrTheme.typography.xl,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.4f),
                    )
                }
                BasicTextField(
                    value = query.value,
                    onValueChange = { query.value = it },
                    textStyle = TextStyle(
                        color = PlayarrTheme.colors.foreground,
                        fontSize = PlayarrTheme.typography.xl.fontSize,
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(PlayarrTheme.colors.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }

            // Results
            if (results.value.isEmpty() && query.value.length >= 2) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    PlayarrText(
                        text = "No results found",
                        style = PlayarrTheme.typography.lg,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                    )
                }
            } else if (results.value.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp),
                    contentPadding = PaddingValues(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(results.value) { item ->
                        PortraitMediaCard(
                            item = item,
                            client = client,
                            imageLoader = imageLoader,
                            onClick = { onItemClick(item) },
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    PlayarrText(
                        text = "Start typing to search",
                        style = PlayarrTheme.typography.lg,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}
