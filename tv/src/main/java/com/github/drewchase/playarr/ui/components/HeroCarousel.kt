package com.github.drewchase.playarr.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Carousel
import androidx.tv.material3.CarouselState
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
    val autoScrollProgress = remember { Animatable(0f) }

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

    // Reset progress when slide changes (manual navigation or auto-advance)
    LaunchedEffect(carouselState.activeItemIndex) {
        autoScrollProgress.snapTo(0f)
    }

    // Drive the auto-scroll timer, pausing/resuming based on focus.
    // Keyed on both activeItemIndex and focus so it restarts on either change.
    LaunchedEffect(carouselState.activeItemIndex, carouselHasFocus.value) {
        if (!carouselHasFocus.value) {
            val remaining = 1f - autoScrollProgress.value
            if (remaining > 0f) {
                autoScrollProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        (AUTO_SCROLL_DURATION_MS * remaining).toInt(),
                        easing = LinearEasing,
                    ),
                )
                // Advance if animation completed (not interrupted by focus/navigation)
                if (autoScrollProgress.value >= 1f) {
                    val nextIndex = (carouselState.activeItemIndex + 1) % items.size
                    carouselState.setActiveIndex(nextIndex)
                }
            }
        }
        // When focused: do nothing — previous animation was cancelled by LaunchedEffect restart
    }

    val carouselButtonFocusModifier = Modifier.focusProperties {
        // Block left/right so the Carousel handles slide navigation via key events
        left = FocusRequester.Cancel
        right = FocusRequester.Cancel
        if (navBarFocusRequester != null) {
            up = navBarFocusRequester
        }
    }

    val lastIndex = items.lastIndex

    Box(modifier = modifier.fillMaxWidth().height(520.dp)) {

        // ---- ANIMATED BACKDROP (horizontal slide + crossfade) ----
        AnimatedContent(
            targetState = carouselState.activeItemIndex,
            transitionSpec = {
                val forward = if (targetState == 0 && initialState == lastIndex) true
                else if (targetState == lastIndex && initialState == 0) false
                else targetState > initialState

                if (forward) {
                    // Navigate right → old slides left, new enters from right
                    (fadeIn(tween(500)) + slideInHorizontally { it / 3 }) togetherWith
                            (fadeOut(tween(500)) + slideOutHorizontally { -it / 3 }) using
                            SizeTransform(clip = false)
                } else {
                    // Navigate left → old slides right, new enters from left
                    (fadeIn(tween(500)) + slideInHorizontally { -it / 3 }) togetherWith
                            (fadeOut(tween(500)) + slideOutHorizontally { it / 3 }) using
                            SizeTransform(clip = false)
                }
            },
            label = "heroBackdrop",
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
            }
        }

        // ---- ANIMATED TEXT OVERLAY (vertical slide + crossfade) ----
        AnimatedContent(
            targetState = carouselState.activeItemIndex,
            transitionSpec = {
                // Exit: fade out + slide down. Enter: fade in + slide up from below.
                (fadeIn(tween(400, delayMillis = 150)) + slideInVertically { it / 4 }) togetherWith
                        (fadeOut(tween(250)) + slideOutVertically { it / 4 }) using
                        SizeTransform(clip = false)
            },
            label = "heroText",
        ) { index ->
            val item = items[index]

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 48.dp, bottom = 108.dp, end = 48.dp)
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
                }
            }
        }

        // ---- CAROUSEL (focus handling, navigation, indicator) ----
        // Visual content is rendered above — this only contains the focusable button.
        Carousel(
            itemCount = items.size,
            carouselState = carouselState,
            autoScrollDurationMillis = Long.MAX_VALUE,
            contentTransformStartToEnd =
                fadeIn(snap()) togetherWith fadeOut(snap()),
            contentTransformEndToStart =
                fadeIn(snap()) togetherWith fadeOut(snap()),
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { focusState ->
                    carouselHasFocus.value = focusState.hasFocus
                    // When the Carousel itself gets focus (not a child), redirect to More Info
                    if (focusState.isFocused && moreInfoFocusRequester != null) {
                        try {
                            moreInfoFocusRequester.requestFocus()
                        } catch (_: IllegalStateException) { }
                    }
                    Log.d(
                        TAG, "HeroCarousel(Carousel): onFocusChanged — " +
                                "isFocused=${focusState.isFocused}, " +
                                "hasFocus=${focusState.hasFocus}"
                    )
                }
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        Log.d(TAG, "HeroCarousel(Carousel): onKeyEvent — key=${keyEvent.key}")
                    }
                    false
                },
            carouselIndicator = {
                CarouselPillIndicator(
                    itemCount = items.size,
                    activeItemIndex = carouselState.activeItemIndex,
                    progress = autoScrollProgress.value,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 48.dp, bottom = 16.dp),
                )
            },
        ) { index ->
            // Only the action button — visual content is in the AnimatedContent layers above
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 48.dp, bottom = 48.dp)
                        .onFocusChanged { focusState ->
                            Log.d(
                                TAG, "HeroCarousel(ButtonRow): onFocusChanged — " +
                                        "isFocused=${focusState.isFocused}, " +
                                        "hasFocus=${focusState.hasFocus}"
                            )
                        },
                ) {
                    // More Info button
                    PlayarrButton(
                        onClick = { onInfoClick(items[index]) },
                        style = PlayarrButtonStyle.OUTLINE,
                        modifier = (if (moreInfoFocusRequester != null) Modifier.focusRequester(
                            moreInfoFocusRequester
                        ) else Modifier)
                            .then(carouselButtonFocusModifier)
                            .onFocusChanged { focusState ->
                                Log.d(
                                    TAG,
                                    "HeroCarousel(MoreInfoButton): onFocusChanged — " +
                                            "isFocused=${focusState.isFocused}, " +
                                            "hasFocus=${focusState.hasFocus}"
                                )
                            }
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    Log.d(
                                        TAG,
                                        "HeroCarousel(MoreInfoButton): onKeyEvent — key=${keyEvent.key}"
                                    )
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

private const val AUTO_SCROLL_DURATION_MS = 5000

/** Advance the carousel via reflection since [CarouselState.activeItemIndex] has internal set. */
@OptIn(ExperimentalTvMaterial3Api::class)
private fun CarouselState.setActiveIndex(index: Int) {
    try {
        val field = javaClass.getDeclaredField("activeItemIndex\$delegate")
        field.isAccessible = true
        (field.get(this) as androidx.compose.runtime.MutableIntState).intValue = index
    } catch (e: Exception) {
        Log.w(TAG, "Failed to set carousel index", e)
    }
}

/**
 * Custom carousel indicator with expanded active pill and progress fill.
 * Inactive items are small dots; the active item is a wider pill with
 * a progress bar showing the auto-scroll timer.
 */
@Composable
private fun CarouselPillIndicator(
    itemCount: Int,
    activeItemIndex: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val pillHeight = 8.dp
    val dotSize = 8.dp
    val activePillWidth = 40.dp
    val transitionSpec = tween<Dp>(300, easing = FastOutSlowInEasing)
    val alphaTransitionSpec = tween<Float>(300, easing = FastOutSlowInEasing)

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        repeat(itemCount) { index ->
            val isActive = index == activeItemIndex
            val animatedWidth by animateDpAsState(
                targetValue = if (isActive) activePillWidth else dotSize,
                animationSpec = transitionSpec,
                label = "pillWidth",
            )
            val animatedAlpha by animateFloatAsState(
                targetValue = if (isActive) 0.3f else 0.5f,
                animationSpec = alphaTransitionSpec,
                label = "pillAlpha",
            )

            Box(
                modifier = Modifier
                    .width(animatedWidth)
                    .height(pillHeight)
                    .background(
                        Color.White.copy(alpha = animatedAlpha),
                        RoundedCornerShape(50),
                    ),
            ) {
                if (isActive && progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.coerceIn(0.01f, 1f))
                            .background(
                                Color.White,
                                RoundedCornerShape(50),
                            ),
                    )
                }
            }
        }
    }
}
