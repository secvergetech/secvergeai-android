package com.secverge.ai.ui.delphi

data class DelphiSubmission(
    val model: String,
    val content: String,
    val revealed: Boolean = false
)

data class DelphiParticipant(
    val name: String,
    val selected: Boolean = true
)

data class DelphiRoomEntry(
    val id: String,
    val title: String,
    val model: String? = null,
    val content: String,
    val hiddenUntilReveal: Boolean = false
)

enum class DelphiStage(
    val label: String,
    val subtitle: String
) {
    Blind(
        label = "1. Blind",
        subtitle = "Independent views"
    ),
    Reveal(
        label = "2. Reveal",
        subtitle = "Show positions"
    ),
    Debate(
        label = "3. Debate",
        subtitle = "Challenge assumptions"
    ),
    Converge(
        label = "4. Converge",
        subtitle = "Rotating moderator"
    )
}

data class DelphiUiState(
    val question: String = "",
    val context: String = "",
    val runId: String? = null,
    val participants: List<DelphiParticipant> = listOf(
        DelphiParticipant("Model A"), DelphiParticipant("Model B")
    ),
    val stage: DelphiStage = DelphiStage.Blind,
    val runtimeOk: Boolean = false,
    val blindRoundComplete: Boolean = false,
    val revealed: Boolean = false,
    val debated: Boolean = false,
    val converged: Boolean = false,
    val entries: List<DelphiRoomEntry> = emptyList(),
    val consensus: String? = null,
    val exportedPdfPath: String? = null,
    val isLoading: Boolean = false,
    val activeAction: String? = null,
    val errorMessage: String? = null
) {
    val selectedModels: List<String>
        get() = participants
            .filter { it.selected }
            .map { it.name }

    val canTestGemmaMax: Boolean
        get() = !isLoading

    val canBlindRound: Boolean
        get() = question.isNotBlank() &&
            selectedModels.size >= 2 &&
            !blindRoundComplete && !isLoading

    val canReveal: Boolean
        get() = blindRoundComplete &&
            !revealed &&
            !isLoading

    val canDebate: Boolean
        get() = revealed &&
            !debated &&
            !isLoading

    val canConverge: Boolean
        get() = debated &&
            !converged &&
            !isLoading

    val canExportPdf: Boolean
        get() = entries.isNotEmpty() &&
            !isLoading
}
