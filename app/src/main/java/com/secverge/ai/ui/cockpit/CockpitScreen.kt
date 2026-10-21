package com.secverge.ai.ui.cockpit

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.components.GlassCard
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
fun CockpitScreen(
    viewModel: CockpitViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val expandedEntries = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightCanvas)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(SECVergeDimens.Space3),
        verticalArrangement = Arrangement.spacedBy(SECVergeDimens.Space3)
    ) {
        PremiumTopBar(
            subtitle = "Universal Context Framework"
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Unified Context Cockpit",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        brush = SECVergeGradients.Signature
                    )
                )

                Text(
                    text = "Ask with one model, then carry the same context into another model without retyping everything.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )

                UcfModelControls(
                    state = state,
                    onFromModel = viewModel::setFromModel,
                    onToModel = viewModel::setToModel,
                    onSwap = {
                        haptic.performHapticFeedback(
                            HapticFeedbackType.TextHandleMove
                        )
                        viewModel.swapModels()
                    }
                )

                UcfCompactStatus(
                    fromModel = state.fromModel,
                    toModel = state.toModel,
                    isReadyToSwitch = state.consoleEntries.isNotEmpty()
                )
            }
        }

        UcfSearchCard(
            state = state,
            onPromptChange = viewModel::updatePrompt,
            onSend = {
                haptic.performHapticFeedback(
                    HapticFeedbackType.TextHandleMove
                )
                viewModel.send()
            },
            onSwitchAndCarry = {
                haptic.performHapticFeedback(
                    HapticFeedbackType.TextHandleMove
                )
                viewModel.switchAndCarry()
            },
            onHistory = {
                haptic.performHapticFeedback(
                    HapticFeedbackType.TextHandleMove
                )
                viewModel.toggleHistory()
            },
            onNewSession = {
                haptic.performHapticFeedback(
                    HapticFeedbackType.TextHandleMove
                )
                viewModel.startNewSession()
            }
        )

        AnimatedVisibility(
            visible = state.isHistoryVisible
        ) {
            UcfHistoryPanel(
                sessions = state.savedSessions,
                activeSessionId = state.activeSessionId,
                onOpen = {
                    haptic.performHapticFeedback(
                        HapticFeedbackType.TextHandleMove
                    )
                    viewModel.loadSession(it)
                },
                onDelete = {
                    haptic.performHapticFeedback(
                        HapticFeedbackType.TextHandleMove
                    )
                    viewModel.deleteSession(it)
                }
            )
        }

        AnswersConversationPanel(
            state = state,
            expandedEntries = expandedEntries,
            onDismissError = viewModel::dismissError
        )
    }
}

@Composable
private fun UcfModelControls(
    state: CockpitUiState,
    onFromModel: (String) -> Unit,
    onToModel: (String) -> Unit,
    onSwap: () -> Unit
) {
    val options = state.modelOptions.ifEmpty { DefaultCockpitModelOptions }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WebsiteModelDropdown(
            title = "Responding Model",
            selected = state.fromModel,
            options = options,
            disabledOptions = setOf(state.toModel),
            enabled = !state.isLoading,
            onSelect = onFromModel
        )

        WebsiteSecondaryButton(
            text = "⇅ Swap models",
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = onSwap
        )

        WebsiteModelDropdown(
            title = "Switch Target",
            selected = state.toModel,
            options = options,
            disabledOptions = setOf(state.fromModel),
            enabled = !state.isLoading,
            onSelect = onToModel
        )
    }
}

