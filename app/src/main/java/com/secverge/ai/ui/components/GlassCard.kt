package com.secverge.ai.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    cornerRadius: Dp = SECVergeDimens.RadiusLarge,
    elevated: Boolean = true,
    borderColor: Color = GlassBorder,
    surfaceColor: Color = GlassSurface,
    content: @Composable () -> Unit
) {
    LiquidPanel(modifier,cornerRadius,MidnightSurface,contentPadding) { content() }
}
