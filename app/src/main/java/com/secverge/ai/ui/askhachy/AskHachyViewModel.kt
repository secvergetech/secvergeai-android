package com.secverge.ai.ui.askhachy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

class AskHachyViewModel(
    private val repository: AskHachyRepository,
    private val historyStore: AskHachyHistoryStore
) : ViewModel() {

    private val messageIds = AtomicLong(0L)

    private val _uiState = MutableStateFlow(
        AskHachyUiState(
            savedConversations =
                historyStore.loadConversations()
        )
    )

    val uiState: StateFlow<AskHachyUiState> =
        _uiState.asStateFlow()

    init {
        val maxSavedMessageId = _uiState.value
            .savedConversations
            .flatMap { it.messages }
            .maxOfOrNull { it.id }
            ?: 0L

        messageIds.set(maxSavedMessageId)
    }

    fun updatePrompt(value: String) {
        _uiState.update {
            it.copy(
                prompt = value,
                errorMessage = null
            )
        }
    }

    fun send() {
        val state = _uiState.value
        val prompt = state.prompt.trim()

        if (prompt.isBlank() || state.isThinking) {
            return
        }

        val activeHistoryId =
            state.activeHistoryId ?: createLocalHistoryId()

        val userMessage = HachyMessage(
            id = messageIds.incrementAndGet(),
            text = prompt,
            isUser = true
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                prompt = "",
                activeHistoryId = activeHistoryId,
                isHistoryVisible = false,
                isThinking = true,
                errorMessage = null
            )
        }

        persistActiveConversation()

        viewModelScope.launch {
            repository.ask(
                prompt = prompt,
                conversationId = state.conversationId
            ).onSuccess { result ->
                val assistantMessage = HachyMessage(
                    id = messageIds.incrementAndGet(),
                    text = result.answer,
                    isUser = false
                )

                _uiState.update { current ->
                    if (current.activeHistoryId != activeHistoryId) {
                        current
                    } else {
                        current.copy(
                            messages = current.messages +
                                assistantMessage,
                            conversationId =
                                result.conversationId
                                    ?: current.conversationId,
                            isThinking = false,
                            errorMessage = null
                        )
                    }
                }

                persistActiveConversation()
            }.onFailure { throwable ->
                _uiState.update { current ->
                    if (current.activeHistoryId != activeHistoryId) {
                        current
                    } else {
                        current.copy(
                            isThinking = false,
                            errorMessage = friendlyError(throwable)
                        )
                    }
                }

                persistActiveConversation()
            }
        }
    }

    fun retry() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    fun startNewChat() {
        if (_uiState.value.isThinking) {
            return
        }

        persistActiveConversation()

        _uiState.value = AskHachyUiState(
            savedConversations =
                historyStore.loadConversations()
        )
    }

    fun toggleHistory() {
        _uiState.update {
            it.copy(
                savedConversations =
                    historyStore.loadConversations(),
                isHistoryVisible = !it.isHistoryVisible,
                errorMessage = null
            )
        }
    }

    fun loadConversation(localId: String) {
        if (_uiState.value.isThinking) {
            return
        }

        val conversation = historyStore
            .loadConversations()
            .firstOrNull { it.localId == localId }
            ?: return

        val maxMessageId = conversation.messages
            .maxOfOrNull { it.id }
            ?: 0L

        if (maxMessageId > messageIds.get()) {
            messageIds.set(maxMessageId)
        }

        _uiState.value = AskHachyUiState(
            messages = conversation.messages,
            prompt = "",
            conversationId = conversation.remoteConversationId,
            activeHistoryId = conversation.localId,
            savedConversations =
                historyStore.loadConversations(),
            isHistoryVisible = false,
            isThinking = false,
            errorMessage = null
        )
    }

    fun deleteConversation(localId: String) {
        if (_uiState.value.isThinking) {
            return
        }

        historyStore.deleteConversation(localId)

        val latestHistory = historyStore.loadConversations()
        val current = _uiState.value

        if (current.activeHistoryId == localId) {
            _uiState.value = AskHachyUiState(
                savedConversations = latestHistory
            )
        } else {
            _uiState.update {
                it.copy(savedConversations = latestHistory)
            }
        }
    }

    private fun persistActiveConversation() {
        val state = _uiState.value

        if (state.messages.isEmpty()) {
            return
        }

        val localId = state.activeHistoryId
            ?: createLocalHistoryId()

        val conversation = AskHachySavedConversation(
            localId = localId,
            remoteConversationId = state.conversationId,
            title = historyTitle(state.messages),
            preview = state.messages
                .lastOrNull()
                ?.text
                ?.trim()
                ?.take(PREVIEW_LIMIT)
                .orEmpty(),
            updatedAtMillis = System.currentTimeMillis(),
            messageCount = state.messages.size,
            messages = state.messages
        )

        historyStore.saveConversation(conversation)

        _uiState.update {
            it.copy(
                activeHistoryId = localId,
                savedConversations =
                    historyStore.loadConversations()
            )
        }
    }

    private fun historyTitle(
        messages: List<HachyMessage>
    ): String {
        val firstUserMessage = messages
            .firstOrNull { it.isUser }
            ?.text
            ?.trim()

        val fallback = messages
            .firstOrNull()
            ?.text
            ?.trim()

        return (firstUserMessage ?: fallback ?: "Ask Hachy chat")
            .replace(Regex("\\s+"), " ")
            .take(TITLE_LIMIT)
            .ifBlank { "Ask Hachy chat" }
    }

    private fun createLocalHistoryId(): String {
        return "hachy-${UUID.randomUUID()}"
    }

    private fun friendlyError(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException ->
                "No internet connection. Check your network and retry."

            is SocketTimeoutException ->
                "Hachy took too long to respond. Please retry."

            else -> {
                val raw = throwable.message.orEmpty()

                when {
                    raw.contains("401") ->
                        "Your session has expired. Please log in again."

                    raw.contains("404") ->
                        "Ask Hachy is not available at the expected endpoint yet."

                    raw.isNotBlank() ->
                        raw

                    else ->
                        "Hachy could not answer right now. Please retry."
                }
            }
        }
    }

    private companion object {
        const val TITLE_LIMIT = 52
        const val PREVIEW_LIMIT = 96
    }
}
