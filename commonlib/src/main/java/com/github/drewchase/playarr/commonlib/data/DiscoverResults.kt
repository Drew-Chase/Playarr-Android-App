package com.github.drewchase.playarr.commonlib.data

data class DiscoverResults(
    val movies: List<TmdbItem> = emptyList(),
    val tv: List<TmdbItem>? = null,
)
