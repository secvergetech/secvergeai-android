package com.secverge.ai.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secverge.ai.ui.components.*
import com.secverge.ai.ui.theme.*

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: ()->Unit) {
    val state by viewModel.uiState.collectAsState()
    val focus=LocalFocusManager.current
    val keyboard=LocalSoftwareKeyboardController.current
    LaunchedEffect(state.loginSuccessful){if(state.loginSuccessful){viewModel.consumeLoginSuccess();onLoginSuccess()}}
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.Center) {
        androidx.compose.foundation.Image(
            painter=androidx.compose.ui.res.painterResource(com.secverge.ai.R.drawable.secverge_brand),
            contentDescription="SECVerge AI",
            modifier=Modifier.size(80.dp),
            contentScale=androidx.compose.ui.layout.ContentScale.Fit
        )
        Spacer(Modifier.height(24.dp))
        Text("SECVERGE AI",color=Cyan,fontSize=11.sp,letterSpacing=3.sp)
        Spacer(Modifier.height(12.dp))
        Text("A clearer way\nto think.",style=MaterialTheme.typography.displayMedium.copy(brush=SECVergeGradients.Signature))
        Spacer(Modifier.height(12.dp))
        Text("Your models. Your ideas. One secure workspace.",color=TextSecondary,style=MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(30.dp))
        LiquidPanel(Modifier.fillMaxWidth()) {
            Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
                Text("Welcome back",color=TextPrimary,style=MaterialTheme.typography.titleLarge)
                OutlinedTextField(state.email,viewModel::updateEmail,label={Text("Email address")},singleLine=true,enabled=!state.isLoading,
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email,imeAction=ImeAction.Next),keyboardActions=KeyboardActions(onNext={focus.moveFocus(FocusDirection.Down)}),modifier=Modifier.fillMaxWidth(),shape=androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                OutlinedTextField(state.password,viewModel::updatePassword,label={Text("Password")},singleLine=true,enabled=!state.isLoading,
                    visualTransformation=if(state.passwordVisible)VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon={TextButton(onClick=viewModel::togglePasswordVisibility){Text(if(state.passwordVisible)"Hide" else "Show")}},
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password,imeAction=ImeAction.Done),keyboardActions=KeyboardActions(onDone={if(state.canSubmit){keyboard?.hide();viewModel.login()}}),modifier=Modifier.fillMaxWidth(),shape=androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                state.errorMessage?.let {PremiumErrorCard("Sign-in didn't complete",it)}
                GradientButton("Sign in",Modifier.fillMaxWidth(),enabled=state.canSubmit,loading=state.isLoading,onClick={keyboard?.hide();viewModel.login()})
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    StudioIcon("lock",TextSecondary,Modifier.size(15.dp))
                    Text("Your session is encrypted on this device.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Beyond intelligence.",color=TextSecondary,fontSize=12.sp)
    }
}
