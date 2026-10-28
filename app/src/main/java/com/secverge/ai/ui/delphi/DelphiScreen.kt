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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.components.*
import com.secverge.ai.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DelphiScreen(viewModel: DelphiViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context=LocalContext.current
    val keyboard=LocalSoftwareKeyboardController.current
    var extraContext by remember {mutableStateOf(false)}
    var reset by remember {mutableStateOf(false)}
    val started=state.runId!=null || state.isLoading
    val step=if(state.converged || state.debated)3 else if(state.revealed)2 else if(state.blindRoundComplete)1 else 0
    Column(Modifier.fillMaxSize().imePadding()) {
        StudioHeader("Delphi","Independent ideas. A confident next step.") {
            GlassAction("New Delphi decision","plus",!state.isLoading){if(state.entries.isEmpty())viewModel.updateQuestion("") else reset=true}
        }
        StepRail(listOf("Blind","Reveal","Debate","Consensus"),step)
        LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            if(!started && !state.blindRoundComplete) {
                item {LiquidPanel(Modifier.fillMaxWidth()) {
                    GlassPill("DECIDE WITH CLARITY")
                    Spacer(Modifier.height(14.dp))
                    Text("Let ideas speak\nbefore identities.",color=TextPrimary,style=MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(10.dp))
                    Text("Two independent answers stay sealed until you reveal them. Compare their thinking, then build a final recommendation.",color=TextSecondary)
                }}
                item {StudioField(state.question,viewModel::updateQuestion,"What decision are you making?",!state.isLoading)}
                item {
                    TextButton(onClick={extraContext=!extraContext}){Text(if(extraContext)"Hide extra context" else "+ Add context or constraints",color=Cyan)}
                    if(extraContext)StudioField(state.context,viewModel::updateContext,"Context (optional)",!state.isLoading)
                }
            } else {
                item {LiquidPanel(Modifier.fillMaxWidth()) {GlassPill(if(state.converged)"DECISION READY" else "TWO INDEPENDENT VOICES");Spacer(Modifier.height(12.dp));Text(state.question,color=TextPrimary,style=MaterialTheme.typography.titleMedium)}}
            }
            items(state.entries.filterNot{it.content==state.consensus && state.converged},key={it.id}) {entry ->
                if(entry.hiddenUntilReveal && !state.revealed) {
                    LiquidPanel(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                            StudioIcon("lock",Cyan,Modifier.size(24.dp))
                            Column {Text(entry.title,color=TextPrimary,style=MaterialTheme.typography.titleMedium);Text("Answer sealed · reveal when ready",color=TextSecondary,style=MaterialTheme.typography.bodySmall)}
                        }
                    }
                } else ResponseCard(entry.model ?: entry.title,entry.content,details=entry.title,compact=state.converged)
            }
            state.consensus?.let {answer -> item {Text("Your final recommendation",color=TextPrimary,style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(12.dp));ResponseCard("Delphi consensus",answer)}}
            if(state.isLoading)item {ModelWorking("Delphi")}
            state.errorMessage?.let {error ->item{PremiumErrorCard(message=error,dismissLabel="Dismiss",onDismiss=viewModel::dismissError)}}
            state.exportedPdfPath?.let {path ->item {LiquidPanel(Modifier.fillMaxWidth()){Text(if(path.startsWith("PDF export failed"))path else "PDF saved to Downloads",color=if(path.startsWith("PDF export failed"))MaterialTheme.colorScheme.error else Cyan)}}}
        }
        Column(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            val label=when {state.converged -> "Export decision as PDF";state.debated -> "Build final recommendation";state.revealed -> "Compare perspectives";state.blindRoundComplete -> "Reveal answers";else -> "Collect blind answers"}
            val enabled=when {state.converged -> state.canExportPdf;state.debated -> state.canConverge;state.revealed -> state.canDebate;state.blindRoundComplete -> state.canReveal;else ->state.canBlindRound}
            GradientButton(label,Modifier.fillMaxWidth(),enabled,loading=state.isLoading) {
                keyboard?.hide()
                when {state.converged -> {
                    val result=runCatching{exportDelphiPdf(context,state)}.getOrElse{"PDF export failed: ${it.message ?: "Please try again"}"}
                    viewModel.markPdfExported(result)
                    Toast.makeText(context,if(result.startsWith("PDF export failed"))result else "Decision PDF saved to Downloads",Toast.LENGTH_LONG).show()
                };state.debated ->viewModel.converge();state.revealed ->viewModel.runDebate();state.blindRoundComplete ->viewModel.revealPositions();else ->viewModel.runBlindRound()}
            }
            Text(when {state.converged -> "Your decision trail is included in the PDF.";state.blindRoundComplete && !state.revealed ->"Both answers are ready. Reveal them together.";else ->"Follow each step at your own pace."},color=TextMuted,style=MaterialTheme.typography.labelSmall)
        }
    }
    if(reset)AlertDialog(onDismissRequest={reset=false},title={Text("Start a new decision?")},text={Text("Export your current decision first if you want to keep it.")},confirmButton={TextButton(onClick={viewModel.updateQuestion("");viewModel.updateContext("");reset=false}){Text("New decision")}},dismissButton={TextButton(onClick={reset=false}){Text("Keep reviewing")}})
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
