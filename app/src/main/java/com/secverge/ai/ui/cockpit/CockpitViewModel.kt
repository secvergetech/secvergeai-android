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
        "Ask Hachy",
        "GPT",
        "Claude",
        "Gemini"
    )

    private val _uiState = MutableStateFlow(CockpitUiState())
    val uiState: StateFlow<CockpitUiState> = _uiState.asStateFlow()

    fun updatePrompt(value: String) {
        _uiState.update { current ->
            if (current.prompt == value) {
                current.copy(errorMessage = null)
            } else {
                current.copy(
                    prompt = value,
                    currentStep = CockpitStep.Context,
                    snapshotId = null,
                    switchId = null,
                    recommendedModel = null,
                    fidelityScore = null,
                    errorMessage = null
                )
            }
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
                currentStep = CockpitStep.Context,
                snapshotId = null,
                switchId = null,
                recommendedModel = null,
                fidelityScore = null,
                errorMessage = null
            )
        }
    }

    fun setToModel(value: String) {
        _uiState.update { current ->
            if (value == current.fromModel) {
                current.copy(
                    errorMessage = "Choose a different switch target."
                )
            } else {
                current.copy(
                    toModel = value,
                    currentStep = CockpitStep.Context,
                    snapshotId = null,
                    switchId = null,
                    recommendedModel = null,
                    fidelityScore = null,
                    errorMessage = null
                )
            }
        }
    }

    fun setTaskType(value: String) {
        _uiState.update { current ->
            current.copy(
                taskType = value,
                currentStep = CockpitStep.Context,
                snapshotId = null,
                switchId = null,
                recommendedModel = null,
                fidelityScore = null,
                errorMessage = null
            )
        }
    }

    fun setPrivacy(value: String) {
        _uiState.update { current ->
            current.copy(
                privacy = value,
                currentStep = CockpitStep.Context,
                snapshotId = null,
                switchId = null,
                recommendedModel = null,
                fidelityScore = null,
                errorMessage = null
            )
        }
    }

    fun swapModels() {
        _uiState.update { current ->
            current.copy(
                fromModel = current.toModel,
                toModel = current.fromModel,
                currentStep = CockpitStep.Context,
                snapshotId = null,
                switchId = null,
                recommendedModel = null,
                fidelityScore = null,
                errorMessage = null
            )
        }
    }

    fun buildContext() {
        val state = _uiState.value
        if (!state.canBuildContext) return

        setLoading()

        viewModelScope.launch {
            repository.buildContext(
                prompt = state.prompt.trim(),
                fromModel = state.fromModel
            ).onSuccess { response ->
                val snapshotId = response.snapshotId
                    ?: error("The backend did not return a context snapshot ID.")

                _uiState.update {
                    it.copy(
                        currentStep = CockpitStep.Route,
                        snapshotId = snapshotId,
                        fidelityScore = response.fidelityScore,
                        isLoading = false,
                        consoleEntries = it.consoleEntries +
                            CockpitConsoleEntry(
                                title = "Context snapshot created",
                                content = response.summary
                                    ?: "Context is ready for routing.",
                                technicalProof = response.raw?.toString()
                            )
                    )
                }
            }.onFailure(::showFailure)
        }
    }

    fun recommendRoute() {
        val state = _uiState.value
        if (!state.canRoute) return

        setLoading()

        viewModelScope.launch {
            repository.recommendRoute(
                prompt = state.prompt.trim(),
                taskType = state.taskType,
                privacy = state.privacy,
                fromModel = state.fromModel,
                toModel = state.toModel
            ).onSuccess { response ->
                val recommended = response.recommendedModel
                    ?.takeIf(String::isNotBlank)
                    ?.takeUnless { it == state.fromModel }
                    ?: state.toModel

                _uiState.update {
                    it.copy(
                        currentStep = CockpitStep.Switch,
                        recommendedModel = recommended,
                        isLoading = false,
                        consoleEntries = it.consoleEntries +
                            CockpitConsoleEntry(
                                title = "Route recommended",
                                content = buildString {
                                    append(recommended)
                                    response.reason
                                        ?.takeIf(String::isNotBlank)
                                        ?.let { reason ->
                                            append(" · ")
                                            append(reason)
                                        }
                                },
                                technicalProof = response.raw?.toString()
                            )
                    )
                }
            }.onFailure(::showFailure)
        }
    }

    fun switchAndCarry() {
        val state = _uiState.value
        val snapshotId = state.snapshotId ?: return
        val destination = state.recommendedModel ?: state.toModel

        if (!state.canSwitch) return

        setLoading()

        viewModelScope.launch {
            repository.switchModel(
                snapshotId = snapshotId,
                fromModel = state.fromModel,
                toModel = destination
            ).onSuccess { response ->
                val switchId = response.switchId
                    ?: error("The backend did not return a switch ID.")

                _uiState.update {
                    it.copy(
                        currentStep = CockpitStep.Execute,
                        switchId = switchId,
                        fidelityScore = response.fidelityScore
                            ?: it.fidelityScore,
                        isLoading = false,
                        consoleEntries = it.consoleEntries +
                            CockpitConsoleEntry(
                                title = "Context carried forward",
                                content = response.carriedContext
                                    ?: "Context was transferred to $destination.",
                                technicalProof = response.raw?.toString()
                            )
                    )
                }
            }.onFailure(::showFailure)
        }
    }

    fun execute() {
        val state = _uiState.value
        if (!state.canExecute) return

        setLoading()

        viewModelScope.launch {
            repository.execute(
                switchId = state.switchId,
                prompt = state.prompt.trim(),
                model = state.recommendedModel ?: state.toModel
            ).onSuccess { response ->
                val output = response.output
                    ?.takeIf(String::isNotBlank)
                    ?: response.response
                        ?.takeIf(String::isNotBlank)
                    ?: error("The backend returned an empty execution response.")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        consoleEntries = it.consoleEntries +
                            CockpitConsoleEntry(
                                title = "Execution complete",
                                content = output,
                                technicalProof = response.raw?.toString()
                            )
                    )
                }
            }.onFailure(::showFailure)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
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
                "The Cockpit step could not be completed."
        }
    }
}
