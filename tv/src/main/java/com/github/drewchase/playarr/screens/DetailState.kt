package com.github.drewchase.playarr.screens

import com.github.drewchase.playarr.commonlib.PlayarrClient
import com.github.drewchase.playarr.commonlib.data.PlexMediaItem

data class DetailState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val item: PlexMediaItem? = null,
    val seasons: List<PlexMediaItem> = emptyList(),
    val episodes: List<PlexMediaItem> = emptyList(),
    val related: List<PlexMediaItem> = emptyList(),
    val onDeck: PlexMediaItem? = null,
)

/**
 * Loads all detail data from the PlayarrClient.
 * Called from a coroutine on Dispatchers.IO.
 * Each API call is independent; failures in one don't block others.
 */
fun loadDetailData(client: PlayarrClient, ratingKey: String, mediaType: String): DetailState {
    var item: PlexMediaItem? = null
    var seasons: List<PlexMediaItem> = emptyList()
    var related: List<PlexMediaItem> = emptyList()
    var onDeck: PlexMediaItem? = null
    var error: String? = null

    try { item = client.getMediaDetail(ratingKey) } catch (_: Exception) {}

    if (mediaType == "show") {
        try { seasons = client.getMediaChildren(ratingKey) } catch (_: Exception) {}
        try { onDeck = client.getOnDeckForMedia(ratingKey) } catch (_: Exception) {}
    }

    if (mediaType == "season") {
        try {
            val episodes = client.getMediaChildren(ratingKey)
            return DetailState(
                isLoading = false,
                item = item,
                episodes = episodes,
                related = related,
            )
        } catch (_: Exception) {}
    }

    try { related = client.getRelatedMedia(ratingKey) } catch (_: Exception) {}

    if (item == null) {
        error = "Unable to load media details."
    }

    return DetailState(
        isLoading = false,
        error = error,
        item = item,
        seasons = seasons,
        related = related,
        onDeck = onDeck,
    )
}

/**
 * Loads episodes for a given season.
 * Called when user selects a season in the detail screen.
 */
fun loadSeasonEpisodes(client: PlayarrClient, seasonRatingKey: String): List<PlexMediaItem> {
    return try {
        client.getMediaChildren(seasonRatingKey)
    } catch (_: Exception) {
        emptyList()
    }
}
