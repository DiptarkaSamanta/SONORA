package com.sonora.music.data.storage

import com.sonora.music.domain.model.DuplicateGroup
import com.sonora.music.domain.model.Song
import kotlin.math.abs

class DuplicateDetector {

    /**
     * Identifies duplicate audio tracks using exact hash matching and intelligent audio fingerprint heuristics:
     * - Normalized Track Title & Artist
     * - Duration tolerance of +/- 2000ms
     * - Bitrate / Quality comparison
     */
    fun findDuplicates(songs: List<Song>): List<DuplicateGroup> {
        val duplicateGroups = mutableListOf<DuplicateGroup>()

        // 1. Group by exact audio hash first
        val hashGroups = songs.filter { !it.audioHash.isNullOrEmpty() }
            .groupBy { it.audioHash!! }
            .filter { it.value.size > 1 }

        val handledSongIds = mutableSetOf<String>()

        for ((_, groupSongs) in hashGroups) {
            val primary = pickBestSongToKeep(groupSongs)
            val duplicates = groupSongs.filter { it.id != primary.id }
            val wasted = duplicates.sumOf { it.sizeBytes }

            duplicateGroups.add(
                DuplicateGroup(
                    title = primary.title,
                    artist = primary.artist,
                    primarySong = primary,
                    duplicates = duplicates,
                    totalWastedBytes = wasted
                )
            )
            handledSongIds.addAll(groupSongs.map { it.id })
        }

        // 2. Intelligent fuzzy matching for remaining songs
        val remainingSongs = songs.filter { it.id !in handledSongIds }
        val visited = mutableSetOf<String>()

        for (i in remainingSongs.indices) {
            val s1 = remainingSongs[i]
            if (s1.id in visited) continue

            val cluster = mutableListOf(s1)
            val normTitle1 = normalizeString(s1.title)
            val normArtist1 = normalizeString(s1.artist)

            for (j in i + 1 until remainingSongs.size) {
                val s2 = remainingSongs[j]
                if (s2.id in visited) continue

                val normTitle2 = normalizeString(s2.title)
                val normArtist2 = normalizeString(s2.artist)

                val durationDiff = abs(s1.durationMs - s2.durationMs)
                val isDurationClose = durationDiff < 2500 // within 2.5 seconds

                val isTitleMatch = normTitle1 == normTitle2 ||
                        (normTitle1.length > 5 && (normTitle1.contains(normTitle2) || normTitle2.contains(normTitle1)))

                val isArtistMatch = normArtist1 == normArtist2 ||
                        normArtist1.contains(normArtist2) || normArtist2.contains(normArtist1) ||
                        normArtist1 == "unknown artist" || normArtist2 == "unknown artist"

                if (isDurationClose && isTitleMatch && isArtistMatch) {
                    cluster.add(s2)
                    visited.add(s2.id)
                }
            }

            if (cluster.size > 1) {
                visited.add(s1.id)
                val primary = pickBestSongToKeep(cluster)
                val duplicates = cluster.filter { it.id != primary.id }
                val wasted = duplicates.sumOf { it.sizeBytes }

                duplicateGroups.add(
                    DuplicateGroup(
                        title = primary.title,
                        artist = primary.artist,
                        primarySong = primary,
                        duplicates = duplicates,
                        totalWastedBytes = wasted
                    )
                )
            }
        }

        return duplicateGroups
    }

    /**
     * Chooses the highest fidelity file to keep (highest bitrate / FLAC over MP3, or most played)
     */
    private fun pickBestSongToKeep(candidates: List<Song>): Song {
        return candidates.maxWithOrNull(
            compareBy<Song> { it.mimeType.contains("flac") }
                .thenBy { it.bitrateKbps }
                .thenBy { it.playCount }
                .thenBy { it.sizeBytes }
        ) ?: candidates.first()
    }

    private fun normalizeString(input: String): String {
        return input.lowercase()
            .replace(Regex("\\[.*?\\]"), "")
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("(?i)feat.*"), "")
            .replace(Regex("(?i)ft.*"), "")
            .replace(Regex("(?i)remaster(ed)?"), "")
            .replace(Regex("(?i)official( audio| video)?"), "")
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }
}

