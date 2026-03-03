package com.github.drewchase.playarr.commonlib.data

data class PlayarrServerInformationData(
    val version: String,
    val setupComplete: Boolean,
    val uptimeMs: Long,
    val startTime: Long,
    val debug: Boolean,
    val os: String,
    val arch: String,
    val activeWatchParties: Int,
    val configPath: String,
    val services: Services
) {
    data class Services(
        val plex: ServiceInfo,
        val sonarr: ServiceInfo,
        val radarr: ServiceInfo,
        val downloadClients: List<DownloadClient>,
        val opensubtitles: OpenSubtitlesInfo
    )

    data class ServiceInfo(
        val configured: Boolean,
        val reachable: Boolean,
        val version: String,
        val lastChecked: Long
    )

    data class DownloadClient(
        val name: String,
        val clientType: String,
        val enabled: Boolean,
        val reachable: Boolean,
        val lastChecked: Long
    )

    data class OpenSubtitlesInfo(
        val configured: Boolean
    )
}
