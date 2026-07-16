package com.secverge.ai.ui.cockpit

data class CockpitConsoleEntry(
    val title: String,
    val content: String,
    val technicalProof: String? = null
)

data class CockpitUiState(
    val prompt: String = "",
    val fromModel: String = "Ask Hachy / Gemma",
    val toModel: String = "Llama",
    val snapshotId: String? = null,
    val switchId: String? = null,
    val fidelityScore: Double? = null,
    val consoleEntries: List<CockpitConsoleEntry> = emptyList(),
    val isLoading: Boolean = false,
    val activeAction: String? = null,
    val errorMessage: String? = null
) {
    val canSend: Boolean
        get() = prompt.isNotBlank() && !isLoading

    val canSwitchAndCarry: Boolean
        get() = prompt.isNotBlank() &&
            fromModel != toModel &&
            !isLoading
}
