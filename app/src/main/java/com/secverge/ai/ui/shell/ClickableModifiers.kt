package com.secverge.ai.ui.shell

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.secverge.ai.ui.theme.SECVergeMotion

@Composable
fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    return clickable(
        enabled = enabled,
        interactionSource = remember {
            MutableInteractionSource()
        },
        indication = null,
        onClick = onClick
    )
}

fun Modifier.premiumPressClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.965f,
    hapticFeedbackType: HapticFeedbackType =
        HapticFeedbackType.TextHandleMove,
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    var pressed by remember {
        mutableStateOf(false)
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (pressed && enabled) {
            pressedScale
        } else {
            1f
        },
        animationSpec = SECVergeMotion.PressSpring,
        label = "premium_press_scale"
    )

    this
        .scale(animatedScale)
        .alpha(if (enabled) 1f else 0.46f)
        .semantics {
            role = Role.Button

            if (!enabled) {
                disabled()
            }

            onClick {
                if (enabled) {
                    onClick()
                    true
                } else {
                    false
                }
            }
        }
        .pointerInput(enabled, onClick) {
            detectTapGestures(
                onPress = {
                    if (!enabled) {
                        return@detectTapGestures
                    }

                    pressed = true

                    try {
                        tryAwaitRelease()
                    } finally {
                        pressed = false
                    }
                },
                onTap = {
                    if (enabled) {
                        haptic.performHapticFeedback(
                            hapticFeedbackType
                        )
                        onClick()
                    }
                }
            )
        }
}
