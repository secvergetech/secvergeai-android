package com.secverge.ai.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secverge.ai.navigation.SECVergeRoute
import com.secverge.ai.ui.components.*
import com.secverge.ai.ui.theme.*

@Composable
fun AppScaffold(currentRoute: String?, onDestinationClick: (SECVergeRoute)->Unit, content: @Composable ()->Unit) {
    val keyboardVisible=WindowInsets.ime.getBottom(LocalDensity.current)>0
    Scaffold(containerColor=Color.Transparent,contentWindowInsets=WindowInsets.safeDrawing,
        bottomBar={ if(!keyboardVisible) {
            LiquidPanel(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=16.dp,vertical=8.dp),
                radius=28.dp,padding=PaddingValues(6.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    SECVergeRoute.bottomDestinations.forEach { route ->
                        val active=currentRoute==route.route
                        val label=when(route) { SECVergeRoute.Home->"Home";SECVergeRoute.AskHachy->"Hachy";SECVergeRoute.Cockpit->"Models";SECVergeRoute.Council->"Council";else->"Delphi" }
                        val icon=when(route) { SECVergeRoute.Home->"home";SECVergeRoute.AskHachy->"chat";SECVergeRoute.Cockpit->"models";SECVergeRoute.Council->"council";else->"delphi" }
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(21.dp))
                            .background(if(active)Cobalt.copy(alpha=.22f) else Color.Transparent)
                            .selectable(active,role=Role.Tab,onClick={if(!active)onDestinationClick(route)})
                            .padding(vertical=10.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
                            StudioIcon(icon,if(active)Cyan else TextSecondary)
                            Text(label,color=if(active)TextPrimary else TextSecondary,fontSize=10.sp)
                        }
                    }
                }
            }
        } }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) { content() }
    }
}
