package com.secverge.ai.ui.askhachy

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
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.components.*
import com.secverge.ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskHachyScreen(viewModel: AskHachyViewModel) {
    val state by viewModel.uiState.collectAsState()
    val list=rememberLazyListState()
    var history by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(state.activeHistoryId,state.messages.size,state.isThinking) {
        val size=state.messages.size+if(state.isThinking)1 else 0
        if(size>0)list.animateScrollToItem(size-1)
    }
    Column(Modifier.fillMaxSize()) {
        StudioHeader("Ask Hachy","A little clarity, whenever you need it.") {
            GlassAction("Recent Hachy chats","history",!state.isThinking){history=true}
            GlassAction("New Hachy chat","plus",!state.isThinking,viewModel::startNewChat)
        }
        LazyColumn(Modifier.weight(1f),state=list,contentPadding=PaddingValues(horizontal=16.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            if(state.messages.isEmpty()) item {
                StudioEmpty("What's on your mind?","Ask a question, untangle an idea or find a fresh perspective.","Hachy",
                    listOf("Explain something simply","Help me plan my day","Turn an idea into a clear next step"),viewModel::updatePrompt)
            }
            items(state.messages,key={it.id}) { message ->
                if(message.isUser)UserQuestion(message.text) else ResponseCard("Hachy",message.text)
            }
            if(state.isThinking)item {ModelWorking("Hachy")}
            state.errorMessage?.let { error ->item {PremiumErrorCard("Couldn't finish that answer",error,dismissLabel="Dismiss",onDismiss=viewModel::retry,retryLabel="Edit question",onRetry={state.messages.lastOrNull{it.isUser}?.let{viewModel.updatePrompt(it.text)}})} }
        }
        MessageComposer(state.prompt,viewModel::updatePrompt,!state.isThinking,"Hachy",viewModel::send)
    }
    if(history) ModalBottomSheet(onDismissRequest={history=false},containerColor=MidnightSurface) {
        Column(Modifier.fillMaxWidth().padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("Your Hachy chats",style=MaterialTheme.typography.headlineMedium,color=TextPrimary)
            Text("Saved privately on this device",color=TextSecondary,style=MaterialTheme.typography.bodyMedium)
        }
        LazyColumn(Modifier.fillMaxWidth().heightIn(max=520.dp),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            if(state.savedConversations.isEmpty())item{Text("Your first conversation will appear here.",color=TextSecondary)}
            items(state.savedConversations,key={it.localId}) { chat ->
                LiquidPanel(Modifier.fillMaxWidth(),padding=PaddingValues(16.dp),radius=20.dp) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).clickable{viewModel.loadConversation(chat.localId);history=false},verticalArrangement=Arrangement.spacedBy(5.dp)) {
                            Text(chat.title,color=TextPrimary,maxLines=2,style=MaterialTheme.typography.titleSmall)
                            Text("${chat.messageCount} messages · ${DateFormat.format("dd MMM",chat.updatedAtMillis)}",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
                        }
                        GlassAction("Open Hachy conversation","arrow"){viewModel.loadConversation(chat.localId);history=false}
                        GlassAction("Delete Hachy conversation","close"){deleting=chat.localId}
                    }
                }
            }
        }
    }
    deleting?.let { id -> AlertDialog(onDismissRequest={deleting=null},title={Text("Delete this chat?")},text={Text("This removes the saved conversation from this device.")},confirmButton={TextButton(onClick={viewModel.deleteConversation(id);deleting=null}){Text("Delete",color=Danger)}},dismissButton={TextButton(onClick={deleting=null}){Text("Keep chat")}}) }
}
