package com.secverge.ai.ui.humancouncil

data class HumanCouncilMessage(
    val id: String,
    val speaker: String,
    val content: String,
    val model: String? = null,
    val isHuman: Boolean = false
)

data class HumanCouncilUiState(
    val topic: String = "",
    val input: String = "",
    val models: List<String> = listOf("GPT", "Claude", "Gemini"),
    val directTo: String = "ALL",
    val sessionId: String? = null,
    val messages: List<HumanCouncilMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val roomCreated: Boolean get() = sessionId != null
    val canCreate: Boolean get() = topic.isNotBlank() && models.size >= 2 && !isLoading
    val canSend: Boolean get() = roomCreated && input.isNotBlank() && !isLoading
}
