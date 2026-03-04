package com.github.drewchase.playarr.commonlib.data

import com.google.gson.annotations.SerializedName

data class SearchHub(
    @SerializedName("hubIdentifier") val hubIdentifier: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("size") val size: Int? = null,
    @SerializedName("Metadata") val metadata: List<PlexMediaItem>? = null,
)
