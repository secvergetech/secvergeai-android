package com.secverge.ai.ui.delphi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DelphiViewModel(private val repository: DelphiRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DelphiUiState())
    val uiState = _uiState.asStateFlow()

    fun updateQuestion(value: String) {
        if (!_uiState.value.isLoading) _uiState.update { DelphiUiState(question = value, context = it.context) }
    }
    fun updateContext(value: String) {
        if (!_uiState.value.isLoading) _uiState.update { DelphiUiState(question = it.question, context = value) }
    }
    // Blind labels and hidden identities are assigned by production.
    fun toggleParticipant(name: String) = Unit
    private fun begin(action: String) {
        _uiState.update { it.copy(isLoading = true, activeAction = action, errorMessage = null) }
    }
    private fun failed(error: Throwable) {
        _uiState.update { it.copy(isLoading = false, activeAction = null,
            errorMessage = "Delphi request failed: ${error.message ?: error.javaClass.simpleName}") }
    }
    fun testGemmaMax() {
        if (!_uiState.value.canTestGemmaMax) return
        begin("test")
        viewModelScope.launch {
            repository.testRuntime().onSuccess { answer ->
                _uiState.update { it.copy(runtimeOk = true, isLoading = false, activeAction = null,
                    entries = it.entries + entry("Gemma live runtime", answer, "gemma3:4b-it-q8_0")) }
            }.onFailure(::failed)
        }
    }
    fun runBlindRound() {
        val state = _uiState.value
        if (!state.canBlindRound) return
        begin("blind")
        viewModelScope.launch {
            var runId = state.runId
            if (runId == null) {
                val result = repository.create(state.question.trim() +
                    if (state.context.isBlank()) "" else "\nContext / constraints:\n${state.context.trim()}")
                if (result.isFailure) { failed(result.exceptionOrNull()!!); return@launch }
                val run = result.getOrThrow()
                runId = run.runId
                _uiState.update { it.copy(runId = runId, participants = run.cards.map { card -> DelphiParticipant(card.modelLabel) },
                    entries = emptyList()) }
            }
            // Resume a partially collected round without replacing successful submissions.
            for (label in _uiState.value.selectedModels) {
                if (_uiState.value.entries.any { it.model == label && it.hiddenUntilReveal }) continue
                val result = repository.submit(runId, label)
                if (result.isFailure) { failed(result.exceptionOrNull()!!); return@launch }
                val submission = result.getOrThrow()
                _uiState.update { it.copy(entries = it.entries +
                    entry("$label · Blind view", submission.response, label, hidden = true)) }
            }
            _uiState.update { it.copy(blindRoundComplete = true, stage = DelphiStage.Reveal,
                isLoading = false, activeAction = null) }
        }
    }
    fun revealPositions() {
        if (!_uiState.value.canReveal) return
        // Production reports Reveal(client-side); no public reveal endpoint exists.
        _uiState.update { it.copy(revealed = true, stage = DelphiStage.Debate) }
    }
    fun runDebate() {
        val state = _uiState.value
        if (!state.canDebate) return
        begin("debate")
        viewModelScope.launch {
            repository.debate(requireNotNull(state.runId)).onSuccess { debate ->
                _uiState.update { it.copy(debated = true, stage = DelphiStage.Converge,
                    isLoading = false, activeAction = null,
                    entries = it.entries + entry("Delphi debate", debate.response, debate.model)) }
            }.onFailure(::failed)
        }
    }
    fun converge() {
        val state = _uiState.value
        if (!state.canConverge) return
        begin("converge")
        viewModelScope.launch {
            repository.converge(requireNotNull(state.runId)).onSuccess { result ->
                _uiState.update { it.copy(converged = true, consensus = result.consensus,
                    isLoading = false, activeAction = null,
                    entries = it.entries + entry("Final Delphi Consensus", result.consensus, result.model)) }
            }.onFailure(::failed)
        }
    }
    fun markPdfExported(path: String) { _uiState.update { it.copy(exportedPdfPath = path) } }
    fun dismissError() { _uiState.update { it.copy(errorMessage = null) } }
    private fun entry(title: String, content: String, model: String, hidden: Boolean = false) =
        DelphiRoomEntry(UUID.randomUUID().toString(), title, model, content, hidden)
}
