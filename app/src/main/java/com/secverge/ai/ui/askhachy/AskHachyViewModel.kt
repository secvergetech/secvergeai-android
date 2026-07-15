package com.secverge.ai.ui.askhachy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicLong

class AskHachyViewModel(
    private val repository: AskHachyRepository
) : ViewModel() {

    private val messageIds = AtomicLong(0L)

    private val _uiState = MutableStateFlow(AskHachyUiState())
    val uiState: StateFlow<AskHachyUiState> = _uiState.asStateFlow()

    fun updatePrompt(value: String) {
        _uiState.update {
            it.copy(
                prompt = value,
                errorMessage = null
            )
        }
    }

    fun send() {
        val state = _uiState.value
        val prompt = state.prompt.trim()

        if (prompt.isBlank() || state.isThinking) {
            return
        }

        val userMessage = HachyMessage(
            id = messageIds.incrementAndGet(),
            text = prompt,
            isUser = true
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                prompt = "",
                isThinking = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            repository.ask(
                prompt = prompt,
                conversationId = state.conversationId
            ).onSuccess { result ->
                val assistantMessage = HachyMessage(
                    id = messageIds.incrementAndGet(),
                    text = result.answer,
                    isUser = false
                )

                _uiState.update {
                    it.copy(
                        messages = it.messages + assistantMessage,
                        conversationId = result.conversationId,
                        isThinking = false,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isThinking = false,
                        errorMessage = friendlyError(throwable)
                    )
                }
            }
        }
    }

    fun retry() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    private fun friendlyError(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException ->
                "No internet connection. Check your network and retry."

            is SocketTimeoutException ->
                "Hachy took too long to respond. Please retry."

            else -> {
                val raw = throwable.message.orEmpty()

                when {
                    raw.contains("401") ->
                        "Your session has expired. Please log in again."

                    raw.contains("404") ->
                        "Ask Hachy is not available at the expected endpoint yet."

                    raw.isNotBlank() ->
                        raw

                    else ->
                        "Hachy could not answer right now. Please retry."
                }
            }
        }
    }
}
