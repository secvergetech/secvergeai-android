package com.secverge.ai.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

object SECVergeGradients {
    val Signature = Brush.linearGradient(
        colors = listOf(SignatureStart, SignatureMidOne, SignatureMidTwo, SignatureEnd),
        start = Offset.Zero,
        end = Offset.Infinite
    )

    val PrimaryButton = Brush.linearGradient(
        colors = listOf(Cobalt, Indigo, Violet)
    )

    val CyanGlow = Brush.radialGradient(
        colors = listOf(Cyan.copy(alpha = 0.28f), MidnightCanvas.copy(alpha = 0f))
    )

    val VioletGlow = Brush.radialGradient(
        colors = listOf(Violet.copy(alpha = 0.32f), MidnightCanvas.copy(alpha = 0f))
    )
}
