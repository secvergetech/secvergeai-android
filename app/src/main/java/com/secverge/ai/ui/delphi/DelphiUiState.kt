package com.secverge.ai.ui.delphi

data class DelphiParticipant(
    val name: String,
    val selected: Boolean = true
)

data class DelphiSubmission(
    val id: String,
    val model: String,
    val content: String,
    val revealed: Boolean = false
)

data class DelphiDebateContribution(
    val id: String,
    val model: String,
    val content: String,
    val round: Int
)

enum class DelphiStage {
    Configure,
    Blind,
    Reveal,
    Debate,
    Converge
}

data class DelphiUiState(
    val question: String = "",
    val participants: List<DelphiParticipant> = listOf(
        DelphiParticipant("GPT"),
        DelphiParticipant("Claude"),
        DelphiParticipant("Gemini"),
        DelphiParticipant("Llama")
    ),
    val sessionId: String? = null,
    val stage: DelphiStage = DelphiStage.Configure,
    val submissions: List<DelphiSubmission> = emptyList(),
    val contributions: List<DelphiDebateContribution> = emptyList(),
    val consensus: String? = null,
    val confidence: Double? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedModels: List<String>
        get() = participants.filter { it.selected }.map { it.name }

    val canCreate: Boolean
        get() = question.isNotBlank() && selectedModels.size >= 2 && !isLoading

    val canCollectBlind: Boolean
        get() = sessionId != null && stage == DelphiStage.Blind && !isLoading

    val canReveal: Boolean
        get() = submissions.isNotEmpty() && stage == DelphiStage.Reveal && !isLoading

    val canDebate: Boolean
        get() = submissions.all { it.revealed } && submissions.isNotEmpty() && stage == DelphiStage.Debate && !isLoading

    val canConverge: Boolean
        get() = contributions.isNotEmpty() && stage == DelphiStage.Converge && !isLoading
}
