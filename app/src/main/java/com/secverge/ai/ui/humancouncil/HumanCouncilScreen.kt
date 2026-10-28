package com.secverge.ai.ui.humancouncil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.components.*
import com.secverge.ai.ui.theme.*

@Composable
fun HumanCouncilScreen(viewModel: HumanCouncilViewModel, onExplore: () -> Unit = {}) {
    Column(Modifier.fillMaxSize()) {
        StudioHeader("Human Council","A space for human perspectives.")
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            LiquidPanel(Modifier.fillMaxWidth(),padding=PaddingValues(24.dp)) {
                StudioIcon("lock",Cyan,Modifier.size(44.dp))
                Spacer(Modifier.height(24.dp))
                GlassPill("CURRENTLY UNAVAILABLE",Warning)
                Spacer(Modifier.height(16.dp))
                Text("Good conversations\\nneed everyone present.".replace("\\n","\n"),color=TextPrimary,style=MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                Text("Human Council is not available on the server yet. You can still explore ideas with individual models, AI Council or Delphi.",color=TextSecondary,style=MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))
                GradientButton("Explore the model studio",Modifier.fillMaxWidth(),onClick=onExplore)
            }
        }
    }
}
