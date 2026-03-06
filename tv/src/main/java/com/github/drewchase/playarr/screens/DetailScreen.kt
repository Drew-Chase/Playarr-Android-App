package com.github.drewchase.playarr.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.github.drewchase.playarr.AppConfiguration
import com.github.drewchase.playarr.commonlib.PlayarrClient
import com.github.drewchase.playarr.commonlib.data.PlexLibrary
import com.github.drewchase.playarr.commonlib.data.PlexMediaItem
import com.github.drewchase.playarr.commonlib.data.PlexUser
import com.github.drewchase.playarr.ui.components.CastCard
import com.github.drewchase.playarr.ui.components.EpisodeCard
import com.github.drewchase.playarr.ui.components.MediaRow
import com.github.drewchase.playarr.ui.components.MetadataBadge
import com.github.drewchase.playarr.ui.components.NavItem
import com.github.drewchase.playarr.ui.components.PlayarrButton
import com.github.drewchase.playarr.ui.components.PlayarrButtonStyle
import com.github.drewchase.playarr.ui.components.PlayarrCard
import com.github.drewchase.playarr.ui.components.PlayarrText
import com.github.drewchase.playarr.ui.components.PortraitMediaCard
import com.github.drewchase.playarr.ui.components.ProgressBar
import com.github.drewchase.playarr.ui.components.TopNavBar
import com.github.drewchase.playarr.ui.components.createPlayarrImageLoader
import com.github.drewchase.playarr.ui.theme.PlayarrTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "PlayarrDetail"

