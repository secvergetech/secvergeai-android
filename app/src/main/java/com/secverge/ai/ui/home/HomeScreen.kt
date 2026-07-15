package com.secverge.ai.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.secverge.ai.navigation.SECVergeRoute
import com.secverge.ai.ui.components.GlassCard
import com.secverge.ai.ui.theme.Cobalt
import com.secverge.ai.ui.theme.Cyan
import com.secverge.ai.ui.theme.Indigo
import com.secverge.ai.ui.theme.MidnightCanvas
import com.secverge.ai.ui.theme.SECVergeDimens
import com.secverge.ai.ui.theme.SECVergeGradients
import com.secverge.ai.ui.theme.TextMuted
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.TextSecondary
import com.secverge.ai.ui.theme.Violet
import kotlinx.coroutines.delay

private data class HomeFeature(
    val route: SECVergeRoute,
    val symbol: String,
    val title: String,
    val description: String,
    val badge: String,
    val accent: Brush
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenFeature: (SECVergeRoute) -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    val visibleItems = remember {
        mutableStateListOf<Int>()
    }

    val infiniteTransition = rememberInfiniteTransition(
        label = "home_background_motion"
    )

    val orbMovement by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 34f,
        animationSpec = infiniteRepeatable(
            animation = tween(7600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "home_orb_movement"
    )

    val features = remember {
        listOf(
            HomeFeature(
                route = SECVergeRoute.AskHachy,
                symbol = "H",
                title = "Ask Hachy",
                description = "Your private, fast Gemma companion for focused answers.",
                badge = "Local-first",
                accent = Brush.linearGradient(
                    listOf(Cyan, Cobalt)
                )
            ),
            HomeFeature(
                route = SECVergeRoute.Cockpit,
                symbol = "U",
                title = "Multi-LLM Cockpit",
                description = "Carry context seamlessly across models with UCF.",
                badge = "Context portability",
                accent = Brush.linearGradient(
                    listOf(Cobalt, Indigo)
                )
            ),
            HomeFeature(
                route = SECVergeRoute.Council,
                symbol = "C",
                title = "AI Council",
                description = "Multiple AI minds debate while Gemma moderates.",
                badge = "Collective intelligence",
                accent = Brush.linearGradient(
                    listOf(Indigo, Violet)
                )
            ),
            HomeFeature(
                route = SECVergeRoute.HumanCouncil,
                symbol = "+",
                title = "Multi-LLM + Human",
                description = "Collaborate with models and direct individual participants.",
                badge = "Human in control",
                accent = Brush.linearGradient(
                    listOf(Violet, Color(0xFFC084FC))
                )
            ),
            HomeFeature(
                route = SECVergeRoute.Delphi,
                symbol = "D",
                title = "Delphi AI",
                description = "Blind, reveal, debate, and converge with cinematic clarity.",
                badge = "Showpiece",
                accent = SECVergeGradients.Signature
            )
        )
    }

    LaunchedEffect(state.isReady) {
        if (state.isReady) {
            visibleItems.clear()

            repeat(features.size + 2) { index ->
                delay(if (index == 0) 100 else 85)
                visibleItems.add(index)
            }
        }
    }

    LaunchedEffect(state.logoutCompleted) {
        if (state.logoutCompleted) {
            viewModel.consumeLogout()
            onLogout()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightCanvas)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(
                    x = 100.dp,
                    y = (-80 + orbMovement).dp
                )
                .fillMaxWidth(0.82f)
                .height(330.dp)
                .blur(62.dp)
                .background(
                    brush = SECVergeGradients.CyanGlow,
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-130).dp)
                .fillMaxWidth(0.72f)
                .height(320.dp)
                .blur(68.dp)
                .background(
                    brush = SECVergeGradients.VioletGlow,
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = SECVergeDimens.Space3,
                    vertical = SECVergeDimens.Space3
                ),
            verticalArrangement = Arrangement.spacedBy(
                SECVergeDimens.Space3
            )
        ) {
            AnimatedVisibility(
                visible = visibleItems.contains(0),
                enter = fadeIn(tween(500)) +
                    slideInVertically(
                        tween(500),
                        initialOffsetY = { it / 3 }
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SECVerge AI",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                brush = SECVergeGradients.Signature
                            )
                        )

                        Text(
                            text = "Beyond Intelligence",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = "Logout",
                        color = TextSecondary,
                        modifier = Modifier.clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.LongPress
                            )
                            viewModel.logout()
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            AnimatedVisibility(
                visible = visibleItems.contains(1),
                enter = fadeIn(tween(520)) +
                    slideInVertically(
                        tween(520),
                        initialOffsetY = { it / 3 }
                    )
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Welcome, ${state.userName}",
                            color = TextPrimary,
                            style = MaterialTheme.typography.headlineLarge
                        )

                        Text(
                            text = "The AI studio that never stops at one.",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                brush = SECVergeGradients.Signature
                            )
                        )

                        Text(
                            text = "Command models, carry context, conduct debates, and converge on stronger decisions—all from one premium workspace.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HomeStatusPill(
                                text = "5 studios"
                            )
                            HomeStatusPill(
                                text = "Secure session"
                            )
                        }
                    }
                }
            }

            features.forEachIndexed { index, feature ->
                AnimatedVisibility(
                    visible = visibleItems.contains(index + 2),
                    enter = fadeIn(
                        animationSpec = tween(500)
                    ) + slideInVertically(
                        animationSpec = tween(500),
                        initialOffsetY = { it / 3 }
                    )
                ) {
                    HomeFeatureCard(
                        symbol = feature.symbol,
                        title = feature.title,
                        description = feature.description,
                        badge = feature.badge,
                        accent = feature.accent,
                        onClick = {
                            onOpenFeature(feature.route)
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Security is not an option, it’s everyone’s right.",
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )
        }
    }
}

@Composable
private fun HomeStatusPill(
    text: String
) {
    Box(
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.06f),
                shape = CircleShape
            )
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
    ) {
        Text(
            text = text,
            color = TextSecondary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
