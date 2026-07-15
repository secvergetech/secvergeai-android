package com.secverge.ai.ui.delphi

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.theme.MidnightSurface
import com.secverge.ai.ui.theme.SECVergeGradients
import com.secverge.ai.ui.theme.SignatureEnd
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.TextSecondary

@Composable
fun DelphiConsensusCard(
    consensus: String,
    confidence: Double?,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(targetValue = 1f, label = "consensusScale")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .background(SECVergeGradients.Signature, RoundedCornerShape(26.dp))
            .padding(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MidnightSurface, RoundedCornerShape(25.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Delphi Convergence",
                    style = MaterialTheme.typography.titleLarge,
                    color = SignatureEnd,
                    fontWeight = FontWeight.Bold
                )
                confidence?.let {
                    Text("${(it * 100).toInt()}%", color = TextSecondary)
                }
            }
            Text(consensus, color = TextPrimary)
            Text(
                "Independent views, revealed together, challenged transparently, and converged into one recommendation.",
                color = TextSecondary
            )
        }
    }
}
