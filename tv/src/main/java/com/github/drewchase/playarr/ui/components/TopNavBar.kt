package com.github.drewchase.playarr.ui.components

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val showMovieModal = remember { mutableStateOf(false) }
    val showTvModal = remember { mutableStateOf(false) }
    val showProfileModal = remember { mutableStateOf(false) }
    val movieLibraries = libraries.filter { it.type == "movie" }
    val tvLibraries = libraries.filter { it.type == "show" }

    // Animated pill indicator state
    val parentCoords = remember { mutableStateOf<LayoutCoordinates?>(null) }
    val itemCoords = remember { mutableStateMapOf<String, LayoutCoordinates>() }
    val navBarHasFocus = remember { mutableStateOf(false) }
    val focusedKey = remember { mutableStateOf<String?>(null) }

    val activeKey = when (activeItem) {
        NavItem.HOME -> "home"
        NavItem.MOVIES -> "movies"
        NavItem.TV_SHOWS -> "tvshows"
        NavItem.DISCOVER -> "discover"
        NavItem.SEARCH -> "search"
    }

    // Pill follows focused item when navbar has focus, otherwise rests on active page
    val pillTargetKey = if (navBarHasFocus.value && focusedKey.value != null)
        focusedKey.value!! else activeKey

    val parent = parentCoords.value
    val target = itemCoords[pillTargetKey]
    val pillVisible = parent != null && target != null
            && parent.isAttached && target.isAttached

    val targetPos =
        if (pillVisible) parent!!.localPositionOf(target!!, Offset.Zero) else Offset.Zero
    val targetW = if (pillVisible) target!!.size.width.toFloat() else 0f
    val targetH = if (pillVisible) target!!.size.height.toFloat() else 0f

    val animSpec = tween<Float>(300, easing = FastOutSlowInEasing)
    val pillX by animateFloatAsState(targetPos.x, animSpec, label = "pillX")
    val pillY by animateFloatAsState(targetPos.y, animSpec, label = "pillY")
    val pillW by animateFloatAsState(targetW, animSpec, label = "pillW")
    val pillH by animateFloatAsState(targetH, animSpec, label = "pillH")
    val pillAlpha by animateFloatAsState(
        if (navBarHasFocus.value) 1.0f else 0.20f,
        tween(300), label = "pillAlpha",
    )

    // Scale animations for search and profile (inline buttons)
    val searchScale by animateFloatAsState(
        if (focusedKey.value == "search" && navBarHasFocus.value) 1.1f else 1f,
        tween(200), label = "searchScale",
    )
    val profileScale by animateFloatAsState(
        if (focusedKey.value == "profile" && navBarHasFocus.value) 1.1f else 1f,
        tween(200), label = "profileScale",
    )

    // Animated content colors — synced with pill animation timing
    val colorAnimSpec = tween<Color>(300, easing = FastOutSlowInEasing)
    val searchContentColor by animateColorAsState(
        if (focusedKey.value == "search" && navBarHasFocus.value)
            PlayarrTheme.colors.background
        else PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
        colorAnimSpec, label = "searchColor",
    )
    val profileContentColor by animateColorAsState(
        if (focusedKey.value == "profile" && navBarHasFocus.value)
            PlayarrTheme.colors.background
        else PlayarrTheme.colors.foreground,
        colorAnimSpec, label = "profileColor",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .drawBehind {
                // Draw a tall gradient that extends well below the navbar row
                // so content scrolling behind remains readable
                val gradientHeight = size.height * 2.5f
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.85f),
                        0.4f to Color.Black.copy(alpha = 0.6f),
                        1f to Color.Transparent,
                        startY = 0f,
                        endY = gradientHeight,
                    ),
                    size = size.copy(height = gradientHeight),
                )
            }
            .padding(horizontal = 48.dp)
            .onGloballyPositioned { parentCoords.value = it }
            .onFocusChanged { focusState ->
                navBarHasFocus.value = focusState.hasFocus
                if (!focusState.hasFocus) focusedKey.value = null
                Log.d(
                    TAG, "TopNavBar(Row): onFocusChanged — " +
                            "isFocused=${focusState.isFocused}, " +
                            "hasFocus=${focusState.hasFocus}"
                )
            }
            .drawBehind {
                if (pillVisible && pillW > 0f) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = pillAlpha),
                        topLeft = Offset(pillX, pillY),
                        size = Size(pillW, pillH),
                        cornerRadius = CornerRadius(pillH / 2f, pillH / 2f),
                    )
                }
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
                itemKey = "home",
                isActive = activeItem == NavItem.HOME,
                isFocused = focusedKey.value == "home" && navBarHasFocus.value,
                onClick = { onNavItemSelected(NavItem.HOME) },
                onFocused = { focusedKey.value = it },
                onPositioned = { key, coords -> itemCoords[key] = coords },
            )

            if (movieLibraries.size == 1) {
                NavButton(
                    label = movieLibraries[0].title,
                    itemKey = "movies",
                    isActive = activeItem == NavItem.MOVIES,
                    isFocused = focusedKey.value == "movies" && navBarHasFocus.value,
                    onClick = { onLibrarySelected(movieLibraries[0]) },
                    onFocused = { focusedKey.value = it },
                    onPositioned = { key, coords -> itemCoords[key] = coords },
                )
            } else if (movieLibraries.size > 1) {
                NavButton(
                    label = "Movies",
                    itemKey = "movies",
                    isActive = activeItem == NavItem.MOVIES,
                    isFocused = focusedKey.value == "movies" && navBarHasFocus.value,
                    onClick = { showMovieModal.value = true },
                    onFocused = { focusedKey.value = it },
                    onPositioned = { key, coords -> itemCoords[key] = coords },
                )
            }

            if (tvLibraries.size == 1) {
                NavButton(
                    label = tvLibraries[0].title,
                    itemKey = "tvshows",
                    isActive = activeItem == NavItem.TV_SHOWS,
                    isFocused = focusedKey.value == "tvshows" && navBarHasFocus.value,
                    onClick = { onLibrarySelected(tvLibraries[0]) },
                    onFocused = { focusedKey.value = it },
                    onPositioned = { key, coords -> itemCoords[key] = coords },
                )
            } else if (tvLibraries.size > 1) {
                NavButton(
                    label = "TV Shows",
                    itemKey = "tvshows",
                    isActive = activeItem == NavItem.TV_SHOWS,
                    isFocused = focusedKey.value == "tvshows" && navBarHasFocus.value,
                    onClick = { showTvModal.value = true },
                    onFocused = { focusedKey.value = it },
                    onPositioned = { key, coords -> itemCoords[key] = coords },
                )
            }
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
                    .onGloballyPositioned { itemCoords["search"] = it }
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) focusedKey.value = "search"
                        Log.d(
                            TAG, "TopNavBar(SearchBtn): onFocusChanged — " +
                                    "isFocused=${focusState.isFocused}, " +
                                    "hasFocus=${focusState.hasFocus}"
                        )
                    }
                    .graphicsLayer {
                        scaleX = searchScale
                        scaleY = searchScale
                    },
                colors = ButtonDefaults.colors(
                    containerColor = Color.Transparent,
                    contentColor = searchContentColor,
                    focusedContainerColor = Color.Transparent,
                    focusedContentColor = searchContentColor,
                ),
                shape = ButtonDefaults.shape(shape = CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = searchContentColor,
                    modifier = Modifier.size(24.dp),
                )
            }

            // User profile button
            Button(
                onClick = { showProfileModal.value = true },
                modifier = Modifier
                    .onGloballyPositioned { itemCoords["profile"] = it }
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) focusedKey.value = "profile"
                        Log.d(
                            TAG, "TopNavBar(ProfileBtn): onFocusChanged — " +
                                    "isFocused=${focusState.isFocused}, " +
                                    "hasFocus=${focusState.hasFocus}"
                        )
                    }
                    .graphicsLayer {
                        scaleX = profileScale
                        scaleY = profileScale
                    },
                colors = ButtonDefaults.colors(
                    containerColor = Color.Transparent,
                    contentColor = profileContentColor,
                    focusedContainerColor = Color.Transparent,
                    focusedContentColor = profileContentColor,
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

    if (showProfileModal.value) {
        ProfileModal(
            user = user,
            onDismiss = { showProfileModal.value = false },
            onSignOut = {
                showProfileModal.value = false
                onSignOut()
            },
        )
    }
}

