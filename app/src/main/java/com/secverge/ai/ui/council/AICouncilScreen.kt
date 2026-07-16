package com.secverge.ai.ui.council

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.secverge.ai.ui.components.PremiumErrorCard
import com.secverge.ai.ui.components.PremiumLoadingState
import com.secverge.ai.ui.components.PremiumTopBar
import com.secverge.ai.ui.theme.Cyan
import com.secverge.ai.ui.theme.GlassBorder
import com.secverge.ai.ui.theme.Indigo
import com.secverge.ai.ui.theme.MidnightCanvas
import com.secverge.ai.ui.theme.MidnightSurface
import com.secverge.ai.ui.theme.MidnightSurfaceHigh
import com.secverge.ai.ui.theme.SECVergeGradients
import com.secverge.ai.ui.theme.SignatureEnd
import com.secverge.ai.ui.theme.TextMuted
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.TextSecondary
import com.secverge.ai.ui.theme.Violet

@Composable
fun AICouncilScreen(
    viewModel: AICouncilViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AICouncilContent(
        state = state,
        onQuestion = viewModel::updateQuestion,
        onToggle = viewModel::toggleModel,
        onCreate = viewModel::createCouncil,
        onDebate = viewModel::runDebate,
        onFinalize = viewModel::finalizeCouncil,
        onDismissError = viewModel::dismissError
    )
}

@Composable
private fun AICouncilContent(
    state: AICouncilUiState,
    onQuestion: (String) -> Unit,
    onToggle: (String) -> Unit,
    onCreate: () -> Unit,
    onDebate: () -> Unit,
    onFinalize: () -> Unit,
    onDismissError: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightCanvas)
            .imePadding()
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Violet.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PremiumTopBar(
                subtitle = "AI Council · Independent minds. One transparent consensus."
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 12.dp,
                    end = 16.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Assemble the council",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Choose at least two independent models. Gemma moderates the debate and prepares the final synthesis.",
                                color = TextSecondary
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                state.participants.forEach { model ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                onToggle(model.name)
                                            },
                                        color = if (model.selected) {
                                            Indigo.copy(alpha = 0.35f)
                                        } else {
                                            MidnightSurfaceHigh
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (model.selected) {
                                                Cyan.copy(alpha = 0.7f)
                                            } else {
                                                GlassBorder
                                            }
                                        )
                                    ) {
                                        Text(
                                            text = model.name,
                                            modifier = Modifier.padding(
                                                vertical = 12.dp,
                                                horizontal = 6.dp
                                            ),
                                            color = if (model.selected) {
                                                TextPrimary
                                            } else {
                                                TextMuted
                                            },
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = state.question,
                                onValueChange = onQuestion,
                                enabled = state.stage == CouncilStage.Configure,
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4,
                                label = {
                                    Text("Question for the council")
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = Cyan,
                                    unfocusedBorderColor = GlassBorder
                                )
                            )

                            GradientButton(
                                text = "Create Council",
                                modifier = Modifier.fillMaxWidth(),
                                enabled = state.canStart,
                                loading = state.isLoading &&
                                    state.stage == CouncilStage.Configure,
                                onClick = onCreate
                            )
                        }
                    }
                }

                if (state.stage != CouncilStage.Configure) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Live debate room",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "${state.selectedModels.size} models · Gemma moderator",
                                        color = TextSecondary
                                    )
                                }

                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            if (state.isLoading) {
                                                "Thinking"
                                            } else {
                                                "Ready"
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                items(
                    items = state.contributions,
                    key = { it.id }
                ) { contribution ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() +
                            slideInVertically { it / 3 }
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = contribution.model,
                                        color = Cyan,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "Round ${contribution.round}",
                                        color = TextMuted
                                    )
                                }

                                Text(
                                    text = contribution.content,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                if (state.stage == CouncilStage.Debate) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GradientButton(
                                text = "Run Debate",
                                modifier = Modifier.weight(1f),
                                enabled = state.canDebate,
                                loading = state.isLoading,
                                onClick = onDebate
                            )

                            GradientButton(
                                text = "Finalize",
                                modifier = Modifier.weight(1f),
                                enabled = state.canFinalize,
                                loading = false,
                                onClick = onFinalize
                            )
                        }
                    }
                }

                state.consensus?.let { consensus ->
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() +
                                slideInVertically { it / 2 }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = SECVergeGradients.Signature,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = MidnightSurface,
                                            shape = RoundedCornerShape(23.dp)
                                        )
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Gemma Consensus",
                                        color = SignatureEnd,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = consensus,
                                        color = TextPrimary
                                    )

                                    Text(
                                        text = "Glass Box trail preserved above",
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                state.errorMessage?.let { message ->
                    item {
                        PremiumErrorCard(
                            title = "Council operation interrupted",
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
                                CouncilStage.Configure ->
                                    "Creating the council chamber…"

                                CouncilStage.Debate ->
                                    "The council is reasoning locally…"

                                CouncilStage.Consensus ->
                                    "Gemma is finalizing consensus…"
                            }
                        )
                    }
                }
            }
        }
    }
}
