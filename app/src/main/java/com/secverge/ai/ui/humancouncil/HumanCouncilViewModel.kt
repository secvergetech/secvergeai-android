package com.secverge.ai.ui.humancouncil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class HumanCouncilViewModel(private val repository: HumanCouncilRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HumanCouncilUiState())
    val uiState: StateFlow<HumanCouncilUiState> = _uiState.asStateFlow()

    fun updateTopic(v: String) = _uiState.update { it.copy(topic = v, errorMessage = null) }
    fun updateInput(v: String) = _uiState.update { it.copy(input = v, errorMessage = null) }
    fun setDirectTo(v: String) = _uiState.update { it.copy(directTo = v) }
    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    fun createRoom() {
        val state = _uiState.value
        if (!state.canCreate) return
        loading()
        viewModelScope.launch {
            repository.create(state.topic.trim(), state.models).onSuccess { response ->
                val id = response.sessionId ?: error("The backend did not return a collaboration room ID.")
                _uiState.update { it.copy(sessionId = id, isLoading = false, messages = listOf(HumanCouncilMessage(UUID.randomUUID().toString(), "Gemma Moderator", "Room created. Share a contribution with every model or direct one model privately.", "Gemma"))) }
            }.onFailure(::failure)
        }
    }

    fun send() {
        val state = _uiState.value
        val id = state.sessionId ?: return
        if (!state.canSend) return
        val text = state.input.trim()
        _uiState.update { it.copy(input = "", isLoading = true, errorMessage = null, messages = it.messages + HumanCouncilMessage(UUID.randomUUID().toString(), "You", text, isHuman = true)) }
        viewModelScope.launch {
            if (state.directTo == "ALL") {
                repository.contribute(id, text).onSuccess { response ->
                    val mapped = response.responses.map { HumanCouncilMessage(UUID.randomUUID().toString(), it.speaker, it.content, it.model) }
                    if (mapped.isEmpty()) error("The backend returned no model responses.")
                    _uiState.update { it.copy(messages = it.messages + mapped, isLoading = false) }
                }.onFailure(::failure)
            } else {
                repository.direct(id, state.directTo, text).onSuccess { response ->
                    val item = response.response ?: error("The backend returned no directed response.")
                    _uiState.update { it.copy(messages = it.messages + HumanCouncilMessage(UUID.randomUUID().toString(), item.speaker, item.content, item.model), isLoading = false) }
                }.onFailure(::failure)
            }
        }
    }

    private fun loading() = _uiState.update { it.copy(isLoading = true, errorMessage = null) }
    private fun failure(t: Throwable) = _uiState.update { it.copy(isLoading = false, errorMessage = friendly(t)) }
    private fun friendly(t: Throwable): String {
        val raw = t.message.orEmpty()
        return when {
            raw.contains("401") -> "Your session has expired. Please log in again."
            raw.contains("404") -> "The Human Council endpoint is not available yet."
            raw.contains("timeout", true) -> "The collaboration room took too long to respond."
            raw.isNotBlank() -> raw
            else -> "The collaboration step could not be completed."
        }
    }
}
