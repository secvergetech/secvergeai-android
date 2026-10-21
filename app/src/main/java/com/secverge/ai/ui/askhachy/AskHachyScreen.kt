package com.secverge.ai.ui.askhachy

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.components.ChatBubble
import com.secverge.ai.ui.components.EmptyState
import com.secverge.ai.ui.components.GlassCard
import com.secverge.ai.ui.components.GradientButton
import com.secverge.ai.ui.components.PremiumErrorCard
import com.secverge.ai.ui.components.PremiumTopBar
import com.secverge.ai.ui.theme.Cyan
import com.secverge.ai.ui.theme.GlassBorder
import com.secverge.ai.ui.theme.GlassSurface
import com.secverge.ai.ui.theme.MidnightCanvas
import com.secverge.ai.ui.theme.MidnightSurfaceHigh
import com.secverge.ai.ui.theme.SECVergeDimens
import com.secverge.ai.ui.theme.SECVergeGradients
import com.secverge.ai.ui.theme.TextMuted
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.TextSecondary
import com.secverge.ai.ui.theme.Violet

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

            HachyQuickActions(
                savedCount = state.savedConversations.size,
                historyVisible = state.isHistoryVisible,
                enabled = !state.isThinking,
                onHistoryClick = {
                    haptic.performHapticFeedback(
                        HapticFeedbackType.TextHandleMove
                    )
                    viewModel.toggleHistory()
                },
                onNewChatClick = {
                    haptic.performHapticFeedback(
                        HapticFeedbackType.TextHandleMove
                    )
                    viewModel.startNewChat()
                }
            )

            AnimatedVisibility(
                visible = state.isHistoryVisible
            ) {
                HachyHistoryPanel(
                    conversations = state.savedConversations,
                    activeHistoryId = state.activeHistoryId,
                    onOpen = {
                        haptic.performHapticFeedback(
                            HapticFeedbackType.TextHandleMove
                        )
                        viewModel.loadConversation(it)
                    },
                    onDelete = {
                        haptic.performHapticFeedback(
                            HapticFeedbackType.TextHandleMove
                        )
                        viewModel.deleteConversation(it)
                    }
                )
            }

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
private fun HachyQuickActions(
    savedCount: Int,
    historyVisible: Boolean,
    enabled: Boolean,
    onHistoryClick: () -> Unit,
    onNewChatClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 4.dp,
                vertical = 10.dp
            ),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HachyPillButton(
            label = if (savedCount > 0) {
                "Recents $savedCount"
            } else {
                "Recents"
            },
            contentDescription = if (historyVisible) {
                "Hide Ask Hachy recent chats"
            } else {
                "Show Ask Hachy recent chats"
            },
            enabled = enabled,
            accentColor = Cyan,
            onClick = onHistoryClick
        )

        Spacer(modifier = Modifier.width(10.dp))

        HachyPillButton(
            label = "＋ New chat",
            contentDescription = "Start a new Ask Hachy chat",
            enabled = enabled,
            accentColor = Violet,
            onClick = onNewChatClick
        )
    }
}

@Composable
private fun HachyHistoryPanel(
    conversations: List<AskHachySavedConversation>,
    activeHistoryId: String?,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        cornerRadius = 24.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Recent chats",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Saved privately on this device",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = "${conversations.size}/50",
                    color = Cyan,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (conversations.isEmpty()) {
                Text(
                    text = "No saved chats yet. Send a message, then tap New chat — your previous conversation will appear here.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 240.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = conversations,
                        key = { it.localId }
                    ) { conversation ->
                        HachyHistoryRow(
                            conversation = conversation,
                            isActive =
                                conversation.localId ==
                                    activeHistoryId,
                            onOpen = {
                                onOpen(conversation.localId)
                            },
                            onDelete = {
                                onDelete(conversation.localId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HachyHistoryRow(
    conversation: AskHachySavedConversation,
    isActive: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MidnightSurfaceHigh.copy(
                    alpha = if (isActive) 0.74f else 0.44f
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                color = if (isActive) {
                    Cyan.copy(alpha = 0.70f)
                } else {
                    GlassBorder.copy(alpha = 0.80f)
                },
                shape = shape
            )
            .clickable(onClick = onOpen)
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = conversation.title,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = conversation.preview.ifBlank {
                    "Ask Hachy conversation"
                },
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${conversation.messageCount} messages • ${
                    formatHistoryTimestamp(
                        conversation.updatedAtMillis
                    )
                }",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Open",
            color = Cyan,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .semantics {
                    contentDescription =
                        "Open saved Ask Hachy chat"
                }
                .clickable(onClick = onOpen)
                .padding(6.dp)
        )

        Spacer(modifier = Modifier.width(2.dp))

        Text(
            text = "Delete",
            color = TextMuted,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .semantics {
                    contentDescription =
                        "Delete saved Ask Hachy chat"
                }
                .clickable(onClick = onDelete)
                .padding(6.dp)
        )
    }
}

@Composable
private fun HachyPillButton(
    label: String,
    contentDescription: String,
    enabled: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(44.dp)
            .semantics {
                this.contentDescription =
                    contentDescription
            },
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(
                alpha = if (enabled) 0.64f else 0.24f
            )
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = accentColor.copy(
                alpha = if (enabled) 0.14f else 0.06f
            ),
            contentColor = TextPrimary,
            disabledContainerColor =
                accentColor.copy(alpha = 0.05f),
            disabledContentColor =
                TextMuted.copy(alpha = 0.70f)
        )
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold
        )
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
                                style =
                                    MaterialTheme.typography.bodyLarge
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

private fun formatHistoryTimestamp(
    updatedAtMillis: Long
): String {
    return DateFormat.format(
        "dd MMM, h:mm a",
        updatedAtMillis
    ).toString()
}
