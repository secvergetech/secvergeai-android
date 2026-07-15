package com.secverge.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.theme.GlassBorder
import com.secverge.ai.ui.theme.GlassSurface
import com.secverge.ai.ui.theme.SECVergeGradients
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.TextSecondary

@Composable
fun ChatBubble(
    text: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = if (isUser) 24.dp else 8.dp,
        bottomEnd = if (isUser) 8.dp else 24.dp
    )

    Box(
        modifier = modifier
            .widthIn(max = 310.dp)
            .then(
                if (isUser) {
                    Modifier.background(SECVergeGradients.PrimaryButton, shape)
                } else {
                    Modifier
                        .background(GlassSurface, shape)
                        .border(1.dp, GlassBorder, shape)
                }
            )
            .padding(PaddingValues(horizontal = 18.dp, vertical = 14.dp))
    ) {
        Text(
            text = text,
            color = if (isUser) TextPrimary else TextSecondary,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
