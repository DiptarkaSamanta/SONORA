package com.sonora.music.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sonora.music.domain.model.Song
import com.sonora.music.ui.components.GlassmorphicCard
import com.sonora.music.ui.components.SongItem
import com.sonora.music.ui.components.SonoraTopBar
import com.sonora.music.ui.theme.SonoraCardBgElevated
import com.sonora.music.ui.theme.SonoraCyan
import com.sonora.music.ui.theme.SonoraElectricBlue
import com.sonora.music.ui.theme.SonoraMagenta
import com.sonora.music.ui.theme.SonoraPrimaryGradient
import com.sonora.music.ui.theme.SonoraPurple
import com.sonora.music.ui.theme.SonoraTextMuted
import com.sonora.music.ui.theme.SonoraTextPrimary
import com.sonora.music.ui.theme.SonoraTextSecondary
import com.sonora.music.ui.theme.SonoraWarning

@Composable
fun HomeScreen(
    onNavigateToStorage: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    currentPlayingSongId: String?,
    viewModel: HomeViewModel = viewModel()
) {
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val mostPlayed by viewModel.mostPlayed.collectAsState()
    val storageMode by viewModel.storageMode.collectAsState()
    val trendingOnline by viewModel.trendingOnline.collectAsState()
    val showXiaomiOpt by viewModel.isXiaomiNeedsOptimization.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            SonoraTopBar(
                storageMode = storageMode,
                onStorageClick = onNavigateToStorage,
                onEqualizerClick = onNavigateToEqualizer
            )
        }

        // Xiaomi HyperOS / MIUI Optimization Card
        if (showXiaomiOpt) {
            item {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = SonoraPurple.copy(alpha = 0.15f),
                    borderColor = SonoraElectricBlue.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = null,
                                tint = SonoraElectricBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Redmi 12 5G Optimization",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SonoraElectricBlue
                            )
                        }

                        Text(
                            text = "To keep uninterrupted background music while gaming or screen locked, set Battery Saver to 'No Restrictions' and allow Autostart.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SonoraTextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.triggerXiaomiBatteryFix() },
                                colors = ButtonDefaults.buttonColors(containerColor = SonoraElectricBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("No Restrictions", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { viewModel.triggerXiaomiAutostart() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Autostart", color = SonoraTextPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Recently Played Carousel
        if (recentlyPlayed.isNotEmpty()) {
            item {
                SectionHeader(title = "Recently Played")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(recentlyPlayed) { index, song ->
                        RecentSongCard(
                            song = song,
                            isPlaying = song.id == currentPlayingSongId,
                            onClick = { viewModel.playQueue(recentlyPlayed, index) }
                        )
                    }
                }
            }
        }

        // Most Played Section
        if (mostPlayed.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "Most Played")
            }
            itemsIndexed(mostPlayed.take(5)) { index, song ->
                SongItem(
                    song = song,
                    isPlaying = song.id == currentPlayingSongId,
                    onClick = { viewModel.playQueue(mostPlayed, index) },
                    onFavoriteClick = { viewModel.toggleFavorite(song) }
                )
            }
        }

        // Favorites Section
        if (favoriteSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "Your Favorites")
            }
            itemsIndexed(favoriteSongs.take(5)) { index, song ->
                SongItem(
                    song = song,
                    isPlaying = song.id == currentPlayingSongId,
                    onClick = { viewModel.playQueue(favoriteSongs, index) },
                    onFavoriteClick = { viewModel.toggleFavorite(song) }
                )
            }
        }

        // Trending Online Discovery (Legal Royalty-Free CC Music)
        if (trendingOnline.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Trending Legal Discovery",
                    subtitle = "Creative Commons & Royalty Free"
                )
            }
            itemsIndexed(trendingOnline) { index, song ->
                SongItem(
                    song = song,
                    isPlaying = song.id == currentPlayingSongId,
                    onClick = { viewModel.playQueue(trendingOnline, index) },
                    onFavoriteClick = { viewModel.toggleFavorite(song) }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = SonoraTextPrimary
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = SonoraTextMuted
            )
        }
    }
}

@Composable
fun RecentSongCard(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        backgroundColor = SonoraCardBgElevated,
        borderColor = if (isPlaying) SonoraElectricBlue else SonoraPurple.copy(alpha = 0.25f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SonoraPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (!song.artworkUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = song.artworkUri,
                        contentDescription = song.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = SonoraPurple,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .background(SonoraPrimaryGradient, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPlaying) SonoraElectricBlue else SonoraTextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = SonoraTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

