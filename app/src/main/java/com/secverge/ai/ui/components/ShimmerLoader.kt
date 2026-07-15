package com.secverge.ai.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.theme.MidnightSurface
import com.secverge.ai.ui.theme.MidnightSurfaceHigh
import com.secverge.ai.ui.theme.SECVergeDimens

@Composable
fun ShimmerLoader(
    modifier: Modifier = Modifier,
    height: Dp = 72.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        MidnightSurface.copy(alpha = alpha),
                        MidnightSurfaceHigh.copy(alpha = alpha),
                        MidnightSurface.copy(alpha = alpha)
                    )
                ),
                shape = RoundedCornerShape(SECVergeDimens.RadiusLarge)
            )
    )
}
