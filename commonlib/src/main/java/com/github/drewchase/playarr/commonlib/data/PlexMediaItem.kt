package com.github.drewchase.playarr.commonlib.data

import com.google.gson.annotations.SerializedName

data class PlexMediaItem(
    @SerializedName("ratingKey") val ratingKey: String,
    @SerializedName("key") val key: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String = "",
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("thumb") val thumb: String? = null,
    @SerializedName("art") val art: String? = null,
    @SerializedName("duration") val duration: Long? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("audienceRating") val audienceRating: Double? = null,
    @SerializedName("contentRating") val contentRating: String? = null,
    @SerializedName("studio") val studio: String? = null,
    @SerializedName("tagline") val tagline: String? = null,
    @SerializedName("viewOffset") val viewOffset: Long? = null,
    @SerializedName("viewCount") val viewCount: Int? = null,
    @SerializedName("addedAt") val addedAt: Long? = null,
    @SerializedName("originallyAvailableAt") val originallyAvailableAt: String? = null,
    @SerializedName("parentTitle") val parentTitle: String? = null,
    @SerializedName("grandparentTitle") val grandparentTitle: String? = null,
    @SerializedName("parentIndex") val parentIndex: Int? = null,
    @SerializedName("index") val index: Int? = null,
    @SerializedName("grandparentRatingKey") val grandparentRatingKey: String? = null,
    @SerializedName("parentRatingKey") val parentRatingKey: String? = null,
    @SerializedName("leafCount") val leafCount: Int? = null,
    @SerializedName("childCount") val childCount: Int? = null,
    @SerializedName("Guid") val guid: List<GuidEntry>? = null,
) {
    data class GuidEntry(val id: String)

    fun progressFraction(): Float? {
        val offset = viewOffset ?: return null
        val dur = duration ?: return null
        if (dur <= 0) return null
        return (offset.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
    }

    fun formattedDuration(): String? {
        val dur = duration ?: return null
        val totalMinutes = dur / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }

    fun episodeLabel(): String? {
        if (type != "episode") return null
        val s = parentIndex ?: return null
        val e = index ?: return null
        return "S${s.toString().padStart(2, '0')} E${e.toString().padStart(2, '0')}"
    }
}
