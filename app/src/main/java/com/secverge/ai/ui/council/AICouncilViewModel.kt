package com.secverge.ai.ui.council

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AICouncilViewModel(private val repository: AICouncilRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AICouncilUiState())
    val uiState = _uiState.asStateFlow()

    fun updateQuestion(value: String) {
        if (_uiState.value.stage == CouncilStage.Configure && !_uiState.value.isLoading) {
            _uiState.update { it.copy(question = value, errorMessage = null) }
        }
    }
    fun toggleModel(name: String) {
        if (_uiState.value.stage != CouncilStage.Configure || _uiState.value.isLoading) return
        _uiState.update { it.copy(participants = it.participants.map { model ->
            if (model.name == name) model.copy(selected = !model.selected) else model
        }) }
    }
    fun dismissError() { _uiState.update { it.copy(errorMessage = null) } }
    private fun failed(error: Throwable) {
        _uiState.update { it.copy(isLoading = false, errorMessage = "Council request failed: ${error.message ?: error.javaClass.simpleName}") }
    }
    fun createCouncil() {
        if (!_uiState.value.canStart) return
        // Local grouping only; perspectives and consensus always come from the provider API.
        _uiState.update { it.copy(sessionId = UUID.randomUUID().toString(),
            stage = CouncilStage.Debate, contributions = emptyList(), consensus = null, errorMessage = null) }
    }
    fun runDebate() {
        val state = _uiState.value
        if (!state.canDebate) return
        val round = (state.contributions.maxOfOrNull { it.round } ?: 0) + 1
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            for (model in state.selectedModels) {
                val result = repository.perspective(state.question.trim(), model)
                if (result.isFailure) { failed(result.exceptionOrNull()!!); return@launch }
                val answer = result.getOrThrow()
                _uiState.update { it.copy(contributions = it.contributions + CouncilContribution(
                    UUID.randomUUID().toString(), "${answer.label} (${answer.providerModel})", answer.content, round)) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    fun finalizeCouncil() {
        val state = _uiState.value
        if (!state.canFinalize) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.consensus(state.question.trim(), state.contributions).onSuccess { result ->
                _uiState.update { it.copy(consensus = result.consensus?.takeIf(String::isNotBlank) ?: result.content,
                    stage = CouncilStage.Consensus, isLoading = false) }
            }.onFailure(::failed)
        }
    }
}
