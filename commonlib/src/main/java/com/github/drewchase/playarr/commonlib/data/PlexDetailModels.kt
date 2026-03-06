package com.github.drewchase.playarr.commonlib.data

import com.google.gson.annotations.SerializedName

/** Cast/crew member with role and optional headshot. */
data class PlexRole(
    @SerializedName("tag") val tag: String,
    @SerializedName("role") val role: String? = null,
    @SerializedName("thumb") val thumb: String? = null,
)

/** Generic tagged item used for Director, Writer, Genre. */
data class PlexTag(
    @SerializedName("tag") val tag: String,
)

/** Technical media information (codecs, resolution, etc.). */
data class PlexMedia(
    @SerializedName("videoCodec") val videoCodec: String? = null,
    @SerializedName("audioCodec") val audioCodec: String? = null,
    @SerializedName("height") val height: Int? = null,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("bitrate") val bitrate: Int? = null,
    @SerializedName("audioChannels") val audioChannels: Int? = null,
    @SerializedName("Part") val parts: List<PlexMediaPart>? = null,
)

data class PlexMediaPart(
    @SerializedName("key") val key: String? = null,
    @SerializedName("duration") val duration: Long? = null,
    @SerializedName("file") val file: String? = null,
    @SerializedName("size") val size: Long? = null,
    @SerializedName("Stream") val streams: List<PlexMediaStream>? = null,
)

data class PlexMediaStream(
    @SerializedName("streamType") val streamType: Int? = null, // 1=video, 2=audio, 3=subtitle
    @SerializedName("codec") val codec: String? = null,
    @SerializedName("displayTitle") val displayTitle: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("channels") val channels: Int? = null,
)
