package com.secverge.ai.ui.askhachy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.components.ChatBubble
import com.secverge.ai.ui.components.EmptyState
import com.secverge.ai.ui.components.GlassCard
import com.secverge.ai.ui.components.GradientButton
import com.secverge.ai.ui.components.PremiumTopBar
import com.secverge.ai.ui.components.PremiumErrorCard
import com.secverge.ai.ui.theme.Cyan
import com.secverge.ai.ui.theme.GlassBorder
import com.secverge.ai.ui.theme.GlassSurface
import com.secverge.ai.ui.theme.MidnightCanvas
import com.secverge.ai.ui.theme.SECVergeDimens
import com.secverge.ai.ui.theme.SECVergeGradients
import com.secverge.ai.ui.theme.TextMuted
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.TextSecondary

@Composable
fun AskHachyScreen(
    viewModel: AskHachyViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(
        state.messages.size,
        state.isThinking
    ) {
        val target = state.messages.size +
            if (state.isThinking) 1 else 0

        if (target > 0) {
            listState.animateScrollToItem(target - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightCanvas)
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-70).dp)
                .fillMaxWidth(0.78f)
                .height(300.dp)
                .blur(60.dp)
                .background(
                    brush = SECVergeGradients.CyanGlow,
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = SECVergeDimens.Space3,
                    vertical = SECVergeDimens.Space2
                )
        ) {
            PremiumTopBar(
                subtitle = "Local-first Gemma companion"
            )

            Spacer(
                modifier = Modifier.height(
                    SECVergeDimens.Space2
                )
            )

            if (
                state.messages.isEmpty() &&
                !state.isThinking
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        title = "Ask Hachy",
                        message = "Ask me anything — private, focused, and fast."
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        items = state.messages,
                        key = { it.id }
                    ) { message ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() +
                                slideInVertically(
                                    initialOffsetY = { it / 4 }
                                )
                        ) {
                            ChatBubble(
                                text = message.text,
                                isUser = message.isUser,
                                modifier = Modifier.align(
                                    if (message.isUser) {
                                        Alignment.End
                                    } else {
                                        Alignment.Start
                                    }
                                )
                            )
                        }
                    }

                    if (state.isThinking) {
                        item(key = "thinking") {
                            HachyThinkingIndicator()
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = state.errorMessage != null
            ) {
                PremiumErrorCard(
                    title = "Hachy could not respond",
                    message = state.errorMessage.orEmpty(),
                    modifier = Modifier.padding(bottom = 12.dp),
                    retryLabel = "Try again",
                    dismissLabel = "Dismiss",
                    onRetry = viewModel::retry,
                    onDismiss = viewModel::retry
                )
            }

            HachyInputBar(
                value = state.prompt,
                enabled = !state.isThinking,
                canSend = state.canSend,
                onValueChange = viewModel::updatePrompt,
                onSend = {
                    haptic.performHapticFeedback(
                        HapticFeedbackType.TextHandleMove
                    )
                    viewModel.send()
                }
            )
        }
    }
}

@Composable
private fun HachyInputBar(
    value: String,
    enabled: Boolean,
    canSend: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimary
                ),
                cursorBrush = SolidColor(Cyan),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (canSend) {
                            onSend()
                        }
                    }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = GlassSurface.copy(
                                    alpha = 0.72f
                                ),
                                shape = RoundedCornerShape(
                                    SECVergeDimens.RadiusMedium
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = GlassBorder,
                                shape = RoundedCornerShape(
                                    SECVergeDimens.RadiusMedium
                                )
                            )
                            .padding(16.dp)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = "Message Hachy…",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        innerTextField()
                    }
                }
            )

            GradientButton(
                text = if (enabled) {
                    "Send to Hachy"
                } else {
                    "Hachy is thinking…"
                },
                enabled = canSend,
                modifier = Modifier.fillMaxWidth(),
                onClick = onSend
            )
        }
    }
}

@Composable
private fun HachyThinkingIndicator() {
    val transition = rememberInfiniteTransition(
        label = "hachy_thinking"
    )

    val alpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(620),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinking_alpha"
    )

    Row(
        modifier = Modifier
            .background(
                color = GlassSurface,
                shape = RoundedCornerShape(22.dp)
            )
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(
                horizontal = 18.dp,
                vertical = 14.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = Cyan.copy(
                            alpha = (alpha - index * 0.16f)
                                .coerceIn(0.18f, 1f)
                        ),
                        shape = CircleShape
                    )
            )
        }

        Text(
            text = "Hachy is thinking",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
