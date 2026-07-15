package com.secverge.ai.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secverge.ai.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                errorMessage = null
            )
        }
    }

    fun updatePassword(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                errorMessage = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(passwordVisible = !it.passwordVisible)
        }
    }

    fun login() {
        val state = _uiState.value

        if (state.isLoading) {
            return
        }

        if (state.email.isBlank()) {
            showError("Enter your SECVerge email address.")
            return
        }

        if (!state.email.contains("@")) {
            showError("Enter a valid email address.")
            return
        }

        if (state.password.isBlank()) {
            showError("Enter your password.")
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            authRepository.login(
                email = state.email,
                password = state.password
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginSuccessful = true,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = friendlyMessage(throwable)
                    )
                }
            }
        }
    }

    fun consumeLoginSuccess() {
        _uiState.update {
            it.copy(loginSuccessful = false)
        }
    }

    private fun showError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = message
            )
        }
    }

    private fun friendlyMessage(throwable: Throwable): String {
        val raw = throwable.message.orEmpty()

        return when {
            raw.contains("401", ignoreCase = true) ->
                "The email or password is incorrect."

            raw.contains("403", ignoreCase = true) ->
                "This account does not currently have access."

            raw.contains("timeout", ignoreCase = true) ->
                "The SECVerge server took too long to respond. Please retry."

            raw.contains("Unable to resolve host", ignoreCase = true) ->
                "No internet connection. Check your network and retry."

            raw.isNotBlank() ->
                raw

            else ->
                "Login could not be completed. Please retry."
        }
    }
}
