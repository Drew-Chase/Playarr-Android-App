package com.github.drewchase.playarr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.github.drewchase.playarr.commonlib.data.PlexLibrary
import com.github.drewchase.playarr.commonlib.data.PlexUser
import com.github.drewchase.playarr.ui.theme.PlayarrTheme

enum class NavItem {
    HOME, MOVIES, TV_SHOWS, DISCOVER, SEARCH
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TopNavBar(
    user: PlexUser?,
    libraries: List<PlexLibrary>,
    imageLoader: ImageLoader,
    activeItem: NavItem = NavItem.HOME,
    onNavItemSelected: (NavItem) -> Unit,
    onLibrarySelected: (PlexLibrary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showMovieModal = remember { mutableStateOf(false) }
    val showTvModal = remember { mutableStateOf(false) }

    val movieLibraries = libraries.filter { it.type == "movie" }
    val tvLibraries = libraries.filter { it.type == "show" }

    // Transparent gradient navbar overlaying content
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PlayarrTheme.colors.background.copy(alpha = 0.8f),
                        Color.Transparent,
                    )
                )
            )
            .padding(horizontal = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Logo + brand
        PlayarrText(
            text = "Playarr",
            style = PlayarrTheme.typography.title.copy(fontWeight = FontWeight.Bold),
            color = PlayarrTheme.colors.primary,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Nav items - centered
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavTextLink(
                label = "Home",
                isActive = activeItem == NavItem.HOME,
                onClick = { onNavItemSelected(NavItem.HOME) },
            )

            if (movieLibraries.size == 1) {
                NavTextLink(
                    label = movieLibraries[0].title,
                    isActive = activeItem == NavItem.MOVIES,
                    onClick = { onLibrarySelected(movieLibraries[0]) },
                )
            } else if (movieLibraries.size > 1) {
                NavTextLink(
                    label = "Movies",
                    isActive = activeItem == NavItem.MOVIES,
                    onClick = { showMovieModal.value = true },
                )
            }

            if (tvLibraries.size == 1) {
                NavTextLink(
                    label = tvLibraries[0].title,
                    isActive = activeItem == NavItem.TV_SHOWS,
                    onClick = { onLibrarySelected(tvLibraries[0]) },
                )
            } else if (tvLibraries.size > 1) {
                NavTextLink(
                    label = "TV Shows",
                    isActive = activeItem == NavItem.TV_SHOWS,
                    onClick = { showTvModal.value = true },
                )
            }

            NavTextLink(
                label = "Discover",
                isActive = activeItem == NavItem.DISCOVER,
                onClick = { onNavItemSelected(NavItem.DISCOVER) },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Right side: Search icon + User avatar
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Search icon
            NavIconButton(
                onClick = { onNavItemSelected(NavItem.SEARCH) },
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp),
                )
            }

            // User avatar
            if (user?.thumb != null) {
                AsyncImage(
                    model = user.thumb,
                    imageLoader = imageLoader,
                    contentDescription = user.username ?: "User",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User",
                    tint = PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }

    // Library picker modals
    if (showMovieModal.value) {
        LibraryPickerModal(
            title = "Movie Libraries",
            libraries = movieLibraries,
            onDismiss = { showMovieModal.value = false },
            onSelect = { lib ->
                showMovieModal.value = false
                onLibrarySelected(lib)
            },
        )
    }

    if (showTvModal.value) {
        LibraryPickerModal(
            title = "TV Show Libraries",
            libraries = tvLibraries,
            onDismiss = { showTvModal.value = false },
            onSelect = { lib ->
                showTvModal.value = false
                onLibrarySelected(lib)
            },
        )
    }
}

/**
 * Plain text nav link with no background. Shows underline-like effect on focus.
 * Active item shows in primary green, inactive in muted foreground.
 */
@Composable
private fun NavTextLink(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val isFocused = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .onFocusChanged { isFocused.value = it.isFocused }
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.Enter || event.key == Key.DirectionCenter)
                ) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .padding(vertical = 8.dp, horizontal = 4.dp),
    ) {
        PlayarrText(
            text = label,
            style = PlayarrTheme.typography.base.copy(
                fontWeight = if (isActive || isFocused.value) FontWeight.Bold else FontWeight.Normal,
            ),
            color = when {
                isActive -> PlayarrTheme.colors.primary
                isFocused.value -> PlayarrTheme.colors.foreground
                else -> PlayarrTheme.colors.foreground.copy(alpha = 0.7f)
            },
        )
    }
}

/**
 * Focusable icon button for the nav bar (search icon).
 */
@Composable
private fun NavIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val isFocused = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .onFocusChanged { isFocused.value = it.isFocused }
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.Enter || event.key == Key.DirectionCenter)
                ) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
