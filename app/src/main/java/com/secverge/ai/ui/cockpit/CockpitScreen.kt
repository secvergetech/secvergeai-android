package com.secverge.ai.ui.cockpit

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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val WebsiteUcfModels = listOf(
    "Ask Hachy / Gemma",
    "Llama",
    "Mistral",
    "Qwen"
)

@Composable
fun CockpitScreen(
    viewModel: CockpitViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val expandedEntries = remember { mutableStateMapOf<Int, Boolean>() }
    var showHistory by remember { mutableStateOf(true) }

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
            subtitle = "Universal Context Framework"
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
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

        UcfInputCard(
            state = state,
            onPromptChange = viewModel::updatePrompt,
            onSend = viewModel::send,
            onSwitchAndCarry = viewModel::switchAndCarry,
            onHistory = {
                showHistory = !showHistory
            },
            onDownloadPdf = {
                val result = runCatching {
                    createUcfPdf(
                        context = context,
                        state = state
                    )
                }.fold(
                    onSuccess = { it },
                    onFailure = {
                        "PDF export failed: ${it.message ?: "Unknown error"}"
                    }
                )

                viewModel.recordLocalMessage(
                    title = "Download PDF",
                    content = result
                )

                Toast.makeText(
                    context,
                    result,
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 230.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Conversation",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge
                )

                AnimatedVisibility(visible = state.errorMessage != null) {
                    PremiumErrorCard(
                        title = "UCF operation interrupted",
                        message = state.errorMessage.orEmpty(),
                        dismissLabel = "Dismiss",
                        onDismiss = viewModel::dismissError
                    )
                }

                if (!showHistory) {
                    Text(
                        text = "History is hidden. Tap History to show it again.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (state.consoleEntries.isEmpty()) {
                    Text(
                        text = "Start with Gemma. Switch when needed.",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Use Send for normal chat. Use Switch & Carry when you want SECVerge AI to carry context into the target model.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
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

                                entry.technicalProof?.let { proof ->
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
                                            text = proof,
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
}

@Composable
private fun UcfInputCard(
    state: CockpitUiState,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
    onSwitchAndCarry: () -> Unit,
    onHistory: () -> Unit,
    onDownloadPdf: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CockpitPromptField(
                value = state.prompt,
                enabled = !state.isLoading,
                onValueChange = onPromptChange
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WebsitePrimaryButton(
                    text = when (state.activeAction) {
                        "send" -> "Sending…"
                        else -> "Send"
                    },
                    enabled = state.canSend,
                    modifier = Modifier.weight(1f),
                    onClick = onSend
                )

                WebsitePrimaryButton(
                    text = when (state.activeAction) {
                        "switch" -> "Switching…"
                        else -> "Switch & Carry"
                    },
                    enabled = state.canSwitchAndCarry,
                    modifier = Modifier.weight(1.35f),
                    onClick = onSwitchAndCarry
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WebsiteSecondaryButton(
                    text = "History",
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoading,
                    onClick = onHistory
                )

                WebsiteSecondaryButton(
                    text = "Download PDF",
                    modifier = Modifier.weight(1.3f),
                    enabled = !state.isLoading,
                    onClick = onDownloadPdf
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
                    .clickable(enabled = enabled) {
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
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent { focusState ->
                if (focusState.isFocused) {
                    coroutineScope.launch {
                        delay(250)
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = TextPrimary
        ),
        cursorBrush = SolidColor(Cyan),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 88.dp)
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
            .clickable(enabled = enabled) {
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) TextPrimary else TextMuted,
            style = MaterialTheme.typography.labelLarge
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
            .clickable(enabled = enabled) {
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) TextPrimary else TextMuted,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun createUcfPdf(
    context: Context,
    state: CockpitUiState
): String {
    val fileName = "SECVerge_UCF_" +
        SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.US
        ).format(Date()) +
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
        PdfDocument.PageInfo.Builder(
            595,
            842,
            pageNumber
        ).create()
    )
    var canvas = page.canvas
    canvas.drawColor(Color.WHITE)

    fun newPage() {
        document.finishPage(page)
        pageNumber += 1
        y = 48f
        page = document.startPage(
            PdfDocument.PageInfo.Builder(
                595,
                842,
                pageNumber
            ).create()
        )
        canvas = page.canvas
        canvas.drawColor(Color.WHITE)
    }

    fun drawLine(
        text: String,
        paint: Paint = bodyPaint,
        extra: Float = 18f
    ) {
        if (y > 790f) {
            newPage()
        }
        canvas.drawText(text, 42f, y, paint)
        y += extra
    }

    fun drawWrapped(
        text: String,
        paint: Paint = bodyPaint
    ) {
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

    drawLine("SECVerge AI — Unified Context Cockpit", titlePaint, 26f)
    drawLine("Responding Model: ${state.fromModel}", labelPaint)
    drawLine("Switch Target: ${state.toModel}", labelPaint)
    y += 8f

    drawLine("Current Prompt", labelPaint)
    drawWrapped(
        state.prompt.ifBlank {
            "No active prompt entered."
        }
    )
    y += 12f

    drawLine("Conversation History", labelPaint)

    if (state.consoleEntries.isEmpty()) {
        drawWrapped("No conversation history yet.")
    } else {
        state.consoleEntries.forEachIndexed { index, entry ->
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
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/SECVergeAI"
            )
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
