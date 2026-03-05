package com.github.drewchase.playarr.commonlib.data

enum class WatchPartyAccessMode {
    everyone,
    invite_only,
    by_user,
}

enum class WatchPartyStatus {
    idle,
    watching,
    paused,
    buffering,
}

data class WatchPartyParticipant(
    val userId: Long = 0,
    val username: String = "",
    val thumb: String = "",
    val joinedAt: String = "",
)

data class WatchRoom(
    val id: String = "",
    val name: String? = null,
    val hostUserId: Long = 0,
    val hostUsername: String = "",
    val mediaId: String = "",
    val mediaTitle: String? = null,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val status: WatchPartyStatus = WatchPartyStatus.idle,
    val accessMode: WatchPartyAccessMode = WatchPartyAccessMode.everyone,
    val inviteCode: String? = null,
    val allowedUserIds: List<Long> = emptyList(),
    val participants: List<WatchPartyParticipant> = emptyList(),
    val episodeQueue: List<String> = emptyList(),
    val createdAt: String = "",
)

data class CreateWatchPartyRequest(
    val name: String? = null,
    val accessMode: WatchPartyAccessMode = WatchPartyAccessMode.everyone,
    val allowedUserIds: List<Long> = emptyList(),
)
