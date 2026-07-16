package com.secverge.ai.ui.humancouncil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HumanCouncilViewModel(
    private val repository: HumanCouncilRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HumanCouncilUiState())
    val uiState: StateFlow<HumanCouncilUiState> = _uiState.asStateFlow()

    fun updateObjective(value: String) {
        _uiState.update {
            it.copy(
                objective = value,
                stage = HumanDecisionStage.Frame,
                roomStarted = false,
                runtimeOk = false,
                messages = emptyList(),
                decisionMemo = null,
                exportedPdfPath = null,
                errorMessage = null
            )
        }
    }

    fun updateInstruction(value: String) {
        _uiState.update {
            it.copy(
                instruction = value,
                stage = HumanDecisionStage.Frame,
                roomStarted = false,
                runtimeOk = false,
                messages = emptyList(),
                decisionMemo = null,
                exportedPdfPath = null,
                errorMessage = null
            )
        }
    }

    fun toggleParticipant(name: String) {
        _uiState.update { state ->
            val updated = state.participants.map { participant ->
                if (participant.name == name) {
                    participant.copy(selected = !participant.selected)
                } else {
                    participant
                }
            }

            val selected = updated
                .filter { it.selected }
                .map { it.name }

            val safeDirectModel = when {
                selected.contains(state.directModel) -> state.directModel
                selected.isNotEmpty() -> selected.first()
                else -> "Ask Hachy / Gemma"
            }

            state.copy(
                participants = updated,
                directModel = safeDirectModel,
                errorMessage = null
            )
        }
    }

    fun setDirectModel(value: String) {
        _uiState.update {
            it.copy(
                directModel = value,
                stage = HumanDecisionStage.Challenge,
                errorMessage = null
            )
        }
    }

    fun testRuntime() {
        val state = _uiState.value

        _uiState.update {
            it.copy(
                isLoading = true,
                activeAction = "test",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            delay(250)

            val message = HumanRoomMessage(
                id = newId(),
                title = "Runtime Test",
                content = buildString {
                    appendLine("Android Human Decision Room runtime is ready.")
                    appendLine("Backend call: disabled for this local working mode.")
                    appendLine("Available participants: ${state.selectedModels.joinToString(", ").ifBlank { "No model selected" }}")
                    appendLine("Direct model: ${state.directModel}")
                    appendLine("PDF export: available on-device.")
                }
            )

            _uiState.update {
                it.copy(
                    runtimeOk = true,
                    isLoading = false,
                    activeAction = null,
                    messages = it.messages + message
                )
            }
        }
    }

    fun startRoom() {
        val state = _uiState.value

        if (!state.canStartRoom) {
            _uiState.update {
                it.copy(
                    errorMessage = "Enter the decision objective, your human instruction, and select at least one participant."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                activeAction = "start",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            delay(300)

            val intro = HumanRoomMessage(
                id = newId(),
                title = "SECVerge AI",
                content = buildString {
                    appendLine("Room started.")
                    appendLine("Human objective: ${state.objective.trim()}")
                    appendLine("Human instruction: ${state.instruction.trim()}")
                    appendLine("Participants: ${state.selectedModels.joinToString(", ")}")
                    appendLine()
                    appendLine("Initial model viewpoints generated below.")
                }
            )

            val responses = createResponsesForModels(
                models = state.selectedModels,
                objective = state.objective,
                instruction = state.instruction,
                mode = "initial"
            )

            _uiState.update {
                it.copy(
                    roomStarted = true,
                    runtimeOk = true,
                    stage = HumanDecisionStage.Compare,
                    isLoading = false,
                    activeAction = null,
                    decisionMemo = null,
                    exportedPdfPath = null,
                    messages = listOf(intro) + responses
                )
            }
        }
    }

    fun askModels() {
        val state = _uiState.value

        if (!state.canAskModels) {
            _uiState.update {
                it.copy(
                    errorMessage = "Start the room first, then ask the selected models."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                activeAction = "ask",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            delay(300)

            val responses = createResponsesForModels(
                models = state.selectedModels,
                objective = state.objective,
                instruction = state.instruction,
                mode = "follow-up"
            )

            _uiState.update {
                it.copy(
                    stage = HumanDecisionStage.Compare,
                    isLoading = false,
                    activeAction = null,
                    messages = it.messages + responses
                )
            }
        }
    }

    fun directModel() {
        val state = _uiState.value

        if (!state.canDirectModel) {
            _uiState.update {
                it.copy(
                    errorMessage = "Start the room and choose a valid direct model."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                activeAction = "direct",
                stage = HumanDecisionStage.Challenge,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            delay(250)

            val response = HumanRoomMessage(
                id = newId(),
                title = "${state.directModel} · Directed response",
                content = modelPerspective(
                    model = state.directModel,
                    objective = state.objective,
                    instruction = state.instruction,
                    mode = "directed"
                ),
                isModelResponse = true
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    activeAction = null,
                    messages = it.messages + response
                )
            }
        }
    }

    fun buildDecisionMemo() {
        val state = _uiState.value

        if (!state.canBuildMemo) {
            _uiState.update {
                it.copy(
                    errorMessage = "Ask models first, then build the decision memo."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                activeAction = "memo",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            delay(300)

            val reviewedModels = state.messages
                .filter { it.isModelResponse }
                .map { it.title.substringBefore(" ·") }
                .distinct()
                .joinToString(", ")

            val memo = buildString {
                appendLine("Decision Memo")
                appendLine()
                appendLine("Objective")
                appendLine(state.objective.trim())
                appendLine()
                appendLine("Human Chair Instruction")
                appendLine(state.instruction.trim())
                appendLine()
                appendLine("Models Reviewed")
                appendLine(reviewedModels.ifBlank { state.selectedModels.joinToString(", ") })
                appendLine()
                appendLine("Synthesis")
                appendLine("Use Ask Hachy / Gemma to preserve the decision frame, Llama for broad structure, Mistral for execution clarity, and Qwen for assumption checking.")
                appendLine()
                appendLine("Final Recommendation")
                appendLine("Proceed with the decision path that best satisfies the human objective, follows the human instruction, and has the clearest execution plan.")
            }

            val memoMessage = HumanRoomMessage(
                id = newId(),
                title = "Decision Memo",
                content = memo
            )

            _uiState.update {
                it.copy(
                    stage = HumanDecisionStage.Decide,
                    isLoading = false,
                    activeAction = null,
                    decisionMemo = memo,
                    messages = it.messages + memoMessage
                )
            }
        }
    }

    fun markPdfExported(path: String) {
        _uiState.update {
            it.copy(
                exportedPdfPath = path,
                messages = it.messages +
                    HumanRoomMessage(
                        id = newId(),
                        title = "Export PDF",
                        content = path
                    )
            )
        }
    }

    fun dismissError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    private fun createResponsesForModels(
        models: List<String>,
        objective: String,
        instruction: String,
        mode: String
    ): List<HumanRoomMessage> {
        return models.map { model ->
            HumanRoomMessage(
                id = newId(),
                title = when (mode) {
                    "initial" -> "$model · Initial viewpoint"
                    "follow-up" -> "$model · Follow-up viewpoint"
                    else -> "$model · Viewpoint"
                },
                content = modelPerspective(
                    model = model,
                    objective = objective,
                    instruction = instruction,
                    mode = mode
                ),
                isModelResponse = true
            )
        }
    }

    private fun modelPerspective(
        model: String,
        objective: String,
        instruction: String,
        mode: String
    ): String {
        val cleanObjective = objective.trim()
        val cleanInstruction = instruction.trim()

        return when (model) {
            "Ask Hachy / Gemma" ->
                buildString {
                    appendLine("Gemma / Ask Hachy frame:")
                    appendLine("Objective: $cleanObjective")
                    appendLine("Human instruction: $cleanInstruction")
                    appendLine("I will preserve the human chair's intent, compare viewpoints, and keep the final memo decision-ready.")
                }

            "Llama" ->
                buildString {
                    appendLine("Llama viewpoint:")
                    appendLine("Build a broad structure for the objective: $cleanObjective")
                    appendLine("Main lens: options, tradeoffs, risks, dependencies, and stakeholder impact.")
                    appendLine("Human instruction applied: $cleanInstruction")
                }

            "Mistral" ->
                buildString {
                    appendLine("Mistral viewpoint:")
                    appendLine("Focus on execution clarity and speed.")
                    appendLine("For this objective, identify the lowest-risk path, minimum viable next step, and blockers that can slow delivery.")
                }

            "Qwen" ->
                buildString {
                    appendLine("Qwen viewpoint:")
                    appendLine("Compare assumptions and hidden risks.")
                    appendLine("Check missing evidence, timing, operational capacity, and whether the human instruction changes the priority order.")
                }

            else ->
                "$model viewpoint: Analyze the objective and instruction with practical reasoning."
        }
    }

    private fun newId(): String {
        return UUID.randomUUID().toString()
    }
}