/**
 * Nav text button with pill indicator support.
 * Reports its position and focus state to the parent for the animated pill.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun NavButton(
    label: String,
    itemKey: String,
    isActive: Boolean,
    isFocused: Boolean,
    onClick: () -> Unit,
    onFocused: (String) -> Unit,
    onPositioned: (String, LayoutCoordinates) -> Unit,
) {
    // Animate text color in sync with the pill (300ms) instead of Button's instant snap
    val textColor by animateColorAsState(
        if (isFocused) PlayarrTheme.colors.background
        else PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
        tween(300, easing = FastOutSlowInEasing), label = "navTextColor",
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .onGloballyPositioned { onPositioned(itemKey, it) }
            .onFocusChanged { focusState ->
                if (focusState.isFocused) onFocused(itemKey)
                Log.d(
                    TAG, "TopNavBar(NavBtn:$label): onFocusChanged — " +
                            "isFocused=${focusState.isFocused}, " +
                            "hasFocus=${focusState.hasFocus}"
                )
            }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    Log.d(TAG, "TopNavBar(NavBtn:$label): onKeyEvent — key=${keyEvent.key}")
                }
                false
            },
        colors = ButtonDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = textColor,
            focusedContainerColor = Color.Transparent,
            focusedContentColor = textColor,
        ),
        shape = ButtonDefaults.shape(shape = PlayarrTheme.shapes.button),
    ) {
        PlayarrText(
            text = label,
            style = PlayarrTheme.typography.base.copy(fontWeight = FontWeight.Normal),
            color = textColor,
        )
    }
}

private data class ProfileMenuItem(
    val key: String,
    val label: String,
    val isSignOut: Boolean = false,
    val isHeader: Boolean = false,
    val onClick: () -> Unit = {},
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ProfileModal(
    user: PlexUser?,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit,
) {
    val menuItems = remember {
        listOf(
            ProfileMenuItem(key = "watch_parties", label = "Watch Parties"),
            ProfileMenuItem(key = "settings", label = "Settings"),
            ProfileMenuItem(key = "sign_out", label = "Sign Out", isSignOut = true),
        )
    }

    // Pill indicator state
    val modalParentCoords = remember { mutableStateOf<LayoutCoordinates?>(null) }
    val modalItemCoords = remember { mutableStateMapOf<String, LayoutCoordinates>() }
    val focusedItemKey = remember { mutableStateOf<String?>(null) }
    // Track previous pill position so we can detect first placement
    val prevPillTarget = remember { mutableStateOf<String?>(null) }

    val parent = modalParentCoords.value
    val target = focusedItemKey.value?.let { modalItemCoords[it] }
    val pillVisible = parent != null && target != null
            && parent.isAttached && target.isAttached

    val targetPos =
        if (pillVisible) parent!!.localPositionOf(target!!, Offset.Zero) else Offset.Zero
    val targetW = if (pillVisible) target.size.width.toFloat() else 0f
    val targetH = if (pillVisible) target.size.height.toFloat() else 0f

    // Use snap when this is the first focused item (no previous target to animate from)
    val isFirstPlacement = prevPillTarget.value == null && focusedItemKey.value != null
    val animSpec: AnimationSpec<Float> = if (isFirstPlacement) snap() else tween(300, easing = FastOutSlowInEasing)

    // Update previous target after computing animSpec
    LaunchedEffect(focusedItemKey.value) {
        if (focusedItemKey.value != null) {
            prevPillTarget.value = focusedItemKey.value
        }
    }

    val isSignOutFocused = focusedItemKey.value == "sign_out"
    val pillColor by animateColorAsState(
        if (isSignOutFocused) PlayarrTheme.colors.statusRed else Color.White,
        tween(300, easing = FastOutSlowInEasing), label = "modalPillColor",
    )

    val mPillX by animateFloatAsState(targetPos.x, animSpec, label = "mPillX")
    val mPillY by animateFloatAsState(targetPos.y, animSpec, label = "mPillY")
    val mPillW by animateFloatAsState(targetW, animSpec, label = "mPillW")
    val mPillH by animateFloatAsState(targetH, animSpec, label = "mPillH")

    Dialog(onDismissRequest = onDismiss) {
        Box(
            contentAlignment = Alignment.CenterStart,
        ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
                .onGloballyPositioned { modalParentCoords.value = it }
                .drawBehind {
                    if (pillVisible && mPillW > 0f) {
                        drawRoundRect(
                            color = pillColor,
                            topLeft = Offset(mPillX, mPillY),
                            size = Size(mPillW, mPillH),
                            cornerRadius = CornerRadius(mPillH / 2f, mPillH / 2f),
                        )
                    }
                },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // User name header
            PlayarrText(
                text = user?.username ?: "Profile",
                style = PlayarrTheme.typography.headline.copy(fontWeight = FontWeight.Bold),
                color = PlayarrTheme.colors.foreground,
            )

            Spacer(modifier = Modifier.height(24.dp))

            menuItems.forEach { item ->
                if (item.isHeader) {
                    PlayarrText(
                        text = item.label,
                        style = PlayarrTheme.typography.lg.copy(fontWeight = FontWeight.Normal),
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
                    )
                } else {
                    val isFocused = focusedItemKey.value == item.key
                    val textColor by animateColorAsState(
                        when {
                            isFocused && item.isSignOut -> Color.White
                            isFocused -> PlayarrTheme.colors.background
                            item.isSignOut -> PlayarrTheme.colors.statusRed
                            item.key == "cancel" -> PlayarrTheme.colors.foreground.copy(alpha = 0.6f)
                            else -> PlayarrTheme.colors.foreground
                        },
                        tween(300, easing = FastOutSlowInEasing),
                        label = "modalTextColor_${item.key}",
                    )

                    Button(
                        onClick = when {
                            item.isSignOut -> onSignOut
                            item.key == "cancel" -> onDismiss
                            else -> item.onClick
                        },
                        modifier = Modifier
                            .onGloballyPositioned { modalItemCoords[item.key] = it }
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) focusedItemKey.value = item.key
                            },
                        colors = ButtonDefaults.colors(
                            containerColor = Color.Transparent,
                            contentColor = textColor,
                            focusedContainerColor = Color.Transparent,
                            focusedContentColor = textColor,
                        ),
                        shape = ButtonDefaults.shape(shape = PlayarrTheme.shapes.button),
                    ) {
                        PlayarrText(
                            text = item.label,
                            style = if (item.key == "cancel") PlayarrTheme.typography.lg
                            else PlayarrTheme.typography.title,
                            color = textColor,
                        )
                    }
                }
            }
        }
        }
    }
}
