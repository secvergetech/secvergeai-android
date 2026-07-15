package com.secverge.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.secverge.ai.navigation.SECVergeNavGraph
import com.secverge.ai.ui.theme.SECVergeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SECVergeTheme {
                SECVergeNavGraph()
            }
        }
    }
}
