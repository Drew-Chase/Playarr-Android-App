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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import kotlinx.coroutines.withContext

private const val TAG = "PlayarrFocus"

class DashboardScreen {
    @RequiresApi(Build.VERSION_CODES.M)
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
    @TvPreviews
    @Composable
    fun View() {
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

        // Load data on first composition
        LaunchedEffect(Unit) {
            val state = withContext(Dispatchers.IO) {
                loadDashboardData(client)
            }
            dashboardState.value = state
        }

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
                    val heroItems = state.heroItems()
                    Log.d(TAG, "DashboardScreen: composing main content — " +
                            "heroItems=${heroItems.size}, " +
                            "watching=${state.watchingItems().size}, " +
                            "recentMovies=${state.recentMovies().size}, " +
                            "recentShows=${state.recentShows().size}")

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onFocusChanged { focusState ->
                                Log.d(TAG, "DashboardScreen(Box): onFocusChanged — " +
                                        "isFocused=${focusState.isFocused}, " +
                                        "hasFocus=${focusState.hasFocus}")
                            }
                    ) {
                        // Main content: single scrollable list so carousel slides up on navigate-down
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
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
                                            },
                                        navBarFocusRequester = navBarFocusRequester,
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
                            onNavItemSelected = { navItem ->
                                if (navItem == NavItem.SEARCH) {
                                    showSearch.value = true
                                }
                            },
                            onLibrarySelected = { /* TODO: navigate to library */ },
                            modifier = Modifier
                                .focusRequester(navBarFocusRequester)
                                .focusProperties {
                                    down = carouselFocusRequester
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
