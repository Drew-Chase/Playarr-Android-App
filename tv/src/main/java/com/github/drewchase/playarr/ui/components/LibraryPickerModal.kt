package com.github.drewchase.playarr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.github.drewchase.playarr.commonlib.data.PlexLibrary
import com.github.drewchase.playarr.ui.theme.PlayarrTheme

/**
 * Modal dialog for selecting a library.
 * Replaces dropdown menus per the TV 7-foot rule.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LibraryPickerModal(
    title: String,
    libraries: List<PlexLibrary>,
    onDismiss: () -> Unit,
    onSelect: (PlexLibrary) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlayarrText(
                text = title,
                style = PlayarrTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                color = PlayarrTheme.colors.foreground,
            )

            libraries.forEach { library ->
                PlayarrButton(
                    onClick = { onSelect(library) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    PlayarrText(
                        text = library.title,
                        style = PlayarrTheme.typography.lg,
                    )
                }
            }

            PlayarrButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                PlayarrText(
                    text = "Cancel",
                    style = PlayarrTheme.typography.base,
                    color = PlayarrTheme.colors.foreground.copy(alpha = 0.6f),
                )
            }
        }
    }
}
