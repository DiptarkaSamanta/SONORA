package com.sonora.music.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import com.sonora.music.playback.SleepTimerManager
import com.sonora.music.ui.theme.SonoraCardBg
import com.sonora.music.ui.theme.SonoraCharcoal
import com.sonora.music.ui.theme.SonoraElectricBlue
import com.sonora.music.ui.theme.SonoraError
import com.sonora.music.ui.theme.SonoraPurple
import com.sonora.music.ui.theme.SonoraTextMuted
import com.sonora.music.ui.theme.SonoraTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerBottomSheet(
    sleepTimer: SleepTimerManager,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val isActive by sleepTimer.isActive.collectAsState()
    val remainingSeconds by sleepTimer.remainingSeconds.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SonoraCharcoal
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Bedtime,
                    contentDescription = null,
                    tint = SonoraElectricBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sleep Timer",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = SonoraTextPrimary
                )
            }

            Text(
                text = "Music will gently fade out over 15 seconds before pausing",
                style = MaterialTheme.typography.bodyMedium,
                color = SonoraTextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            if (isActive) {
                val mins = remainingSeconds / 60
                val secs = remainingSeconds % 60
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SonoraPurple.copy(alpha = 0.2f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = SonoraElectricBlue
                            )
                        )
                        Text(
                            text = "Remaining before pause",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SonoraTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        sleepTimer.cancelTimer()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SonoraError),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Turn Off Timer", color = SonoraTextPrimary)
                }
            } else {
                val durations = listOf(15, 30, 45, 60)
                durations.forEach { minutes ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SonoraCardBg)
                            .clickable {
                                sleepTimer.startTimer(minutes)
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "$minutes Minutes",
                            style = MaterialTheme.typography.titleMedium,
                            color = SonoraTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

