package com.sonora.music.domain.model

data class LyricsLine(
    val timestampMs: Long,
    val text: String
)

data class LyricsData(
    val trackTitle: String,
    val artistName: String,
    val plainLyrics: String? = null,
    val syncedLines: List<LyricsLine> = emptyList(),
    val isSynced: Boolean = false
) {
    fun findActiveLineIndex(currentPositionMs: Long): Int {
        if (!isSynced || syncedLines.isEmpty()) return -1
        for (i in syncedLines.indices.reversed()) {
            if (currentPositionMs >= syncedLines[i].timestampMs) {
                return i
            }
        }
        return 0
    }

    companion object {
        /**
         * Parses LRC format string e.g. "[00:15.30] Lyric line text"
         */
        fun parseLrc(lrcContent: String, title: String, artist: String): LyricsData {
            val lines = mutableListOf<LyricsLine>()
            val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")

            lrcContent.lineSequence().forEach { line ->
                val match = regex.find(line.trim())
                if (match != null) {
                    val (minStr, secStr, fracStr, text) = match.destructured
                    val minutes = minStr.toLongOrNull() ?: 0L
                    val seconds = secStr.toLongOrNull() ?: 0L
                    val fraction = fracStr.toLongOrNull() ?: 0L
                    val fractionMs = if (fracStr.length == 2) fraction * 10 else fraction
                    val timestamp = (minutes * 60 + seconds) * 1000 + fractionMs
                    lines.add(LyricsLine(timestamp, text.trim()))
                }
            }

            return if (lines.isNotEmpty()) {
                LyricsData(
                    trackTitle = title,
                    artistName = artist,
                    plainLyrics = lines.joinToString("\n") { it.text },
                    syncedLines = lines.sortedBy { it.timestampMs },
                    isSynced = true
                )
            } else {
                LyricsData(
                    trackTitle = title,
                    artistName = artist,
                    plainLyrics = lrcContent,
                    syncedLines = emptyList(),
                    isSynced = false
                )
            }
        }
    }
}

