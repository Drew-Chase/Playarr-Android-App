package com.github.drewchase.playarr.screens

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.focusGroup
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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

class DashboardScreen {
    @RequiresApi(Build.VERSION_CODES.M)
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @OptIn(ExperimentalTvMaterial3Api::class)
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

        // Focus requesters for explicit nav ↔ content focus wiring
        val navFocusRequester = remember { FocusRequester() }
        val contentFocusRequester = remember { FocusRequester() }

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
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Top navigation bar - zIndex keeps it visually on top.
                        // focusProperties wires D-pad Down to the content below.
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
                                .zIndex(1f)
                                .focusRequester(navFocusRequester)
                                .focusProperties { down = contentFocusRequester }
                                .focusGroup(),
                        )

                        // Main scrollable content.
                        // focusProperties wires D-pad Up to the nav bar.
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(contentFocusRequester)
                                .focusProperties { up = navFocusRequester }
                                .focusGroup(),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            contentPadding = PaddingValues(bottom = 48.dp),
                        ) {
                            // Hero Carousel
                            val heroItems = state.heroItems()
                            if (heroItems.isNotEmpty()) {
                                item {
                                    HeroCarousel(
                                        items = heroItems,
                                        client = client,
                                        imageLoader = imageLoader,
                                        onPlayClick = { /* TODO: navigate to player */ },
                                        onInfoClick = { /* TODO: navigate to detail */ },
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
