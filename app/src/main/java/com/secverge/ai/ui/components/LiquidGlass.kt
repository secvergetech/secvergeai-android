package com.secverge.ai.ui.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secverge.ai.ui.theme.*
import kotlinx.coroutines.delay

private class GlassBackdrop(val layer: GraphicsLayer) { var origin by mutableStateOf(Offset.Zero) }
private val LocalGlassBackdrop = staticCompositionLocalOf<GlassBackdrop?> { null }

/** Record only the ambient scene. Glass samples and blurs this layer, never its foreground text. */
@Composable
fun LiquidBackdrop(content: @Composable () -> Unit) {
    val layer = rememberGraphicsLayer()
    val backdrop = remember(layer) { GlassBackdrop(layer) }
    CompositionLocalProvider(LocalGlassBackdrop provides backdrop) {
        Box(Modifier.fillMaxSize().background(MidnightCanvas)) {
            Canvas(Modifier.matchParentSize().onGloballyPositioned { backdrop.origin = it.positionInRoot() }
                .drawWithContent { layer.record { this@drawWithContent.drawContent() }; drawLayer(layer) }) {
                drawRect(MidnightCanvas)
                drawRect(Brush.radialGradient(listOf(Cobalt.copy(alpha = .24f), Color.Transparent),
                    center = Offset(size.width * .94f, size.height * .12f), radius = size.width * .95f))
                drawRect(Brush.radialGradient(listOf(Violet.copy(alpha = .22f), Color.Transparent),
                    center = Offset(-size.width * .2f, size.height * .64f), radius = size.width * 1.1f))
                drawRect(Brush.radialGradient(listOf(Cyan.copy(alpha = .12f), Color.Transparent),
                    center = Offset(size.width * .7f, size.height * .92f), radius = size.width * .85f))
                drawLine(Color.White.copy(alpha = .035f), Offset(size.width * .12f, 0f), Offset(size.width * .12f, size.height), 1f)
            }
            content()
        }
    }
}

@Composable
fun LiquidPanel(
    modifier: Modifier = Modifier,
    radius: androidx.compose.ui.unit.Dp = 26.dp,
    tint: Color = MidnightSurface,
    padding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val backdrop = LocalGlassBackdrop.current
    var position by remember { mutableStateOf(Offset.Zero) }
    val shape = RoundedCornerShape(radius)
    Box(modifier.clip(shape).onGloballyPositioned { position = it.positionInRoot() }
        .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = .27f), Cyan.copy(alpha = .1f), Color.White.copy(alpha = .07f))), shape)) {
        if (backdrop != null) {
            Spacer(Modifier.matchParentSize().graphicsLayer {
                // RenderEffect is supported on Android 12+. Earlier devices retain a translucent, legible fallback.
                if (Build.VERSION.SDK_INT >= 31) renderEffect = BlurEffect(26f, 26f, TileMode.Clamp)
            }.drawWithContent {
                val offset = position - backdrop.origin
                translate(-offset.x, -offset.y) { drawLayer(backdrop.layer) }
            })
        }
        Spacer(Modifier.matchParentSize().background(Brush.linearGradient(listOf(
            tint.copy(alpha = .68f), tint.copy(alpha = .48f), tint.copy(alpha = .6f)))))
        Spacer(Modifier.matchParentSize().background(Brush.linearGradient(listOf(
            Color.White.copy(alpha = .065f), Color.Transparent, Color.White.copy(alpha = .018f)))))
        Column(Modifier.padding(padding), content = content)
    }
}

