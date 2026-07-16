package com.secverge.ai.ui.delphi

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.secverge.ai.ui.components.GlassCard
import com.secverge.ai.ui.theme.TextPrimary

@Composable
fun DelphiConsensusCard(
    consensus: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
    ) {
        Text(
            text = consensus,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
