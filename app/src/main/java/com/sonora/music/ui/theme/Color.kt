package com.sonora.music.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Sonora Premium Dark AMOLED Palette
val SonoraBlack = Color(0xFF0B0C10)
val SonoraCharcoal = Color(0xFF14161E)
val SonoraCardBg = Color(0xFF1C1F2B)
val SonoraCardBgElevated = Color(0xFF242838)
val SonoraGlassBorder = Color(0x337C4DFF)

val SonoraPurple = Color(0xFF7C4DFF)
val SonoraPurpleDark = Color(0xFF5E35B1)
val SonoraElectricBlue = Color(0xFF00D2FF)
val SonoraMagenta = Color(0xFFFF2E93)
val SonoraCyan = Color(0xFF00E5FF)

val SonoraTextPrimary = Color(0xFFF6F7FB)
val SonoraTextSecondary = Color(0xFF9EA3B6)
val SonoraTextMuted = Color(0xFF62687A)

val SonoraSuccess = Color(0xFF00E676)
val SonoraWarning = Color(0xFFFFB300)
val SonoraError = Color(0xFFFF5252)

// Gradients
val SonoraPrimaryGradient = Brush.horizontalGradient(
    colors = listOf(SonoraPurple, SonoraElectricBlue)
)

val SonoraMagentaGradient = Brush.horizontalGradient(
    colors = listOf(SonoraPurple, SonoraMagenta)
)

val SonoraCardGradient = Brush.verticalGradient(
    colors = listOf(SonoraCardBgElevated, SonoraCardBg)
)

val SonoraAmoledGradient = Brush.verticalGradient(
    colors = listOf(SonoraCharcoal, SonoraBlack)
)

