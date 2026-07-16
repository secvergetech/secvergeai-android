package com.secverge.ai.ui.humancouncil

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.components.GlassCard
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HumanCouncilScreen(
    viewModel: HumanCouncilViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightCanvas)
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(SECVergeDimens.Space3),
        verticalArrangement = Arrangement.spacedBy(SECVergeDimens.Space3)
    ) {
        PremiumTopBar(
            subtitle = "Multi-LLM + Human · You are the Gatekeeper"
        )

        HumanHeroCard()

        HumanStepRail(state.stage)

        HumanControlPanel(
            state = state,
            onObjectiveChange = viewModel::updateObjective,
            onInstructionChange = viewModel::updateInstruction,
            onToggleParticipant = viewModel::toggleParticipant,
            onDirectModel = viewModel::setDirectModel,
            onTestRuntime = viewModel::testRuntime,
            onStartRoom = viewModel::startRoom,
            onAskModels = viewModel::askModels,
            onDirect = viewModel::directModel,
            onMemo = viewModel::buildDecisionMemo,
            onExportPdf = {
                val result = runCatching {
                    exportHumanPdf(
                        context = context,
                        state = state
                    )
                }.fold(
                    onSuccess = { it },
                    onFailure = {
                        "PDF export failed: ${it.message ?: "Unknown error"}"
                    }
                )

                viewModel.markPdfExported(result)

                Toast.makeText(
                    context,
                    result,
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        HumanWorkspace(state)

        state.errorMessage?.let { message ->
            PremiumErrorCard(
                title = "Human decision room interrupted",
                message = message,
                dismissLabel = "Dismiss",
                onDismiss = viewModel::dismissError
            )
        }
    }
}

@Composable
private fun HumanHeroCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SECVERGE AI · HUMAN DECISION ROOM",
                color = Cyan,
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = "Human judgment. Multi-model intelligence. One final decision.",
                style = MaterialTheme.typography.headlineLarge.copy(
                    brush = SECVergeGradients.Signature
                )
            )

            Text(
                text = "A clean executive workroom where the human chair frames the decision, directs specific LLMs, compares viewpoints, and turns the discussion into a decision memo.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun HumanStepRail(activeStage: HumanDecisionStage) {
    HumanDecisionStage.entries.chunked(2).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row.forEach { stage ->
                val active = stage.ordinal <= activeStage.ordinal

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (active) {
                                Cyan.copy(alpha = 0.14f)
                            } else {
                                GlassSurface.copy(alpha = 0.70f)
                            },
                            shape = RoundedCornerShape(18.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (active) Cyan.copy(alpha = 0.55f) else GlassBorder,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = stage.label,
                            color = if (active) TextPrimary else TextMuted,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = stage.subtitle,
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HumanControlPanel(
    state: HumanCouncilUiState,
    onObjectiveChange: (String) -> Unit,
    onInstructionChange: (String) -> Unit,
    onToggleParticipant: (String) -> Unit,
    onDirectModel: (String) -> Unit,
    onTestRuntime: () -> Unit,
    onStartRoom: () -> Unit,
    onAskModels: () -> Unit,
    onDirect: () -> Unit,
    onMemo: () -> Unit,
    onExportPdf: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Control Panel",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Keep it simple: define the objective, add your human view, then ask all models or direct one model.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            HumanTextArea(
                title = "Decision objective",
                value = state.objective,
                placeholder = "Example: Should SECVerge AI position Delphi as a premium boardroom decision feature?",
                enabled = !state.isLoading,
                onValueChange = onObjectiveChange
            )

            HumanTextArea(
                title = "Your human instruction",
                value = state.instruction,
                placeholder = "Add your view, constraints, objection, or instruction.",
                enabled = !state.isLoading,
                onValueChange = onInstructionChange
            )

            Text(
                text = "Participants",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )

            state.participants.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { participant ->
                        HumanParticipantButton(
                            name = participant.name,
                            selected = participant.selected,
                            enabled = !state.isLoading,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onToggleParticipant(participant.name)
                            }
                        )
                    }

                    if (row.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }

            Text(
                text = "Direct one model",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )

            HumanDropdown(
                selected = state.directModel,
                options = state.directModelOptions,
                enabled = !state.isLoading,
                onSelect = onDirectModel
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HumanActionButton(
                    text = if (state.activeAction == "test") "Testing…" else "Test Runtime",
                    enabled = state.canTestRuntime,
                    modifier = Modifier.weight(1f),
                    primary = true,
                    onClick = onTestRuntime
                )

                HumanActionButton(
                    text = if (state.activeAction == "start") {
                        "Starting…"
                    } else if (state.roomStarted) {
                        "Restart Room"
                    } else {
                        "Start Room"
                    },
                    enabled = state.canStartRoom,
                    modifier = Modifier.weight(1f),
                    primary = true,
                    onClick = onStartRoom
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HumanActionButton(
                    text = if (state.activeAction == "ask") "Asking…" else "Ask Models",
                    enabled = state.canAskModels,
                    modifier = Modifier.weight(1f),
                    primary = false,
                    onClick = onAskModels
                )

                HumanActionButton(
                    text = if (state.activeAction == "direct") "Directing…" else "Direct Model",
                    enabled = state.canDirectModel,
                    modifier = Modifier.weight(1f),
                    primary = false,
                    onClick = onDirect
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HumanActionButton(
                    text = if (state.activeAction == "memo") "Building…" else "Decision Memo",
                    enabled = state.canBuildMemo,
                    modifier = Modifier.weight(1f),
                    primary = false,
                    onClick = onMemo
                )

                HumanActionButton(
                    text = "Export PDF",
                    enabled = state.canExportPdf,
                    modifier = Modifier.weight(1f),
                    primary = false,
                    onClick = onExportPdf
                )
            }
        }
    }
}

@Composable
private fun HumanWorkspace(state: HumanCouncilUiState) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "SECVerge AI",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = when {
                            state.isLoading -> "Working…"
                            state.roomStarted -> "Room active"
                            state.runtimeOk -> "Runtime ready"
                            else -> "Ready"
                        },
                        color = if (state.roomStarted || state.runtimeOk) Cyan else TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (state.messages.isEmpty()) {
                Text(
                    text = "Start the room, then ask selected models. Runtime status, model viewpoints, decision memo, and PDF export status will appear here.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                state.messages.forEach { message ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        elevated = false
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = message.title,
                                color = Cyan,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = message.content,
                                color = TextPrimary,
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
private fun HumanTextArea(
    title: String,
    value: String,
    placeholder: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.labelLarge
        )

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
                        .heightIn(min = 110.dp)
                        .background(
                            color = GlassSurface.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
                        )
                        .border(
                            width = 1.dp,
                            color = GlassBorder,
                            shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
                        )
                        .padding(16.dp)
                ) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun HumanParticipantButton(
    name: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = if (selected) Cyan.copy(alpha = 0.15f) else GlassSurface.copy(alpha = 0.72f),
                shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
            )
            .border(
                width = 1.dp,
                color = if (selected) Cyan else GlassBorder,
                shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 8.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (selected) "✓ $name" else name,
            color = if (selected) TextPrimary else TextMuted,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun HumanDropdown(
    selected: String,
    options: List<String>,
    enabled: Boolean,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = GlassSurface.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
                )
                .border(
                    width = 1.dp,
                    color = if (expanded) Cyan else GlassBorder,
                    shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
                )
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
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
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = TextPrimary,
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

@Composable
private fun HumanActionButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier,
    primary: Boolean,
    onClick: () -> Unit
) {
    val shapedModifier = if (primary) {
        modifier.background(
            brush = SECVergeGradients.Signature,
            shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
        )
    } else {
        modifier.background(
            color = GlassSurface.copy(alpha = 0.78f),
            shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
        )
    }

    Box(
        modifier = shapedModifier
            .heightIn(min = 54.dp)
            .border(
                width = 1.dp,
                color = if (enabled) {
                    Cyan.copy(alpha = 0.50f)
                } else {
                    GlassBorder
                },
                shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
            )
            .clickable(enabled = enabled) {
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) {
                TextPrimary
            } else {
                TextMuted
            },
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun exportHumanPdf(
    context: Context,
    state: HumanCouncilUiState
): String {
    val fileName = "SECVerge_Human_Decision_" +
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) +
        ".pdf"

    val document = PdfDocument()
    val titlePaint = Paint().apply {
        color = Color.rgb(12, 18, 32)
        textSize = 18f
        isFakeBoldText = true
    }
    val labelPaint = Paint().apply {
        color = Color.rgb(35, 60, 100)
        textSize = 12f
        isFakeBoldText = true
    }
    val bodyPaint = Paint().apply {
        color = Color.rgb(40, 46, 62)
        textSize = 11f
    }

    var pageNumber = 1
    var y = 48f
    var page = document.startPage(
        PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
    )
    var canvas = page.canvas
    canvas.drawColor(Color.WHITE)

    fun newPage() {
        document.finishPage(page)
        pageNumber += 1
        y = 48f
        page = document.startPage(
            PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        )
        canvas = page.canvas
        canvas.drawColor(Color.WHITE)
    }

    fun drawLine(text: String, paint: Paint = bodyPaint, extra: Float = 18f) {
        if (y > 790f) newPage()
        canvas.drawText(text.take(95), 42f, y, paint)
        y += extra
    }

    fun drawWrapped(text: String, paint: Paint = bodyPaint) {
        text.lines().forEach { paragraph ->
            if (paragraph.isBlank()) {
                y += 10f
            } else {
                var line = ""
                paragraph.split(" ").forEach { word ->
                    val candidate = if (line.isBlank()) word else "$line $word"
                    if (paint.measureText(candidate) > 505f) {
                        drawLine(line, paint)
                        line = word
                    } else {
                        line = candidate
                    }
                }
                if (line.isNotBlank()) drawLine(line, paint)
            }
        }
    }

    drawLine("SECVerge AI — Human Decision Room", titlePaint, 26f)
    drawLine("Decision Objective", labelPaint)
    drawWrapped(state.objective.ifBlank { "No objective entered." })
    y += 8f
    drawLine("Human Instruction", labelPaint)
    drawWrapped(state.instruction.ifBlank { "No instruction entered." })
    y += 8f
    drawLine("Participants", labelPaint)
    drawWrapped(state.selectedModels.joinToString(", ").ifBlank { "No participants selected." })
    y += 8f
    drawLine("Room Messages", labelPaint)

    if (state.messages.isEmpty()) {
        drawWrapped("No room messages yet.")
    } else {
        state.messages.forEachIndexed { index, message ->
            y += 8f
            drawLine("${index + 1}. ${message.title}", labelPaint)
            drawWrapped(message.content)
        }
    }

    document.finishPage(page)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SECVergeAI")
        }

        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            values
        ) ?: error("Unable to create PDF file.")

        context.contentResolver.openOutputStream(uri)?.use { output ->
            document.writeTo(output)
        } ?: error("Unable to write PDF file.")

        document.close()
        return "PDF saved to Downloads/SECVergeAI/$fileName"
    } else {
        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "SECVergeAI"
        )
        dir.mkdirs()

        val file = File(dir, fileName)

        file.outputStream().use { output ->
            document.writeTo(output)
        }

        document.close()
        return "PDF saved to ${file.absolutePath}"
    }
}
