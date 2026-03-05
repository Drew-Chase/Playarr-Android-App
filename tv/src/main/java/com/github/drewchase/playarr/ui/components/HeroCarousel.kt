package com.github.drewchase.playarr.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Carousel
import androidx.tv.material3.CarouselDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.rememberCarouselState
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.github.drewchase.playarr.commonlib.PlayarrClient
import com.github.drewchase.playarr.commonlib.data.PlexMediaItem
import com.github.drewchase.playarr.ui.theme.PlayarrTheme

private const val TAG = "PlayarrFocus"

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HeroCarousel(
    items: List<PlexMediaItem>,
    client: PlayarrClient,
    imageLoader: ImageLoader,
    onPlayClick: (PlexMediaItem) -> Unit,
    onInfoClick: (PlexMediaItem) -> Unit,
    modifier: Modifier = Modifier,
    navBarFocusRequester: FocusRequester? = null,
    moreInfoFocusRequester: FocusRequester? = null,
) {
    if (items.isEmpty()) {
        Log.w(TAG, "HeroCarousel: items is EMPTY, returning early")
        return
    }

    Log.d(TAG, "HeroCarousel: composing with ${items.size} items")

    val backgroundColor = PlayarrTheme.colors.background
    val carouselState = rememberCarouselState()
    val carouselHasFocus = remember { mutableStateOf(false) }

    // Refocus More Info button when carousel slide changes while focused
    LaunchedEffect(carouselState.activeItemIndex) {
        if (carouselHasFocus.value && moreInfoFocusRequester != null) {
            try {
                moreInfoFocusRequester.requestFocus()
            } catch (_: IllegalStateException) {
                // FocusRequester not yet attached to new slide
            }
        }
    }

    val carouselButtonFocusModifier = Modifier.focusProperties {
        // Block left/right so the Carousel handles slide navigation via key events
        left = FocusRequester.Cancel
        right = FocusRequester.Cancel
        if (navBarFocusRequester != null) {
            up = navBarFocusRequester
        }
    }

    Carousel(
        itemCount = items.size,
        carouselState = carouselState,
        modifier = modifier
            .fillMaxWidth()
            .height(520.dp)
            .onFocusChanged { focusState ->
                carouselHasFocus.value = focusState.hasFocus
                // When the Carousel itself gets focus (not a child), redirect to More Info
                if (focusState.isFocused && moreInfoFocusRequester != null) {
                    try {
                        moreInfoFocusRequester.requestFocus()
                    } catch (_: IllegalStateException) { }
                }
                Log.d(TAG, "HeroCarousel(Carousel): onFocusChanged — " +
                        "isFocused=${focusState.isFocused}, " +
                        "hasFocus=${focusState.hasFocus}")
            }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    Log.d(TAG, "HeroCarousel(Carousel): onKeyEvent — key=${keyEvent.key}")
                }
                false
            },
        carouselIndicator = {
            CarouselDefaults.IndicatorRow(
                itemCount = items.size,
                activeItemIndex = carouselState.activeItemIndex,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 48.dp, bottom = 16.dp),
            )
        },
    ) { index ->
        val item = items[index]
        val artUrl = item.art?.let { client.getImageUrl(it, width = 1920, height = 800) }

        Box(modifier = Modifier.fillMaxSize()) {
            // Backdrop image
            if (artUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artUrl)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Left-to-right gradient for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    backgroundColor,
                                    backgroundColor.copy(alpha = 0.7f),
                                    Color.Transparent,
                                )
                            )
                        )
                    },
            )

            // Bottom gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    backgroundColor.copy(alpha = 0.9f),
                                ),
                                startY = size.height * 0.5f,
                                endY = size.height,
                            )
                        )
                    },
            )

            // Content overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 48.dp, bottom = 48.dp, end = 48.dp)
                    .fillMaxWidth(0.5f),
            ) {
                // Title
                PlayarrText(
                    text = when (item.type) {
                        "episode" -> item.grandparentTitle ?: item.title
                        else -> item.title
                    },
                    style = PlayarrTheme.typography.headline.copy(fontWeight = FontWeight.Bold),
                    color = PlayarrTheme.colors.foreground,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Metadata line
                val metaParts = buildList {
                    item.year?.let { add(it.toString()) }
                    item.contentRating?.let { add(it) }
                    item.formattedDuration()?.let { add(it) }
                    item.episodeLabel()?.let { add(it) }
                }
                if (metaParts.isNotEmpty()) {
                    PlayarrText(
                        text = metaParts.joinToString(" \u2022 "),
                        style = PlayarrTheme.typography.lg,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
                    )
                }

                // Summary
                val summary = item.summary
                if (!summary.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PlayarrText(
                        text = summary,
                        style = PlayarrTheme.typography.base,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                    )
                }

                // Progress bar for continue watching
                val progress = item.progressFraction()
                if (progress != null && progress > 0f) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ProgressBar(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(0.6f),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons: [Play/Resume] [More Info]
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.onFocusChanged { focusState ->
                        Log.d(TAG, "HeroCarousel(ButtonRow): onFocusChanged — " +
                                "isFocused=${focusState.isFocused}, " +
                                "hasFocus=${focusState.hasFocus}")
                    },
                ) {
                    // More Info button
                    PlayarrButton(
                        onClick = { onInfoClick(item) },
                        style = PlayarrButtonStyle.OUTLINE,
                        modifier = (if (moreInfoFocusRequester != null) Modifier.focusRequester(moreInfoFocusRequester) else Modifier)
                            .then(carouselButtonFocusModifier)
                            .onFocusChanged { focusState ->
                                Log.d(TAG, "HeroCarousel(MoreInfoButton): onFocusChanged — " +
                                        "isFocused=${focusState.isFocused}, " +
                                        "hasFocus=${focusState.hasFocus}")
                            }
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    Log.d(TAG, "HeroCarousel(MoreInfoButton): onKeyEvent — key=${keyEvent.key}")
                                }
                                false
                            },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        PlayarrText(
                            text = "More Info",
                            style = PlayarrTheme.typography.lg,
                        )
                    }
                }
            }
        }
    }
}
