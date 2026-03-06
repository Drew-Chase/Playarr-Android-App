package com.github.drewchase.playarr.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import com.github.drewchase.playarr.commonlib.PlayarrClient
import com.github.drewchase.playarr.commonlib.data.CreateWatchPartyRequest
import com.github.drewchase.playarr.commonlib.data.PlexServerUser
import com.github.drewchase.playarr.commonlib.data.WatchPartyAccessMode
import com.github.drewchase.playarr.commonlib.data.WatchRoom
import com.github.drewchase.playarr.ui.theme.PlayarrTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class WatchPartyTab { CREATE, JOIN }

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun WatchPartyModal(
    client: PlayarrClient,
    onDismiss: () -> Unit,
) {
    val selectedTab = remember { mutableStateOf(WatchPartyTab.CREATE) }

    // Tab pill indicator state
    val tabParentCoords = remember { mutableStateOf<LayoutCoordinates?>(null) }
    val tabItemCoords = remember { mutableStateMapOf<WatchPartyTab, LayoutCoordinates>() }
    val focusedTab = remember { mutableStateOf<WatchPartyTab?>(null) }
    val prevTabTarget = remember { mutableStateOf<WatchPartyTab?>(null) }
    val tabFocusRequesters = remember { WatchPartyTab.entries.associateWith { FocusRequester() } }
    val partyNameFocusRequester = remember { FocusRequester() }

    val tabParent = tabParentCoords.value
    val tabTarget = focusedTab.value?.let { tabItemCoords[it] }
    val tabPillVisible = tabParent != null && tabTarget != null
            && tabParent.isAttached && tabTarget.isAttached

    val tabTargetPos =
        if (tabPillVisible) tabParent!!.localPositionOf(tabTarget!!, Offset.Zero) else Offset.Zero
    val tabTargetW = if (tabPillVisible) tabTarget!!.size.width.toFloat() else 0f
    val tabTargetH = if (tabPillVisible) tabTarget!!.size.height.toFloat() else 0f

    val isFirstTabPlacement = prevTabTarget.value == null && focusedTab.value != null
    val tabAnimSpec: AnimationSpec<Float> =
        if (isFirstTabPlacement) snap() else tween(300, easing = FastOutSlowInEasing)

    LaunchedEffect(focusedTab.value) {
        if (focusedTab.value != null) {
            prevTabTarget.value = focusedTab.value
        }
    }

    val tPillX by animateFloatAsState(tabTargetPos.x, tabAnimSpec, label = "tPillX")
    val tPillY by animateFloatAsState(tabTargetPos.y, tabAnimSpec, label = "tPillY")
    val tPillW by animateFloatAsState(tabTargetW, tabAnimSpec, label = "tPillW")
    val tPillH by animateFloatAsState(tabTargetH, tabAnimSpec, label = "tPillH")

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        PlayarrTheme.colors.content1,
                        RoundedCornerShape(12.dp),
                    )
                    .padding(24.dp),
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = if (selectedTab.value == WatchPartyTab.CREATE)
                                Icons.Default.Add else Icons.Default.Person,
                            contentDescription = null,
                            tint = PlayarrTheme.colors.primary,
                            modifier = Modifier.size(28.dp),
                        )
                        PlayarrText(
                            text = if (selectedTab.value == WatchPartyTab.CREATE)
                                "Create Watch Party" else "Join Watch Party",
                            style = PlayarrTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                            color = PlayarrTheme.colors.foreground,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab row with green pill
                Row(
                    modifier = Modifier
                        .onGloballyPositioned { tabParentCoords.value = it }
                        .drawBehind {
                            if (tabPillVisible && tPillW > 0f) {
                                drawRoundRect(
                                    color = Color(0xFF1CE783),
                                    topLeft = Offset(tPillX, tPillY),
                                    size = Size(tPillW, tPillH),
                                    cornerRadius = CornerRadius(tPillH / 2f, tPillH / 2f),
                                )
                            }
                        },
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    WatchPartyTab.entries.forEach { tab ->
                        val isFocused = focusedTab.value == tab
                        val tabTextColor by animateColorAsState(
                            if (isFocused) PlayarrTheme.colors.primaryForeground
                            else PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
                            tween(300, easing = FastOutSlowInEasing),
                            label = "tabColor_$tab",
                        )

                        Button(
                            onClick = { selectedTab.value = tab },
                            modifier = Modifier
                                .focusRequester(tabFocusRequesters[tab]!!)
                                .focusProperties { down = partyNameFocusRequester }
                                .onGloballyPositioned { tabItemCoords[tab] = it }
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        focusedTab.value = tab
                                        selectedTab.value = tab
                                    }
                                },
                            colors = ButtonDefaults.colors(
                                containerColor = Color.Transparent,
                                contentColor = tabTextColor,
                                focusedContainerColor = Color.Transparent,
                                focusedContentColor = tabTextColor,
                            ),
                            shape = ButtonDefaults.shape(shape = PlayarrTheme.shapes.button),
                        ) {
                            PlayarrText(
                                text = if (tab == WatchPartyTab.CREATE) "Create" else "Join",
                                style = PlayarrTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                                color = tabTextColor,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tab content
                when (selectedTab.value) {
                    WatchPartyTab.CREATE -> CreateWatchPartyContent(
                        client = client,
                        onDismiss = onDismiss,
                        selectedTab = selectedTab.value,
                        tabFocusRequesters = tabFocusRequesters,
                        partyNameFocusRequester = partyNameFocusRequester,
                    )
                    WatchPartyTab.JOIN -> JoinWatchPartyContent(client = client, onDismiss = onDismiss)
                }
            }
        }
    }
}

