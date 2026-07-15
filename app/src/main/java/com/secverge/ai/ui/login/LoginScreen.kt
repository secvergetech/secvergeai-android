package com.secverge.ai.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.components.GlassCard
import com.secverge.ai.ui.components.GradientButton
import com.secverge.ai.ui.theme.Danger
import com.secverge.ai.ui.theme.MidnightCanvas
import com.secverge.ai.ui.theme.SECVergeDimens
import com.secverge.ai.ui.theme.SECVergeGradients
import com.secverge.ai.ui.theme.TextMuted
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    var contentVisible by remember {
        mutableStateOf(false)
    }

    val shakeOffset = remember {
        Animatable(0f)
    }

    val infiniteTransition = rememberInfiniteTransition(
        label = "login_orbs"
    )

    val cyanOrbMovement by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cyan_orb_movement"
    )

    val violetOrbMovement by infiniteTransition.animateFloat(
        initialValue = 25f,
        targetValue = -35f,
        animationSpec = infiniteRepeatable(
            animation = tween(8200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "violet_orb_movement"
    )

    LaunchedEffect(Unit) {
        delay(120)
        contentVisible = true
    }

    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            haptic.performHapticFeedback(
                HapticFeedbackType.LongPress
            )

            listOf(
                -14f,
                14f,
                -10f,
                10f,
                -6f,
                6f,
                0f
            ).forEach { target ->
                shakeOffset.animateTo(
                    targetValue = target,
                    animationSpec = tween(55)
                )
            }
        }
    }

    LaunchedEffect(state.loginSuccessful) {
        if (state.loginSuccessful) {
            haptic.performHapticFeedback(
                HapticFeedbackType.TextHandleMove
            )

            viewModel.consumeLoginSuccess()
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightCanvas)
            .statusBarsPadding()
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(
                    x = 90.dp,
                    y = (-80 + cyanOrbMovement).dp
                )
                .fillMaxWidth(0.82f)
                .height(320.dp)
                .blur(58.dp)
                .background(
                    brush = SECVergeGradients.CyanGlow,
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(
                    x = (-100).dp,
                    y = (90 + violetOrbMovement).dp
                )
                .fillMaxWidth(0.88f)
                .height(360.dp)
                .blur(64.dp)
                .background(
                    brush = SECVergeGradients.VioletGlow,
                    shape = CircleShape
                )
        )

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(
                animationSpec = tween(650)
            ) + slideInVertically(
                animationSpec = spring(),
                initialOffsetY = { it / 5 }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = SECVergeDimens.Space3,
                        vertical = SECVergeDimens.Space4
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SECVerge AI",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayMedium.copy(
                        brush = SECVergeGradients.Signature
                    )
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = "Beyond Intelligence",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(
                        SECVergeDimens.Space4
                    )
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationX = shakeOffset.value
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(
                            SECVergeDimens.Space2
                        )
                    ) {
                        Text(
                            text = "Enter the studio",
                            color = TextPrimary,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Text(
                            text = "Sign in securely to command multiple AI minds from one premium workspace.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

                        PremiumLoginField(
                            value = state.email,
                            placeholder = "Email address",
                            enabled = !state.isLoading,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            onValueChange = viewModel::updateEmail
                        )

                        PremiumLoginField(
                            value = state.password,
                            placeholder = "Password",
                            enabled = !state.isLoading,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            visualTransformation = if (
                                state.passwordVisible
                            ) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingContent = {
                                Text(
                                    text = if (
                                        state.passwordVisible
                                    ) {
                                        "Hide"
                                    } else {
                                        "Show"
                                    },
                                    color = TextSecondary,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .clickable(
                                            interactionSource = remember {
                                                MutableInteractionSource()
                                            },
                                            indication = null,
                                            onClick = viewModel::togglePasswordVisibility
                                        ),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            onValueChange = viewModel::updatePassword
                        )

                        AnimatedVisibility(
                            visible = state.errorMessage != null
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Danger.copy(
                                            alpha = 0.12f
                                        ),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                            SECVergeDimens.RadiusSmall
                                        )
                                    )
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = state.errorMessage.orEmpty(),
                                    color = Danger.copy(
                                        alpha = 0.92f
                                    ),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        GradientButton(
                            text = if (state.isLoading) {
                                "Entering SECVerge AI…"
                            } else {
                                "Enter SECVerge AI"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.canSubmit,
                            onClick = viewModel::login
                        )

                        if (state.isLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .scale(0.72f),
                                    color = TextPrimary,
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        Text(
                            text = "Your token is encrypted on this device. Credentials are never stored.",
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(
                        SECVergeDimens.Space3
                    )
                )

                Text(
                    text = "SECVerge Technologies · Security is everyone’s right",
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
