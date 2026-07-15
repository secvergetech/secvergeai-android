package com.secverge.ai.ui.council

data class CouncilParticipant(
    val name: String,
    val selected: Boolean = true
)

data class CouncilContribution(
    val id: String,
    val model: String,
    val content: String,
    val round: Int
)

enum class CouncilStage { Configure, Debate, Consensus }

data class AICouncilUiState(
    val question: String = "",
    val participants: List<CouncilParticipant> = listOf(
        CouncilParticipant("GPT"), CouncilParticipant("Claude"),
        CouncilParticipant("Gemini"), CouncilParticipant("Llama")
    ),
    val sessionId: String? = null,
    val stage: CouncilStage = CouncilStage.Configure,
    val contributions: List<CouncilContribution> = emptyList(),
    val consensus: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedModels: List<String> get() = participants.filter { it.selected }.map { it.name }
    val canStart: Boolean get() = question.isNotBlank() && selectedModels.size >= 2 && !isLoading
    val canDebate: Boolean get() = sessionId != null && stage == CouncilStage.Debate && !isLoading
    val canFinalize: Boolean get() = contributions.isNotEmpty() && stage == CouncilStage.Debate && !isLoading
}
