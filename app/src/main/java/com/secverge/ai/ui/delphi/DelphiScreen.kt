package com.secverge.ai.ui.delphi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.secverge.ai.ui.components.GlassCard
import com.secverge.ai.ui.components.GradientButton
import com.secverge.ai.ui.components.PremiumTopBar
import com.secverge.ai.ui.components.PremiumErrorCard
import com.secverge.ai.ui.components.PremiumLoadingState
import com.secverge.ai.ui.theme.Cyan
import com.secverge.ai.ui.theme.GlassBorder
import com.secverge.ai.ui.theme.Indigo
import com.secverge.ai.ui.theme.MidnightCanvas
import com.secverge.ai.ui.theme.MidnightSurfaceHigh
import com.secverge.ai.ui.theme.TextMuted
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.TextSecondary
import com.secverge.ai.ui.theme.Violet

@Composable
fun DelphiScreen(
    viewModel: DelphiViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DelphiContent(
        state = state,
        onQuestionChange = viewModel::updateQuestion,
        onToggleModel = viewModel::toggleModel,
        onCreate = viewModel::createSession,
        onCollectBlind = viewModel::collectBlindSubmissions,
        onReveal = viewModel::revealSubmissions,
        onDebate = viewModel::runDebate,
        onConverge = viewModel::converge,
        onDismissError = viewModel::dismissError,
        onReset = viewModel::reset
    )
}

@Composable
private fun DelphiContent(
    state: DelphiUiState,
    onQuestionChange: (String) -> Unit,
    onToggleModel: (String) -> Unit,
    onCreate: () -> Unit,
    onCollectBlind: () -> Unit,
    onReveal: () -> Unit,
    onDebate: () -> Unit,
    onConverge: () -> Unit,
    onDismissError: () -> Unit,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightCanvas)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Violet.copy(alpha = 0.28f), Indigo.copy(alpha = 0.10f), Color.Transparent)
                    )
                )
        )

        Column(Modifier.fillMaxSize()) {
            PremiumTopBar(subtitle = "Delphi AI · Blind reasoning. Transparent convergence.")

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                "Independent minds, one considered answer",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Each model reasons privately. Every view is revealed together, debated openly, and converged into a transparent recommendation.",
                                color = TextSecondary
                            )
                            DelphiStageRail(stage = state.stage)
                        }
                    }
                }

                if (state.stage == DelphiStage.Configure) {
                    item {
                        GlassCard(Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Configure the Delphi panel", color = TextPrimary, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    state.participants.forEach { participant ->
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onToggleModel(participant.name) },
                                            color = if (participant.selected) Indigo.copy(alpha = 0.36f) else MidnightSurfaceHigh,
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (participant.selected) Cyan.copy(alpha = 0.72f) else GlassBorder
                                            )
                                        ) {
                                            Text(
                                                text = participant.name,
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                color = if (participant.selected) TextPrimary else TextMuted,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = state.question,
                                    onValueChange = onQuestionChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 4,
                                    label = { Text("Question for independent analysis") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = Cyan,
                                        unfocusedBorderColor = GlassBorder
                                    )
                                )
                                GradientButton(
                                    text = "Create Delphi Session",
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = state.canCreate,
                                    onClick = onCreate
                                )
                            }
                        }
                    }
                }

                if (state.stage != DelphiStage.Configure) {
                    item {
                        GlassCard(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Delphi chamber", color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Text("${state.selectedModels.size} independent models", color = TextSecondary)
                                }
                                AssistChip(
                                    onClick = {},
                                    label = { Text(if (state.isLoading) "Processing" else state.stage.name) }
                                )
                            }
                        }
                    }
                }

                if (state.stage == DelphiStage.Blind) {
                    item {
                        GlassCard(Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Stage 1 · Blind", color = Violet, fontWeight = FontWeight.Bold)
                                Text(
                                    "Collect private submissions. No model sees another model's reasoning.",
                                    color = TextSecondary
                                )
                                GradientButton(
                                    text = "Collect Blind Submissions",
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = state.canCollectBlind,
                                    onClick = onCollectBlind
                                )
                            }
                        }
                    }
                }

                items(state.submissions, key = { it.id }) { submission ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 3 }
                    ) {
                        SealedSubmissionCard(submission = submission)
                    }
                }

                if (state.stage == DelphiStage.Reveal) {
                    item {
                        GradientButton(
                            text = "Reveal All Independent Views",
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.canReveal,
                            onClick = onReveal
                        )
                    }
                }

                if (state.stage == DelphiStage.Debate) {
                    item {
                        GlassCard(Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Stage 3 · Debate", color = Cyan, fontWeight = FontWeight.Bold)
                                Text(
                                    "The independent positions now challenge, refine, and cross-check one another.",
                                    color = TextSecondary
                                )
                                GradientButton(
                                    text = "Begin Transparent Debate",
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = state.canDebate,
                                    onClick = onDebate
                                )
                            }
                        }
                    }
                }

                items(state.contributions, key = { it.id }) { contribution ->
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(contribution.model, color = Cyan, fontWeight = FontWeight.Bold)
                                Text("Round ${contribution.round}", color = TextMuted)
                            }
                            Text(contribution.content, color = TextPrimary)
                        }
                    }
                }

                if (state.stage == DelphiStage.Converge && state.consensus == null) {
                    item {
                        GlassCard(Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Stage 4 · Converge", color = Violet, fontWeight = FontWeight.Bold)
                                Text(
                                    "Synthesize the strongest shared conclusion while preserving the Glass Box trail above.",
                                    color = TextSecondary
                                )
                                GradientButton(
                                    text = "Converge on Consensus",
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = state.canConverge,
                                    onClick = onConverge
                                )
                            }
                        }
                    }
                }

                state.consensus?.let { consensus ->
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically { it / 2 }
                        ) {
                            DelphiConsensusCard(
                                consensus = consensus,
                                confidence = state.confidence
                            )
                        }
                    }
                    item {
                        TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                            Text("Start a new Delphi session")
                        }
                    }
                }

                state.errorMessage?.let { message ->
                    item {
                        PremiumErrorCard(
                            title = "Delphi stage interrupted",
                            message = message,
                            dismissLabel = "Dismiss",
                            onDismiss = onDismissError
                        )
                    }
                }

                if (state.isLoading) {
                    item {
                        PremiumLoadingState(
                            message = when (state.stage) {
                                DelphiStage.Configure ->
                                    "Preparing the Delphi chamber…"
                                DelphiStage.Blind ->
                                    "Collecting sealed submissions…"
                                DelphiStage.Reveal ->
                                    "Revealing independent perspectives…"
                                DelphiStage.Debate ->
                                    "Models are challenging each other…"
                                DelphiStage.Converge ->
                                    "Converging on a final recommendation…"
                            }
                        )
                    }
                }
            }
        }
    }
}
