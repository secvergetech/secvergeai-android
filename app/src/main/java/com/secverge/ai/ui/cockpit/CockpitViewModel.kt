package com.secverge.ai.ui.cockpit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CockpitViewModel(
    private val repository: CockpitRepository
) : ViewModel() {

    private val modelOptions = listOf(
        "Ask Hachy / Gemma",
        "GPT",
        "Claude",
        "Gemini"
    )

    private val _uiState = MutableStateFlow(CockpitUiState())
    val uiState: StateFlow<CockpitUiState> = _uiState.asStateFlow()

    fun updatePrompt(value: String) {
        _uiState.update {
            it.copy(
                prompt = value,
                snapshotId = null,
                switchId = null,
                fidelityScore = null,
                errorMessage = null
            )
        }
    }

    fun setFromModel(value: String) {
        _uiState.update { current ->
            val safeTarget = if (value == current.toModel) {
                fallbackModel(excluding = value)
            } else {
                current.toModel
            }

            current.copy(
                fromModel = value,
                toModel = safeTarget,
                snapshotId = null,
                switchId = null,
                fidelityScore = null,
                errorMessage = null
            )
        }
    }

    fun setToModel(value: String) {
        _uiState.update { current ->
            if (value == current.fromModel) {
                current.copy(
                    errorMessage = "Responding Model and Switch Target must be different."
                )
            } else {
                current.copy(
                    toModel = value,
                    snapshotId = null,
                    switchId = null,
                    fidelityScore = null,
                    errorMessage = null
                )
            }
        }
    }

    fun send() {
        val state = _uiState.value
        if (!state.canSend) return

        setLoading()

        viewModelScope.launch {
            repository.execute(
                switchId = null,
                prompt = state.prompt.trim(),
                model = state.fromModel
            ).onSuccess { response ->
                val output = response.output
                    ?.takeIf(String::isNotBlank)
                    ?: response.response
                        ?.takeIf(String::isNotBlank)
                    ?: "Message sent to ${state.fromModel}."

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        consoleEntries = it.consoleEntries +
                            CockpitConsoleEntry(
                                title = state.fromModel,
                                content = output,
                                technicalProof = response.raw?.toString()
                            )
                    )
                }
            }.onFailure(::showFailure)
        }
    }

    fun switchAndCarry() {
        val state = _uiState.value
        if (!state.canSwitchAndCarry) return

        setLoading()

        viewModelScope.launch {
            repository.buildContext(
                prompt = state.prompt.trim(),
                fromModel = state.fromModel
            ).onSuccess { snapshot ->
                val snapshotId = snapshot.snapshotId
                    ?: error("The backend did not return a context snapshot ID.")

                repository.switchModel(
                    snapshotId = snapshotId,
                    fromModel = state.fromModel,
                    toModel = state.toModel
                ).onSuccess { switchResponse ->
                    val switchId = switchResponse.switchId

                    repository.execute(
                        switchId = switchId,
                        prompt = state.prompt.trim(),
                        model = state.toModel
                    ).onSuccess { executeResponse ->
                        val output = executeResponse.output
                            ?.takeIf(String::isNotBlank)
                            ?: executeResponse.response
                                ?.takeIf(String::isNotBlank)
                            ?: switchResponse.carriedContext
                                ?.takeIf(String::isNotBlank)
                            ?: "Context carried from ${state.fromModel} to ${state.toModel}."

                        _uiState.update {
                            it.copy(
                                snapshotId = snapshotId,
                                switchId = switchId,
                                fidelityScore = switchResponse.fidelityScore
                                    ?: snapshot.fidelityScore,
                                isLoading = false,
                                consoleEntries = it.consoleEntries +
                                    CockpitConsoleEntry(
                                        title = "${state.fromModel} → ${state.toModel}",
                                        content = output,
                                        technicalProof = buildString {
                                            appendLine("Snapshot: $snapshotId")
                                            appendLine("Switch: ${switchId ?: "not returned"}")
                                            snapshot.raw?.let { raw ->
                                                appendLine("Snapshot raw: $raw")
                                            }
                                            switchResponse.raw?.let { raw ->
                                                appendLine("Switch raw: $raw")
                                            }
                                            executeResponse.raw?.let { raw ->
                                                appendLine("Execute raw: $raw")
                                            }
                                        }
                                    )
                            )
                        }
                    }.onFailure(::showFailure)
                }.onFailure(::showFailure)
            }.onFailure(::showFailure)
        }
    }

    fun showAdvanced() {
        _uiState.update {
            it.copy(
                consoleEntries = it.consoleEntries +
                    CockpitConsoleEntry(
                        title = "Advanced",
                        content = "Website parity mode is active. Android UCF now keeps only Responding Model, Switch Target, Send, Switch & Carry, Advanced, History, and Download PDF on the main screen."
                    )
            )
        }
    }

    fun showPdfStatus() {
        _uiState.update {
            it.copy(
                consoleEntries = it.consoleEntries +
                    CockpitConsoleEntry(
                        title = "Download PDF",
                        content = "PDF export button is now present for website parity. Mobile PDF export will connect when the PDF endpoint is available."
                    )
            )
        }
    }

    fun dismissError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    private fun fallbackModel(excluding: String): String {
        return modelOptions.firstOrNull { it != excluding } ?: "GPT"
    }

    private fun setLoading() {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }
    }

    private fun showFailure(throwable: Throwable) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = friendlyError(throwable)
            )
        }
    }

    private fun friendlyError(throwable: Throwable): String {
        val raw = throwable.message.orEmpty()

        return when {
            raw.contains("401") ->
                "Your session has expired. Please log in again."

            raw.contains("404") ->
                "The expected UCF endpoint is not available yet."

            raw.contains("timeout", ignoreCase = true) ->
                "The server took too long to respond."

            raw.isNotBlank() ->
                raw

            else ->
                "The UCF request could not be completed."
        }
    }
}
