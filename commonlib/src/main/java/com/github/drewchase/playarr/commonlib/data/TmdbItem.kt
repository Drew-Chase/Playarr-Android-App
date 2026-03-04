package com.github.drewchase.playarr.commonlib.data

data class TmdbItem(
    val id: Int,
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val releaseDate: String? = null,
    val firstAirDate: String? = null,
    val voteAverage: Double? = null,
    val mediaType: String? = null,
) {
    fun displayTitle(): String = title ?: name ?: "Unknown"

    fun displayYear(): String? {
        val date = releaseDate ?: firstAirDate ?: return null
        return if (date.length >= 4) date.take(4) else null
    }
}
