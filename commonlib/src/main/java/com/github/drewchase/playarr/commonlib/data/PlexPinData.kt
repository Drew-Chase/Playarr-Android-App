package com.github.drewchase.playarr.commonlib.data

data class PlexPinData(
    val id: Long,
    val code: String,
    val authToken: String?,
    val expiresAt: String?,
)
