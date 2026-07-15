package com.secverge.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.shell.premiumPressClickable
import com.secverge.ai.ui.theme.SECVergeDimens
import com.secverge.ai.ui.theme.SECVergeGradients
import com.secverge.ai.ui.theme.TextPrimary

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = 22.dp,
        vertical = 15.dp
    ),
    onClick: () -> Unit
) {
    val interactive = enabled && !loading

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 54.dp)
            .background(
                brush = SECVergeGradients.PrimaryButton,
                shape = RoundedCornerShape(
                    SECVergeDimens.RadiusMedium
                )
            )
            .semantics {
                role = Role.Button
                contentDescription =
                    if (loading) "$text, loading" else text

                if (!interactive) {
                    disabled()
                }
            }
            .premiumPressClickable(
                enabled = interactive,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = TextPrimary,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