class DetailScreen(
    private val ratingKey: String,
    private val mediaType: String,
) {
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun View(
        onBack: () -> Unit = {},
        onItemClick: (PlexMediaItem) -> Unit = {},
        onSignOut: () -> Unit = {},
    ) {
        val context = LocalContext.current
        val config = remember { AppConfiguration(context) }
        val client = remember {
            PlayarrClient(
                playarrConnectionUrl = config.serverUrl ?: "",
                authToken = config.authToken,
            )
        }
        val imageLoader = remember { createPlayarrImageLoader(context, config.authToken) }
        val detailState = remember { mutableStateOf(DetailState()) }
        val selectedSeason = remember { mutableStateOf<PlexMediaItem?>(null) }
        val lazyListState = rememberLazyListState()

        // Nav bar data
        val user = remember { mutableStateOf<PlexUser?>(null) }
        val libraries = remember { mutableStateOf<List<PlexLibrary>>(emptyList()) }

        val playButtonFocusRequester = remember { FocusRequester() }
        val navBarFocusRequester = remember { FocusRequester() }
        val scope = rememberCoroutineScope()

        // Focus / scroll lock — same pattern as DashboardScreen
        val navBarFocused = remember { mutableStateOf(false) }
        val heroFocused = remember { mutableStateOf(false) }
        val dpadDownPressed = remember { mutableStateOf(false) }
        val scrollLock = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (!heroFocused.value && !navBarFocused.value) return Offset.Zero
                    if (dpadDownPressed.value) return Offset.Zero
                    val atTop = lazyListState.firstVisibleItemIndex == 0
                            && lazyListState.firstVisibleItemScrollOffset == 0
                    return if (atTop) {
                        Offset(0f, available.y)
                    } else if (available.y > 0) {
                        Offset.Zero
                    } else {
                        Offset(0f, available.y)
                    }
                }
            }
        }

        // Load detail data + nav bar data
        LaunchedEffect(ratingKey) {
            val state = withContext(Dispatchers.IO) {
                loadDetailData(client, ratingKey, mediaType)
            }
            detailState.value = state

            // Auto-select first season for TV shows
            if (state.seasons.isNotEmpty()) {
                selectedSeason.value = state.seasons.first()
            }

            // Load nav bar data
            withContext(Dispatchers.IO) {
                try { user.value = client.getUser() } catch (_: Exception) {}
                try { libraries.value = client.getLibraries() } catch (_: Exception) {}
            }
        }

        // Load episodes when selected season changes
        LaunchedEffect(selectedSeason.value?.ratingKey) {
            val season = selectedSeason.value ?: return@LaunchedEffect
            val episodes = withContext(Dispatchers.IO) {
                loadSeasonEpisodes(client, season.ratingKey)
            }
            detailState.value = detailState.value.copy(episodes = episodes)
        }

        // Focus the navbar after loading — NOT the play button.
        // Focusing the play button causes BringIntoView to scroll down.
        // The user can D-pad down from navbar to reach the play button.
        LaunchedEffect(detailState.value.isLoading) {
            if (!detailState.value.isLoading && detailState.value.item != null) {
                try {
                    navBarFocusRequester.requestFocus()
                } catch (_: Exception) {
                    Log.d(TAG, "Could not request focus on navbar")
                }
            }
        }

        BackHandler(enabled = true) {
            onBack()
        }

        PlayarrTheme {
            val state = detailState.value

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        PlayarrText(
                            text = "Loading...",
                            style = PlayarrTheme.typography.title,
                            color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                        )
                    }
                }

                state.error != null && state.item == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        PlayarrText(
                            text = state.error,
                            style = PlayarrTheme.typography.title,
                            color = PlayarrTheme.colors.statusRed,
                        )
                    }
                }

                state.item != null -> {
                    val item = state.item
                    val bgColor = PlayarrTheme.colors.background

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollLock)
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.key == Key.DirectionDown
                                    && keyEvent.type == KeyEventType.KeyDown
                                    && heroFocused.value
                                ) {
                                    dpadDownPressed.value = true
                                    scope.launch {
                                        lazyListState.animateScrollToItem(1)
                                    }
                                }
                                false
                            },
                    ) {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            contentPadding = PaddingValues(bottom = 48.dp),
                        ) {
                            // ── Hero Section ──
                            item {
                                HeroSection(
                                    item = item,
                                    onDeck = state.onDeck,
                                    client = client,
                                    imageLoader = imageLoader,
                                    bgColor = bgColor,
                                    playButtonFocusRequester = playButtonFocusRequester,
                                    navBarFocusRequester = navBarFocusRequester,
                                    onItemClick = onItemClick,
                                    onHeroFocusChanged = { hasFocus ->
                                        heroFocused.value = hasFocus
                                        if (!hasFocus) dpadDownPressed.value = false
                                    },
                                )
                            }

                            // ── Cast & Crew ──
                            val roles = item.roles
                            if (!roles.isNullOrEmpty()) {
                                item {
                                    CastRow(
                                        roles = roles,
                                        client = client,
                                        imageLoader = imageLoader,
                                    )
                                }
                            }

                            // ── Seasons (TV shows) ──
                            if (item.type == "show" && state.seasons.isNotEmpty()) {
                                item {
                                    SeasonsRow(
                                        seasons = state.seasons,
                                        selectedSeason = selectedSeason.value,
                                        client = client,
                                        imageLoader = imageLoader,
                                        onSeasonSelected = { season ->
                                            selectedSeason.value = season
                                        },
                                    )
                                }
                            }

                            // ── Episodes ──
                            if ((item.type == "show" || item.type == "season") && state.episodes.isNotEmpty()) {
                                item {
                                    PlayarrText(
                                        text = "Episodes",
                                        style = PlayarrTheme.typography.title,
                                        color = PlayarrTheme.colors.foreground,
                                        modifier = Modifier.padding(start = 48.dp),
                                    )
                                }
                                items(state.episodes) { episode ->
                                    Box(modifier = Modifier.padding(horizontal = 48.dp)) {
                                        EpisodeCard(
                                            item = episode,
                                            client = client,
                                            imageLoader = imageLoader,
                                            onClick = { onItemClick(episode) },
                                        )
                                    }
                                }
                            }

                            // ── Technical Info ──
                            val badges = buildList {
                                item.resolutionLabel()?.let { add(it) }
                                item.media?.firstOrNull()?.videoCodec?.uppercase()?.let { add(it) }
                                item.audioLabel()?.let { add(it) }
                            }
                            if (badges.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 48.dp),
                                        horizontalArrangement = Arrangement.spacedBy(PlayarrTheme.spacing.md),
                                    ) {
                                        badges.forEach { badge ->
                                            MetadataBadge(text = badge)
                                        }
                                    }
                                }
                            }

                            // ── Related Content ──
                            if (state.related.isNotEmpty()) {
                                item {
                                    MediaRow(
                                        title = "More Like This",
                                        items = state.related,
                                    ) { relatedItem ->
                                        PortraitMediaCard(
                                            item = relatedItem,
                                            client = client,
                                            imageLoader = imageLoader,
                                            onClick = { onItemClick(relatedItem) },
                                        )
                                    }
                                }
                            }
                        }

                        // Nav bar — drawn on top (last in Box = visually on top)
                        TopNavBar(
                            user = user.value,
                            libraries = libraries.value,
                            imageLoader = imageLoader,
                            client = client,
                            onNavItemSelected = { navItem ->
                                if (navItem == NavItem.HOME) {
                                    onBack()
                                }
                            },
                            onLibrarySelected = { },
                            onSignOut = onSignOut,
                            modifier = Modifier
                                .focusRequester(navBarFocusRequester)
                                .focusProperties {
                                    down = playButtonFocusRequester
                                }
                                .onFocusChanged {
                                    navBarFocused.value = it.hasFocus
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSection(
    item: PlexMediaItem,
    onDeck: PlexMediaItem?,
    client: PlayarrClient,
    imageLoader: coil3.ImageLoader,
    bgColor: Color,
    playButtonFocusRequester: FocusRequester,
    navBarFocusRequester: FocusRequester,
    onItemClick: (PlexMediaItem) -> Unit,
    onHeroFocusChanged: (Boolean) -> Unit = {},
) {
    val artUrl = (item.art ?: item.thumb)?.let { client.getImageUrl(it, width = 1920, height = 1080) }
    val thumbUrl = item.thumb?.let { client.getImageUrl(it, width = 400, height = 600) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .onFocusChanged { onHeroFocusChanged(it.hasFocus) },
    ) {
        // Backdrop
        if (artUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artUrl)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Left gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.horizontalGradient(
                            colors = listOf(
                                bgColor,
                                bgColor.copy(alpha = 0.7f),
                                Color.Transparent,
                            ),
                            startX = 0f,
                            endX = size.width * 0.7f,
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
                                bgColor.copy(alpha = 0.9f),
                            ),
                            startY = size.height * 0.6f,
                            endY = size.height,
                        )
                    )
                },
        )

        // Content overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 48.dp, end = 48.dp, bottom = 32.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            // Poster
            if (thumbUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thumbUrl)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(200.dp)
                        .height(300.dp)
                        .clip(PlayarrTheme.shapes.medium),
                )
                Spacer(modifier = Modifier.width(PlayarrTheme.spacing.xxl))
            }

            // Metadata column
            Column(modifier = Modifier.weight(1f)) {
                // Title
                PlayarrText(
                    text = item.title,
                    style = PlayarrTheme.typography.headline,
                    color = PlayarrTheme.colors.foreground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(PlayarrTheme.spacing.md))

                // Meta line: year | contentRating | duration | season/episode info
                val metaParts = buildList {
                    item.year?.let { add(it.toString()) }
                    item.contentRating?.let { add(it) }
                    item.formattedDuration()?.let { add(it) }
                    if (item.type == "show" && item.childCount != null) {
                        add("${item.childCount} Season${if (item.childCount != 1) "s" else ""}")
                    }
                    if (item.type == "season" && item.leafCount != null) {
                        add("${item.leafCount} Episode${if (item.leafCount != 1) "s" else ""}")
                    }
                }
                if (metaParts.isNotEmpty()) {
                    PlayarrText(
                        text = metaParts.joinToString("  \u2022  "),
                        style = PlayarrTheme.typography.lg,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
                    )
                }

                // Rating
                val rating = item.audienceRating ?: item.rating
                if (rating != null) {
                    Spacer(modifier = Modifier.height(PlayarrTheme.spacing.sm))
                    PlayarrText(
                        text = "\u2605 ${String.format("%.1f", rating)}/10",
                        style = PlayarrTheme.typography.lg,
                        color = PlayarrTheme.colors.statusYellow,
                    )
                }

                // Studio
                val studio = item.studio
                if (studio != null) {
                    PlayarrText(
                        text = studio,
                        style = PlayarrTheme.typography.sm,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                    )
                }

                // Director
                val directors = item.directors
                if (!directors.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(PlayarrTheme.spacing.sm))
                    Row {
                        PlayarrText(
                            text = "Directed by  ",
                            style = PlayarrTheme.typography.sm,
                            color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                        )
                        PlayarrText(
                            text = directors.joinToString(", ") { it.tag },
                            style = PlayarrTheme.typography.sm,
                            color = PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
                        )
                    }
                }

                // Writer
                val writers = item.writers
                if (!writers.isNullOrEmpty()) {
                    Row {
                        PlayarrText(
                            text = "Written by  ",
                            style = PlayarrTheme.typography.sm,
                            color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                        )
                        PlayarrText(
                            text = writers.joinToString(", ") { it.tag },
                            style = PlayarrTheme.typography.sm,
                            color = PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
                        )
                    }
                }

                // Genres
                val genres = item.genres
                if (!genres.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(PlayarrTheme.spacing.md))
                    Row(horizontalArrangement = Arrangement.spacedBy(PlayarrTheme.spacing.md)) {
                        genres.forEach { genre ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = PlayarrTheme.colors.content2,
                                        shape = PlayarrTheme.shapes.small,
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                            ) {
                                PlayarrText(
                                    text = genre.tag,
                                    style = PlayarrTheme.typography.xs,
                                    color = PlayarrTheme.colors.foreground.copy(alpha = 0.8f),
                                )
                            }
                        }
                    }
                }

                // Summary
                val summary = item.summary
                if (!summary.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(PlayarrTheme.spacing.md))
                    PlayarrText(
                        text = summary,
                        style = PlayarrTheme.typography.base,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(PlayarrTheme.spacing.xl))

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(PlayarrTheme.spacing.lg)) {
                    val playTarget = onDeck ?: item
                    val viewOffset = playTarget.viewOffset
                    val hasProgress = viewOffset != null && viewOffset > 0

                    val playLabel = if (onDeck != null) {
                        val code = onDeck.episodeLabel() ?: ""
                        if (hasProgress) "Resume $code" else "Play $code"
                    } else {
                        if (hasProgress) "Resume" else "Play"
                    }

                    PlayarrButton(
                        onClick = { onItemClick(playTarget) },
                        style = PlayarrButtonStyle.PRIMARY,
                        modifier = Modifier
                            .focusRequester(playButtonFocusRequester)
                            .focusProperties {
                                up = navBarFocusRequester
                            },
                    ) {
                        PlayarrText(
                            text = playLabel,
                            style = PlayarrTheme.typography.base,
                        )
                    }
                }

                // Progress bar
                val progress = (onDeck ?: item).progressFraction()
                if (progress != null && progress > 0f) {
                    Spacer(modifier = Modifier.height(PlayarrTheme.spacing.md))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(200.dp),
                    ) {
                        ProgressBar(
                            progress = progress,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(PlayarrTheme.spacing.md))
                        PlayarrText(
                            text = "${(progress * 100).toInt()}%",
                            style = PlayarrTheme.typography.xs,
                            color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CastRow(
    roles: List<com.github.drewchase.playarr.commonlib.data.PlexRole>,
    client: PlayarrClient,
    imageLoader: coil3.ImageLoader,
) {
    Column(
        modifier = Modifier.focusGroup(),
    ) {
        PlayarrText(
            text = "Cast & Crew",
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
            modifier = Modifier.focusRestorer(),
        ) {
            items(roles) { role ->
                CastCard(
                    role = role,
                    client = client,
                    imageLoader = imageLoader,
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SeasonsRow(
    seasons: List<PlexMediaItem>,
    selectedSeason: PlexMediaItem?,
    client: PlayarrClient,
    imageLoader: coil3.ImageLoader,
    onSeasonSelected: (PlexMediaItem) -> Unit,
) {
    val bgColor = PlayarrTheme.colors.background
    val content2Color = PlayarrTheme.colors.content2
    val fgColor = PlayarrTheme.colors.foreground
    val primaryColor = PlayarrTheme.colors.primary

    Column(
        modifier = Modifier.focusGroup(),
    ) {
        PlayarrText(
            text = "Seasons",
            style = PlayarrTheme.typography.title,
            color = fgColor,
            modifier = Modifier.padding(
                start = 48.dp,
                bottom = PlayarrTheme.spacing.lg,
            ),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(PlayarrTheme.spacing.xl),
            modifier = Modifier.focusRestorer(),
        ) {
            items(seasons) { season ->
                val isSelected = season.ratingKey == selectedSeason?.ratingKey
                val thumbUrl = season.thumb?.let { client.getImageUrl(it, width = 320, height = 480) }

                val borderModifier = if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = primaryColor,
                        shape = PlayarrTheme.shapes.medium,
                    )
                } else {
                    Modifier
                }

                PlayarrCard(
                    onClick = { onSeasonSelected(season) },
                    width = 160.dp,
                    height = 280.dp,
                    modifier = borderModifier,
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (thumbUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(thumbUrl)
                                    .build(),
                                imageLoader = imageLoader,
                                contentDescription = season.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(content2Color),
                                contentAlignment = Alignment.Center,
                            ) {
                                PlayarrText(
                                    text = season.title.take(1),
                                    style = PlayarrTheme.typography.xxl,
                                    color = fgColor,
                                )
                            }
                        }

                        // Bottom overlay with title + episode count
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .drawBehind {
                                    drawRect(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                bgColor.copy(alpha = 0.9f),
                                            ),
                                            startY = 0f,
                                            endY = size.height,
                                        )
                                    )
                                }
                                .padding(PlayarrTheme.spacing.lg),
                        ) {
                            PlayarrText(
                                text = season.title,
                                style = PlayarrTheme.typography.base,
                                color = PlayarrTheme.colors.foreground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val episodeCount = season.leafCount
                            if (episodeCount != null) {
                                PlayarrText(
                                    text = "$episodeCount episode${if (episodeCount != 1) "s" else ""}",
                                    style = PlayarrTheme.typography.xs,
                                    color = PlayarrTheme.colors.foreground.copy(alpha = 0.6f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
