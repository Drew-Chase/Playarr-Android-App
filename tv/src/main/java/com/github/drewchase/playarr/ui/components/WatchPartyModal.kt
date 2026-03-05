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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import com.github.drewchase.playarr.ui.theme.PlayarrTheme

private enum class WatchPartyTab { CREATE, JOIN }

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun WatchPartyModal(
    onDismiss: () -> Unit,
) {
    val selectedTab = remember { mutableStateOf(WatchPartyTab.CREATE) }

    // Tab pill indicator state
    val tabParentCoords = remember { mutableStateOf<LayoutCoordinates?>(null) }
    val tabItemCoords = remember { mutableStateMapOf<WatchPartyTab, LayoutCoordinates>() }
    val focusedTab = remember { mutableStateOf<WatchPartyTab?>(null) }
    val prevTabTarget = remember { mutableStateOf<WatchPartyTab?>(null) }

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
                                style = PlayarrTheme.typography.lg.copy(fontWeight = FontWeight.SemiBold),
                                color = tabTextColor,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tab content
                when (selectedTab.value) {
                    WatchPartyTab.CREATE -> CreateWatchPartyContent(onDismiss = onDismiss)
                    WatchPartyTab.JOIN -> JoinWatchPartyContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun CreateWatchPartyContent(onDismiss: () -> Unit) {
    val partyName = remember { mutableStateOf("") }
    val selectedOption = remember { mutableStateOf(0) }
    val focusedOption = remember { mutableStateOf(-1) }

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
            BasicTextField(
                value = partyName.value,
                onValueChange = { partyName.value = it },
                textStyle = PlayarrTheme.typography.base.copy(
                    color = PlayarrTheme.colors.foreground,
                ),
                cursorBrush = SolidColor(PlayarrTheme.colors.primary),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                PlayarrTheme.colors.content2,
                                RoundedCornerShape(8.dp),
                            )
                            .border(
                                1.dp,
                                PlayarrTheme.colors.foreground.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp),
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        if (partyName.value.isEmpty()) {
                            PlayarrText(
                                text = "Movie Night",
                                style = PlayarrTheme.typography.base,
                                color = PlayarrTheme.colors.foreground.copy(alpha = 0.3f),
                            )
                        }
                        innerTextField()
                    }
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
                style = PlayarrTheme.typography.lg.copy(fontWeight = FontWeight.SemiBold),
                color = PlayarrTheme.colors.foreground,
            )

            val options = listOf(
                "Everyone" to "Any user on this server can join",
                "Invite Only" to "Share an invite code to let people join",
                "Select Users" to "Choose specific users from your server",
            )

            options.forEachIndexed { index, (title, description) ->
                val isSelected = selectedOption.value == index
                val isFocused = focusedOption.value == index

                Button(
                    onClick = { selectedOption.value = index },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            focusedOption.value = if (focusState.isFocused) index else focusedOption.value
                        },
                    colors = ButtonDefaults.colors(
                        containerColor = Color.Transparent,
                        contentColor = PlayarrTheme.colors.foreground,
                        focusedContainerColor = PlayarrTheme.colors.content2,
                        focusedContentColor = PlayarrTheme.colors.foreground,
                    ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp)),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // Radio circle
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(
                                    2.dp,
                                    if (isSelected) PlayarrTheme.colors.primary
                                    else PlayarrTheme.colors.foreground.copy(alpha = 0.3f),
                                    CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(PlayarrTheme.colors.primary, CircleShape),
                                )
                            }
                        }

                        Column {
                            PlayarrText(
                                text = title,
                                style = PlayarrTheme.typography.base.copy(fontWeight = FontWeight.SemiBold),
                                color = PlayarrTheme.colors.foreground,
                            )
                            PlayarrText(
                                text = description,
                                style = PlayarrTheme.typography.sm,
                                color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
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

            // Create Party button
            PlayarrButton(
                onClick = { /* TODO: Create party */ },
                isPrimary = true,
            ) {
                PlayarrText(
                    text = "Create Party",
                    style = PlayarrTheme.typography.base.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun JoinWatchPartyContent() {
    val inviteCode = remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Invite code input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = inviteCode.value,
                onValueChange = { inviteCode.value = it },
                textStyle = PlayarrTheme.typography.base.copy(
                    color = PlayarrTheme.colors.foreground,
                ),
                cursorBrush = SolidColor(PlayarrTheme.colors.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                PlayarrTheme.colors.content2,
                                RoundedCornerShape(8.dp),
                            )
                            .border(
                                1.dp,
                                PlayarrTheme.colors.foreground.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp),
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PlayarrTheme.colors.foreground.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp),
                            )
                            Box {
                                if (inviteCode.value.isEmpty()) {
                                    PlayarrText(
                                        text = "Enter invite code...",
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

            PlayarrButton(
                onClick = { /* TODO: Join party */ },
                isPrimary = true,
            ) {
                PlayarrText(
                    text = "Join",
                    style = PlayarrTheme.typography.base.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }

        // Active parties section (empty state)
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
    }
}