private val ACCESS_MODES = listOf(
    WatchPartyAccessMode.everyone,
    WatchPartyAccessMode.invite_only,
    WatchPartyAccessMode.by_user,
)

private val ACCESS_MODE_LABELS = listOf("Everyone", "Invite Only", "Select Users")
private val ACCESS_MODE_DESCRIPTIONS = listOf(
    "Any user on this server can join",
    "Share an invite code to let people join",
    "Choose specific users from your server",
)

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun CreateWatchPartyContent(
    client: PlayarrClient,
    onDismiss: () -> Unit,
    selectedTab: WatchPartyTab,
    tabFocusRequesters: Map<WatchPartyTab, FocusRequester>,
    partyNameFocusRequester: FocusRequester,
) {
    val scope = rememberCoroutineScope()
    val partyName = remember { mutableStateOf("") }
    val selectedOption = remember { mutableStateOf(0) }
    val optFocusRequesters = remember { List(3) { FocusRequester() } }
    val isCreating = remember { mutableStateOf(false) }
    val createdRoom = remember { mutableStateOf<WatchRoom?>(null) }
    val error = remember { mutableStateOf<String?>(null) }

    // User selection state for "Select Users"
    val users = remember { mutableStateOf<List<PlexServerUser>>(emptyList()) }
    val usersLoading = remember { mutableStateOf(false) }
    val selectedUserIds = remember { mutableStateListOf<Long>() }
    val userFilter = remember { mutableStateOf("") }

    // Load users when "Select Users" is selected
    LaunchedEffect(selectedOption.value) {
        if (selectedOption.value == 2 && users.value.isEmpty() && !usersLoading.value) {
            usersLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    users.value = client.getPlexUsers()
                } catch (_: Exception) {
                    // silently fail — empty list shown
                }
                usersLoading.value = false
            }
        }
    }

    // If room was created with invite code, show the code screen
    val room = createdRoom.value
    if (room != null && room.inviteCode != null) {
        InviteCodeScreen(inviteCode = room.inviteCode!!, onDismiss = onDismiss)
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Party Name field
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PlayarrText(
                text = "Party Name",
                style = PlayarrTheme.typography.sm,
                color = PlayarrTheme.colors.foreground.copy(alpha = 0.6f),
            )
            TvTextField(
                value = partyName.value,
                onValueChange = { partyName.value = it },
                placeholder = "Movie Night",
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(partyNameFocusRequester)
                    .focusProperties {
                        up = tabFocusRequesters[selectedTab]!!
                        down = optFocusRequesters[selectedOption.value]
                    },
            )
            PlayarrText(
                text = "Optional - defaults to username's party",
                style = PlayarrTheme.typography.sm,
                color = PlayarrTheme.colors.primary.copy(alpha = 0.7f),
            )
        }

        // Who can join section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PlayarrText(
                text = "Who can join?",
                style = PlayarrTheme.typography.base.copy(fontWeight = FontWeight.SemiBold),
                color = PlayarrTheme.colors.foreground,
            )

            // Underline indicator for options
            val optParentCoords = remember { mutableStateOf<LayoutCoordinates?>(null) }
            val optItemCoords = remember { mutableStateMapOf<Int, LayoutCoordinates>() }
            val prevOptTarget = remember { mutableStateOf<Int?>(null) }

            val optParent = optParentCoords.value
            val optTarget = optItemCoords[selectedOption.value]
            val optVisible = optParent != null && optTarget != null
                    && optParent.isAttached && optTarget.isAttached

            val optTargetPos =
                if (optVisible) optParent!!.localPositionOf(optTarget!!, Offset.Zero) else Offset.Zero
            val optTargetW = if (optVisible) optTarget!!.size.width.toFloat() else 0f
            val optTargetH = if (optVisible) optTarget!!.size.height.toFloat() else 0f

            val isFirstOptPlacement = prevOptTarget.value == null && optVisible
            val optAnimSpec: AnimationSpec<Float> =
                if (isFirstOptPlacement) snap() else tween(300, easing = FastOutSlowInEasing)

            LaunchedEffect(selectedOption.value) {
                prevOptTarget.value = selectedOption.value
            }

            val oLineX by animateFloatAsState(optTargetPos.x, optAnimSpec, label = "oLineX")
            val oLineW by animateFloatAsState(optTargetW, optAnimSpec, label = "oLineW")
            val oLineY = optTargetPos.y + optTargetH

            Row(
                modifier = Modifier
                    .onGloballyPositioned { optParentCoords.value = it }
                    .drawBehind {
                        if (optVisible && oLineW > 0f) {
                            val lineHeight = 2.dp.toPx()
                            drawRoundRect(
                                color = Color(0xFF1CE783),
                                topLeft = Offset(oLineX, oLineY - lineHeight),
                                size = Size(oLineW, lineHeight),
                                cornerRadius = CornerRadius(lineHeight / 2f, lineHeight / 2f),
                            )
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ACCESS_MODE_LABELS.forEachIndexed { index, title ->
                    val isSelected = selectedOption.value == index
                    val optTextColor by animateColorAsState(
                        if (isSelected) PlayarrTheme.colors.foreground
                        else PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                        tween(300, easing = FastOutSlowInEasing),
                        label = "optColor_$index",
                    )

                    Button(
                        onClick = { selectedOption.value = index },
                        modifier = Modifier
                            .focusRequester(optFocusRequesters[index])
                            .focusProperties { up = partyNameFocusRequester }
                            .onGloballyPositioned { optItemCoords[index] = it }
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) selectedOption.value = index
                            },
                        colors = ButtonDefaults.colors(
                            containerColor = Color.Transparent,
                            contentColor = optTextColor,
                            focusedContainerColor = Color.Transparent,
                            focusedContentColor = optTextColor,
                        ),
                        shape = ButtonDefaults.shape(shape = RoundedCornerShape(6.dp)),
                    ) {
                        PlayarrText(
                            text = title,
                            style = PlayarrTheme.typography.base.copy(fontWeight = FontWeight.SemiBold),
                            color = optTextColor,
                        )
                    }
                }
            }

            // Description for selected option
            PlayarrText(
                text = ACCESS_MODE_DESCRIPTIONS[selectedOption.value],
                style = PlayarrTheme.typography.sm,
                color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
            )
        }

        // User picker when "Select Users" is selected
        if (selectedOption.value == 2) {
            val showUserPicker = remember { mutableStateOf(false) }

            // Selected user avatar pills
            if (selectedUserIds.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(
                        users.value.filter { selectedUserIds.contains(it.id) },
                        key = { it.id },
                    ) { user ->
                        Row(
                            modifier = Modifier
                                .background(
                                    PlayarrTheme.colors.content2,
                                    RoundedCornerShape(20.dp),
                                )
                                .border(
                                    1.dp,
                                    PlayarrTheme.colors.foreground.copy(alpha = 0.1f),
                                    RoundedCornerShape(20.dp),
                                )
                                .padding(start = 4.dp, top = 4.dp, bottom = 4.dp, end = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(PlayarrTheme.colors.content3, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                PlayarrText(
                                    text = (user.title.ifBlank { user.username })
                                        .take(1).uppercase(),
                                    style = PlayarrTheme.typography.sm.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = PlayarrTheme.colors.primary,
                                )
                            }
                            PlayarrText(
                                text = user.title.ifBlank { user.username },
                                style = PlayarrTheme.typography.sm,
                                color = PlayarrTheme.colors.foreground,
                            )
                        }
                    }
                }
            }

            // "Select Users" button
            Button(
                onClick = { showUserPicker.value = true },
                colors = ButtonDefaults.colors(
                    containerColor = PlayarrTheme.colors.content2,
                    contentColor = PlayarrTheme.colors.foreground,
                    focusedContainerColor = PlayarrTheme.colors.content3,
                    focusedContentColor = PlayarrTheme.colors.foreground,
                ),
                shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PlayarrTheme.colors.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    PlayarrText(
                        text = if (selectedUserIds.isEmpty()) "Select Users"
                        else "${selectedUserIds.size} user${if (selectedUserIds.size != 1) "s" else ""} selected",
                        style = PlayarrTheme.typography.base,
                        color = PlayarrTheme.colors.foreground,
                    )
                }
            }

            // User picker dialog
            if (showUserPicker.value) {
                UserPickerDialog(
                    users = users.value,
                    isLoading = usersLoading.value,
                    selectedUserIds = selectedUserIds,
                    filter = userFilter.value,
                    onFilterChange = { userFilter.value = it },
                    onToggleUser = { userId ->
                        if (selectedUserIds.contains(userId)) {
                            selectedUserIds.remove(userId)
                        } else {
                            selectedUserIds.add(userId)
                        }
                    },
                    onDismiss = { showUserPicker.value = false },
                )
            }
        }

        // Error message
        error.value?.let { msg ->
            PlayarrText(
                text = msg,
                style = PlayarrTheme.typography.sm,
                color = Color(0xFFFF5555),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Cancel button
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.colors(
                    containerColor = Color.Transparent,
                    contentColor = PlayarrTheme.colors.foreground,
                    focusedContainerColor = PlayarrTheme.colors.content3,
                    focusedContentColor = PlayarrTheme.colors.foreground,
                ),
                shape = ButtonDefaults.shape(shape = PlayarrTheme.shapes.button),
            ) {
                PlayarrText(
                    text = "Cancel",
                    style = PlayarrTheme.typography.base,
                    color = PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            val accessMode = ACCESS_MODES[selectedOption.value]
            val createEnabled = !isCreating.value &&
                    (accessMode != WatchPartyAccessMode.by_user || selectedUserIds.isNotEmpty())

            PlayarrButton(
                onClick = {
                    if (!createEnabled) return@PlayarrButton
                    isCreating.value = true
                    error.value = null
                    scope.launch {
                        try {
                            val result = withContext(Dispatchers.IO) {
                                client.createWatchParty(
                                    CreateWatchPartyRequest(
                                        name = partyName.value.trim().ifEmpty { null },
                                        accessMode = accessMode,
                                        allowedUserIds = if (accessMode == WatchPartyAccessMode.by_user)
                                            selectedUserIds.toList() else emptyList(),
                                    )
                                )
                            }
                            if (result.inviteCode != null) {
                                createdRoom.value = result
                            } else {
                                onDismiss()
                            }
                        } catch (e: Exception) {
                            error.value = e.message ?: "Failed to create party"
                        } finally {
                            isCreating.value = false
                        }
                    }
                },
                isPrimary = true,
                enabled = createEnabled,
            ) {
                PlayarrText(
                    text = if (isCreating.value) "Creating..." else "Create Party",
                    style = PlayarrTheme.typography.base.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun InviteCodeScreen(
    inviteCode: String,
    onDismiss: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        PlayarrText(
            text = "Watch Party Created!",
            style = PlayarrTheme.typography.lg.copy(fontWeight = FontWeight.Bold),
            color = PlayarrTheme.colors.foreground,
        )

        PlayarrText(
            text = "Share this invite code with friends to join your watch party:",
            style = PlayarrTheme.typography.base,
            color = PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )

        // Invite code display
        Box(
            modifier = Modifier
                .background(PlayarrTheme.colors.content2, RoundedCornerShape(8.dp))
                .border(1.dp, PlayarrTheme.colors.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 32.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            PlayarrText(
                text = inviteCode,
                style = PlayarrTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                color = PlayarrTheme.colors.primary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        PlayarrButton(
            onClick = onDismiss,
            isPrimary = true,
        ) {
            PlayarrText(
                text = "Done",
                style = PlayarrTheme.typography.base.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun UserPickerDialog(
    users: List<PlexServerUser>,
    isLoading: Boolean,
    selectedUserIds: List<Long>,
    filter: String,
    onFilterChange: (String) -> Unit,
    onToggleUser: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        PlayarrTheme.colors.content1,
                        RoundedCornerShape(12.dp),
                    )
                    .padding(24.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PlayarrTheme.colors.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        PlayarrText(
                            text = "Select Users",
                            style = PlayarrTheme.typography.title.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = PlayarrTheme.colors.foreground,
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.colors(
                            containerColor = Color.Transparent,
                            contentColor = PlayarrTheme.colors.foreground,
                            focusedContainerColor = PlayarrTheme.colors.content3,
                            focusedContentColor = PlayarrTheme.colors.foreground,
                        ),
                        shape = ButtonDefaults.shape(shape = CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                UserPickerSection(
                    users = users,
                    isLoading = isLoading,
                    selectedUserIds = selectedUserIds,
                    filter = filter,
                    onFilterChange = onFilterChange,
                    onToggleUser = onToggleUser,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Done button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    PlayarrButton(
                        onClick = onDismiss,
                        isPrimary = true,
                    ) {
                        PlayarrText(
                            text = "Done",
                            style = PlayarrTheme.typography.base.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun UserPickerSection(
    users: List<PlexServerUser>,
    isLoading: Boolean,
    selectedUserIds: List<Long>,
    filter: String,
    onFilterChange: (String) -> Unit,
    onToggleUser: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Filter field
        TvTextField(
            value = filter,
            onValueChange = onFilterChange,
            placeholder = "Filter users...",
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = PlayarrTheme.colors.foreground.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp),
                )
            },
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center,
            ) {
                PlayarrText(
                    text = "Loading users...",
                    style = PlayarrTheme.typography.base,
                    color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                )
            }
        } else {
            val filtered = users.filter { user ->
                filter.isBlank() ||
                        user.username.contains(filter, ignoreCase = true) ||
                        user.title.contains(filter, ignoreCase = true)
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    PlayarrText(
                        text = "No users found",
                        style = PlayarrTheme.typography.base,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.4f),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filtered, key = { it.id }) { user ->
                        val isSelected = selectedUserIds.contains(user.id)
                        UserRow(
                            user = user,
                            isSelected = isSelected,
                            onToggle = { onToggleUser(user.id) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun UserRow(
    user: PlexServerUser,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    Button(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = PlayarrTheme.colors.foreground,
            focusedContainerColor = PlayarrTheme.colors.content3,
            focusedContentColor = PlayarrTheme.colors.foreground,
        ),
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Checkbox indicator
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        if (isSelected) PlayarrTheme.colors.primary else Color.Transparent,
                        RoundedCornerShape(4.dp),
                    )
                    .border(
                        1.dp,
                        if (isSelected) PlayarrTheme.colors.primary
                        else PlayarrTheme.colors.foreground.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = PlayarrTheme.colors.primaryForeground,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(PlayarrTheme.colors.content3, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                PlayarrText(
                    text = (user.title.ifBlank { user.username }).take(1).uppercase(),
                    style = PlayarrTheme.typography.sm.copy(fontWeight = FontWeight.Bold),
                    color = PlayarrTheme.colors.primary,
                )
            }

            PlayarrText(
                text = user.title.ifBlank { user.username },
                style = PlayarrTheme.typography.base,
                color = PlayarrTheme.colors.foreground,
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun JoinWatchPartyContent(
    client: PlayarrClient,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val inviteCode = remember { mutableStateOf("") }
    val isJoining = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val rooms = remember { mutableStateOf<List<WatchRoom>>(emptyList()) }
    val roomsLoading = remember { mutableStateOf(true) }

    // Load active rooms on mount
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                rooms.value = client.listWatchPartyRooms()
            } catch (_: Exception) {
                // silently fail
            }
            roomsLoading.value = false
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Invite code input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvTextField(
                value = inviteCode.value,
                onValueChange = { inviteCode.value = it },
                placeholder = "Enter invite code...",
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PlayarrTheme.colors.foreground.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp),
                    )
                },
            )

            PlayarrButton(
                onClick = {
                    val code = inviteCode.value.trim()
                    if (code.isBlank() || isJoining.value) return@PlayarrButton
                    isJoining.value = true
                    error.value = null
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                client.joinByInviteCode(code)
                            }
                            onDismiss()
                        } catch (e: Exception) {
                            error.value = e.message ?: "Invalid or expired invite code"
                        } finally {
                            isJoining.value = false
                        }
                    }
                },
                isPrimary = true,
                enabled = inviteCode.value.isNotBlank() && !isJoining.value,
            ) {
                PlayarrText(
                    text = if (isJoining.value) "Joining..." else "Join",
                    style = PlayarrTheme.typography.base.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }

        // Error message
        error.value?.let { msg ->
            PlayarrText(
                text = msg,
                style = PlayarrTheme.typography.sm,
                color = Color(0xFFFF5555),
            )
        }

        // Active parties section
        if (roomsLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                PlayarrText(
                    text = "Loading parties...",
                    style = PlayarrTheme.typography.base,
                    color = PlayarrTheme.colors.foreground.copy(alpha = 0.4f),
                )
            }
        } else if (rooms.value.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                PlayarrText(
                    text = "No active parties found",
                    style = PlayarrTheme.typography.base,
                    color = PlayarrTheme.colors.foreground.copy(alpha = 0.4f),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(rooms.value, key = { it.id }) { room ->
                    RoomRow(room = room, onJoin = {
                        scope.launch {
                            try {
                                // Room is already visible to us, just dismiss to join
                                onDismiss()
                            } catch (_: Exception) {
                                // handled
                            }
                        }
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun RoomRow(
    room: WatchRoom,
    onJoin: () -> Unit,
) {
    Button(
        onClick = onJoin,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.colors(
            containerColor = PlayarrTheme.colors.content2,
            contentColor = PlayarrTheme.colors.foreground,
            focusedContainerColor = PlayarrTheme.colors.content3,
            focusedContentColor = PlayarrTheme.colors.foreground,
        ),
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                PlayarrText(
                    text = room.name ?: "${room.hostUsername}'s party",
                    style = PlayarrTheme.typography.base.copy(fontWeight = FontWeight.SemiBold),
                    color = PlayarrTheme.colors.foreground,
                )
                PlayarrText(
                    text = "${room.participants.size} watching",
                    style = PlayarrTheme.typography.sm,
                    color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                )
            }
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = PlayarrTheme.colors.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * A text field designed for TV D-pad navigation.
 * Renders as a single BasicTextField that starts in readOnly mode.
 * Press select (DpadCenter/Enter) to activate editing and show keyboard.
 * Press Done on keyboard or lose focus to return to readOnly mode.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TvTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val isEditing = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isFocused = remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        when {
            isEditing.value -> PlayarrTheme.colors.primary
            isFocused.value -> PlayarrTheme.colors.foreground.copy(alpha = 0.5f)
            else -> PlayarrTheme.colors.foreground.copy(alpha = 0.1f)
        },
        tween(200),
        label = "tvTextFieldBorder",
    )

    // Show keyboard when entering edit mode
    LaunchedEffect(isEditing.value) {
        if (isEditing.value) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = !isEditing.value,
        textStyle = PlayarrTheme.typography.base.copy(
            color = PlayarrTheme.colors.foreground,
        ),
        cursorBrush = SolidColor(
            if (isEditing.value) PlayarrTheme.colors.primary else Color.Transparent,
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                isEditing.value = false
                keyboardController?.hide()
            },
        ),
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                isFocused.value = focusState.hasFocus
                if (!focusState.hasFocus && isEditing.value) {
                    isEditing.value = false
                    keyboardController?.hide()
                }
            }
            .onPreviewKeyEvent { keyEvent ->
                if (!isEditing.value
                    && keyEvent.type == KeyEventType.KeyDown
                    && (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter)
                ) {
                    isEditing.value = true
                    true
                } else {
                    false
                }
            },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PlayarrTheme.colors.content2, RoundedCornerShape(8.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    leadingIcon?.invoke()
                    Box {
                        if (value.isEmpty()) {
                            PlayarrText(
                                text = placeholder,
                                style = PlayarrTheme.typography.base,
                                color = PlayarrTheme.colors.foreground.copy(alpha = 0.3f),
                            )
                        }
                        innerTextField()
                    }
                }
            }
        },
    )
}
