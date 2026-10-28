package com.secverge.ai.ui.council

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.components.*
import com.secverge.ai.ui.theme.*

@Composable
fun AICouncilScreen(viewModel: AICouncilViewModel) {
    val state by viewModel.uiState.collectAsState()
    val keyboard=LocalSoftwareKeyboardController.current
    var reset by remember {mutableStateOf(false)}
    val setup=state.stage==CouncilStage.Configure
    val complete=state.stage==CouncilStage.Consensus
    val fullRound=state.selectedModels.all {name ->state.contributions.any{it.model.startsWith(name,ignoreCase=true)}}
    Column(Modifier.fillMaxSize().imePadding()) {
        StudioHeader("AI Council","More perspectives. A clearer decision.") {
            GlassAction("New council","plus",!state.isLoading){if(state.contributions.isEmpty())viewModel.startNewCouncil() else reset=true}
        }
        StepRail(listOf("Question","Perspectives","Consensus"),if(setup)0 else if(complete)2 else 1)
        LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            if(setup) {
                item {LiquidPanel(Modifier.fillMaxWidth()) {
                    GlassPill("THINK TOGETHER")
                    Spacer(Modifier.height(14.dp))
                    Text("One question.\nMultiple ways forward.",color=TextPrimary,style=MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(10.dp))
                    Text("Gather independent perspectives, then bring them together into a shared answer.",color=TextSecondary)
                }}
                item {StudioField(state.question,viewModel::updateQuestion,"What would you like to explore?",!state.isLoading)}
                item {LiquidPanel(Modifier.fillMaxWidth()) {
                    Text("Choose your council",color=TextPrimary,style=MaterialTheme.typography.titleMedium)
                    Text("Select at least two models",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth().padding(top=14.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        state.participants.forEach {member -> FilterChip(selected=member.selected,onClick={viewModel.toggleModel(member.name)},enabled=!state.isLoading,label={Text(member.name)},colors=FilterChipDefaults.filterChipColors(containerColor=androidx.compose.ui.graphics.Color.Transparent,labelColor=TextSecondary,selectedContainerColor=modelAccent(member.name).copy(alpha=.18f),selectedLabelColor=TextPrimary),leadingIcon={StudioIcon(if(member.selected)"check" else "plus",modelAccent(member.name),Modifier.size(16.dp))},modifier=Modifier.weight(1f))}
                    }
                }}
            } else {
                item {LiquidPanel(Modifier.fillMaxWidth()) {GlassPill("${state.selectedModels.size} MODEL PERSPECTIVES");Spacer(Modifier.height(12.dp));Text(state.question,color=TextPrimary,style=MaterialTheme.typography.titleMedium)}}
                items(state.contributions,key={it.id}) {entry ->ResponseCard(entry.model.substringBefore(" ("),entry.content,details="Round ${entry.round}\nProvider: ${entry.model}",compact=true)}
                state.consensus?.let{answer ->item {Text("Shared answer",color=TextPrimary,style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(12.dp));ResponseCard("Gemma",answer)}}
                if(state.isLoading)item {ModelWorking("Council")}
            }
            state.errorMessage?.let {error ->item {PremiumErrorCard(message=error,dismissLabel="Dismiss",onDismiss=viewModel::dismissError)}}
        }
        Column(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            val label=if(setup)"Gather perspectives" else if(complete)"Start a new council" else if(!fullRound && state.contributions.isNotEmpty())"Resume perspectives" else if(!fullRound)"Gather perspectives" else "Build a shared answer"
            GradientButton(label,Modifier.fillMaxWidth(),enabled=if(setup)state.canStart else if(complete)true else if(!fullRound)state.canDebate else state.canFinalize,loading=state.isLoading) {
                keyboard?.hide()
                if(setup){viewModel.createCouncil();viewModel.runDebate()} else if(complete)reset=true else if(!fullRound)viewModel.runDebate() else viewModel.finalizeCouncil()
            }
            Text(if(setup)"Your selected models answer independently." else if(complete)"Review each perspective above to see how the answer formed." else "You can read each perspective as it arrives.",color=TextMuted,style=MaterialTheme.typography.labelSmall)
        }
    }
    if(reset)AlertDialog(onDismissRequest={reset=false},title={Text("Start a new council?")},text={Text("This clears the current council from this screen.")},confirmButton={TextButton(onClick={viewModel.startNewCouncil();reset=false}){Text("New council")}},dismissButton={TextButton(onClick={reset=false}){Text("Keep reviewing")}})
}
