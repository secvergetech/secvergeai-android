package com.secverge.ai.ui.council

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.secverge.ai.ui.components.GlassCard
import com.secverge.ai.ui.components.GradientButton
import com.secverge.ai.ui.components.PremiumTopBar
import com.secverge.ai.ui.components.PremiumErrorCard
import com.secverge.ai.ui.components.PremiumLoadingState
import com.secverge.ai.ui.theme.*

@Composable
fun AICouncilScreen(viewModel: AICouncilViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AICouncilContent(state, viewModel::updateQuestion, viewModel::toggleModel, viewModel::createCouncil, viewModel::runDebate, viewModel::finalizeCouncil, viewModel::dismissError)
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
    Box(Modifier.fillMaxSize().background(MidnightCanvas)) {
        Box(Modifier.fillMaxWidth().height(240.dp).background(Brush.verticalGradient(listOf(Violet.copy(.22f), Color.Transparent))))
        Column(Modifier.fillMaxSize()) {
            PremiumTopBar(subtitle = "AI Council · Independent minds. One transparent consensus.")
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Assemble the council", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Choose at least two independent models. Gemma moderates the debate and prepares the final synthesis.", color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                state.participants.forEach { model ->
                                    Surface(
                                        modifier = Modifier.weight(1f).clickable { onToggle(model.name) },
                                        color = if (model.selected) Indigo.copy(.35f) else MidnightSurfaceHigh,
                                        shape = RoundedCornerShape(14.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (model.selected) Cyan.copy(.7f) else GlassBorder)
                                    ) { Text(model.name, Modifier.padding(vertical = 12.dp), color = if (model.selected) TextPrimary else TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
                                }
                            }
                            OutlinedTextField(
                                value = state.question,
                                onValueChange = onQuestion,
                                enabled = state.stage == CouncilStage.Configure,
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4,
                                label = { Text("Question for the council") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = Cyan, unfocusedBorderColor = GlassBorder)
                            )
                            GradientButton("Create Council", Modifier.fillMaxWidth(), enabled = state.canStart, onClick = onCreate)
                        }
                    }
                }
                if (state.stage != CouncilStage.Configure) item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column { Text("Live debate room", color = TextPrimary, fontWeight = FontWeight.Bold); Text("${state.selectedModels.size} models · Gemma moderator", color = TextSecondary) }
                            AssistChip(onClick = {}, label = { Text(if (state.isLoading) "Thinking" else "Ready") })
                        }
                    }
                }
                items(state.contributions, key = { it.id }) { item ->
                    AnimatedVisibility(true, enter = fadeIn() + slideInVertically { it / 3 }) {
                        GlassCard(Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(item.model, color = Cyan, fontWeight = FontWeight.Bold)
                                    Text("Round ${item.round}", color = TextMuted)
                                }
                                Text(item.content, color = TextPrimary)
                            }
                        }
                    }
                }
                if (state.stage == CouncilStage.Debate) item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GradientButton("Run Debate", Modifier.weight(1f), enabled = state.canDebate, onClick = onDebate)
                        GradientButton("Finalize", Modifier.weight(1f), enabled = state.canFinalize, onClick = onFinalize)
                    }
                }
                state.consensus?.let { consensus -> item {
                    AnimatedVisibility(true, enter = fadeIn() + slideInVertically { it / 2 }) {
                        Box(Modifier.fillMaxWidth().background(SECVergeGradients.Signature, RoundedCornerShape(24.dp)).padding(1.dp)) {
                            Column(Modifier.fillMaxWidth().background(MidnightSurface, RoundedCornerShape(23.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Gemma Consensus", color = SignatureEnd, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(consensus, color = TextPrimary)
                                Text("Glass Box trail preserved above", color = TextSecondary)
                            }
                        }
                    }
                } }
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
                                    "The council is reasoning…"
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
