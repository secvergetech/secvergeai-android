package com.secverge.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.secverge.ai.ui.theme.GlassBorder
import com.secverge.ai.ui.theme.GlassSurface
import com.secverge.ai.ui.theme.Success
import com.secverge.ai.ui.theme.TextPrimary
import com.secverge.ai.ui.theme.Warning
import com.secverge.ai.ui.theme.Cyan
import androidx.compose.ui.graphics.Color

enum class ModelStatus {
    Verified,
    Configured,
    NeedsAttention,
    Local
}

@Composable
fun ModelChip(
    name: String,
    status: ModelStatus,
    modifier: Modifier = Modifier
) {
    val statusColor: Color = when (status) {
        ModelStatus.Verified -> Success
        ModelStatus.Configured -> Cyan
        ModelStatus.NeedsAttention -> Warning
        ModelStatus.Local -> Color(0xFFC084FC)
    }

    Row(
        modifier = modifier
            .background(GlassSurface, RoundedCornerShape(50))
            .border(1.dp, GlassBorder, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(8.dp)
                .background(statusColor, CircleShape)
        )
        Text(
            text = "  $name",
            color = TextPrimary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
