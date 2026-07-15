package com.secverge.ai.ui.shell

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.secverge.ai.navigation.SECVergeRoute
import com.secverge.ai.ui.theme.GlassBorder
import com.secverge.ai.ui.theme.GlassSurface
import com.secverge.ai.ui.theme.MidnightCanvas
import com.secverge.ai.ui.theme.SECVergeMotion
import com.secverge.ai.ui.theme.SECVergeGradients
import com.secverge.ai.ui.theme.TextMuted
import com.secverge.ai.ui.theme.TextPrimary

@Composable
fun AppScaffold(
    currentRoute: String?,
    onDestinationClick: (SECVergeRoute) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MidnightCanvas,
        bottomBar = {
            SECVergeBottomBar(
                currentRoute = currentRoute,
                onDestinationClick = onDestinationClick
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding)
        ) {
            content()
        }
    }
}

@Composable
private fun SECVergeBottomBar(
    currentRoute: String?,
    onDestinationClick: (SECVergeRoute) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = 14.dp,
                vertical = 10.dp
            )
            .background(
                color = GlassSurface,
                shape = RoundedCornerShape(28.dp)
            )
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            SECVergeRoute.bottomDestinations.forEach {
                destination ->

                val selected =
                    currentRoute == destination.route

                val scale by animateFloatAsState(
                    targetValue =
                        if (selected) 1.035f else 1f,
                    animationSpec =
                        SECVergeMotion.PremiumSpring,
                    label = "bottom_nav_scale"
                )

                val contentAlpha by animateFloatAsState(
                    targetValue =
                        if (selected) 1f else 0.76f,
                    animationSpec =
                        SECVergeMotion.fastTween(),
                    label = "bottom_nav_alpha"
                )

                val textColor by animateColorAsState(
                    targetValue =
                        if (selected) {
                            TextPrimary
                        } else {
                            TextMuted
                        },
                    animationSpec =
                        SECVergeMotion.premiumTween(),
                    label = "bottom_nav_text_color"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .scale(scale)
                        .alpha(contentAlpha)
                        .background(
                            brush = if (selected) {
                                SECVergeGradients.PrimaryButton
                            } else {
                                Brush.linearGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Transparent
                                    )
                                )
                            },
                            shape =
                                RoundedCornerShape(22.dp)
                        )
                        .premiumPressClickable(
                            pressedScale = 0.94f
                        ) {
                            if (!selected) {
                                onDestinationClick(destination)
                            }
                        }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = shortLabel(destination),
                        color = textColor,
                        fontWeight =
                            if (selected) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Medium
                            },
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun shortLabel(
    route: SECVergeRoute
): String {
    return when (route) {
        SECVergeRoute.AskHachy -> "Hachy"
        SECVergeRoute.Cockpit -> "UCF"
        SECVergeRoute.Council -> "Council"
        SECVergeRoute.HumanCouncil -> "Human"
        SECVergeRoute.Delphi -> "Delphi"
        else -> route.label
    }
}
