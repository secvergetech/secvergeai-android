package com.secverge.ai.ui.cockpit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secverge.ai.data.models.cockpit.AdapterStatusDto
import com.secverge.ai.data.models.cockpit.ContextSnapshotResponse
import com.secverge.ai.data.models.cockpit.ExecuteResponse
import com.secverge.ai.data.models.cockpit.RouteResponse
import com.secverge.ai.data.models.cockpit.SwitchResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Locale
import java.util.UUID

class CockpitViewModel(
    private val repository: CockpitRepository,
    private val sessionStore: CockpitSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CockpitUiState(
            savedSessions = sessionStore.loadSessions()
        )
    )

    val uiState: StateFlow<CockpitUiState> =
        _uiState.asStateFlow()

    init {
        refreshAdapters()
    }

    fun refreshAdapters() {
        viewModelScope.launch {
            repository.adapterStatus()
                .onSuccess { response ->
                    val options = response.adapters
                        .filter(::adapterIsUsable)
                        .map(::adapterDisplayName)
                        .filter { it.isNotBlank() }
                        .distinct()

                    if (options.isNotEmpty()) {
                        _uiState.update { current ->
                            val merged = (
                                listOf("Ask Hachy / Gemma") +
                                    options +
                                    DefaultCockpitModelOptions
                                ).distinct()

                            current.copy(
                                modelOptions = merged,
                                fromModel = if (current.fromModel in merged) {
                                    current.fromModel
                                } else {
                                    "Ask Hachy / Gemma"
                                },
                                toModel = if (
                                    current.toModel in merged &&
                                    current.toModel != current.fromModel
                                ) {
                                    current.toModel
                                } else {
                                    fallbackModel(
                                        excluding = "Ask Hachy / Gemma",
                                        options = merged
                                    )
                                }
                            )
                        }
                    }
                }
        }
    }

    fun updatePrompt(value: String) {
        _uiState.update {
            it.copy(
                prompt = value,
                errorMessage = null
            )
        }
    }

    fun setFromModel(value: String) {
        _uiState.update { current ->
            val options = current.modelOptions
                .ifEmpty { DefaultCockpitModelOptions }

            current.copy(
                fromModel = value,
                toModel = if (value == current.toModel) {
                    fallbackModel(
                        excluding = value,
                        options = options
                    )
                } else {
                    current.toModel
                },
                errorMessage = null
            )
        }
    }

    fun setToModel(value: String) {
        _uiState.update { current ->
            if (value == current.fromModel) {
                current.copy(
                    errorMessage =
                        "Responding Model and Switch Target must be different."
                )
            } else {
                current.copy(
                    toModel = value,
                    errorMessage = null
                )
            }
        }
    }

    fun swapModels() {
        _uiState.update { current ->
            current.copy(
                fromModel = current.toModel,
                toModel = current.fromModel,
                errorMessage = null
            )
        }
    }

    fun send() {
        val state = _uiState.value
        val cleanPrompt = state.prompt.trim()

        if (cleanPrompt.isBlank() || state.isLoading) {
            return
        }

        val sessionId = state.activeSessionId
            ?: createSessionId()

        val userEntry = CockpitConsoleEntry(
            title = "You → ${state.fromModel}",
            content = cleanPrompt,
            kind = "user"
        )

        _uiState.update { current ->
            current.copy(
                activeSessionId = sessionId,
                prompt = "",
                isHistoryVisible = false,
                isLoading = true,
                activeAction = "send",
                errorMessage = null,
                consoleEntries = current.consoleEntries +
                    userEntry
            )
        }

        persistActiveSession()

        viewModelScope.launch {
            if (isAskHachyDisplayModel(state.fromModel)) {
                sendDirectToAskHachy(
                    cleanPrompt = cleanPrompt,
                    displayModel = state.fromModel
                )
                return@launch
            }

            val directPrompt = buildDirectAnswerPrompt(
                question = cleanPrompt
            )

            executeWithModelFallback(
                switchId = state.switchId,
                snapshotId = state.snapshotId,
                routeId = null,
                prompt = directPrompt,
                displayModel = state.fromModel,
                preferredBackendModel = toBackendModel(state.fromModel)
            ).onSuccess { executionPair ->
                val response = executionPair.first
                val usedBackendModel = executionPair.second

                val output = response.bestText()
                    ?: "${state.fromModel} responded, but Android could not find a readable answer field in the backend result."

                val assistantEntry = CockpitConsoleEntry(
                    title = state.fromModel,
                    content = output,
                    technicalProof = buildString {
                        appendLine("Endpoint: /api/v1/multillm/execute")
                        appendLine("Display model: ${state.fromModel}")
                        appendLine("Used backend alias: $usedBackendModel")
                        appendLine("Direct current-question prompt: yes")
                        appendLine("Switch ID: ${state.switchId ?: "none"}")
                        appendLine("Snapshot ID: ${state.snapshotId ?: "none"}")
                        appendLine("Prompt cleared after successful send: yes")
                    }
                )

                _uiState.update { current ->
                    current.copy(
                        prompt = "",
                        isLoading = false,
                        activeAction = null,
                        errorMessage = null,
                        consoleEntries =
                            current.consoleEntries +
                                assistantEntry
                    )
                }

                persistActiveSession()
            }.onFailure { throwable ->
                appendTargetUnavailableResult(
                    sourceDisplay = state.fromModel,
                    targetDisplay = state.fromModel,
                    attemptedAliases = backendModelAliases(
                        displayModel = state.fromModel,
                        preferredBackendModel =
                            toBackendModel(state.fromModel)
                    ),
                    throwable = throwable
                )
            }
        }
    }

    private suspend fun sendDirectToAskHachy(
        cleanPrompt: String,
        displayModel: String
    ) {
        val strictPrompt = buildDirectAnswerPrompt(
            question = cleanPrompt
        )

        repository.askHachyDirect(
            prompt = strictPrompt,
            conversationId = null
        ).onSuccess { result ->
            val directAnswer = result.answer.trim()

            if (
                directAnswer.isNotBlank() &&
                !isStaleHistoryRefusal(directAnswer)
            ) {
                val assistantEntry = CockpitConsoleEntry(
                    title = displayModel,
                    content = directAnswer,
                    technicalProof = buildString {
                        appendLine("Endpoint: /api/v1/ask")
                        appendLine("Reason: Ask Hachy / Gemma direct answer")
                        appendLine("UCF execute bypassed: yes")
                        appendLine("Fallback needed: no")
                        appendLine("Old UCF context ignored for normal Get Answer: yes")
                        appendLine("Prompt cleared after successful send: yes")
                        appendLine("Conversation ID: ${result.conversationId ?: "not returned"}")
                    }
                )

                _uiState.update { current ->
                    current.copy(
                        prompt = "",
                        isLoading = false,
                        activeAction = null,
                        errorMessage = null,
                        consoleEntries =
                            current.consoleEntries +
                                assistantEntry
                    )
                }

                persistActiveSession()
                return
            }
        }

        // If /api/v1/ask fails OR gives stale old-history answer,
        // immediately fall back to /api/v1/multillm/execute.
        executeWithModelFallback(
            switchId = null,
            snapshotId = null,
            routeId = null,
            prompt = strictPrompt,
            displayModel = displayModel,
            preferredBackendModel = toBackendModel(displayModel)
        ).onSuccess { executionPair ->
            val response = executionPair.first
            val usedBackendModel = executionPair.second

            val output = response.bestText()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.takeIf { !isStaleHistoryRefusal(it) }

            val finalAnswer = output ?: buildBackendFallbackExplanation(
                question = cleanPrompt,
                displayModel = displayModel
            )

            val assistantEntry = CockpitConsoleEntry(
                title = displayModel,
                content = finalAnswer,
                technicalProof = buildString {
                    appendLine("Primary endpoint attempted: /api/v1/ask")
                    appendLine("Primary endpoint result: failed or stale")
                    appendLine("Fallback endpoint: /api/v1/multillm/execute")
                    appendLine("Used backend alias: $usedBackendModel")
                    appendLine("Standalone current-question mode: yes")
                    appendLine("Old conversation-history refusal filtered: yes")
                    appendLine("Prompt cleared after answer: yes")
                    appendLine("Conversation preserved: yes")
                }
            )

            _uiState.update { current ->
                current.copy(
                    prompt = "",
                    isLoading = false,
                    activeAction = null,
                    errorMessage = null,
                    consoleEntries =
                        current.consoleEntries +
                            assistantEntry
                )
            }

            persistActiveSession()
        }.onFailure { throwable ->
            val assistantEntry = CockpitConsoleEntry(
                title = "Ask Hachy / Gemma connection issue",
                content = buildString {
                    appendLine("I could not get a live answer from the current backend for this question.")
                    appendLine()
                    appendLine("Your question:")
                    appendLine(cleanPrompt)
                    appendLine()
                    appendLine("What happened:")
                    appendLine("- The direct Ask Hachy endpoint failed.")
                    appendLine("- The UCF execute fallback also failed.")
                    appendLine("- The app did not crash and your conversation was preserved.")
                    appendLine()
                    appendLine("This is now shown as a normal conversation result instead of a red dead-end error.")
                },
                technicalProof = buildString {
                    appendLine("Primary endpoint: /api/v1/ask")
                    appendLine("Fallback endpoint: /api/v1/multillm/execute")
                    appendLine("Display model: $displayModel")
                    appendLine("Attempted aliases:")
                    backendModelAliases(
                        displayModel = displayModel,
                        preferredBackendModel = toBackendModel(displayModel)
                    ).forEach {
                        appendLine("- $it")
                    }
                    appendLine()
                    appendLine("Backend error:")
                    appendLine(throwable.message ?: "No backend detail returned.")
                    appendLine()
                    appendLine("Prompt cleared: yes")
                    appendLine("Conversation preserved: yes")
                    appendLine("Backend/web/database unchanged: yes")
                },
                kind = "system"
            )

            _uiState.update { current ->
                current.copy(
                    prompt = "",
                    isLoading = false,
                    activeAction = null,
                    errorMessage = null,
                    consoleEntries =
                        current.consoleEntries +
                            assistantEntry
                )
            }

            persistActiveSession()
        }
    }


    fun switchAndCarry() {
        val state = _uiState.value

        if (state.isLoading || state.fromModel == state.toModel) {
            return
        }

        val typedPrompt = state.prompt.trim()
        val contextPayload = typedPrompt.ifBlank {
            transcriptForSwitch(state.consoleEntries)
        }

        if (contextPayload.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage =
                        "Ask a question first, then use Switch & Carry."
                )
            }
            return
        }

        val sessionId = state.activeSessionId
            ?: createSessionId()

        val sourceDisplay = state.fromModel
        val targetDisplay = state.toModel
        val sourceModel = toBackendModel(sourceDisplay)
        val targetModel = toBackendModel(targetDisplay)

        val newEntries = if (typedPrompt.isNotBlank()) {
            listOf(
                CockpitConsoleEntry(
                    title = "You → UCF Context",
                    content = typedPrompt,
                    kind = "user"
                )
            )
        } else {
            emptyList()
        }

        _uiState.update { current ->
            current.copy(
                activeSessionId = sessionId,
                prompt = "",
                isHistoryVisible = false,
                isLoading = true,
                activeAction = "switch",
                errorMessage = null,
                consoleEntries =
                    current.consoleEntries + newEntries
            )
        }

        persistActiveSession()

        viewModelScope.launch {
            val snapshot = repository.buildContext(
                prompt = contextPayload,
                fromModel = sourceModel
            ).getOrNull()

            val snapshotId = snapshot?.bestSnapshotId()

            if (snapshotId.isNullOrBlank()) {
                carryWithoutBackendSnapshot(
                    typedPrompt = typedPrompt,
                    contextPayload = contextPayload,
                    sourceDisplay = sourceDisplay,
                    targetDisplay = targetDisplay,
                    targetModel = targetModel,
                    state = state
                )
                return@launch
            }

            val route = repository.recommendRoute(
                prompt = contextPayload,
                taskType = "general",
                privacy = "balanced",
                fromModel = sourceModel,
                toModel = targetModel
            ).getOrNull()

            val switchResponse = repository.switchModel(
                snapshotId = snapshotId,
                fromModel = sourceModel,
                toModel = targetModel
            ).getOrNull()

            val switchId = switchResponse?.bestSwitchId()

            val executionPrompt = buildCarryPrompt(
                sourceDisplay = sourceDisplay,
                targetDisplay = targetDisplay,
                contextPayload = contextPayload,
                typedPrompt = typedPrompt
            )

            executeWithModelFallback(
                switchId = switchId,
                snapshotId = snapshotId,
                routeId = route?.bestRouteId(),
                prompt = executionPrompt,
                displayModel = targetDisplay,
                preferredBackendModel = targetModel
            ).onSuccess { executionPair ->
                val executeResponse = executionPair.first
                val usedBackendModel = executionPair.second

                val output = executeResponse.bestText()
                    ?: switchResponse?.bestCarriedContext()
                    ?: "Context carried from $sourceDisplay to $targetDisplay, but Android could not find a readable answer field in the backend result."

                val switchedEntry = CockpitConsoleEntry(
                    title = "UCF carried: $sourceDisplay → $targetDisplay",
                    content = output,
                    technicalProof = buildString {
                        appendLine("Endpoint chain:")
                        appendLine("1. /api/v1/multillm/context/snapshot")
                        appendLine("2. /api/v1/multillm/route")
                        appendLine("3. /api/v1/multillm/switch")
                        appendLine("4. /api/v1/multillm/execute")
                        appendLine("")
                        appendLine("From model: $sourceModel")
                        appendLine("To model: $targetModel")
                        appendLine("Used backend alias: $usedBackendModel")
                        appendLine("Snapshot ID: $snapshotId")
                        appendLine("Route ID: ${route?.bestRouteId() ?: "not returned"}")
                        appendLine("Switch ID: ${switchId ?: "not returned"}")
                        appendLine("Prompt cleared after switch: yes")
                        appendLine("Conversation preserved: yes")
                        appendLine("Snapshot fidelity: ${snapshot.bestFidelity() ?: "not returned"}")
                        appendLine("Switch fidelity: ${switchResponse?.bestFidelity() ?: "not returned"}")
                    }
                )

                val options = state.modelOptions
                    .ifEmpty { DefaultCockpitModelOptions }

                _uiState.update { current ->
                    current.copy(
                        fromModel = targetDisplay,
                        toModel = fallbackModel(
                            excluding = targetDisplay,
                            options = options
                        ),
                        snapshotId = snapshotId,
                        switchId = switchId,
                        fidelityScore =
                            switchResponse?.bestFidelity()
                                ?: snapshot.bestFidelity(),
                        prompt = "",
                        isLoading = false,
                        activeAction = null,
                        errorMessage = null,
                        consoleEntries =
                            current.consoleEntries +
                                switchedEntry
                    )
                }

                persistActiveSession()
            }.onFailure { throwable ->
                appendTargetUnavailableResult(
                    sourceDisplay = sourceDisplay,
                    targetDisplay = targetDisplay,
                    attemptedAliases = backendModelAliases(
                        displayModel = targetDisplay,
                        preferredBackendModel = targetModel
                    ),
                    throwable = throwable
                )
            }
        }
    }

    private suspend fun carryWithoutBackendSnapshot(
        typedPrompt: String,
        contextPayload: String,
        sourceDisplay: String,
        targetDisplay: String,
        targetModel: String,
        state: CockpitUiState
    ) {
        val executionPrompt = buildCarryPrompt(
            sourceDisplay = sourceDisplay,
            targetDisplay = targetDisplay,
            contextPayload = contextPayload,
            typedPrompt = typedPrompt
        )

        executeWithModelFallback(
            switchId = null,
            snapshotId = null,
            routeId = null,
            prompt = executionPrompt,
            displayModel = targetDisplay,
            preferredBackendModel = targetModel
        ).onSuccess { executionPair ->
            val executeResponse = executionPair.first
            val usedBackendModel = executionPair.second

            val output = executeResponse.bestText()
                ?: "Context carried to $targetDisplay, but Android could not find a readable answer field in the backend result."

            val carriedEntry = CockpitConsoleEntry(
                title = "UCF carried: $sourceDisplay → $targetDisplay",
                content = output,
                technicalProof = buildString {
                    appendLine("Endpoint chain:")
                    appendLine("1. /api/v1/multillm/context/snapshot returned no usable snapshot_id")
                    appendLine("2. Android used visible conversation as carried context")
                    appendLine("3. /api/v1/multillm/execute called target model directly")
                    appendLine("")
                    appendLine("From model: $sourceDisplay")
                    appendLine("To model: $targetDisplay")
                    appendLine("Backend target model: $targetModel")
                    appendLine("Used backend alias: $usedBackendModel")
                    appendLine("Snapshot ID: fallback-not-returned")
                    appendLine("Switch ID: fallback-not-required")
                    appendLine("Prompt cleared after switch: yes")
                    appendLine("Conversation preserved: yes")
                }
            )

            val options = state.modelOptions
                .ifEmpty { DefaultCockpitModelOptions }

            _uiState.update { current ->
                current.copy(
                    fromModel = targetDisplay,
                    toModel = fallbackModel(
                        excluding = targetDisplay,
                        options = options
                    ),
                    snapshotId = null,
                    switchId = null,
                    prompt = "",
                    isLoading = false,
                    activeAction = null,
                    errorMessage = null,
                    consoleEntries =
                        current.consoleEntries +
                            carriedEntry
                )
            }

            persistActiveSession()
        }.onFailure { throwable ->
            appendTargetUnavailableResult(
                sourceDisplay = sourceDisplay,
                targetDisplay = targetDisplay,
                attemptedAliases = backendModelAliases(
                    displayModel = targetDisplay,
                    preferredBackendModel = targetModel
                ),
                throwable = throwable
            )
        }
    }

    fun startNewSession() {
        if (_uiState.value.isLoading) {
            return
        }

        persistActiveSession()

        val current = _uiState.value
        val options = current.modelOptions
            .ifEmpty { DefaultCockpitModelOptions }

        _uiState.value = CockpitUiState(
            modelOptions = options,
            fromModel = "Ask Hachy / Gemma",
            toModel = fallbackModel(
                excluding = "Ask Hachy / Gemma",
                options = options
            ),
            savedSessions = sessionStore.loadSessions()
        )
    }

    fun toggleHistory() {
        _uiState.update {
            it.copy(
                savedSessions = sessionStore.loadSessions(),
                isHistoryVisible = !it.isHistoryVisible,
                errorMessage = null
            )
        }
    }

    fun loadSession(localId: String) {
        if (_uiState.value.isLoading) {
            return
        }

        val session = sessionStore
            .loadSessions()
            .firstOrNull { it.localId == localId }
            ?: return

        val options = _uiState.value.modelOptions
            .ifEmpty { DefaultCockpitModelOptions }

        _uiState.value = CockpitUiState(
            prompt = "",
            fromModel = session.fromModel,
            toModel = if (session.toModel != session.fromModel) {
                session.toModel
            } else {
                fallbackModel(
                    excluding = session.fromModel,
                    options = options
                )
            },
            modelOptions = options,
            activeSessionId = session.localId,
            savedSessions = sessionStore.loadSessions(),
            isHistoryVisible = false,
            snapshotId = session.snapshotId,
            switchId = session.switchId,
            fidelityScore = session.fidelityScore,
            consoleEntries = session.entries
        )
    }

    fun deleteSession(localId: String) {
        if (_uiState.value.isLoading) {
            return
        }

        sessionStore.deleteSession(localId)

        val latest = sessionStore.loadSessions()

        if (_uiState.value.activeSessionId == localId) {
            val current = _uiState.value

            _uiState.value = CockpitUiState(
                modelOptions = current.modelOptions
                    .ifEmpty { DefaultCockpitModelOptions },
                savedSessions = latest
            )
        } else {
            _uiState.update {
                it.copy(savedSessions = latest)
            }
        }
    }

    fun recordLocalMessage(
        title: String,
        content: String
    ) {
        val entry = CockpitConsoleEntry(
            title = title,
            content = content,
            kind = "system"
        )

        _uiState.update {
            it.copy(
                activeSessionId = it.activeSessionId
                    ?: createSessionId(),
                consoleEntries = it.consoleEntries + entry
            )
        }

        persistActiveSession()
    }

    fun dismissError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    private fun persistActiveSession() {
        val state = _uiState.value

        if (state.consoleEntries.isEmpty()) {
            return
        }

        val sessionId = state.activeSessionId
            ?: createSessionId()

        val session = CockpitSavedSession(
            localId = sessionId,
            title = sessionTitle(state.consoleEntries),
            preview = state.consoleEntries
                .lastOrNull()
                ?.content
                ?.trim()
                ?.replace(Regex("\\s+"), " ")
                ?.take(PREVIEW_LIMIT)
                .orEmpty(),
            fromModel = state.fromModel,
            toModel = state.toModel,
            snapshotId = state.snapshotId,
            switchId = state.switchId,
            fidelityScore = state.fidelityScore,
            updatedAtMillis = System.currentTimeMillis(),
            entries = state.consoleEntries
        )

        sessionStore.saveSession(session)

        _uiState.update {
            it.copy(
                activeSessionId = sessionId,
                savedSessions = sessionStore.loadSessions()
            )
        }
    }

    private suspend fun executeWithModelFallback(
        switchId: String?,
        snapshotId: String?,
        routeId: String?,
        prompt: String,
        displayModel: String,
        preferredBackendModel: String
    ): Result<Pair<ExecuteResponse, String>> {
        var lastFailure: Throwable? = null

        val aliases = backendModelAliases(
            displayModel = displayModel,
            preferredBackendModel = preferredBackendModel
        )

        for (alias in aliases) {
            val result = repository.execute(
                switchId = switchId,
                snapshotId = snapshotId,
                routeId = routeId,
                prompt = prompt,
                model = alias
            )

            if (result.isSuccess) {
                val response = result.getOrThrow()
                val readable = response.bestText()

                if (!readable.isNullOrBlank()) {
                    return Result.success(response to alias)
                }

                lastFailure = IllegalStateException(
                    "Backend returned empty response for alias: $alias"
                )
            } else {
                lastFailure = result.exceptionOrNull()
            }
        }

        return Result.failure(
            lastFailure ?: IllegalStateException(
                "No backend alias returned a usable answer for $displayModel."
            )
        )
    }

    private fun appendTargetUnavailableResult(
        sourceDisplay: String,
        targetDisplay: String,
        attemptedAliases: List<String>,
        throwable: Throwable?
    ) {
        val entry = CockpitConsoleEntry(
            title = "Model not available: $targetDisplay",
            content = buildString {
                appendLine("$targetDisplay did not return a usable answer from the current SECVerge backend.")
                appendLine()
                appendLine("Ask Hachy / Gemma direct answering is fixed separately and should work through /api/v1/ask.")
                appendLine()
                appendLine("This selected target model may be unavailable, unconfigured, or mapped differently on backend.")
            },
            technicalProof = buildString {
                appendLine("UCF target execution failed cleanly.")
                appendLine("From model: $sourceDisplay")
                appendLine("Target model: $targetDisplay")
                appendLine("Attempted backend aliases:")
                attemptedAliases.forEach {
                    appendLine("- $it")
                }
                appendLine()
                appendLine("Backend error:")
                appendLine(throwable?.message ?: "No detailed backend error returned.")
                appendLine()
                appendLine("Android result:")
                appendLine("- No fake success shown")
                appendLine("- Conversation preserved")
                appendLine("- Prompt remains clear")
                appendLine("- Backend/web/database unchanged")
            },
            kind = "system"
        )

        _uiState.update { current ->
            current.copy(
                prompt = "",
                isLoading = false,
                activeAction = null,
                errorMessage = null,
                consoleEntries = current.consoleEntries + entry
            )
        }

        persistActiveSession()
    }

    private fun buildDirectAnswerPrompt(
        question: String
    ): String {
        return buildString {
            appendLine("You are Ask Hachy inside SECVerge AI.")
            appendLine("Answer the user's CURRENT question directly and helpfully.")
            appendLine("Treat this as a brand-new standalone question.")
            appendLine("Ignore any old conversation history unless the current question explicitly asks about earlier messages.")
            appendLine("Do not say: previous conversation, conversation history, not enough context, or clarify, when the question is generally answerable.")
            appendLine("If the user asks to explain a concept, give a clear simple explanation.")
            appendLine()
            appendLine("CURRENT QUESTION:")
            appendLine(question)
        }
    }

    private fun isStaleHistoryRefusal(
        answer: String
    ): Boolean {
        val normalized = answer.lowercase(Locale.US)

        return (
            normalized.contains("previous conversation") ||
                normalized.contains("conversation history") ||
                normalized.contains("discussing history") ||
                normalized.contains("haven't covered") ||
                normalized.contains("hasn't covered") ||
                normalized.contains("doesn't include") ||
                normalized.contains("does not include") ||
                normalized.contains("not enough information") ||
                normalized.contains("provide more context") ||
                normalized.contains("clarify what")
            )
    }

    private fun buildBackendFallbackExplanation(
        question: String,
        displayModel: String
    ): String {
        return buildString {
            appendLine("$displayModel could not return a clean live answer from backend for this question.")
            appendLine()
            appendLine("Question:")
            appendLine(question)
            appendLine()
            appendLine("The app filtered a stale old-history style response and preserved the conversation.")
            appendLine("Please retry once. If this repeats, the backend model response contract needs repair.")
        }
    }


    private fun buildCarryPrompt(
        sourceDisplay: String,
        targetDisplay: String,
        contextPayload: String,
        typedPrompt: String
    ): String {
        return buildString {
            appendLine("SECVerge UCF context carry.")
            appendLine("Carry from: $sourceDisplay")
            appendLine("Carry to: $targetDisplay")
            appendLine()
            appendLine("Visible conversation context:")
            appendLine(contextPayload)

            if (typedPrompt.isNotBlank()) {
                appendLine()
                appendLine("Latest user instruction:")
                appendLine(typedPrompt)
            }

            appendLine()
            appendLine("Answer clearly using the carried visible conversation.")
            appendLine("Do not say context is unavailable if the visible conversation contains useful information.")
        }
    }

    private fun sessionTitle(
        entries: List<CockpitConsoleEntry>
    ): String {
        val firstUser = entries
            .firstOrNull { it.kind == "user" }
            ?.content
            ?.trim()

        val fallback = entries
            .firstOrNull()
            ?.content
            ?.trim()

        return (firstUser ?: fallback ?: "UCF session")
            .replace(Regex("\\s+"), " ")
            .take(TITLE_LIMIT)
            .ifBlank { "UCF session" }
    }

    private fun transcriptForSwitch(
        entries: List<CockpitConsoleEntry>
    ): String {
        return entries
            .joinToString(separator = "\n\n") { entry ->
                "${entry.title}:\n${entry.content}"
            }
            .takeLast(MAX_CONTEXT_CHARS)
    }

    private fun adapterIsUsable(
        adapter: AdapterStatusDto
    ): Boolean {
        val status = adapter.status.lowercase(Locale.US)

        return adapter.configured ||
            status in setOf(
                "ok",
                "online",
                "ready",
                "local",
                "verified",
                "configured",
                "available",
                "active"
            )
    }

    private fun adapterDisplayName(
        adapter: AdapterStatusDto
    ): String {
        val direct = adapter.displayName
            ?: adapter.name
            ?: adapter.model

        val base = direct
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: adapter.provider.trim()

        return when (base.lowercase(Locale.US)) {
            "ask hachy",
            "hachy",
            "gemma",
            "ask-hachy",
            "ask_hachy" -> "Ask Hachy / Gemma"

            "llama",
            "llama3",
            "llama 3" -> "Llama"

            "mistral" -> "Mistral"
            "qwen" -> "Qwen"
            "claude" -> "Claude"
            "gemini" -> "Gemini"
            "gpt",
            "chatgpt",
            "openai" -> "GPT"

            else -> base.replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(Locale.US)
                } else {
                    it.toString()
                }
            }
        }
    }

    private fun isAskHachyDisplayModel(
        displayModel: String
    ): Boolean {
        val normalized = displayModel
            .lowercase(Locale.US)

        return normalized.contains("hachy") ||
            normalized.contains("gemma")
    }

    private fun backendModelAliases(
        displayModel: String,
        preferredBackendModel: String
    ): List<String> {
        val normalized = displayModel
            .trim()
            .lowercase(Locale.US)

        val aliases = when {
            normalized.contains("hachy") ||
                normalized.contains("gemma") -> listOf(
                    preferredBackendModel,
                    "Ask Hachy",
                    "ask_hachy",
                    "ask-hachy",
                    "hachy",
                    "gemma",
                    "gemma3",
                    "gemma-3"
                )

            normalized.contains("llama") -> listOf(
                preferredBackendModel,
                "llama",
                "Llama",
                "llama3",
                "llama-3",
                "llama3:latest",
                "meta-llama",
                "meta_llama"
            )

            normalized.contains("mistral") -> listOf(
                preferredBackendModel,
                "mistral",
                "Mistral",
                "mistral:latest"
            )

            normalized.contains("qwen") -> listOf(
                preferredBackendModel,
                "qwen",
                "Qwen",
                "qwen2",
                "qwen2.5",
                "qwen:latest"
            )

            normalized.contains("claude") -> listOf(
                preferredBackendModel,
                "claude",
                "Claude",
                "anthropic",
                "claude-3",
                "claude-3-5-sonnet"
            )

            normalized.contains("gemini") -> listOf(
                preferredBackendModel,
                "gemini",
                "Gemini",
                "google",
                "gemini-pro",
                "gemini-1.5-pro"
            )

            normalized.contains("gpt") ||
                normalized.contains("openai") ||
                normalized.contains("chatgpt") -> listOf(
                    preferredBackendModel,
                    "gpt",
                    "GPT",
                    "openai",
                    "chatgpt",
                    "gpt-4",
                    "gpt-4o"
                )

            else -> listOf(
                preferredBackendModel,
                displayModel,
                displayModel.lowercase(Locale.US)
            )
        }

        return aliases
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun fallbackModel(
        excluding: String,
        options: List<String>
    ): String {
        return options.firstOrNull { it != excluding }
            ?: DefaultCockpitModelOptions
                .firstOrNull { it != excluding }
            ?: "Llama"
    }

    private fun toBackendModel(displayModel: String): String {
        return when (displayModel) {
            "Ask Hachy / Gemma" -> "Ask Hachy"
            "Llama" -> "llama"
            "Mistral" -> "mistral"
            "Qwen" -> "qwen"
            "Claude" -> "claude"
            "Gemini" -> "gemini"
            "GPT" -> "gpt"
            else -> displayModel
        }
    }

    private fun createSessionId(): String {
        return "ucf-${UUID.randomUUID()}"
    }

    private fun ExecuteResponse.bestText(): String? {
        return listOf(
            output,
            response,
            answer,
            result,
            content,
            message
        ).firstNotNullOfOrNull { element ->
            element?.toReadableText()
        }
    }

    private fun ContextSnapshotResponse.bestSnapshotId(): String? {
        return listOf(
            snapshotId,
            contextSnapshotId,
            id
        ).firstOrNull {
            !it.isNullOrBlank()
        }
    }

    private fun ContextSnapshotResponse.bestFidelity(): Double? {
        return fidelityScore ?: contextFidelity
    }

    private fun RouteResponse.bestRouteId(): String? {
        return routeId ?: id
    }

    private fun SwitchResponse.bestSwitchId(): String? {
        return switchId ?: id
    }

    private fun SwitchResponse.bestFidelity(): Double? {
        return fidelityScore ?: contextFidelity
    }

    private fun SwitchResponse.bestCarriedContext(): String? {
        return listOf(
            carriedContext,
            context,
            summary,
            message
        ).firstOrNull {
            !it.isNullOrBlank()
        }
    }

    private fun JsonElement.toReadableText(): String? {
        return when (this) {
            is JsonPrimitive -> {
                contentOrNull
                    ?.takeIf { it.isNotBlank() }
                    ?: booleanOrNull?.toString()
                    ?: intOrNull?.toString()
                    ?: doubleOrNull?.toString()
            }

            is JsonObject -> {
                listOf(
                    "answer",
                    "response",
                    "output",
                    "text",
                    "content",
                    "message",
                    "summary"
                ).firstNotNullOfOrNull { key ->
                    this[key]?.toReadableText()
                } ?: entries.joinToString(
                    separator = "\n"
                ) { (key, value) ->
                    "$key: ${value.toReadableText() ?: value.toString()}"
                }.takeIf { it.isNotBlank() }
            }

            else -> toString().takeIf { it.isNotBlank() }
        }
    }

    private fun friendlyError(throwable: Throwable): String {
        val raw = throwable.message.orEmpty()

        return when {
            throwable is UnknownHostException ->
                "No internet connection. Please check your network and try again."

            throwable is SocketTimeoutException ->
                "The backend took too long to respond. Please try again."

            raw.contains("401") ->
                "Your session has expired. Please log in again."

            raw.contains("404") ->
                "The expected endpoint is not available yet."

            raw.isNotBlank() ->
                raw

            else ->
                "The request could not be completed."
        }
    }

    private companion object {
        const val TITLE_LIMIT = 54
        const val PREVIEW_LIMIT = 110
        const val MAX_CONTEXT_CHARS = 9000
    }
}
