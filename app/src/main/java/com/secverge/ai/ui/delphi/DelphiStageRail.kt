package com.secverge.ai.ui.delphi

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.theme.Cyan
import com.secverge.ai.ui.theme.GlassBorder
import com.secverge.ai.ui.theme.TextMuted
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.Violet

@Composable
fun DelphiStageRail(
    stage: DelphiStage,
    modifier: Modifier = Modifier
) {
    val stages = listOf(
        DelphiStage.Blind to "Blind",
        DelphiStage.Reveal to "Reveal",
        DelphiStage.Debate to "Debate",
        DelphiStage.Converge to "Converge"
    )
    val activeIndex = when (stage) {
        DelphiStage.Configure -> -1
        else -> stages.indexOfFirst { it.first == stage }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        stages.forEachIndexed { index, (_, label) ->
            val reached = index <= activeIndex
            val dotColor by animateColorAsState(
                targetValue = when {
                    index == activeIndex -> Cyan
                    reached -> Violet
                    else -> GlassBorder
                },
                label = "delphiStageColor"
            )
            val scale by animateFloatAsState(
                targetValue = if (index == activeIndex) 1.18f else 1f,
                label = "delphiStageScale"
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .scale(scale)
                        .background(dotColor, CircleShape)
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reached) TextPrimary else TextMuted,
                    fontWeight = if (index == activeIndex) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
