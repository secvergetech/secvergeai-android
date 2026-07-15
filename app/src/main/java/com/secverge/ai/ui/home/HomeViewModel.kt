package com.secverge.ai.ui.home

import androidx.lifecycle.ViewModel
import com.secverge.ai.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val user = authRepository.currentUser()

        _uiState.update {
            it.copy(
                userName = user
                    ?.name
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
                    ?: user
                        ?.email
                        ?.substringBefore("@")
                        ?.replaceFirstChar { character ->
                            character.uppercase()
                        }
                    ?: "Shannu",
                email = user?.email,
                role = user?.role,
                isReady = true
            )
        }
    }

    fun logout() {
        authRepository.logout()

        _uiState.update {
            it.copy(logoutCompleted = true)
        }
    }

    fun consumeLogout() {
        _uiState.update {
            it.copy(logoutCompleted = false)
        }
    }
}
