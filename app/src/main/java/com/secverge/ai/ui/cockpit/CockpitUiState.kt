package com.secverge.ai.ui.cockpit

enum class CockpitStep {
    Context,
    Route,
    Switch,
    Execute
}

data class CockpitConsoleEntry(
    val title: String,
    val content: String,
    val technicalProof: String? = null
)

data class CockpitUiState(
    val prompt: String = "",
    val fromModel: String = "Ask Hachy",
    val toModel: String = "GPT",
    val taskType: String = "General",
    val privacy: String = "Standard",
    val currentStep: CockpitStep = CockpitStep.Context,
    val snapshotId: String? = null,
    val switchId: String? = null,
    val recommendedModel: String? = null,
    val fidelityScore: Double? = null,
    val consoleEntries: List<CockpitConsoleEntry> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val canBuildContext: Boolean
        get() = prompt.isNotBlank() && !isLoading

    val canRoute: Boolean
        get() = snapshotId != null && !isLoading

    val canSwitch: Boolean
        get() = snapshotId != null &&
            recommendedModel != null &&
            !isLoading

    val canExecute: Boolean
        get() = switchId != null && !isLoading
}
