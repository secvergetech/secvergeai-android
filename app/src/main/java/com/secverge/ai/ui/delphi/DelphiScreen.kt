package com.secverge.ai.ui.delphi

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun DelphiScreen(
    viewModel: DelphiViewModel
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
            subtitle = "Delphi AI · Blind reasoning. Transparent convergence."
        )

        DelphiHeroCard()

        DelphiStepRail(
            activeStage = state.stage
        )

        DelphiControls(
            state = state,
            onQuestionChange = viewModel::updateQuestion,
            onContextChange = viewModel::updateContext,
            onToggleParticipant = viewModel::toggleParticipant,
            onTestGemmaMax = viewModel::testGemmaMax,
            onBlindRound = viewModel::runBlindRound,
            onReveal = viewModel::revealPositions,
            onDebate = viewModel::runDebate,
            onConverge = viewModel::converge,
            onExportPdf = {
                val result = runCatching {
                    exportDelphiPdf(
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

        DelphiWorkspace(
            state = state
        )

        state.errorMessage?.let { message ->
            PremiumErrorCard(
                title = "Delphi operation interrupted",
                message = message,
                dismissLabel = "Dismiss",
                onDismiss = viewModel::dismissError
            )
        }
    }
}

@Composable
private fun DelphiHeroCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SECVERGE AI · DELPHI CONSENSUS LAB",
                color = Cyan,
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = "Blind views first. Debate second. Converge with confidence.",
                style = MaterialTheme.typography.headlineLarge.copy(
                    brush = SECVergeGradients.Signature
                )
            )

            Text(
                text = "A clean Delphi workroom: collect independent model views without cross-contamination, reveal the positions, debate the differences, and produce a final executive consensus.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun DelphiStepRail(
    activeStage: DelphiStage
) {
    DelphiStage.values().toList().chunked(2).forEach { row ->
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
                            color = if (active) {
                                Cyan.copy(alpha = 0.55f)
                            } else {
                                GlassBorder
                            },
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
private fun DelphiControls(
    state: DelphiUiState,
    onQuestionChange: (String) -> Unit,
    onContextChange: (String) -> Unit,
    onToggleParticipant: (String) -> Unit,
    onTestGemmaMax: () -> Unit,
    onBlindRound: () -> Unit,
    onReveal: () -> Unit,
    onDebate: () -> Unit,
    onConverge: () -> Unit,
    onExportPdf: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Delphi Controls",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "One decision question. Independent first views. Then reveal, debate, and converge.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            DelphiTextArea(
                title = "Decision question",
                value = state.question,
                placeholder = "Example: Should SECVerge AI launch Delphi as a premium boardroom feature?",
                enabled = !state.isLoading,
                onValueChange = onQuestionChange
            )

            DelphiTextArea(
                title = "Context / constraints",
                value = state.context,
                placeholder = "Add business context, risks, constraints, desired output style, or decision criteria.",
                enabled = !state.isLoading,
                onValueChange = onContextChange
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
                        DelphiParticipantButton(
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DelphiActionButton(
                    text = if (state.activeAction == "test") {
                        "Testing…"
                    } else {
                        "Test Gemma MAX"
                    },
                    enabled = state.canTestGemmaMax,
                    primary = true,
                    modifier = Modifier.weight(1f),
                    onClick = onTestGemmaMax
                )

                DelphiActionButton(
                    text = if (state.activeAction == "blind") {
                        "Collecting…"
                    } else {
                        "1. Blind Round"
                    },
                    enabled = state.canBlindRound,
                    primary = false,
                    modifier = Modifier.weight(1f),
                    onClick = onBlindRound
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DelphiActionButton(
                    text = if (state.activeAction == "reveal") {
                        "Revealing…"
                    } else {
                        "2. Reveal"
                    },
                    enabled = state.canReveal,
                    primary = false,
                    modifier = Modifier.weight(1f),
                    onClick = onReveal
                )

                DelphiActionButton(
                    text = if (state.activeAction == "debate") {
                        "Debating…"
                    } else {
                        "3. Debate"
                    },
                    enabled = state.canDebate,
                    primary = false,
                    modifier = Modifier.weight(1f),
                    onClick = onDebate
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DelphiActionButton(
                    text = if (state.activeAction == "converge") {
                        "Converging…"
                    } else {
                        "4. Converge"
                    },
                    enabled = state.canConverge,
                    primary = false,
                    modifier = Modifier.weight(1f),
                    onClick = onConverge
                )

                DelphiActionButton(
                    text = "Export PDF",
                    enabled = state.canExportPdf,
                    primary = false,
                    modifier = Modifier.weight(1f),
                    onClick = onExportPdf
                )
            }
        }
    }
}

@Composable
private fun DelphiWorkspace(
    state: DelphiUiState
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp)
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
                        text = "SECVerge Delphi",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = when {
                            state.isLoading -> "Working…"
                            state.converged -> "Consensus ready"
                            state.debated -> "Ready to converge"
                            state.revealed -> "Positions revealed"
                            state.blindRoundComplete -> "Blind views collected"
                            state.runtimeOk -> "Gemma MAX ready"
                            else -> "Ready"
                        },
                        color = if (state.runtimeOk || state.blindRoundComplete) {
                            Cyan
                        } else {
                            TextSecondary
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (state.entries.isEmpty()) {
                Text(
                    text = "Run Blind Round first. Gemma MAX retry and rotating moderator fallback are active.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                state.entries.forEach { entry ->
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
                                text = entry.title,
                                color = Cyan,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = if (entry.hiddenUntilReveal && !state.revealed) {
                                    "Blind view collected privately. Tap 2. Reveal to show all positions together."
                                } else {
                                    entry.content
                                },
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
private fun DelphiTextArea(
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
private fun DelphiParticipantButton(
    name: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = if (selected) {
                    Cyan.copy(alpha = 0.15f)
                } else {
                    GlassSurface.copy(alpha = 0.72f)
                },
                shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    Cyan
                } else {
                    GlassBorder
                },
                shape = RoundedCornerShape(SECVergeDimens.RadiusMedium)
            )
            .clickable(enabled = enabled) {
                onClick()
            }
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
private fun DelphiActionButton(
    text: String,
    enabled: Boolean,
    primary: Boolean,
    modifier: Modifier,
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
            color = if (enabled) TextPrimary else TextMuted,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun exportDelphiPdf(
    context: Context,
    state: DelphiUiState
): String {
    val fileName = "SECVerge_Delphi_" +
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
        if (y > 790f) {
            newPage()
        }
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
                if (line.isNotBlank()) {
                    drawLine(line, paint)
                }
            }
        }
    }

    drawLine("SECVerge AI — Delphi Consensus Lab", titlePaint, 26f)
    drawLine("Decision Question", labelPaint)
    drawWrapped(state.question.ifBlank { "No question entered." })
    y += 8f
    drawLine("Context / Constraints", labelPaint)
    drawWrapped(state.context.ifBlank { "No context supplied." })
    y += 8f
    drawLine("Participants", labelPaint)
    drawWrapped(state.selectedModels.joinToString(", ").ifBlank { "No participants selected." })
    y += 8f
    drawLine("Delphi Trail", labelPaint)

    if (state.entries.isEmpty()) {
        drawWrapped("No Delphi entries yet.")
    } else {
        state.entries.forEachIndexed { index, entry ->
            y += 8f
            drawLine("${index + 1}. ${entry.title}", labelPaint)
            drawWrapped(entry.content)
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
