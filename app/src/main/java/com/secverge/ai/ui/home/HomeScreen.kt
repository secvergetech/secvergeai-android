package com.secverge.ai.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secverge.ai.navigation.SECVergeRoute
import com.secverge.ai.ui.components.*
import com.secverge.ai.ui.theme.*

@Composable
fun HomeScreen(viewModel: HomeViewModel, onOpenFeature: (SECVergeRoute)->Unit, onLogout: ()->Unit) {
    val state by viewModel.uiState.collectAsState()
    var account by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }
    LaunchedEffect(state.logoutCompleted) { if(state.logoutCompleted) {viewModel.consumeLogout();onLogout()} }
    Column(Modifier.fillMaxSize()) {
        StudioHeader("Your workspace","Welcome back, ${state.userName}") { GlassAction("Account","lock") {account=true} }
        LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(start=20.dp,end=20.dp,bottom=20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            item {
                LiquidPanel(Modifier.fillMaxWidth(),padding=PaddingValues(22.dp)) {
                    Column(verticalArrangement=Arrangement.spacedBy(18.dp)) {
                        GlassPill("BEYOND INTELLIGENCE")
                        Text("Your next idea,\namplified.",style=MaterialTheme.typography.headlineLarge.copy(brush=SECVergeGradients.Signature),fontWeight=FontWeight.SemiBold)
                        Text("A space to ask, explore and think together.",color=TextSecondary,style=MaterialTheme.typography.bodyMedium)
                        GradientButton("Start a conversation",Modifier.fillMaxWidth(),onClick={onOpenFeature(SECVergeRoute.AskHachy)})
                    }
                }
            }
            item { Text("CHOOSE YOUR STUDIO",color=TextSecondary,fontSize=10.sp,letterSpacing=2.sp,modifier=Modifier.padding(top=8.dp,bottom=2.dp)) }
            item {
                LiquidPanel(Modifier.fillMaxWidth().clickable(role=Role.Button) {onOpenFeature(SECVergeRoute.Cockpit)}) {
                    Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment=Alignment.CenterVertically) {
                            StudioIcon("models",Cyan)
                            Text("Model studio",color=TextPrimary,style=MaterialTheme.typography.titleLarge,modifier=Modifier.weight(1f).padding(start=12.dp))
                            StudioIcon("arrow",TextSecondary)
                        }
                        Text("Four minds. One conversation. Switch models and bring your context with you.",color=TextSecondary,style=MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                            listOf("Gemma","Llama","Mistral","Qwen").forEach { name ->
                                Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)) {
                                    ModelOrb(name,size=36.dp);Text(name,color=TextSecondary,fontSize=10.sp)
                                }
                            }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    StudioTile("AI Council","Different views.\nA stronger answer.","council",Modifier.weight(1f)) {onOpenFeature(SECVergeRoute.Council)}
                    StudioTile("Delphi","Think independently.\nFind common ground.","delphi",Modifier.weight(1f)) {onOpenFeature(SECVergeRoute.Delphi)}
                }
            }
            item {
                LiquidPanel(Modifier.fillMaxWidth().clickable(role=Role.Button){onOpenFeature(SECVergeRoute.HumanCouncil)},padding=PaddingValues(16.dp),radius=20.dp) {
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        StudioIcon("council",TextSecondary)
                        Column(Modifier.weight(1f)) {Text("Human Council",color=TextPrimary,style=MaterialTheme.typography.titleSmall);Text("Currently unavailable",color=TextSecondary,style=MaterialTheme.typography.bodySmall)}
                        StudioIcon("chevron",TextSecondary,Modifier.size(18.dp))
                    }
                }
            }
            item { Text("Your history, securely saved on this device.",color=TextSecondary,fontSize=11.sp,modifier=Modifier.fillMaxWidth().padding(vertical=10.dp),textAlign=androidx.compose.ui.text.style.TextAlign.Center) }
        }
    }
    if(account) AlertDialog(onDismissRequest={account=false},title={Text("Your account")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text(state.userName);state.email?.let{Text(it,color=TextSecondary)};Text("Your session is encrypted on this device.",color=TextSecondary)}},confirmButton={TextButton(onClick={account=false}){Text("Done")}},dismissButton={TextButton(onClick={account=false;confirmLogout=true}){Text("Sign out",color=TextSecondary)}})
    if(confirmLogout) AlertDialog(onDismissRequest={confirmLogout=false},title={Text("Sign out?")},text={Text("Your saved conversations will stay on this device.")},confirmButton={TextButton(onClick={confirmLogout=false;viewModel.logout()}){Text("Sign out")}},dismissButton={TextButton(onClick={confirmLogout=false}){Text("Stay signed in")}})
}

@Composable
private fun StudioTile(title: String, description: String, icon: String, modifier: Modifier, onClick: ()->Unit) {
    LiquidPanel(modifier.clickable(role=Role.Button,onClick=onClick),padding=PaddingValues(17.dp)) {
        Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            StudioIcon(icon,if(icon=="delphi")SignatureEnd else Cyan,Modifier.size(28.dp))
            Text(title,color=TextPrimary,style=MaterialTheme.typography.titleMedium)
            Text(description,color=TextSecondary,style=MaterialTheme.typography.bodySmall)
            StudioIcon("arrow",TextSecondary,Modifier.size(18.dp))
        }
    }
}
