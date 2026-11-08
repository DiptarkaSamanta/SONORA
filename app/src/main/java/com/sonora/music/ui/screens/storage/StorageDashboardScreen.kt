package com.sonora.music.ui.screens.storage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonora.music.domain.model.DuplicateGroup
import com.sonora.music.domain.model.StorageMode
import com.sonora.music.ui.components.GlassmorphicCard
import com.sonora.music.ui.screens.home.SectionHeader
import com.sonora.music.ui.theme.SonoraCardBg
import com.sonora.music.ui.theme.SonoraCardBgElevated
import com.sonora.music.ui.theme.SonoraElectricBlue
import com.sonora.music.ui.theme.SonoraError
import com.sonora.music.ui.theme.SonoraMagenta
import com.sonora.music.ui.theme.SonoraPrimaryGradient
import com.sonora.music.ui.theme.SonoraPurple
import com.sonora.music.ui.theme.SonoraSuccess
import com.sonora.music.ui.theme.SonoraTextMuted
import com.sonora.music.ui.theme.SonoraTextPrimary
import com.sonora.music.ui.theme.SonoraTextSecondary
import com.sonora.music.ui.theme.SonoraWarning

@Composable
fun StorageDashboardScreen(
    viewModel: StorageViewModel = viewModel()
) {
    val stats by viewModel.storageStats.collectAsState()
    val duplicateGroups by viewModel.duplicateGroups.collectAsState()
    val isCleaning by viewModel.isCleaning.collectAsState()
    val isSyncingCloud by viewModel.isSyncingCloud.collectAsState()
    val savedCloudUrl by viewModel.cloudCatalogUrl.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var cloudUrlInput by remember { mutableStateOf("") }

    LaunchedEffect(savedCloudUrl) {
        if (!savedCloudUrl.isNullOrEmpty() && cloudUrlInput.isEmpty()) {
            cloudUrlInput = savedCloudUrl ?: ""
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Smart Storage Optimizer",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = SonoraTextPrimary
                    )
                    Text(
                        text = "Intelligent compression, cache management & duplicate cleanup",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SonoraTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Storage Overview Card
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = SonoraCardBgElevated
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Device Storage",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SonoraTextPrimary
                            )
                            Text(
                                text = "${stats.freeStorageFormatted} Free",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SonoraSuccess
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual progress bar
                        LinearProgressIndicator(
                            progress = { stats.deviceUsagePercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = SonoraElectricBlue,
                            trackColor = SonoraPurple.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Metrics breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StorageMetricItem(label = "Music Files", value = stats.musicStorageFormatted, color = SonoraElectricBlue)
                            StorageMetricItem(label = "Image/HTTP Cache", value = stats.cacheStorageFormatted, color = SonoraPurple)
                            StorageMetricItem(label = "Duplicate Waste", value = stats.duplicateSavingsFormatted, color = SonoraWarning)
                        }
                    }
                }
            }

            // Cloud Music Database Sync Card
            item {
                Spacer(modifier = Modifier.height(16.dp))
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = SonoraPurple.copy(alpha = 0.15f),
                    borderColor = SonoraElectricBlue.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CloudSync,
                                contentDescription = null,
                                tint = SonoraElectricBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Cloud Music Database (Firebase / GDrive)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SonoraElectricBlue
                            )
                        }

                        Text(
                            text = "Keep all 1,000 Spotify songs stored in the cloud (~1.2 MB each). Streams with 0 MB phone storage. Songs never get lost or deleted on app updates!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SonoraTextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        OutlinedTextField(
                            value = cloudUrlInput,
                            onValueChange = { cloudUrlInput = it },
                            placeholder = { Text("https://.../sonora_catalog.json", color = SonoraTextMuted, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SonoraCardBg,
                                unfocusedContainerColor = SonoraCardBg,
                                focusedBorderColor = SonoraElectricBlue,
                                unfocusedBorderColor = SonoraPurple.copy(alpha = 0.3f),
                                focusedTextColor = SonoraTextPrimary,
                                unfocusedTextColor = SonoraTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.syncCloudCatalog(cloudUrlInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = SonoraElectricBlue),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSyncingCloud,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isSyncingCloud) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Syncing Songs...", color = Color.Black, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Sync Cloud Songs Now", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Storage Mode Selection
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(
                    title = "Audio Storage Mode",
                    subtitle = "Select quality and storage optimization preference"
                )
            }

            items(StorageMode.values()) { mode ->
                StorageModeCard(
                    mode = mode,
                    isSelected = mode == currentMode,
                    onClick = { viewModel.selectStorageMode(mode) }
                )
            }

            // Cache & Cleaning Quick Actions
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(title = "One-Click Cleanup")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.cleanTempCache() },
                        colors = ButtonDefaults.buttonColors(containerColor = SonoraPurple),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isCleaning
                    ) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Cache", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.cleanAllDuplicates() },
                        colors = ButtonDefaults.buttonColors(containerColor = SonoraElectricBlue),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isCleaning && duplicateGroups.isNotEmpty()
                    ) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clean Duplicates", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Duplicate Songs List
            if (duplicateGroups.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader(
                        title = "Detected Duplicates (${duplicateGroups.size})",
                        subtitle = "Audio hash & heuristic matching identified redundant tracks"
                    )
                }

                items(duplicateGroups) { group ->
                    DuplicateGroupCard(group = group)
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        )
    }
}

@Composable
fun StorageMetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SonoraTextMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
fun StorageModeCard(
    mode: StorageMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        backgroundColor = if (isSelected) SonoraPurple.copy(alpha = 0.2f) else SonoraCardBg,
        borderColor = if (isSelected) SonoraElectricBlue else SonoraPurple.copy(alpha = 0.2f),
        borderWidth = if (isSelected) 1.5.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) SonoraElectricBlue else SonoraTextMuted,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mode.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SonoraTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ~${mode.estimatedMbPerMinute}MB/min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SonoraTextMuted
                    )
                }

                Text(
                    text = mode.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SonoraTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun DuplicateGroupCard(group: DuplicateGroup) {
    val wastedMb = group.totalWastedBytes / (1024 * 1024)
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SonoraTextPrimary
                    )
                    Text(
                        text = group.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SonoraTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SonoraWarning.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+$wastedMb MB wasted",
                        color = SonoraWarning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Keeping: ${group.primarySong.qualityBadge} (${group.primarySong.sizeFormatted})",
                style = MaterialTheme.typography.bodyMedium,
                color = SonoraSuccess
            )
            Text(
                text = "Redundant: ${group.duplicates.size} lower/duplicate versions",
                style = MaterialTheme.typography.bodyMedium,
                color = SonoraTextMuted
            )
        }
    }
}

