package com.secverge.ai.ui.cockpit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.components.GlassCard
import com.secverge.ai.ui.components.GradientButton
import com.secverge.ai.ui.components.PremiumErrorCard
import com.secverge.ai.ui.components.PremiumTopBar
import com.secverge.ai.ui.shell.premiumPressClickable
import com.secverge.ai.ui.theme.Cyan
import com.secverge.ai.ui.theme.GlassBorder
import com.secverge.ai.ui.theme.GlassSurface
import com.secverge.ai.ui.theme.MidnightCanvas
import com.secverge.ai.ui.theme.SECVergeDimens
import com.secverge.ai.ui.theme.SECVergeGradients
import com.secverge.ai.ui.theme.TextMuted
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.TextSecondary

private val WebsiteUcfModels = listOf(
    "Ask Hachy / Gemma",
    "GPT",
    "Claude",
    "Gemini"
)

@Composable
fun CockpitScreen(
    viewModel: CockpitViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val expandedEntries = remember { mutableStateMapOf<Int, Boolean>() }
    var showHistory by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightCanvas)
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Unified Context Cockpit",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                brush = SECVergeGradients.Signature
                            )
                        )

                        Text(
                            text = "Chat normally, or use Switch & Carry to move the same context into another AI model without re-explaining.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                WebsiteModelDropdown(
                    title = "Responding Model",
                    selected = state.fromModel,
                    options = WebsiteUcfModels,
                    disabledOptions = setOf(state.toModel),
                    enabled = !state.isLoading,
                    onSelect = viewModel::setFromModel
                )

                WebsiteModelDropdown(
                    title = "Switch Target",
                    selected = state.toModel,
                    options = WebsiteUcfModels,
                    disabledOptions = setOf(state.fromModel),
                    enabled = !state.isLoading,
                    onSelect = viewModel::setToModel
                )
            }
        }

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 250.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.consoleEntries.isEmpty()) {
                    Text(
                        text = "Start with Gemma. Switch when needed.",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Use Send for normal chat. Use Switch & Carry when you want SECVerge AI to build context, carry context, and run the target model in one flow.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                AnimatedVisibility(visible = state.errorMessage != null) {
                    PremiumErrorCard(
                        title = "UCF operation interrupted",
                        message = state.errorMessage.orEmpty(),
                        dismissLabel = "Dismiss",
                        onDismiss = viewModel::dismissError
                    )
                }

                AnimatedVisibility(
                    visible = showHistory && state.consoleEntries.isNotEmpty()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.consoleEntries.forEachIndexed { index, entry ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(),
                                elevated = false
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = entry.title,
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = entry.content,
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    if (entry.technicalProof != null) {
                                        Text(
                                            text = if (expandedEntries[index] == true) {
                                                "Hide technical proof"
                                            } else {
                                                "Show technical proof"
                                            },
                                            color = Cyan,
                                            modifier = Modifier.clickable {
                                                expandedEntries[index] =
                                                    expandedEntries[index] != true
                                            },
                                            style = MaterialTheme.typography.labelLarge
                                        )

                                        AnimatedVisibility(
                                            visible = expandedEntries[index] == true
                                        ) {
                                            Text(
                                                text = entry.technicalProof,
                                                color = TextMuted,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CockpitPromptField(
                    value = state.prompt,
                    enabled = !state.isLoading,
                    onValueChange = viewModel::updatePrompt
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GradientButton(
                        text = "Send",
                        enabled = state.canSend,
                        loading = state.isLoading,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            horizontal = 10.dp,
                            vertical = 13.dp
                        ),
                        onClick = viewModel::send
                    )

                    GradientButton(
                        text = "Switch & Carry",
                        enabled = state.canSwitchAndCarry,
                        loading = state.isLoading,
                        modifier = Modifier.weight(1.35f),
                        contentPadding = PaddingValues(
                            horizontal = 10.dp,
                            vertical = 13.dp
                        ),
                        onClick = viewModel::switchAndCarry
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WebsiteSecondaryButton(
                        text = "Advanced",
                        modifier = Modifier.weight(1f),
                        onClick = viewModel::showAdvanced
                    )

                    WebsiteSecondaryButton(
                        text = "History",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showHistory = !showHistory
                        }
                    )

                    WebsiteSecondaryButton(
                        text = "Download PDF",
                        modifier = Modifier.weight(1.2f),
                        onClick = viewModel::showPdfStatus
                    )
                }
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

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
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
                    .premiumPressClickable(enabled = enabled) {
                        expanded = true
                    }
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
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(GlassSurface)
            ) {
                options.forEach { option ->
                    val disabled = disabledOptions.contains(option)

                    DropdownMenuItem(
                        enabled = enabled && !disabled,
                        text = {
                            Text(
                                text = if (disabled) {
                                    "$option · selected"
                                } else {
                                    option
                                },
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
private fun CockpitPromptField(
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
                    .heightIn(min = 74.dp)
                    .background(
                        color = GlassSurface.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
                    )
                    .border(
                        width = 1.dp,
                        color = GlassBorder,
                        shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
                    )
                    .padding(18.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "Ask SECVerge AI anything...",
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
private fun WebsiteSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
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
            .premiumPressClickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TextPrimary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