@Composable
fun StudioIcon(name: String, color: Color = TextPrimary, modifier: Modifier = Modifier) {
    Canvas(modifier.size(22.dp)) {
        val u = size.width / 24f
        val stroke = Stroke(1.65f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun line(x: Float, y: Float, X: Float, Y: Float) = drawLine(color, Offset(x*u,y*u), Offset(X*u,Y*u), 1.65f*u, StrokeCap.Round)
        fun path(vararg points: Pair<Float,Float>, close: Boolean = false) {
            val p = Path(); points.forEachIndexed { i, pt -> if(i==0)p.moveTo(pt.first*u,pt.second*u) else p.lineTo(pt.first*u,pt.second*u) }; if(close)p.close(); drawPath(p,color,style=stroke)
        }
        when(name) {
            "home" -> { path(3f to 10f,12f to 3f,21f to 10f); path(5f to 9f,5f to 21f,10f to 21f,10f to 14f,14f to 14f,14f to 21f,19f to 21f,19f to 9f) }
            "chat" -> { drawRoundRect(color,Offset(3*u,3*u),Size(18*u,15*u),CornerRadius(5*u),style=stroke);path(7f to 18f,5f to 22f,12f to 18f);line(8f,9f,16f,9f);line(8f,13f,13f,13f) }
            "models" -> { path(12f to 2f,22f to 8f,12f to 14f,2f to 8f,12f to 2f);path(3f to 13f,12f to 19f,21f to 13f);path(5f to 18f,12f to 22f,19f to 18f) }
            "council" -> { for(x in listOf(5f,12f,19f))drawCircle(color,2.4f*u,Offset(x*u,7*u),style=stroke);path(1f to 20f,2f to 14f,7f to 14f);path(8f to 21f,9f to 13f,15f to 13f,16f to 21f);path(17f to 14f,22f to 14f,23f to 20f) }
            "delphi" -> { path(12f to 2f,22f to 12f,12f to 22f,2f to 12f,12f to 2f);line(12f,7f,12f,17f);line(7f,12f,17f,12f) }
            "history" -> { drawArc(color, -80f, 305f, false, Offset(4*u,4*u),Size(17*u,17*u),style=stroke);path(2f to 4f,2f to 10f,8f to 10f);path(12f to 7f,12f to 13f,16f to 15f) }
            "plus" -> { line(12f,5f,12f,19f);line(5f,12f,19f,12f) }
            "send" -> { path(5f to 11f,12f to 4f,19f to 11f);line(12f,4f,12f,21f) }
            "arrow" -> { line(4f,12f,20f,12f);path(14f to 6f,20f to 12f,14f to 18f) }
            "copy" -> { drawRoundRect(color,Offset(8*u,8*u),Size(13*u,13*u),CornerRadius(3*u),style=stroke);path(15f to 4f,4f to 4f,4f to 15f) }
            "lock" -> { drawRoundRect(color,Offset(5*u,10*u),Size(14*u,11*u),CornerRadius(3*u),style=stroke);drawArc(color,180f,180f,false,Offset(8*u,2*u),Size(8*u,15*u),style=stroke);line(12f,14f,12f,17f) }
            "check" -> path(5f to 12f,10f to 17f,20f to 6f)
            "close" -> {line(6f,6f,18f,18f);line(6f,18f,18f,6f)}
            "chevron" -> path(9f to 6f,15f to 12f,9f to 18f)
            else -> { drawCircle(color,7*u,center,style=stroke);drawCircle(color,2*u,center) }
        }
    }
}

fun modelAccent(name: String): Color = when {
    name.contains("llama",true) -> Cobalt
    name.contains("mistral",true) -> Violet
    name.contains("qwen",true) -> SignatureEnd
    else -> Cyan
}
fun modelLabel(name: String) = if(name == "Hachy" || name == "Ask Hachy") "Hachy" else if(name.contains("gemma",true)) "Gemma" else name.substringBefore(" (")

@Composable
fun ModelOrb(name: String, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 38.dp) {
    val accent = modelAccent(name)
    Box(modifier.size(size).clip(RoundedCornerShape(size * .34f))
        .background(Brush.linearGradient(listOf(accent.copy(alpha=.28f),accent.copy(alpha=.06f))))
        .border(1.dp, accent.copy(alpha=.4f),RoundedCornerShape(size * .34f)), contentAlignment=Alignment.Center) {
        Text(modelLabel(name).take(1), color=accent, fontWeight=FontWeight.Bold, fontSize=(size.value*.42f).sp)
    }
}

@Composable
fun GlassAction(label: String, icon: String, enabled: Boolean = true, onClick: () -> Unit) {
    IconButton(onClick, enabled=enabled, modifier=Modifier.size(48.dp).semantics { contentDescription=label }) {
        StudioIcon(icon, if(enabled) TextSecondary else TextMuted)
    }
}

@Composable
fun StudioHeader(title: String, subtitle: String, actions: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("SECVERGE AI",color=Cyan.copy(alpha=.85f),fontSize=10.sp,letterSpacing=2.sp,fontWeight=FontWeight.SemiBold)
            Text(title,color=TextPrimary,style=MaterialTheme.typography.headlineMedium)
            if(subtitle.isNotBlank()) Text(subtitle,color=TextSecondary,style=MaterialTheme.typography.bodySmall)
        }
        actions()
    }
}

