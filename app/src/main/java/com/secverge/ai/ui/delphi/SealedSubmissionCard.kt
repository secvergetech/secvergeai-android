package com.secverge.ai.ui.delphi

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.theme.Cyan
import com.secverge.ai.ui.theme.GlassBorder
import com.secverge.ai.ui.theme.MidnightSurface
import com.secverge.ai.ui.theme.TextMuted
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.Violet

@Composable
fun SealedSubmissionCard(
    submission: DelphiSubmission,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MidnightSurface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            if (submission.revealed) Cyan.copy(alpha = 0.72f) else GlassBorder
        )
    ) {
        AnimatedContent(
            targetState = submission.revealed,
            transitionSpec = {
                (fadeIn() togetherWith fadeOut()).using(SizeTransform(clip = false))
            },
            label = "sealedReveal"
        ) { revealed ->
            if (revealed) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            submission.model,
                            color = Cyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text("REVEALED", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    }
                    Text(submission.content, color = TextPrimary)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Violet.copy(alpha = 0.18f), Cyan.copy(alpha = 0.08f))
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("◈  SEALED SUBMISSION", color = Violet, fontWeight = FontWeight.Bold)
                        Text(
                            "Independent reasoning is protected until every model has submitted.",
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
