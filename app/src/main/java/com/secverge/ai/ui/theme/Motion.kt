package com.secverge.ai.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object SECVergeMotion {
    const val Instant = 90
    const val Fast = 180
    const val Normal = 320
    const val Deliberate = 480
    const val Slow = 720

    val PressSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val PremiumSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val GentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessVeryLow
    )

    fun <T> fastTween() = tween<T>(
        durationMillis = Fast
    )

    fun <T> premiumTween() = tween<T>(
        durationMillis = Normal
    )

    fun <T> deliberateTween() = tween<T>(
        durationMillis = Deliberate
    )

    fun <T> slowTween() = tween<T>(
        durationMillis = Slow
    )
}
