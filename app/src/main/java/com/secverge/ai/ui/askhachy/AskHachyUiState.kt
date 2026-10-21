package com.secverge.ai.ui.askhachy

import kotlinx.serialization.Serializable

@Serializable
data class HachyMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean
)

@Serializable
data class AskHachySavedConversation(
    val localId: String,
    val remoteConversationId: String? = null,
    val title: String,
    val preview: String,
    val updatedAtMillis: Long,
    val messageCount: Int,
    val messages: List<HachyMessage>
)

data class AskHachyUiState(
    val messages: List<HachyMessage> = emptyList(),
    val prompt: String = "",
    val conversationId: String? = null,
    val activeHistoryId: String? = null,
    val savedConversations: List<AskHachySavedConversation> = emptyList(),
    val isHistoryVisible: Boolean = false,
    val isThinking: Boolean = false,
    val errorMessage: String? = null
) {
    val canSend: Boolean
        get() = prompt.isNotBlank() && !isThinking
}
