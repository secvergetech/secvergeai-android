package com.secverge.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.shell.premiumPressClickable
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.TextSecondary

@Composable
fun PremiumErrorCard(
    title: String = "Something went wrong",
    message: String,
    modifier: Modifier = Modifier,
    retryLabel: String? = null,
    dismissLabel: String? = null,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(20.dp)
    val errorAccent = MaterialTheme.colorScheme.error

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                liveRegion = LiveRegionMode.Assertive
            }
            .background(
                color = errorAccent.copy(alpha = 0.09f),
                shape = shape
            )
            .border(
                width = 1.dp,
                color = errorAccent.copy(alpha = 0.34f),
                shape = shape
            )
            .padding(18.dp)
    ) {
        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )

        if (
            (retryLabel != null && onRetry != null) ||
            (dismissLabel != null && onDismiss != null)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                if (
                    dismissLabel != null &&
                    onDismiss != null
                ) {
                    ErrorAction(
                        label = dismissLabel,
                        filled = false,
                        accent = errorAccent,
                        onClick = onDismiss
                    )
                }

                if (
                    retryLabel != null &&
                    onRetry != null
                ) {
                    ErrorAction(
                        label = retryLabel,
                        filled = true,
                        accent = errorAccent,
                        onClick = onRetry
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorAction(
    label: String,
    filled: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)

    Text(
        text = label,
        color = if (filled) {
            Color.White
        } else {
            accent
        },
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .background(
                color = if (filled) {
                    accent.copy(alpha = 0.82f)
                } else {
                    Color.Transparent
                },
                shape = shape
            )
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.42f),
                shape = shape
            )
            .premiumPressClickable(
                pressedScale = 0.96f,
                onClick = onClick
            )
            .padding(
                PaddingValues(
                    horizontal = 16.dp,
                    vertical = 10.dp
                )
            )
    )
}
