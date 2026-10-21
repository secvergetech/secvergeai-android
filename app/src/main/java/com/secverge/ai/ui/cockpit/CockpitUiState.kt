package com.secverge.ai.ui.cockpit

import kotlinx.serialization.Serializable
import java.util.UUID

val DefaultCockpitModelOptions = listOf(
    "Ask Hachy / Gemma",
    "Llama",
    "Mistral",
    "Qwen"
)

@Serializable
data class CockpitConsoleEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val technicalProof: String? = null,
    val kind: String = "assistant",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Serializable
data class CockpitSavedSession(
    val localId: String,
    val title: String,
    val preview: String,
    val fromModel: String,
    val toModel: String,
    val snapshotId: String? = null,
    val switchId: String? = null,
    val fidelityScore: Double? = null,
    val updatedAtMillis: Long,
    val entries: List<CockpitConsoleEntry>
)

data class CockpitUiState(
    val prompt: String = "",
    val fromModel: String = "Ask Hachy / Gemma",
    val toModel: String = "Llama",
    val modelOptions: List<String> = DefaultCockpitModelOptions,
    val activeSessionId: String? = null,
    val savedSessions: List<CockpitSavedSession> = emptyList(),
    val isHistoryVisible: Boolean = false,
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
        get() = !isLoading &&
            fromModel != toModel &&
            (prompt.isNotBlank() || consoleEntries.isNotEmpty())
}
