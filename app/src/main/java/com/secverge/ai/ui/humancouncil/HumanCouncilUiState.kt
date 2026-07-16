package com.secverge.ai.ui.humancouncil

data class HumanParticipant(
    val name: String,
    val selected: Boolean = true
)

data class HumanRoomMessage(
    val id: String,
    val title: String,
    val content: String,
    val isModelResponse: Boolean = false
)

enum class HumanDecisionStage(
    val label: String,
    val subtitle: String
) {
    Frame(
        label = "1. Frame",
        subtitle = "Human objective"
    ),
    Compare(
        label = "2. Compare",
        subtitle = "Model viewpoints"
    ),
    Challenge(
        label = "3. Challenge",
        subtitle = "Directed model input"
    ),
    Decide(
        label = "4. Decide",
        subtitle = "Final memo"
    )
}

data class HumanCouncilUiState(
    val objective: String = "",
    val instruction: String = "",
    val participants: List<HumanParticipant> = listOf(
        HumanParticipant("Ask Hachy / Gemma"),
        HumanParticipant("Llama"),
        HumanParticipant("Mistral"),
        HumanParticipant("Qwen")
    ),
    val directModel: String = "Ask Hachy / Gemma",
    val stage: HumanDecisionStage = HumanDecisionStage.Frame,
    val roomStarted: Boolean = false,
    val runtimeOk: Boolean = false,
    val messages: List<HumanRoomMessage> = emptyList(),
    val decisionMemo: String? = null,
    val exportedPdfPath: String? = null,
    val isLoading: Boolean = false,
    val activeAction: String? = null,
    val errorMessage: String? = null
) {
    val selectedModels: List<String>
        get() = participants
            .filter { it.selected }
            .map { it.name }

    val directModelOptions: List<String>
        get() = selectedModels.ifEmpty {
            listOf("Ask Hachy / Gemma")
        }

    val hasModelResponses: Boolean
        get() = messages.any { it.isModelResponse }

    val canTestRuntime: Boolean
        get() = !isLoading

    val canStartRoom: Boolean
        get() = objective.isNotBlank() &&
            instruction.isNotBlank() &&
            selectedModels.isNotEmpty() &&
            !isLoading

    val canAskModels: Boolean
        get() = roomStarted &&
            selectedModels.isNotEmpty() &&
            !isLoading

    val canDirectModel: Boolean
        get() = roomStarted &&
            directModel.isNotBlank() &&
            selectedModels.contains(directModel) &&
            !isLoading

    val canBuildMemo: Boolean
        get() = roomStarted &&
            hasModelResponses &&
            !isLoading

    val canExportPdf: Boolean
        get() = messages.isNotEmpty() &&
            !isLoading
}
