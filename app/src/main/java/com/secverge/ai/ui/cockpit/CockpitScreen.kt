package com.secverge.ai.ui.cockpit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
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
import com.secverge.ai.ui.components.ModelChip
import com.secverge.ai.ui.components.ModelStatus
import com.secverge.ai.ui.components.PremiumErrorCard
import com.secverge.ai.ui.components.PremiumTopBar
import com.secverge.ai.ui.theme.Cyan
import com.secverge.ai.ui.theme.GlassBorder
import com.secverge.ai.ui.theme.GlassSurface
import com.secverge.ai.ui.theme.MidnightCanvas
import com.secverge.ai.ui.theme.SECVergeDimens
import com.secverge.ai.ui.theme.SECVergeGradients
import com.secverge.ai.ui.theme.TextMuted
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.TextSecondary

private val CockpitModelOptions = listOf(
    "Ask Hachy",
    "GPT",
    "Claude",
    "Gemini"
)

@Composable
fun CockpitScreen(
    viewModel: CockpitViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val expandedEntries = remember {
        mutableStateMapOf<Int, Boolean>()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightCanvas)
            .verticalScroll(rememberScrollState())
            .padding(SECVergeDimens.Space3),
        verticalArrangement = Arrangement.spacedBy(
            SECVergeDimens.Space3
        )
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
                Text(
                    text = "Context Carry Forward",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        brush = SECVergeGradients.Signature
                    )
                )

                Text(
                    text = "Build context, route intelligently, switch models, and continue without starting over.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModelChip("Hachy", ModelStatus.Local)
                    ModelChip("GPT", ModelStatus.Verified)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModelChip("Claude", ModelStatus.Configured)
                    ModelChip("Gemini", ModelStatus.NeedsAttention)
                }
            }
        }

        CockpitStepRail(
            currentStep = state.currentStep
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Model Routing",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Choose the responding model and the switch target, matching the web UCF cockpit dropdown behavior.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )

                ModelDropdownField(
                    title = "Responding model",
                    selected = state.fromModel,
                    options = CockpitModelOptions,
                    enabled = !state.isLoading,
                    disabledOptions = setOf(state.toModel),
                    onSelect = viewModel::setFromModel
                )

                ModelDropdownField(
                    title = "Switch target",
                    selected = state.toModel,
                    options = CockpitModelOptions,
                    enabled = !state.isLoading,
                    disabledOptions = setOf(state.fromModel),
                    onSelect = viewModel::setToModel
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = GlassSurface.copy(alpha = 0.72f),
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
                        .padding(14.dp)
                ) {
                    Text(
                        text = "From: ${state.fromModel}  →  To: ${state.toModel}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                SegmentSelector(
                    title = "Task type",
                    options = listOf("General", "Code", "Research"),
                    selected = state.taskType,
                    onSelect = viewModel::setTaskType
                )

                SegmentSelector(
                    title = "Privacy",
                    options = listOf("Standard", "Sensitive", "Local"),
                    selected = state.privacy,
                    onSelect = viewModel::setPrivacy
                )

                CockpitPromptField(
                    value = state.prompt,
                    enabled = !state.isLoading,
                    onValueChange = viewModel::updatePrompt
                )
            }
        }

        state.fidelityScore?.let {
            FidelityCard(score = it)
        }

        AnimatedVisibility(
            visible = state.errorMessage != null
        ) {
            PremiumErrorCard(
                title = "Cockpit operation interrupted",
                message = state.errorMessage.orEmpty(),
                dismissLabel = "Dismiss",
                onDismiss = viewModel::dismissError
            )
        }

        GradientButton(
            text = if (state.isLoading) {
                "Working…"
            } else {
                "1 · Build Context"
            },
            enabled = state.canBuildContext,
            modifier = Modifier.fillMaxWidth(),
            onClick = viewModel::buildContext
        )

        GradientButton(
            text = "2 · Recommend Model",
            enabled = state.canRoute,
            modifier = Modifier.fillMaxWidth(),
            onClick = viewModel::recommendRoute
        )

        GradientButton(
            text = "3 · Switch & Carry",
            enabled = state.canSwitch,
            modifier = Modifier.fillMaxWidth(),
            onClick = viewModel::switchAndCarry
        )

        GradientButton(
            text = "4 · Run",
            enabled = state.canExecute,
            modifier = Modifier.fillMaxWidth(),
            onClick = viewModel::execute
        )

        state.consoleEntries.forEachIndexed { index, entry ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = entry.title,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = entry.content,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (entry.technicalProof != null) {
                        Text(
                            text = if (
                                expandedEntries[index] == true
                            ) {
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
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelDropdownField(
    title: String,
    selected: String,
    options: List<String>,
    enabled: Boolean,
    disabledOptions: Set<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        color = GlassSurface.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(
                            SECVergeDimens.RadiusSmall
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (expanded) {
                            Cyan
                        } else {
                            GlassBorder
                        },
                        shape = RoundedCornerShape(
                            SECVergeDimens.RadiusSmall
                        )
                    )
                    .clickable(enabled = enabled) {
                        expanded = true
                    }
                    .padding(
                        horizontal = 14.dp,
                        vertical = 13.dp
                    )
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
                        text = if (expanded) {
                            "▲"
                        } else {
                            "▼"
                        },
                        color = Cyan,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                modifier = Modifier.background(
                    GlassSurface
                )
            ) {
                options.forEach { option ->
                    val disabled = disabledOptions.contains(option)

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (disabled) {
                                    "$option · already selected"
                                } else {
                                    option
                                },
                                color = if (disabled) {
                                    TextMuted
                                } else {
                                    TextPrimary
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        enabled = enabled && !disabled,
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
private fun CockpitStepRail(
    currentStep: CockpitStep
) {
    val steps = CockpitStep.entries

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEach { step ->
                val active = step.ordinal <= currentStep.ordinal

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (active) {
                                    Cyan.copy(alpha = 0.18f)
                                } else {
                                    GlassSurface
                                },
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (active) {
                                    Cyan
                                } else {
                                    GlassBorder
                                },
                                shape = CircleShape
                            )
                            .padding(
                                horizontal = 11.dp,
                                vertical = 7.dp
                            )
                    ) {
                        Text(
                            text = (step.ordinal + 1).toString(),
                            color = if (active) {
                                TextPrimary
                            } else {
                                TextMuted
                            }
                        )
                    }

                    Text(
                        text = step.name,
                        color = if (active) {
                            TextPrimary
                        } else {
                            TextMuted
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentSelector(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            color = TextSecondary,
            style = MaterialTheme.typography.labelLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val active = option == selected

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (active) {
                                Cyan.copy(alpha = 0.18f)
                            } else {
                                GlassSurface
                            },
                            shape = RoundedCornerShape(
                                SECVergeDimens.RadiusSmall
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = if (active) {
                                Cyan
                            } else {
                                GlassBorder
                            },
                            shape = RoundedCornerShape(
                                SECVergeDimens.RadiusSmall
                            )
                        )
                        .clickable {
                            onSelect(option)
                        }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (active) {
                            TextPrimary
                        } else {
                            TextMuted
                        },
                        style = MaterialTheme.typography.bodyMedium
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
                    .background(
                        color = GlassSurface.copy(alpha = 0.78f),
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
                    .padding(18.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "Describe the task and context to carry…",
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
private fun FidelityCard(
    score: Double
) {
    val normalized = if (score > 1.0) {
        score / 100.0
    } else {
        score
    }.coerceIn(0.0, 1.0)

    val animated by animateFloatAsState(
        targetValue = normalized.toFloat(),
        label = "fidelity_progress"
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Context fidelity",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge
            )

            LinearProgressIndicator(
                progress = { animated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Cyan,
                trackColor = GlassSurface
            )

            Text(
                text = "${(normalized * 100).toInt()}%",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
