package com.github.drewchase.playarr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.github.drewchase.playarr.commonlib.PlayarrClient
import com.github.drewchase.playarr.commonlib.data.PlexMediaItem
import com.github.drewchase.playarr.commonlib.data.PlexRole
import com.github.drewchase.playarr.commonlib.data.TmdbItem
import com.github.drewchase.playarr.ui.theme.PlayarrTheme

/**
 * Landscape card for Continue Watching / On Deck.
 * Wide card with backdrop art, gradient text overlay, progress bar, and episode info.
 * Matches the web version's landscape card style.
 */
@Composable
fun LandscapeMediaCard(
    item: PlexMediaItem,
    client: PlayarrClient,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
) {
    // Use art (backdrop) for landscape cards like the web version
    val imageUrl = (item.art ?: item.thumb)?.let { client.getImageUrl(it, width = 800, height = 450) }
    val bgColor = PlayarrTheme.colors.background

    PlayarrCard(onClick = onClick, width = 400.dp, height = 225.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Gradient overlay from bottom (like the web version)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    bgColor.copy(alpha = 0.5f),
                                    bgColor.copy(alpha = 0.95f),
                                ),
                                startY = size.height * 0.3f,
                                endY = size.height,
                            )
                        )
                    },
            )

            // Text content at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(PlayarrTheme.spacing.xl),
            ) {
                PlayarrText(
                    text = when (item.type) {
                        "episode" -> item.grandparentTitle ?: item.title
                        else -> item.title
                    },
                    style = PlayarrTheme.typography.lg,
                    color = PlayarrTheme.colors.foreground,
                )
                val subtitle = item.episodeLabel()?.let { ep ->
                    "$ep ${item.title}"
                }
                if (subtitle != null) {
                    PlayarrText(
                        text = subtitle,
                        style = PlayarrTheme.typography.sm,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
                    )
                }
                val progress = item.progressFraction()
                if (progress != null && progress > 0f) {
                    Spacer(modifier = Modifier.height(6.dp))
                    ProgressBar(progress = progress)
                }
            }
        }
    }
}

/**
 * Portrait card for Recently Added Movies/Shows.
 * Full-bleed poster with gradient title overlay at bottom.
 * Matches the web version's portrait card style.
 */
