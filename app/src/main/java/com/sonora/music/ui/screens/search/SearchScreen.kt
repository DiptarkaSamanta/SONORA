package com.sonora.music.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonora.music.ui.components.GlassmorphicCard
import com.sonora.music.ui.components.SongItem
import com.sonora.music.ui.screens.home.SectionHeader
import com.sonora.music.ui.theme.SonoraCardBg
import com.sonora.music.ui.theme.SonoraCharcoal
import com.sonora.music.ui.theme.SonoraElectricBlue
import com.sonora.music.ui.theme.SonoraPurple
import com.sonora.music.ui.theme.SonoraTextMuted
import com.sonora.music.ui.theme.SonoraTextPrimary
import com.sonora.music.ui.theme.SonoraTextSecondary

@Composable
fun SearchScreen(
    currentPlayingSongId: String?,
    viewModel: SearchViewModel = viewModel()
) {
    val query by viewModel.query.collectAsState()
    val filter by viewModel.selectedFilter.collectAsState()
    val localResults by viewModel.localResults.collectAsState()
    val onlineResults by viewModel.onlineResults.collectAsState()
    val lyricsResults by viewModel.lyricsResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Search Input Field
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onQueryChange(it) },
            placeholder = {
                Text(
                    text = "Search songs, artists, albums or lyrics...",
                    color = SonoraTextMuted,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = SonoraElectricBlue
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Clear",
                            tint = SonoraTextSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SonoraCardBg,
                unfocusedContainerColor = SonoraCardBg,
                focusedBorderColor = SonoraElectricBlue,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = SonoraTextPrimary,
                unfocusedTextColor = SonoraTextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchFilterChip(
                label = "All",
                selected = filter == SearchFilter.ALL,
                onClick = { viewModel.setFilter(SearchFilter.ALL) }
            )
            SearchFilterChip(
                label = "Local Files",
                selected = filter == SearchFilter.LOCAL,
                onClick = { viewModel.setFilter(SearchFilter.LOCAL) }
            )
            SearchFilterChip(
                label = "Online CC Music",
                selected = filter == SearchFilter.ONLINE_DISCOVERY,
                onClick = { viewModel.setFilter(SearchFilter.ONLINE_DISCOVERY) }
            )
            SearchFilterChip(
                label = "Search by Lyrics",
                selected = filter == SearchFilter.BY_LYRICS,
                onClick = { viewModel.setFilter(SearchFilter.BY_LYRICS) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results or Empty State
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SonoraElectricBlue)
            }
        } else if (query.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = SonoraPurple.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Discover songs, artists, or lyrics",
                        style = MaterialTheme.typography.titleMedium,
                        color = SonoraTextSecondary
                    )
                    Text(
                        text = "Search offline local tracks or legal online music",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SonoraTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Local matches
                if (localResults.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Local Music (${localResults.size})")
                    }
                    items(localResults) { song ->
                        SongItem(
                            song = song,
                            isPlaying = song.id == currentPlayingSongId,
                            onClick = { viewModel.playSong(song) },
                            onFavoriteClick = { viewModel.toggleFavorite(song) }
                        )
                    }
                }

                // Online CC discovery matches
                if (onlineResults.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader(
                            title = "Online Discovery (${onlineResults.size})",
                            subtitle = "Creative Commons • Authorized Legal Downloads"
                        )
                    }
                    items(onlineResults) { song ->
                        SongItem(
                            song = song,
                            isPlaying = song.id == currentPlayingSongId,
                            onClick = { viewModel.playSong(song) },
                            onDownloadClick = { viewModel.downloadSong(song) },
                            onFavoriteClick = { viewModel.toggleFavorite(song) }
                        )
                    }
                }

                // Lyrics matches
                if (lyricsResults.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader(title = "Lyrics Matches (${lyricsResults.size})")
                    }
                    items(lyricsResults) { lyrics ->
                        GlassmorphicCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = SonoraCardBg
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lyrics,
                                        contentDescription = null,
                                        tint = SonoraElectricBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${lyrics.trackTitle} - ${lyrics.artistName}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = SonoraTextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = lyrics.plainLyrics?.take(200)?.plus("...") ?: "No preview available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SonoraTextSecondary
                                )
                            }
                        }
                    }
                }

                if (localResults.isEmpty() && onlineResults.isEmpty() && lyricsResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results found for \"$query\"",
                                color = SonoraTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) SonoraElectricBlue else SonoraCardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (selected) SonoraCharcoal else SonoraTextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

