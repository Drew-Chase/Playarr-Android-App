package com.github.drewchase.playarr.commonlib.data

data class PlexLibrary(
    val key: String,
    val title: String,
    val type: String = "",
    val thumb: String? = null,
    val art: String? = null,
)