@Composable
fun GlassPill(text: String, accent: Color = Cyan) {
    Text(text, color=accent, style=MaterialTheme.typography.labelSmall,
        modifier=Modifier.clip(CircleShape).background(accent.copy(alpha=.1f))
            .border(1.dp,accent.copy(alpha=.2f),CircleShape).padding(horizontal=10.dp,vertical=6.dp))
}

@Composable
fun StudioEmpty(title: String, description: String, model: String = "Gemma", suggestions: List<String> = emptyList(), onSuggestion: (String) -> Unit = {}) {
    Column(Modifier.fillMaxWidth().padding(vertical=28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)) {
        ModelOrb(model,size=72.dp)
        Text(title,color=TextPrimary,style=MaterialTheme.typography.headlineMedium)
        Text(description,color=TextSecondary,style=MaterialTheme.typography.bodyMedium,
            textAlign=androidx.compose.ui.text.style.TextAlign.Center,modifier=Modifier.padding(horizontal=20.dp))
        suggestions.forEach { prompt ->
            LiquidPanel(Modifier.fillMaxWidth().clickable { onSuggestion(prompt) },padding=PaddingValues(16.dp),radius=18.dp) {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text(prompt,color=TextPrimary,style=MaterialTheme.typography.bodyMedium,modifier=Modifier.weight(1f))
                    StudioIcon("arrow",modelAccent(model),Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ModelStrip(selected: String, enabled: Boolean, onSelect: (String) -> Unit) {
    val names=listOf("Ask Hachy / Gemma","Llama","Mistral","Qwen")
    LiquidPanel(Modifier.fillMaxWidth().padding(horizontal=16.dp),padding=PaddingValues(6.dp),radius=24.dp) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            names.forEach { name ->
                val active=selected==name
                val accent=modelAccent(name)
                Column(Modifier.weight(1f).clip(RoundedCornerShape(18.dp))
                    .background(if(active) accent.copy(alpha=.15f) else Color.Transparent)
                    .border(1.dp,if(active)accent.copy(alpha=.45f) else Color.Transparent,RoundedCornerShape(18.dp))
                    .clickable(enabled=enabled,role=Role.Tab) { onSelect(name) }
                    .semantics { this.selected=active; contentDescription="${modelLabel(name)} model"; if(!enabled)disabled() }
                    .padding(vertical=10.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
                    ModelOrb(name,size=30.dp)
                    Text(modelLabel(name),color=if(active)TextPrimary else TextSecondary,fontSize=11.sp,fontWeight=if(active)FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
fun MessageComposer(value: String, onChange: (String)->Unit, enabled: Boolean, model: String, onSend: ()->Unit) {
    val keyboard=LocalSoftwareKeyboardController.current
    val focus=LocalFocusManager.current
    fun send() { if(enabled && value.isNotBlank()) { onSend();keyboard?.hide();focus.clearFocus() } }
    Column(Modifier.fillMaxWidth().imePadding().padding(horizontal=16.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(7.dp)) {
        LiquidPanel(Modifier.fillMaxWidth(),radius=26.dp,padding=PaddingValues(start=18.dp,end=7.dp,top=7.dp,bottom=7.dp)) {
            Row(verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                BasicTextField(value,onChange,enabled=enabled,textStyle=MaterialTheme.typography.bodyLarge.copy(color=TextPrimary),
                    cursorBrush=SolidColor(Cyan),maxLines=5,keyboardOptions=KeyboardOptions(imeAction=ImeAction.Send),
                    keyboardActions=KeyboardActions(onSend={send()}),modifier=Modifier.weight(1f).heightIn(min=46.dp,max=124.dp)
                        .padding(vertical=11.dp).semantics { contentDescription="Message ${modelLabel(model)}" },
                    decorationBox={ inner -> if(value.isBlank())Text("Message ${modelLabel(model)}…",color=TextSecondary);inner() })
                Box(Modifier.size(48.dp).clip(CircleShape)
                    .background(if(enabled&&value.isNotBlank())SECVergeGradients.PrimaryButton else Brush.linearGradient(listOf(Color.White.copy(alpha=.06f),Color.White.copy(alpha=.04f))))
                    .clickable(enabled=enabled&&value.isNotBlank(),role=Role.Button) { send() }
                    .semantics { contentDescription="Send message"; if(!enabled||value.isBlank())disabled() },contentAlignment=Alignment.Center) {
                    StudioIcon("send",if(enabled&&value.isNotBlank())TextPrimary else TextMuted)
                }
            }
        }
        Text("${modelLabel(model)}  ·  History saved on this device",color=TextSecondary.copy(alpha=.8f),fontSize=10.sp,modifier=Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun ModelWorking(model: String) {
    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while(true) { delay(1000);seconds++ } }
    LiquidPanel(Modifier.fillMaxWidth(),padding=PaddingValues(16.dp),radius=22.dp) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(Modifier.size(22.dp),color=modelAccent(model),strokeWidth=2.dp)
            Column(Modifier.weight(1f)) {
                Text("${modelLabel(model)} is thinking",color=TextPrimary,style=MaterialTheme.typography.titleSmall)
                Text("Longer answers can take a little time.",color=TextSecondary,style=MaterialTheme.typography.bodySmall)
            }
            Text("${seconds/60}:${(seconds%60).toString().padStart(2,'0')}",color=TextSecondary,fontFamily=FontFamily.Monospace,fontSize=12.sp)
        }
    }
}

private fun inlineMarkdown(text: String) = buildAnnotatedString {
    val pattern=Regex("\\*\\*([^*]+)\\*\\*|`([^`]+)`")
    var end=0
    pattern.findAll(text).forEach { match ->
        append(text.substring(end,match.range.first))
        withStyle(if(match.groupValues[1].isNotEmpty())SpanStyle(fontWeight=FontWeight.Bold) else SpanStyle(fontFamily=FontFamily.Monospace,color=Cyan)) {
            append(match.groupValues[1].ifEmpty { match.groupValues[2] })
        }
        end=match.range.last+1
    }
    append(text.substring(end))
}

@Composable
fun ReadableText(text: String) {
    val blocks=remember(text) { text.split(Regex("\n\\s*\n")) }
    SelectionContainer {
        Column(verticalArrangement=Arrangement.spacedBy(13.dp)) {
            blocks.forEach { block ->
                val clean=block.trim()
                when {
                    clean.isBlank() -> Unit
                    clean.startsWith("```") -> Text(clean.substringAfter('\n',clean).substringBeforeLast("```"),color=TextPrimary,fontFamily=FontFamily.Monospace,fontSize=12.sp,
                        modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MidnightCanvas.copy(alpha=.55f)).horizontalScroll(rememberScrollState()).padding(14.dp))
                    clean.startsWith("|") -> MarkdownTable(clean)
                    else -> {
                        val normalized=clean.lines().filterNot { it.matches(Regex("[-=]{3,}")) }.joinToString("\n") { line ->
                            line.replace(Regex("^\\s*[-*]\\s+"),"• ").replace(Regex("^#{1,6}\\s+"),"")
                        }
                        Text(inlineMarkdown(normalized),color=TextPrimary.copy(alpha=.94f),
                            style=if(clean.startsWith("#"))MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

/** A readable, horizontally scrollable table; cells wrap without shrinking the text. */
@Composable
private fun MarkdownTable(block: String) {
    val rows = remember(block) {
        block.lines().filter { it.isNotBlank() && !it.matches(Regex("[| :\\-]+")) }
            .map { it.trim().trim('|').split('|').map(String::trim) }
    }
    val columns = rows.maxOfOrNull { it.size } ?: 0
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
        .background(MidnightCanvas.copy(alpha=.35f)).horizontalScroll(rememberScrollState())) {
        rows.forEachIndexed { index, row ->
            Row(Modifier.background(if(index==0)Cobalt.copy(alpha=.15f) else Color.Transparent)) {
                repeat(columns) { column ->
                    Text(inlineMarkdown(row.getOrElse(column){""}),
                        color=if(index==0)TextPrimary else TextSecondary,
                        style=if(index==0)MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
                        modifier=Modifier.width(170.dp).padding(12.dp))
                }
            }
            if(index<rows.lastIndex)HorizontalDivider(color=GlassBorder)
        }
    }
}

@Composable
fun ResponseCard(model: String, text: String, details: String? = null, compact: Boolean = false) {
    val clipboard=LocalClipboardManager.current
    var expanded by remember(text) { mutableStateOf(!compact) }
    var showDetails by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) { if(copied) {delay(1800);copied=false} }
    LiquidPanel(Modifier.fillMaxWidth(),padding=PaddingValues(18.dp)) {
        Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                ModelOrb(model,size=32.dp)
                Text(modelLabel(model),color=TextPrimary,style=MaterialTheme.typography.titleSmall,modifier=Modifier.weight(1f))
                GlassAction(if(copied)"Answer copied" else "Copy answer",if(copied)"check" else "copy") { clipboard.setText(AnnotatedString(text));copied=true }
            }
            if(expanded) ReadableText(text) else Text(text.replace("**","").replace("#","").take(240)+if(text.length>240)"…" else "",color=TextSecondary,style=MaterialTheme.typography.bodyMedium)
            if(compact) TextButton(onClick={expanded=!expanded},contentPadding=PaddingValues(0.dp)) { Text(if(expanded)"Show less" else "Read perspective",color=modelAccent(model)) }
            if(details!=null) {
                TextButton(onClick={showDetails=!showDetails},contentPadding=PaddingValues(0.dp)) { Text(if(showDetails)"Hide response details" else "Response details",color=TextSecondary,style=MaterialTheme.typography.labelSmall) }
                if(showDetails)Text(details,color=TextSecondary,fontFamily=FontFamily.Monospace,fontSize=11.sp,lineHeight=17.sp)
            }
        }
    }
}

@Composable
fun UserQuestion(text: String) {
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
        LiquidPanel(Modifier.fillMaxWidth(.92f),tint=Indigo,padding=PaddingValues(16.dp),radius=22.dp) {
            Text(text,color=TextPrimary,style=MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun StudioField(value: String, onChange: (String)->Unit, label: String, enabled: Boolean = true, minLines: Int = 3) {
    OutlinedTextField(value,onChange,enabled=enabled,label={Text(label)},minLines=minLines,
        modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),
        colors=OutlinedTextFieldDefaults.colors(focusedTextColor=TextPrimary,unfocusedTextColor=TextPrimary,
            disabledTextColor=TextSecondary,focusedBorderColor=Cyan,unfocusedBorderColor=Color.White.copy(alpha=.15f),
            focusedContainerColor=MidnightCanvas.copy(alpha=.2f),unfocusedContainerColor=MidnightCanvas.copy(alpha=.15f),
            cursorColor=Cyan,focusedLabelColor=Cyan,unfocusedLabelColor=TextSecondary))
}

@Composable
fun StepRail(steps: List<String>, active: Int) {
    Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=12.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        steps.forEachIndexed { index, title ->
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(if(index<=active)Cyan else Color.White.copy(alpha=.12f)))
                Text("${index+1}  $title",color=if(index==active)TextPrimary else TextSecondary,fontSize=10.sp,fontWeight=if(index==active)FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}
