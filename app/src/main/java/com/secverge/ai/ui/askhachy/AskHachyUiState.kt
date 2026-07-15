package com.secverge.ai.ui.askhachy

data class HachyMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean
)

data class AskHachyUiState(
    val messages: List<HachyMessage> = emptyList(),
    val prompt: String = "",
    val conversationId: String? = null,
    val isThinking: Boolean = false,
    val errorMessage: String? = null
) {
    val canSend: Boolean
        get() = prompt.isNotBlank() && !isThinking
}
