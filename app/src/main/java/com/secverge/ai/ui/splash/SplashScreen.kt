package com.secverge.ai.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.secverge.ai.R
import com.secverge.ai.ui.theme.MidnightCanvas
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val zoom = remember { Animatable(0.88f) }
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        zoom.animateTo(1.04f, tween(2600, easing = FastOutSlowInEasing))
        delay(200)
        currentOnFinished()
    }
    Box(Modifier.fillMaxSize().background(MidnightCanvas),contentAlignment=Alignment.Center) {
        Image(painterResource(R.drawable.secverge_brand),
            contentDescription="SECVerge AI — Beyond Intelligence",
            contentScale=ContentScale.Fit,
            modifier=Modifier.fillMaxWidth().aspectRatio(1f).graphicsLayer {
                scaleX=zoom.value; scaleY=zoom.value
            })
    }
}