@Composable
private fun UcfCompactStatus(
    fromModel: String,
    toModel: String,
    isReadyToSwitch: Boolean
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevated = false
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Current flow",
                color = TextMuted,
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = "$fromModel → $toModel",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = if (isReadyToSwitch) {
                    "You already have conversation context. You can now use Switch & Carry."
                } else {
                    "Start by asking a question in the Search / Chat box below."
                },
                color = Cyan,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun UcfSearchCard(
    state: CockpitUiState,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
    onSwitchAndCarry: () -> Unit,
    onHistory: () -> Unit,
    onNewSession: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Search / Chat",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Search here or chat here. Ask your question clearly and get the answer below.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Active model: ${state.fromModel}",
                color = Cyan,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )

            UcfPromptField(
                value = state.prompt,
                enabled = !state.isLoading,
                onValueChange = onPromptChange
            )

            if (state.prompt.isBlank()) {
                Text(
                    text = if (state.consoleEntries.isNotEmpty()) {
                        "Prompt is empty. Switch & Carry will use the current visible conversation."
                    } else {
                        "Type your first question here."
                    },
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WebsitePrimaryButton(
                    text = when (state.activeAction) {
                        "send" -> "Getting Answer..."
                        else -> "Get Answer"
                    },
                    enabled = state.canSend,
                    modifier = Modifier.weight(1f),
                    onClick = onSend
                )

                WebsitePrimaryButton(
                    text = when (state.activeAction) {
                        "switch" -> "Carrying..."
                        else -> "Switch & Carry"
                    },
                    enabled = state.canSwitchAndCarry,
                    modifier = Modifier.weight(1f),
                    onClick = onSwitchAndCarry
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WebsiteSecondaryButton(
                    text = if (state.savedSessions.isEmpty()) {
                        "Recents"
                    } else {
                        "Recents ${state.savedSessions.size}"
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoading,
                    onClick = onHistory
                )

                WebsiteSecondaryButton(
                    text = "New UCF Chat",
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoading,
                    onClick = onNewSession
                )
            }
        }
    }
}

@Composable
private fun UcfHistoryPanel(
    sessions: List<CockpitSavedSession>,
    activeSessionId: String?,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Recent UCF Chats",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Open your saved UCF conversations here.",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
            )

            if (sessions.isEmpty()) {
                Text(
                    text = "No recent UCF chats yet.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                sessions.forEach { session ->
                    UcfSessionRow(
                        session = session,
                        isActive = session.localId == activeSessionId,
                        onOpen = { onOpen(session.localId) },
                        onDelete = { onDelete(session.localId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UcfSessionRow(
    session: CockpitSavedSession,
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
                    alpha = if (isActive) 0.76f else 0.46f
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                color = if (isActive) Cyan.copy(alpha = 0.72f) else GlassBorder,
                shape = shape
            )
            .clickable(onClick = onOpen)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = session.title,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = session.preview.ifBlank { "UCF conversation" },
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${session.fromModel} → ${session.toModel} • ${formatUcfTimestamp(session.updatedAtMillis)}",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Open",
            color = Cyan,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable(onClick = onOpen)
                .padding(6.dp)
        )

        Text(
            text = "Delete",
            color = TextMuted,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .clickable(onClick = onDelete)
                .padding(6.dp)
        )
    }
}

@Composable
private fun AnswersConversationPanel(
    state: CockpitUiState,
    expandedEntries: MutableMap<String, Boolean>,
    onDismissError: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 240.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Answers & Conversation",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Your questions and model answers will appear here clearly.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            AnimatedVisibility(
                visible = state.errorMessage != null
            ) {
                PremiumErrorCard(
                    title = "UCF operation interrupted",
                    message = state.errorMessage.orEmpty(),
                    dismissLabel = "Dismiss",
                    onDismiss = onDismissError
                )
            }

            if (state.consoleEntries.isEmpty()) {
                Text(
                    text = "No conversation yet.",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Use the Search / Chat box above to ask your first question.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                state.consoleEntries.forEach { entry ->
                    ConversationEntryCard(
                        entry = entry,
                        expanded = expandedEntries[entry.id] == true,
                        onToggleProof = {
                            expandedEntries[entry.id] = expandedEntries[entry.id] != true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationEntryCard(
    entry: CockpitConsoleEntry,
    expanded: Boolean,
    onToggleProof: () -> Unit
) {
    val isUser = entry.kind == "user"
    val isSystem = entry.kind == "system"

    val shape = RoundedCornerShape(22.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when {
                    isUser -> MidnightSurfaceHigh.copy(alpha = 0.70f)
                    isSystem -> MidnightSurfaceHigh.copy(alpha = 0.58f)
                    else -> GlassSurface.copy(alpha = 0.88f)
                },
                shape = shape
            )
            .border(
                width = 1.dp,
                color = when {
                    isUser -> Cyan.copy(alpha = 0.55f)
                    isSystem -> Violet.copy(alpha = 0.45f)
                    else -> Cyan.copy(alpha = 0.25f)
                },
                shape = shape
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (isUser) "Your Question" else "Answer / Result",
            color = if (isUser) Cyan else TextPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = entry.title,
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = entry.content,
            color = if (isUser) TextSecondary else TextPrimary,
            style = if (isUser) {
                MaterialTheme.typography.bodyLarge
            } else {
                MaterialTheme.typography.bodyLarge
            }
        )

        entry.technicalProof?.let {
            Text(
                text = if (expanded) {
                    "Hide technical details"
                } else {
                    "Show technical details"
                },
                color = Cyan,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onToggleProof)
            )

            AnimatedVisibility(visible = expanded) {
                Text(
                    text = it,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun WebsiteModelDropdown(
    title: String,
    selected: String,
    options: List<String>,
    disabledOptions: Set<String>,
    enabled: Boolean,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = TextSecondary,
            style = MaterialTheme.typography.labelLarge
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = GlassSurface.copy(alpha = 0.86f),
                        shape = RoundedCornerShape(SECVergeDimens.RadiusSmall)
                    )
                    .border(
                        width = 1.dp,
                        color = if (expanded) Cyan else GlassBorder,
                        shape = RoundedCornerShape(SECVergeDimens.RadiusSmall)
                    )
                    .clickable(enabled = enabled) { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 13.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selected,
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = if (expanded) "▲" else "▼",
                        color = Cyan,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    val disabled = disabledOptions.contains(option)

                    DropdownMenuItem(
                        enabled = enabled && !disabled,
                        text = {
                            Text(
                                text = if (disabled) "$option · selected" else option,
                                color = if (disabled) TextMuted else TextPrimary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            expanded = false
                            onSelect(option)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UcfPromptField(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
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
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 84.dp)
                    .background(
                        color = GlassSurface.copy(alpha = 0.82f),
                        shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
                    )
                    .border(
                        width = 1.5.dp,
                        color = Cyan.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
                    )
                    .padding(18.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "Search here or chat here…",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                innerTextField()
            }
        }
    )
}

@Composable
private fun WebsitePrimaryButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .heightIn(min = 56.dp)
            .background(
                brush = SECVergeGradients.Signature,
                shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
            )
            .border(
                width = 1.dp,
                color = if (enabled) Cyan.copy(alpha = 0.35f) else GlassBorder,
                shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 10.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) TextPrimary else TextMuted,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WebsiteSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .background(
                color = GlassSurface.copy(alpha = 0.86f),
                shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
            )
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 10.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) TextPrimary else TextMuted,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatUcfTimestamp(
    updatedAtMillis: Long
): String {
    return DateFormat.format(
        "dd MMM, h:mm a",
        updatedAtMillis
    ).toString()
}
