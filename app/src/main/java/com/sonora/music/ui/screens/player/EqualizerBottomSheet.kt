package com.sonora.music.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonora.music.domain.model.EqualizerPreset
import com.sonora.music.playback.AudioEffectsController
import com.sonora.music.ui.theme.SonoraCardBg
import com.sonora.music.ui.theme.SonoraCharcoal
import com.sonora.music.ui.theme.SonoraElectricBlue
import com.sonora.music.ui.theme.SonoraMagenta
import com.sonora.music.ui.theme.SonoraPurple
import com.sonora.music.ui.theme.SonoraTextMuted
import com.sonora.music.ui.theme.SonoraTextPrimary
import com.sonora.music.ui.theme.SonoraTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerBottomSheet(
    audioEffects: AudioEffectsController,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isEnabled by audioEffects.isEnabled.collectAsState()
    val bands by audioEffects.bands.collectAsState()
    val currentPreset by audioEffects.currentPreset.collectAsState()
    val bassStrength by audioEffects.bassStrength.collectAsState()
    val virtualizerStrength by audioEffects.virtualizerStrength.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SonoraCharcoal
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header with Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Audio Equalizer & FX",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = SonoraTextPrimary
                    )
                    Text(
                        text = "Hardware DSP audio processing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SonoraTextMuted
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { audioEffects.setEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SonoraElectricBlue,
                        checkedTrackColor = SonoraPurple
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets Horizontal Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EqualizerPreset.ALL_PRESETS.forEach { preset ->
                    val isSelected = currentPreset == preset.name
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) SonoraElectricBlue else SonoraCardBg
                            )
                            .clickable(enabled = isEnabled) {
                                audioEffects.applyPreset(preset)
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = preset.name,
                            color = if (isSelected) SonoraCharcoal else SonoraTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5 Equalizer Frequency Sliders
            Text(
                text = "Frequency Bands",
                style = MaterialTheme.typography.titleSmall,
                color = SonoraTextPrimary
            )

            bands.forEach { band ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = band.frequencyLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SonoraTextSecondary,
                        modifier = Modifier.width(60.dp)
                    )
                    Slider(
                        value = band.gainDb,
                        onValueChange = { gain -> audioEffects.setBandGain(band.index, gain) },
                        valueRange = -12f..12f,
                        enabled = isEnabled,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = SonoraElectricBlue,
                            activeTrackColor = SonoraPurple
                        )
                    )
                    Text(
                        text = String.format("%+.1fdB", band.gainDb),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SonoraTextMuted,
                        modifier = Modifier.width(55.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bass Boost Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bass Boost",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SonoraTextSecondary,
                    modifier = Modifier.width(90.dp)
                )
                Slider(
                    value = (bassStrength / 10f),
                    onValueChange = { audioEffects.setBassBoost((it * 10).toInt()) },
                    valueRange = 0f..100f,
                    enabled = isEnabled,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = SonoraMagenta,
                        activeTrackColor = SonoraMagenta.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = "${(bassStrength / 10)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SonoraTextMuted,
                    modifier = Modifier.width(45.dp)
                )
            }

            // Virtualizer Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3D Virtualizer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SonoraTextSecondary,
                    modifier = Modifier.width(90.dp)
                )
                Slider(
                    value = (virtualizerStrength / 10f),
                    onValueChange = { audioEffects.setVirtualizer((it * 10).toInt()) },
                    valueRange = 0f..100f,
                    enabled = isEnabled,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = SonoraElectricBlue,
                        activeTrackColor = SonoraElectricBlue.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = "${(virtualizerStrength / 10)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SonoraTextMuted,
                    modifier = Modifier.width(45.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

