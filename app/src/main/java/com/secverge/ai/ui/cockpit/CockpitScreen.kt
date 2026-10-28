package com.secverge.ai.ui.cockpit

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.components.*
import com.secverge.ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CockpitScreen(viewModel: CockpitViewModel) {
    val state by viewModel.uiState.collectAsState()
    val respondingModel = if(state.activeAction == "switch") state.toModel else state.fromModel
    val list = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current
    var carry by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(state.activeSessionId, state.consoleEntries.size, state.isLoading) {
        val size = state.consoleEntries.size + if(state.isLoading) 1 else 0
        if(size > 0) list.animateScrollToItem(size - 1)
    }
    Column(Modifier.fillMaxSize()) {
        StudioHeader("Model studio", "Four perspectives. One place to think.") {
            GlassAction("Model conversation history", "history", !state.isLoading, viewModel::toggleHistory)
            GlassAction("New model conversation", "plus", !state.isLoading, viewModel::startNewSession)
        }
        ModelStrip(respondingModel, !state.isLoading, viewModel::setFromModel)
        if(state.consoleEntries.isNotEmpty()) Row(Modifier.fillMaxWidth().padding(horizontal=20.dp),verticalAlignment=Alignment.CenterVertically) {
            Text("Responding: ${modelLabel(respondingModel)}",color=TextSecondary,style=MaterialTheme.typography.labelMedium,modifier=Modifier.weight(1f))
            TextButton(onClick={carry=true;keyboard?.hide()},enabled=!state.isLoading) {Text("Continue in another model",color=Cyan,style=MaterialTheme.typography.labelMedium)}
        }
        LazyColumn(Modifier.weight(1f),state=list,contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            if(state.consoleEntries.isEmpty()) item {
                val prompts=when(modelLabel(state.fromModel)) {
                    "Llama" -> listOf("Break a complex idea into clear steps", "Help me reason through a decision", "Create a practical learning plan")
                    "Mistral" -> listOf("Make my writing clear and concise", "Draft a compelling introduction", "Brainstorm fresh angles for an idea")
                    "Qwen" -> listOf("Compare the trade-offs between two options", "Explain a technical concept with examples", "Help me analyze a problem step by step")
                    else -> listOf("Explain something simply", "Help me explore a new idea", "Turn a goal into an action plan")
                }
                StudioEmpty("Think with ${modelLabel(state.fromModel)}", "Choose a model above, then start a conversation. Bring the conversation to another model whenever you need a fresh perspective.",state.fromModel,prompts,viewModel::updatePrompt)
            }
            items(state.consoleEntries,key={it.id}) {entry ->
                when(entry.kind) {
                    "user" -> UserQuestion(entry.content)
                    "system" -> PremiumErrorCard(entry.title,entry.content)
                    else -> ResponseCard(if(entry.title.startsWith("UCF carried:"))entry.title.substringAfter("→").trim() else entry.title,entry.content,entry.technicalProof)
                }
            }
            if(state.isLoading)item {ModelWorking(if(state.activeAction=="switch")state.toModel else state.fromModel)}
            state.errorMessage?.let {error ->item {PremiumErrorCard(message=error,dismissLabel="Dismiss",onDismiss=viewModel::dismissError)}}
        }
        MessageComposer(state.prompt,viewModel::updatePrompt,!state.isLoading,respondingModel,viewModel::send)
    }
    if(carry) ModalBottomSheet(onDismissRequest={carry=false},containerColor=MidnightSurface) {
        Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Text("A fresh perspective",color=TextPrimary,style=MaterialTheme.typography.headlineMedium)
            Text("Continue with your conversation and any message in the composer as context.",color=TextSecondary,style=MaterialTheme.typography.bodyMedium)
            DefaultCockpitModelOptions.filter{it!=state.fromModel}.forEach {model ->
                LiquidPanel(Modifier.fillMaxWidth().clickable(enabled=!state.isLoading){viewModel.setToModel(model);viewModel.switchAndCarry();carry=false},padding=PaddingValues(16.dp),radius=20.dp) {
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                        ModelOrb(model)
                        Text("Continue with ${modelLabel(model)}",color=TextPrimary,modifier=Modifier.weight(1f))
                        StudioIcon("arrow",modelAccent(model),Modifier.size(20.dp))
                    }
                }
            }
        }
    }
    if(state.isHistoryVisible) ModalBottomSheet(onDismissRequest=viewModel::toggleHistory,containerColor=MidnightSurface) {
        Column(Modifier.padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("Your model conversations",color=TextPrimary,style=MaterialTheme.typography.headlineMedium)
            Text("Saved privately on this device",color=TextSecondary,style=MaterialTheme.typography.bodyMedium)
        }
        LazyColumn(Modifier.fillMaxWidth().heightIn(max=520.dp),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            if(state.savedSessions.isEmpty())item{Text("Your first conversation will appear here.",color=TextSecondary)}
            items(state.savedSessions,key={it.localId}) {chat ->
                LiquidPanel(Modifier.fillMaxWidth(),padding=PaddingValues(16.dp),radius=20.dp) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).clickable{viewModel.loadSession(chat.localId)},verticalArrangement=Arrangement.spacedBy(5.dp)) {
                            Text(chat.title,color=TextPrimary,maxLines=2,style=MaterialTheme.typography.titleSmall)
                            Text("${modelLabel(chat.fromModel)} · ${DateFormat.format("dd MMM",chat.updatedAtMillis)}",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
                        }
                        GlassAction("Open model conversation","arrow"){viewModel.loadSession(chat.localId)}
                        GlassAction("Delete model conversation","close"){deleting=chat.localId}
                    }
                }
            }
        }
    }
    deleting?.let {id -> AlertDialog(onDismissRequest={deleting=null},title={Text("Delete this conversation?")},text={Text("This removes the saved conversation from this device.")},confirmButton={TextButton(onClick={viewModel.deleteSession(id);deleting=null}){Text("Delete",color=Danger)}},dismissButton={TextButton(onClick={deleting=null}){Text("Keep conversation")}})}
}
