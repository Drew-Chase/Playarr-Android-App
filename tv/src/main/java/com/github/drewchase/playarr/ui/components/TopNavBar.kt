package com.github.drewchase.playarr.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.github.drewchase.playarr.commonlib.data.PlexLibrary
import com.github.drewchase.playarr.commonlib.data.PlexUser
import com.github.drewchase.playarr.ui.theme.PlayarrTheme

private const val TAG = "PlayarrFocus"

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
            .padding(horizontal = 48.dp)
            .onFocusChanged { focusState ->
                Log.d(TAG, "TopNavBar(Row): onFocusChanged — " +
                        "isFocused=${focusState.isFocused}, " +
                        "hasFocus=${focusState.hasFocus}")
            }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    Log.d(TAG, "TopNavBar(Row): onKeyEvent — key=${keyEvent.key}")
                }
                false
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Logo
        PlayarrText(
            text = "Playarr",
            style = PlayarrTheme.typography.title.copy(fontWeight = FontWeight.Bold),
            color = PlayarrTheme.colors.primary,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Centered nav items
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavButton(
                label = "Home",
                isActive = activeItem == NavItem.HOME,
                onClick = { onNavItemSelected(NavItem.HOME) },
            )

            if (movieLibraries.size == 1) {
                NavButton(
                    label = movieLibraries[0].title,
                    isActive = activeItem == NavItem.MOVIES,
                    onClick = { onLibrarySelected(movieLibraries[0]) },
                )
            } else if (movieLibraries.size > 1) {
                NavButton(
                    label = "Movies",
                    isActive = activeItem == NavItem.MOVIES,
                    onClick = { showMovieModal.value = true },
                )
            }

            if (tvLibraries.size == 1) {
                NavButton(
                    label = tvLibraries[0].title,
                    isActive = activeItem == NavItem.TV_SHOWS,
                    onClick = { onLibrarySelected(tvLibraries[0]) },
                )
            } else if (tvLibraries.size > 1) {
                NavButton(
                    label = "TV Shows",
                    isActive = activeItem == NavItem.TV_SHOWS,
                    onClick = { showTvModal.value = true },
                )
            }

            NavButton(
                label = "Discover",
                isActive = activeItem == NavItem.DISCOVER,
                onClick = { onNavItemSelected(NavItem.DISCOVER) },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Right side: Search + User profile
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Search icon button
            Button(
                onClick = { onNavItemSelected(NavItem.SEARCH) },
                modifier = Modifier
                    .onFocusChanged { focusState ->
                        Log.d(TAG, "TopNavBar(SearchBtn): onFocusChanged — " +
                                "isFocused=${focusState.isFocused}, " +
                                "hasFocus=${focusState.hasFocus}")
                    },
                colors = ButtonDefaults.colors(
                    containerColor = Color.Transparent,
                    contentColor = PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
                    focusedContainerColor = PlayarrTheme.colors.foreground.copy(alpha = 0.1f),
                    focusedContentColor = PlayarrTheme.colors.foreground,
                ),
                shape = ButtonDefaults.shape(shape = CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(24.dp),
                )
            }

            // User profile button
            Button(
                onClick = { /* TODO: show user profile */ },
                modifier = Modifier
                    .onFocusChanged { focusState ->
                        Log.d(TAG, "TopNavBar(ProfileBtn): onFocusChanged — " +
                                "isFocused=${focusState.isFocused}, " +
                                "hasFocus=${focusState.hasFocus}")
                    },
                colors = ButtonDefaults.colors(
                    containerColor = Color.Transparent,
                    contentColor = PlayarrTheme.colors.foreground,
                    focusedContainerColor = PlayarrTheme.colors.foreground.copy(alpha = 0.1f),
                    focusedContentColor = PlayarrTheme.colors.foreground,
                ),
                shape = ButtonDefaults.shape(shape = CircleShape),
            ) {
                if (user?.thumb?.isNotBlank() == true) {
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
                        modifier = Modifier.size(32.dp),
                    )
                }
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
 * Nav text button using tv-material3 Button with transparent background.
 * Proper TV D-pad focus handling.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun NavButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .onFocusChanged { focusState ->
                Log.d(TAG, "TopNavBar(NavBtn:$label): onFocusChanged — " +
                        "isFocused=${focusState.isFocused}, " +
                        "hasFocus=${focusState.hasFocus}")
            }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    Log.d(TAG, "TopNavBar(NavBtn:$label): onKeyEvent — key=${keyEvent.key}")
                }
                false
            },
        colors = ButtonDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = if (isActive) PlayarrTheme.colors.primary
            else PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
            focusedContainerColor = Color.Transparent,
            focusedContentColor = if (isActive) PlayarrTheme.colors.primary
            else PlayarrTheme.colors.foreground,
        ),
        shape = ButtonDefaults.shape(shape = PlayarrTheme.shapes.button),
    ) {
        PlayarrText(
            text = label,
            style = PlayarrTheme.typography.base.copy(
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            ),
        )
    }
}
