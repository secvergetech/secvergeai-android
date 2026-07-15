package com.secverge.ai.ui.council

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AICouncilViewModel(private val repository: AICouncilRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AICouncilUiState())
    val uiState: StateFlow<AICouncilUiState> = _uiState.asStateFlow()

    fun updateQuestion(value: String) = _uiState.update { it.copy(question = value, errorMessage = null) }
    fun toggleModel(name: String) = _uiState.update { state ->
        state.copy(participants = state.participants.map { if (it.name == name) it.copy(selected = !it.selected) else it })
    }
    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    fun createCouncil() {
        val state = _uiState.value
        if (!state.canStart) return
        loading()
        viewModelScope.launch {
            repository.create(state.question.trim(), state.selectedModels).onSuccess { response ->
                val id = response.sessionId ?: error("The backend did not return a council session ID.")
                _uiState.update { it.copy(sessionId = id, stage = CouncilStage.Debate, isLoading = false) }
            }.onFailure(::failure)
        }
    }

    fun runDebate() {
        val state = _uiState.value
        val id = state.sessionId ?: return
        val round = (state.contributions.maxOfOrNull { it.round } ?: 0) + 1
        loading()
        viewModelScope.launch {
            repository.debate(id, round).onSuccess { response ->
                val mapped = response.contributions.map {
                    CouncilContribution(UUID.randomUUID().toString(), it.model, it.content, it.round ?: round)
                }
                if (mapped.isEmpty()) error("The backend returned no debate contributions.")
                _uiState.update { it.copy(contributions = it.contributions + mapped, isLoading = false) }
            }.onFailure(::failure)
        }
    }

    fun finalizeCouncil() {
        val id = _uiState.value.sessionId ?: return
        loading()
        viewModelScope.launch {
            repository.finalize(id).onSuccess { response ->
                val text = response.consensus?.takeIf(String::isNotBlank)
                    ?: response.summary?.takeIf(String::isNotBlank)
                    ?: error("The backend returned an empty consensus.")
                _uiState.update { it.copy(consensus = text, stage = CouncilStage.Consensus, isLoading = false) }
            }.onFailure(::failure)
        }
    }

    private fun loading() = _uiState.update { it.copy(isLoading = true, errorMessage = null) }
    private fun failure(t: Throwable) = _uiState.update { it.copy(isLoading = false, errorMessage = friendly(t)) }
    private fun friendly(t: Throwable): String {
        val raw = t.message.orEmpty()
        return when {
            raw.contains("401") -> "Your session has expired. Please log in again."
            raw.contains("404") -> "The AI Council endpoint is not available yet."
            raw.contains("timeout", true) -> "The council took too long to respond."
            raw.isNotBlank() -> raw
            else -> "The council could not complete this step."
        }
    }
}
