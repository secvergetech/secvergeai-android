package com.secverge.ai.ui.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.theme.Cyan
import com.secverge.ai.ui.theme.GlassBorder
import com.secverge.ai.ui.theme.GlassSurface
import com.secverge.ai.ui.theme.SECVergeDimens
import com.secverge.ai.ui.theme.TextMuted
import com.secverge.ai.ui.theme.TextPrimary

@Composable
fun PremiumLoginField(
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val focused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (focused) {
            Cyan.copy(alpha = 0.78f)
        } else {
            GlassBorder
        },
        label = "login_field_border"
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = TextPrimary
        ),
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        singleLine = true,
        cursorBrush = SolidColor(Cyan),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = GlassSurface.copy(alpha = 0.82f),
                        shape = RoundedCornerShape(
                            SECVergeDimens.RadiusMedium
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(
                            SECVergeDimens.RadiusMedium
                        )
                    )
                    .then(
                        Modifier.background(
                            color = GlassSurface.copy(alpha = 0.01f),
                            shape = RoundedCornerShape(
                                SECVergeDimens.RadiusMedium
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = GlassSurface.copy(alpha = 0.01f)
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = GlassSurface.copy(alpha = 0.01f)
                            )
                            .then(
                                Modifier
                            )
                            .padding(
                                start = 18.dp,
                                top = 17.dp,
                                end = if (trailingContent == null) {
                                    18.dp
                                } else {
                                    56.dp
                                },
                                bottom = 17.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            innerTextField()
                        }
                    }

                    if (trailingContent != null) {
                        Box(
                            modifier = Modifier.align(
                                Alignment.CenterEnd
                            )
                        ) {
                            trailingContent()
                        }
                    }
                }
            }
        }
    )
}
