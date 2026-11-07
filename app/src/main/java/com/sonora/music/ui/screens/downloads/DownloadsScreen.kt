package com.sonora.music.ui.screens.downloads

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonora.music.core.database.entity.DownloadEntity
import com.sonora.music.core.database.entity.DownloadStatus
import com.sonora.music.ui.components.GlassmorphicCard
import com.sonora.music.ui.components.SongItem
import com.sonora.music.ui.screens.home.SectionHeader
import com.sonora.music.ui.theme.SonoraCardBg
import com.sonora.music.ui.theme.SonoraElectricBlue
import com.sonora.music.ui.theme.SonoraPurple
import com.sonora.music.ui.theme.SonoraTextMuted
import com.sonora.music.ui.theme.SonoraTextPrimary
import com.sonora.music.ui.theme.SonoraTextSecondary

@Composable
fun DownloadsScreen(
    currentPlayingSongId: String?,
    viewModel: DownloadsViewModel = viewModel()
) {
    val activeDownloads by viewModel.activeDownloads.collectAsState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val isWifiOnly by viewModel.isWifiOnly.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        item {
            // Header
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Downloads",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = SonoraTextPrimary
                )
                Text(
                    text = "Manage offline legal music and download tasks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SonoraTextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Wi-Fi only switch card
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                backgroundColor = SonoraCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Wifi,
                            contentDescription = null,
                            tint = SonoraElectricBlue
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Download on Wi-Fi Only",
                                style = MaterialTheme.typography.titleMedium,
                                color = SonoraTextPrimary
                            )
                            Text(
                                text = "Conserves mobile data plan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SonoraTextMuted
                            )
                        }
                    }
                    Switch(
                        checked = isWifiOnly,
                        onCheckedChange = { viewModel.toggleWifiOnly(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SonoraElectricBlue,
                            checkedTrackColor = SonoraPurple
                        )
                    )
                }
            }
        }

        // Active downloads
        if (activeDownloads.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(title = "In Progress (${activeDownloads.size})")
            }
            items(activeDownloads) { task ->
                ActiveDownloadItem(
                    task = task,
                    onPause = { viewModel.pauseDownload(task.songId) },
                    onResume = { viewModel.resumeDownload(task.songId) },
                    onCancel = { viewModel.cancelDownload(task.songId) }
                )
            }
        }

        // Downloaded offline songs
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(
                title = "Downloaded Music (${downloadedSongs.size})",
                subtitle = "Available completely offline without internet"
            )
        }

        if (downloadedSongs.isEmpty() && activeDownloads.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.CloudDownload,
                            contentDescription = null,
                            tint = SonoraPurple.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No offline downloads yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = SonoraTextSecondary
                        )
                        Text(
                            text = "Search online music to download Creative Commons tracks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SonoraTextMuted
                        )
                    }
                }
            }
        } else {
            items(downloadedSongs) { song ->
                SongItem(
                    song = song,
                    isPlaying = song.id == currentPlayingSongId,
                    onClick = { viewModel.playSong(song) }
                )
            }
        }
    }
}

@Composable
fun ActiveDownloadItem(
    task: DownloadEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = SonoraCardBg
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${task.artist} • ${task.storageMode}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SonoraTextSecondary
                    )
                }

                if (task.status == DownloadStatus.DOWNLOADING) {
                    IconButton(onClick = onPause) {
                        Icon(Icons.Rounded.Pause, contentDescription = "Pause", tint = SonoraElectricBlue)
                    }
                } else if (task.status == DownloadStatus.PAUSED) {
                    IconButton(onClick = onResume) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Resume", tint = SonoraElectricBlue)
                    }
                }

                IconButton(onClick = onCancel) {
                    Icon(Icons.Rounded.Close, contentDescription = "Cancel", tint = SonoraTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { task.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = SonoraElectricBlue,
                trackColor = SonoraPurple.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(task.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = SonoraElectricBlue
                )
                Text(
                    text = "${(task.downloadedBytes / (1024 * 1024))} MB / ${(task.totalBytes / (1024 * 1024))} MB",
                    style = MaterialTheme.typography.labelSmall,
                    color = SonoraTextMuted
                )
            }
        }
    }
}

