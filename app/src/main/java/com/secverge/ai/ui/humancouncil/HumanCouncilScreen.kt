package com.secverge.ai.ui.humancouncil

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
fun HumanCouncilScreen(viewModel: HumanCouncilViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize().background(MidnightCanvas)) {
        Box(Modifier.fillMaxWidth().height(220.dp).background(Brush.verticalGradient(listOf(Cobalt.copy(.22f), Color.Transparent))))
        Column(Modifier.fillMaxSize()) {
            PremiumTopBar(subtitle = "Multi-LLM + Human · You are the Gatekeeper")
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!state.roomCreated) item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Open an Intellectual Talks room", color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Collaborate with several AI minds while retaining full control over who receives each instruction.", color = TextSecondary)
                            OutlinedTextField(value = state.topic, onValueChange = viewModel::updateTopic, modifier = Modifier.fillMaxWidth(), minLines = 3, label = { Text("Room topic") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = Cyan, unfocusedBorderColor = GlassBorder))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { state.models.forEach { AssistChip(onClick = {}, label = { Text(it) }) } }
                            GradientButton("Create Collaboration Room", Modifier.fillMaxWidth(), enabled = state.canCreate, onClick = viewModel::createRoom)
                        }
                    }
                }
                if (state.roomCreated) item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Council room active", color = TextPrimary, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                (listOf("ALL") + state.models).forEach { target ->
                                    Surface(modifier = Modifier.clickable { viewModel.setDirectTo(target) }, color = if (state.directTo == target) Violet.copy(.38f) else MidnightSurfaceHigh, shape = RoundedCornerShape(50), border = androidx.compose.foundation.BorderStroke(1.dp, if (state.directTo == target) Cyan else GlassBorder)) {
                                        Text(target, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = if (state.directTo == target) TextPrimary else TextMuted)
                                    }
                                }
                            }
                            Text(if (state.directTo == "ALL") "Broadcasting to every model" else "Directing only ${state.directTo}", color = TextSecondary)
                        }
                    }
                }
                items(state.messages, key = { it.id }) { message ->
                    AnimatedVisibility(true, enter = fadeIn() + slideInVertically { it / 3 }) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.isHuman) Arrangement.End else Arrangement.Start) {
                            Box(Modifier.fillMaxWidth(.88f).background(if (message.isHuman) SECVergeGradients.PrimaryButton else Brush.linearGradient(listOf(GlassSurface, GlassSurface)), RoundedCornerShape(20.dp)).padding(16.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(message.model ?: message.speaker, color = if (message.isHuman) TextPrimary else Cyan, fontWeight = FontWeight.Bold)
                                    Text(message.content, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
                state.errorMessage?.let { message ->
                    item {
                        PremiumErrorCard(
                            title = "Collaboration interrupted",
                            message = message,
                            dismissLabel = "Dismiss",
                            onDismiss = viewModel::dismissError
                        )
                    }
                }
                if (state.isLoading) {
                    item {
                        PremiumLoadingState(
                            message = if (state.roomCreated) {
                                "Waiting for model contributions…"
                            } else {
                                "Creating the collaboration room…"
                            }
                        )
                    }
                }
            }
            if (state.roomCreated) {
                GlassCard(Modifier.fillMaxWidth().padding(16.dp), contentPadding = PaddingValues(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = state.input, onValueChange = viewModel::updateInput, modifier = Modifier.weight(1f), maxLines = 4, placeholder = { Text("Add your perspective…") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = Cyan, unfocusedBorderColor = GlassBorder))
                        GradientButton(if (state.directTo == "ALL") "Send" else "Direct", enabled = state.canSend, onClick = viewModel::send)
                    }
                }
            }
        }
    }
}
