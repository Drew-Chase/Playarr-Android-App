package com.github.drewchase.playarr.screens

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.github.drewchase.playarr.AppConfiguration
import com.github.drewchase.playarr.commonlib.PlayarrClient
import com.github.drewchase.playarr.ui.components.DiscoverCard
import com.github.drewchase.playarr.ui.components.HeroCarousel
import com.github.drewchase.playarr.ui.components.LandscapeMediaCard
import com.github.drewchase.playarr.ui.components.MediaRow
import com.github.drewchase.playarr.ui.components.NavItem
import com.github.drewchase.playarr.ui.components.PlayarrText
import com.github.drewchase.playarr.ui.components.PortraitMediaCard
import com.github.drewchase.playarr.ui.components.SearchOverlay
import com.github.drewchase.playarr.ui.components.TopNavBar
import com.github.drewchase.playarr.ui.components.createPlayarrImageLoader
import com.github.drewchase.playarr.ui.theme.PlayarrTheme
import com.github.drewchase.playarr.ui.theme.TvPreviews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "PlayarrFocus"

class DashboardScreen {
    @RequiresApi(Build.VERSION_CODES.M)
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
    @TvPreviews
    @Composable
    fun View(onSignOut: () -> Unit = {}) {
        val context = LocalContext.current
        val config = remember { AppConfiguration(context) }
        val client = remember {
            PlayarrClient(
                playarrConnectionUrl = config.serverUrl ?: "",
                authToken = config.authToken,
            )
        }
        val imageLoader = remember { createPlayarrImageLoader(context, config.authToken) }
        val dashboardState = remember { mutableStateOf(DashboardState()) }
        val showSearch = remember { mutableStateOf(false) }

        // FocusRequesters for explicit nav <-> carousel wiring
        val carouselFocusRequester = remember { FocusRequester() }
        val navBarFocusRequester = remember { FocusRequester() }
        val moreInfoFocusRequester = remember { FocusRequester() }
        val contentFocusRequester = remember { FocusRequester() }
        val lazyListState = rememberLazyListState()
        val carouselFocused = remember { mutableStateOf(false) }

        // Block all LazyColumn scroll while carousel or navbar has focus.
        // This prevents BringIntoView from scrolling to center the More Info
        // button — both when entering the carousel and when briefly passing
        // through it on the way up to the navbar.
        val navBarFocused = remember { mutableStateOf(false) }
        // Track intentional D-pad down presses to allow scroll through the lock
        val dpadDownPressed = remember { mutableStateOf(false) }
        val carouselScrollLock = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (!carouselFocused.value && !navBarFocused.value) return Offset.Zero
                    // Allow scroll when user intentionally pressed D-pad down
                    // Don't reset — BringIntoView needs multiple scroll increments
                    if (dpadDownPressed.value) return Offset.Zero
                    val atTop = lazyListState.firstVisibleItemIndex == 0
                            && lazyListState.firstVisibleItemScrollOffset == 0
                    // At top: block downward scroll (BringIntoView centering the button)
                    // Not at top: allow upward scroll (bringing carousel back into view)
                    return if (atTop) {
                        Offset(0f, available.y) // consume everything at top
                    } else if (available.y > 0) {
                        Offset.Zero // allow scroll toward top
                    } else {
                        Offset(0f, available.y) // block further downward scroll
                    }
                }
            }
        }

        // Load data on first composition
        LaunchedEffect(Unit) {
            val state = withContext(Dispatchers.IO) {
                loadDashboardData(client)
            }
            dashboardState.value = state
        }

        val scope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current

        PlayarrTheme {
            val state = dashboardState.value

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
                state.error != null && state.heroItems().isEmpty() -> {
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
                else -> {
                    // Focus the navbar on initial load
                    LaunchedEffect(Unit) {
                        navBarFocusRequester.requestFocus()
                    }

                    val heroItems = state.heroItems()
                    Log.d(TAG, "DashboardScreen: composing main content — " +
                            "heroItems=${heroItems.size}, " +
                            "watching=${state.watchingItems().size}, " +
                            "recentMovies=${state.recentMovies().size}, " +
                            "recentShows=${state.recentShows().size}")

                    // Intercept back when scrolled down — focus navbar instead of exiting
                    val shouldInterceptBack by remember {
                        derivedStateOf {
                            (lazyListState.firstVisibleItemIndex > 0
                                    || lazyListState.firstVisibleItemScrollOffset > 0)
                                    && !navBarFocused.value
                        }
                    }
                    BackHandler(enabled = shouldInterceptBack) {
                        navBarFocusRequester.requestFocus()
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(carouselScrollLock)
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.key == Key.DirectionDown
                                    && keyEvent.type == KeyEventType.KeyDown
                                    && carouselFocused.value
                                ) {
                                    dpadDownPressed.value = true
                                    scope.launch {
                                        lazyListState.animateScrollToItem(1)
                                    }
                                }
                                false
                            }
                            .onFocusChanged { focusState ->
                                Log.d(TAG, "DashboardScreen(Box): onFocusChanged — " +
                                        "isFocused=${focusState.isFocused}, " +
                                        "hasFocus=${focusState.hasFocus}")
                            }
                    ) {
                        // Main content: single scrollable list so carousel slides up on navigate-down
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(contentFocusRequester)
                                .focusRestorer()
                                .onFocusChanged { focusState ->
                                    Log.d(TAG, "DashboardScreen(LazyColumn): onFocusChanged — " +
                                            "isFocused=${focusState.isFocused}, " +
                                            "hasFocus=${focusState.hasFocus}")
                                },
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            contentPadding = PaddingValues(bottom = 48.dp),
                        ) {
                            // Hero Carousel — first scrollable item, slides up when navigating down
                            if (heroItems.isNotEmpty()) {
                                item {
                                    HeroCarousel(
                                        items = heroItems,
                                        client = client,
                                        imageLoader = imageLoader,
                                        onPlayClick = { /* TODO: navigate to player */ },
                                        onInfoClick = { /* TODO: navigate to detail */ },
                                        modifier = Modifier
                                            .focusRequester(carouselFocusRequester)
                                            .focusProperties {
                                                up = navBarFocusRequester
                                            }
                                            .onFocusChanged {
                                                carouselFocused.value = it.hasFocus
                                                if (!it.hasFocus) dpadDownPressed.value = false
                                            },
                                        navBarFocusRequester = navBarFocusRequester,
                                        moreInfoFocusRequester = moreInfoFocusRequester,
                                    )
                                }
                            }
                                // Continue Watching
                                val watching = state.watchingItems()
                                if (watching.isNotEmpty()) {
                                    item {
                                        MediaRow(
                                            title = "Continue Watching",
                                            items = watching,
                                        ) { mediaItem ->
                                            LandscapeMediaCard(
                                                item = mediaItem,
                                                client = client,
                                                imageLoader = imageLoader,
                                                onClick = { /* TODO: navigate to player */ },
                                            )
                                        }
                                    }
                                }

                                // Recently Added Movies
                                val recentMovies = state.recentMovies()
                                if (recentMovies.isNotEmpty()) {
                                    item {
                                        MediaRow(
                                            title = "Recently Added Movies",
                                            items = recentMovies,
                                        ) { mediaItem ->
                                            PortraitMediaCard(
                                                item = mediaItem,
                                                client = client,
                                                imageLoader = imageLoader,
                                                onClick = { /* TODO: navigate to detail */ },
                                            )
                                        }
                                    }
                                }

                                // Recently Added TV Shows
                                val recentShows = state.recentShows()
                                if (recentShows.isNotEmpty()) {
                                    item {
                                        MediaRow(
                                            title = "Recently Added TV Shows",
                                            items = recentShows,
                                        ) { mediaItem ->
                                            PortraitMediaCard(
                                                item = mediaItem,
                                                client = client,
                                                imageLoader = imageLoader,
                                                onClick = { /* TODO: navigate to detail */ },
                                            )
                                        }
                                    }
                                }

                                // Trending Movies
                                val trendingMovies = state.trending?.movies
                                if (!trendingMovies.isNullOrEmpty()) {
                                    item {
                                        MediaRow(
                                            title = "Trending Movies",
                                            items = trendingMovies,
                                        ) { tmdbItem ->
                                            DiscoverCard(
                                                item = tmdbItem,
                                                onClick = { /* TODO: navigate to detail */ },
                                            )
                                        }
                                    }
                                }

                                // Trending TV Shows
                                val trendingTv = state.trending?.tv
                                if (!trendingTv.isNullOrEmpty()) {
                                    item {
                                        MediaRow(
                                            title = "Trending TV Shows",
                                            items = trendingTv,
                                        ) { tmdbItem ->
                                            DiscoverCard(
                                                item = tmdbItem,
                                                onClick = { /* TODO: navigate to detail */ },
                                            )
                                        }
                                    }
                                }

                                // Upcoming Movies
                                val upcomingMovies = state.upcoming?.movies
                                if (!upcomingMovies.isNullOrEmpty()) {
                                    item {
                                        MediaRow(
                                            title = "Upcoming Movies",
                                            items = upcomingMovies,
                                        ) { tmdbItem ->
                                            DiscoverCard(
                                                item = tmdbItem,
                                                onClick = { /* TODO: navigate to detail */ },
                                            )
                                        }
                                    }
                                }
                            }

                        // Nav bar — drawn on top (last in Box = visually on top).
                        TopNavBar(
                            user = state.user,
                            libraries = state.libraries,
                            imageLoader = imageLoader,
                            client = client,
                            onNavItemSelected = { navItem ->
                                if (navItem == NavItem.SEARCH) {
                                    showSearch.value = true
                                }
                            },
                            onLibrarySelected = { /* TODO: navigate to library */ },
                            onSignOut = onSignOut,
                            modifier = Modifier
                                .focusRequester(navBarFocusRequester)
                                .focusProperties {
                                    down = moreInfoFocusRequester
                                }
                                .onPreviewKeyEvent { keyEvent ->
                                    // When scrolled down, intercept D-pad down and focus the
                                    // LazyColumn directly. focusRestorer() will restore focus
                                    // to the last focused child, then Enter drills into the
                                    // MediaRow focusGroup to reach the actual card.
                                    if (keyEvent.key == Key.DirectionDown
                                        && keyEvent.type == KeyEventType.KeyDown
                                    ) {
                                        val isScrolled = lazyListState.firstVisibleItemIndex > 0
                                                || lazyListState.firstVisibleItemScrollOffset > 0
                                        if (isScrolled) {
                                            contentFocusRequester.requestFocus()
                                            // Enter the focus groups to reach the actual card
                                            focusManager.moveFocus(FocusDirection.Enter)
                                            focusManager.moveFocus(FocusDirection.Enter)
                                            true
                                        } else false
                                    } else false
                                }
                                .onFocusChanged {
                                    navBarFocused.value = it.hasFocus
                                },
                        )

                        // Search overlay
                        if (showSearch.value) {
                            SearchOverlay(
                                client = client,
                                imageLoader = imageLoader,
                                onDismiss = { showSearch.value = false },
                                onItemClick = { /* TODO: navigate to detail */ },
                            )
                        }
                    }
                }
            }
        }
    }
}
