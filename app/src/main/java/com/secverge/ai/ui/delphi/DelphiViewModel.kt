package com.secverge.ai.ui.delphi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class DelphiViewModel(
    private val repository: DelphiRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DelphiUiState())
    val uiState: StateFlow<DelphiUiState> = _uiState.asStateFlow()

    fun updateQuestion(value: String) {
        _uiState.update { it.copy(question = value, errorMessage = null) }
    }

    fun toggleModel(name: String) {
        _uiState.update { state ->
            if (state.stage != DelphiStage.Configure) return@update state
            state.copy(
                participants = state.participants.map { participant ->
                    if (participant.name == name) participant.copy(selected = !participant.selected)
                    else participant
                },
                errorMessage = null
            )
        }
    }

    fun createSession() {
        val state = _uiState.value
        if (!state.canCreate) return
        setLoading()
        viewModelScope.launch {
            repository.create(state.question.trim(), state.selectedModels)
                .onSuccess { response ->
                    val sessionId = response.sessionId
                        ?: error("The backend did not return a Delphi session ID.")
                    _uiState.update {
                        it.copy(
                            sessionId = sessionId,
                            stage = DelphiStage.Blind,
                            isLoading = false
                        )
                    }
                }
                .onFailure(::setFailure)
        }
    }

    fun collectBlindSubmissions() {
        val sessionId = _uiState.value.sessionId ?: return
        if (!_uiState.value.canCollectBlind) return
        setLoading()
        viewModelScope.launch {
            repository.collectBlindSubmissions(sessionId)
                .onSuccess { response ->
                    val submissions = response.submissions.map { submission ->
                        DelphiSubmission(
                            id = UUID.randomUUID().toString(),
                            model = submission.model,
                            content = submission.content,
                            revealed = false
                        )
                    }
                    if (submissions.isEmpty()) error("The backend returned no blind submissions.")
                    _uiState.update {
                        it.copy(
                            submissions = submissions,
                            stage = DelphiStage.Reveal,
                            isLoading = false
                        )
                    }
                }
                .onFailure(::setFailure)
        }
    }

    fun revealSubmissions() {
        if (!_uiState.value.canReveal) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val total = _uiState.value.submissions.size
            repeat(total) { index ->
                delay(260)
                _uiState.update { state ->
                    state.copy(
                        submissions = state.submissions.mapIndexed { itemIndex, item ->
                            if (itemIndex <= index) item.copy(revealed = true) else item
                        }
                    )
                }
            }
            _uiState.update {
                it.copy(stage = DelphiStage.Debate, isLoading = false)
            }
        }
    }

    fun runDebate() {
        val state = _uiState.value
        val sessionId = state.sessionId ?: return
        if (!state.canDebate) return
        setLoading()
        viewModelScope.launch {
            repository.debate(sessionId)
                .onSuccess { response ->
                    val contributions = response.contributions.map { contribution ->
                        DelphiDebateContribution(
                            id = UUID.randomUUID().toString(),
                            model = contribution.model,
                            content = contribution.content,
                            round = contribution.round ?: 1
                        )
                    }
                    if (contributions.isEmpty()) error("The backend returned no debate contributions.")
                    _uiState.update {
                        it.copy(
                            contributions = contributions,
                            stage = DelphiStage.Converge,
                            isLoading = false
                        )
                    }
                }
                .onFailure(::setFailure)
        }
    }

    fun converge() {
        val state = _uiState.value
        val sessionId = state.sessionId ?: return
        if (!state.canConverge) return
        setLoading()
        viewModelScope.launch {
            repository.converge(sessionId)
                .onSuccess { response ->
                    val consensus = response.consensus?.takeIf { it.isNotBlank() }
                        ?: error("The backend returned an empty Delphi consensus.")
                    _uiState.update {
                        it.copy(
                            consensus = consensus,
                            confidence = response.confidence,
                            isLoading = false
                        )
                    }
                }
                .onFailure(::setFailure)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun reset() {
        _uiState.value = DelphiUiState()
    }

    private fun setLoading() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
    }

    private fun setFailure(throwable: Throwable) {
        _uiState.update {
            it.copy(isLoading = false, errorMessage = friendlyMessage(throwable))
        }
    }

    private fun friendlyMessage(throwable: Throwable): String {
        val raw = throwable.message.orEmpty()
        return when {
            raw.contains("401") -> "Your session has expired. Please log in again."
            raw.contains("404") -> "The Delphi endpoint is not available yet."
            raw.contains("timeout", ignoreCase = true) -> "Delphi took too long to respond. Please try again."
            raw.isNotBlank() -> raw
            else -> "Delphi could not complete this stage."
        }
    }
}
