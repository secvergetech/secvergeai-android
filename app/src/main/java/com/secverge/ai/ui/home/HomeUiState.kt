package com.secverge.ai.ui.home

data class HomeUiState(
    val userName: String = "Shannu",
    val email: String? = null,
    val role: String? = null,
    val isReady: Boolean = false,
    val logoutCompleted: Boolean = false
)
