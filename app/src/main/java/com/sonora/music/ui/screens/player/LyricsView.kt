package com.sonora.music.ui.screens.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonora.music.domain.model.LyricsData
import com.sonora.music.ui.theme.SonoraElectricBlue
import com.sonora.music.ui.theme.SonoraTextMuted
import com.sonora.music.ui.theme.SonoraTextPrimary
import com.sonora.music.ui.theme.SonoraTextSecondary

@Composable
fun LyricsView(
    lyricsData: LyricsData?,
    isLoading: Boolean,
    currentPositionMs: Long,
    onSeekToTimestamp: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SonoraElectricBlue)
        }
        return
    }

    if (lyricsData == null || (lyricsData.syncedLines.isEmpty() && lyricsData.plainLyrics.isNullOrBlank())) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No lyrics found for this song\n(Search available in Search tab)",
                style = MaterialTheme.typography.bodyLarge,
                color = SonoraTextMuted,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    if (lyricsData.isSynced && lyricsData.syncedLines.isNotEmpty()) {
        val listState = rememberLazyListState()
        val activeIndex = lyricsData.findActiveLineIndex(currentPositionMs)

        LaunchedEffect(activeIndex) {
            if (activeIndex >= 0) {
                // Scroll smoothly to center active line
                listState.animateScrollToItem(
                    index = (activeIndex - 2).coerceAtLeast(0)
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 40.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(lyricsData.syncedLines) { index, line ->
                val isActive = index == activeIndex
                Text(
                    text = line.text.ifEmpty { "♪ ♪ ♪" },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = if (isActive) 22.sp else 16.sp,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium
                    ),
                    color = if (isActive) SonoraElectricBlue else SonoraTextMuted.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSeekToTimestamp(line.timestampMs) }
                        .padding(vertical = 12.dp)
                )
            }
        }
    } else {
        // Plain lyrics fallback
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = lyricsData.plainLyrics ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 28.sp,
                        fontSize = 17.sp
                    ),
                    color = SonoraTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

