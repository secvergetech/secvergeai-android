package com.secverge.ai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun <T> AnimatedList(
    items: List<T>,
    itemContent: @Composable (T) -> Unit
) {
    val visibleItems = remember(items) { mutableStateListOf<T>() }

    LaunchedEffect(items) {
        visibleItems.clear()
        items.forEachIndexed { index, item ->
            delay(70L * index)
            visibleItems.add(item)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        items.forEach { item ->
            AnimatedVisibility(
                visible = visibleItems.contains(item),
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
            ) {
                itemContent(item)
            }
        }
    }
}
