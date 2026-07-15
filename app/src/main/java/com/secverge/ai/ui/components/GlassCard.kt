package com.secverge.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.secverge.ai.ui.theme.GlassBorder
import com.secverge.ai.ui.theme.GlassSurface
import com.secverge.ai.ui.theme.SECVergeDimens

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues =
        PaddingValues(SECVergeDimens.Space3),
    cornerRadius: Dp = SECVergeDimens.RadiusLarge,
    elevated: Boolean = true,
    borderColor: Color = GlassBorder,
    surfaceColor: Color = GlassSurface,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .then(
                if (elevated) {
                    Modifier.shadow(
                        elevation =
                            SECVergeDimens.SoftElevation,
                        shape = shape,
                        ambientColor =
                            Color.Black.copy(alpha = 0.36f),
                        spotColor =
                            Color.White.copy(alpha = 0.07f)
                    )
                } else {
                    Modifier
                }
            )
            .background(
                color = surfaceColor,
                shape = shape
            )
            .border(
                width = SECVergeDimens.GlassBorderWidth,
                color = borderColor,
                shape = shape
            )
            .padding(contentPadding)
    ) {
        content()
    }
}
