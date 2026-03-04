package com.github.drewchase.playarr.screens

import com.github.drewchase.playarr.commonlib.PlayarrClient
import com.github.drewchase.playarr.commonlib.data.DiscoverResults
import com.github.drewchase.playarr.commonlib.data.PlexLibrary
import com.github.drewchase.playarr.commonlib.data.PlexMediaItem
import com.github.drewchase.playarr.commonlib.data.PlexUser

data class DashboardState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val user: PlexUser? = null,
    val continueWatching: List<PlexMediaItem> = emptyList(),
    val onDeck: List<PlexMediaItem> = emptyList(),
    val recentlyAdded: List<PlexMediaItem> = emptyList(),
    val libraries: List<PlexLibrary> = emptyList(),
    val trending: DiscoverResults? = null,
    val upcoming: DiscoverResults? = null,
) {
    /** Combined hero items: up to 5 from continue-watching, on-deck, then recently-added, deduplicated. */
    fun heroItems(): List<PlexMediaItem> {
        val combined = mutableListOf<PlexMediaItem>()
        combined.addAll(continueWatching)
        combined.addAll(onDeck)
        combined.addAll(recentlyAdded)
        return combined.distinctBy { it.ratingKey }.take(5)
    }

    /** Recently added items filtered to movies. */
    fun recentMovies(): List<PlexMediaItem> =
        recentlyAdded.filter { it.type == "movie" }

    /** Recently added items filtered to TV shows/seasons/episodes. */
    fun recentShows(): List<PlexMediaItem> =
        recentlyAdded.filter { it.type != "movie" }

    /** Merged continue watching + on deck, deduplicated. */
    fun watchingItems(): List<PlexMediaItem> {
        val combined = mutableListOf<PlexMediaItem>()
        combined.addAll(continueWatching)
        combined.addAll(onDeck)
        return combined.distinctBy { it.ratingKey }
    }
}

/**
 * Loads all dashboard data from the PlayarrClient.
 * Called from a coroutine on Dispatchers.IO.
 * Each API call is independent; failures in one don't block others.
 */
fun loadDashboardData(client: PlayarrClient): DashboardState {
    var user: PlexUser? = null
    var continueWatching: List<PlexMediaItem> = emptyList()
    var onDeck: List<PlexMediaItem> = emptyList()
    var recentlyAdded: List<PlexMediaItem> = emptyList()
    var libraries: List<PlexLibrary> = emptyList()
    var trending: DiscoverResults? = null
    var upcoming: DiscoverResults? = null
    var error: String? = null

    try { user = client.getUser() } catch (_: Exception) {}
    try { continueWatching = client.getContinueWatching() } catch (_: Exception) {}
    try { onDeck = client.getOnDeck() } catch (_: Exception) {}
    try { recentlyAdded = client.getRecentlyAdded() } catch (_: Exception) {}
    try { libraries = client.getLibraries() } catch (_: Exception) {}
    try { trending = client.getTrending() } catch (_: Exception) {}
    try { upcoming = client.getUpcoming() } catch (_: Exception) {}

    if (continueWatching.isEmpty() && onDeck.isEmpty() && recentlyAdded.isEmpty() && libraries.isEmpty()) {
        error = "Unable to load content from the server."
    }

    return DashboardState(
        isLoading = false,
        error = error,
        user = user,
        continueWatching = continueWatching,
        onDeck = onDeck,
        recentlyAdded = recentlyAdded,
        libraries = libraries,
        trending = trending,
        upcoming = upcoming,
    )
}
