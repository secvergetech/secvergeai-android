package com.secverge.ai.ui.delphi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.components.GlassCard
import com.secverge.ai.ui.theme.Cyan
import com.secverge.ai.ui.theme.TextMuted
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.TextSecondary

@Composable
fun SealedSubmissionCard(
    submission: DelphiSubmission,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevated = false
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = submission.model,
                color = Cyan,
                style = MaterialTheme.typography.titleMedium
            )

            if (submission.revealed) {
                Text(
                    text = submission.content,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Blind view collected privately. Reveal positions to inspect this submission.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            AnimatedVisibility(
                visible = !submission.revealed
            ) {
                Text(
                    text = "Sealed Delphi submission",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun SealedSubmissionCard(
    entry: DelphiRoomEntry,
    revealed: Boolean,
    modifier: Modifier = Modifier
) {
    SealedSubmissionCard(
        submission = DelphiSubmission(
            model = entry.model ?: entry.title,
            content = entry.content,
            revealed = revealed || !entry.hiddenUntilReveal
        ),
        modifier = modifier
    )
}