@Composable
fun PortraitMediaCard(
    item: PlexMediaItem,
    client: PlayarrClient,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
) {
    val thumbUrl = item.thumb?.let { client.getImageUrl(it, width = 400, height = 600) }
    val bgColor = PlayarrTheme.colors.background

    PlayarrCard(onClick = onClick, width = 200.dp, height = 300.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (thumbUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thumbUrl)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PlayarrTheme.colors.content2),
                    contentAlignment = Alignment.Center,
                ) {
                    PlayarrText(
                        text = item.title.take(1),
                        style = PlayarrTheme.typography.xxl,
                        color = PlayarrTheme.colors.foreground,
                    )
                }
            }

            // Bottom gradient overlay with title
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    bgColor.copy(alpha = 0.9f),
                                ),
                                startY = 0f,
                                endY = size.height,
                            )
                        )
                    }
                    .padding(PlayarrTheme.spacing.lg),
            ) {
                PlayarrText(
                    text = item.title,
                    style = PlayarrTheme.typography.base,
                    color = PlayarrTheme.colors.foreground,
                )
                val yearText = item.year?.toString()
                if (yearText != null) {
                    PlayarrText(
                        text = yearText,
                        style = PlayarrTheme.typography.sm,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

/**
 * Discover card for Trending / Upcoming TMDB items.
 * Full-bleed poster with vote badge and gradient title overlay.
 * Matches the web version's discover card style.
 */
@Composable
fun DiscoverCard(
    item: TmdbItem,
    onClick: () -> Unit,
) {
    val posterUrl = item.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }
    val bgColor = PlayarrTheme.colors.background

    PlayarrCard(onClick = onClick, width = 200.dp, height = 300.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (posterUrl != null) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = item.displayTitle(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PlayarrTheme.colors.content2),
                    contentAlignment = Alignment.Center,
                ) {
                    PlayarrText(
                        text = item.displayTitle().take(1),
                        style = PlayarrTheme.typography.xxl,
                        color = PlayarrTheme.colors.foreground,
                    )
                }
            }

            // Vote badge (top-right)
            val vote = item.voteAverage
            if (vote != null && vote > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(PlayarrTheme.spacing.md)
                        .background(
                            color = PlayarrTheme.colors.primary.copy(alpha = 0.9f),
                            shape = PlayarrTheme.shapes.small,
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    PlayarrText(
                        text = String.format("%.1f", vote),
                        style = PlayarrTheme.typography.sm,
                        color = PlayarrTheme.colors.primaryForeground,
                    )
                }
            }

            // Bottom gradient overlay with title + year
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    bgColor.copy(alpha = 0.9f),
                                ),
                                startY = 0f,
                                endY = size.height,
                            )
                        )
                    }
                    .padding(PlayarrTheme.spacing.lg),
            ) {
                PlayarrText(
                    text = item.displayTitle(),
                    style = PlayarrTheme.typography.base,
                    color = PlayarrTheme.colors.foreground,
                )
                val year = item.displayYear()
                if (year != null) {
                    PlayarrText(
                        text = year,
                        style = PlayarrTheme.typography.sm,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

/**
 * Cast/crew card with circular headshot, actor name, and character name.
 * Used in the detail screen's Cast & Crew horizontal row.
 */
@Composable
fun CastCard(
    role: PlexRole,
    client: PlayarrClient,
    imageLoader: ImageLoader,
) {
    PlayarrCard(onClick = {}, width = 120.dp, height = 160.dp) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PlayarrTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val thumbUrl = role.thumb?.let { client.getImageUrl(it, width = 240, height = 240) }
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PlayarrTheme.colors.content2),
                contentAlignment = Alignment.Center,
            ) {
                if (thumbUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(thumbUrl)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = role.tag,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    PlayarrText(
                        text = role.tag.take(1).uppercase(),
                        style = PlayarrTheme.typography.xl,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(PlayarrTheme.spacing.sm))
            PlayarrText(
                text = role.tag,
                style = PlayarrTheme.typography.xs,
                color = PlayarrTheme.colors.foreground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val roleText = role.role
            if (roleText != null) {
                PlayarrText(
                    text = roleText,
                    style = PlayarrTheme.typography.xs,
                    color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Wide episode card with landscape thumbnail on left and metadata on right.
 * Used in the detail screen's episode list for a selected season.
 */
@Composable
fun EpisodeCard(
    item: PlexMediaItem,
    client: PlayarrClient,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
) {
    val thumbUrl = item.thumb?.let { client.getImageUrl(it, width = 480, height = 270) }
    val bgColor = PlayarrTheme.colors.background

    PlayarrCard(onClick = onClick, width = 700.dp, height = 130.dp) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Episode thumbnail
            Box(
                modifier = Modifier
                    .width(230.dp)
                    .fillMaxHeight()
                    .background(PlayarrTheme.colors.content2),
            ) {
                if (thumbUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(thumbUrl)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                // Episode number badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(PlayarrTheme.spacing.md)
                        .background(
                            color = PlayarrTheme.colors.primary.copy(alpha = 0.9f),
                            shape = PlayarrTheme.shapes.small,
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    PlayarrText(
                        text = "E${item.index?.toString()?.padStart(2, '0') ?: "?"}",
                        style = PlayarrTheme.typography.xs,
                        color = PlayarrTheme.colors.primaryForeground,
                    )
                }
                // Duration badge
                val duration = item.formattedDuration()
                if (duration != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(PlayarrTheme.spacing.md)
                            .background(
                                color = bgColor.copy(alpha = 0.8f),
                                shape = PlayarrTheme.shapes.small,
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        PlayarrText(
                            text = duration,
                            style = PlayarrTheme.typography.xs,
                            color = PlayarrTheme.colors.foreground.copy(alpha = 0.8f),
                        )
                    }
                }
                // Progress bar at bottom of thumbnail
                val progress = item.progressFraction()
                if (progress != null && progress > 0f) {
                    Box(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()) {
                        ProgressBar(progress = progress)
                    }
                }
            }

            // Episode info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(PlayarrTheme.spacing.xl),
            ) {
                PlayarrText(
                    text = item.title,
                    style = PlayarrTheme.typography.lg,
                    color = PlayarrTheme.colors.foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val summaryText = item.summary
                if (summaryText != null) {
                    Spacer(modifier = Modifier.height(PlayarrTheme.spacing.sm))
                    PlayarrText(
                        text = summaryText,
                        style = PlayarrTheme.typography.sm,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Small pill/badge for displaying metadata like resolution, codec, etc.
 */
@Composable
fun MetadataBadge(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = PlayarrTheme.colors.content2,
                shape = PlayarrTheme.shapes.small,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        PlayarrText(
            text = text,
            style = PlayarrTheme.typography.xs,
            color = PlayarrTheme.colors.foreground.copy(alpha = 0.7f),
        )
    }
}
