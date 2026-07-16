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
    "Llama",
    "Mistral",
    "Qwen"
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

        setLoading("send")

        viewModelScope.launch {
            repository.execute(
                switchId = null,
                prompt = state.prompt.trim(),
                model = toBackendModel(state.fromModel)
            ).onSuccess { response ->
                val output = response.output
                    ?.takeIf(String::isNotBlank)
                    ?: response.response
                        ?.takeIf(String::isNotBlank)
                    ?: "Message sent to ${state.fromModel}."

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeAction = null,
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

        setLoading("switch")

        val sourceModel = toBackendModel(state.fromModel)
        val targetModel = toBackendModel(state.toModel)

        viewModelScope.launch {
            repository.buildContext(
                prompt = state.prompt.trim(),
                fromModel = sourceModel
            ).onSuccess { snapshot ->
                val snapshotId = snapshot.snapshotId
                    ?: error("The backend did not return a context snapshot ID.")

                repository.switchModel(
                    snapshotId = snapshotId,
                    fromModel = sourceModel,
                    toModel = targetModel
                ).onSuccess { switchResponse ->
                    val switchId = switchResponse.switchId

                    repository.execute(
                        switchId = switchId,
                        prompt = state.prompt.trim(),
                        model = targetModel
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
                                activeAction = null,
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

    fun recordLocalMessage(
        title: String,
        content: String
    ) {
        _uiState.update {
            it.copy(
                consoleEntries = it.consoleEntries +
                    CockpitConsoleEntry(
                        title = title,
                        content = content
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
        return modelOptions.firstOrNull { it != excluding } ?: "Llama"
    }

    private fun toBackendModel(displayModel: String): String {
        return when (displayModel) {
            "Ask Hachy / Gemma" -> "Ask Hachy"
            "Llama" -> "llama"
            "Mistral" -> "mistral"
            "Qwen" -> "qwen"
            else -> displayModel
        }
    }

    private fun setLoading(action: String) {
        _uiState.update {
            it.copy(
                isLoading = true,
                activeAction = action,
                errorMessage = null
            )
        }
    }

    private fun showFailure(throwable: Throwable) {
        _uiState.update {
            it.copy(
                isLoading = false,
                activeAction = null,
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
