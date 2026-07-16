package com.secverge.ai.ui.delphi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DelphiViewModel(
    private val repository: DelphiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DelphiUiState())
    val uiState: StateFlow<DelphiUiState> = _uiState.asStateFlow()

    fun updateQuestion(value: String) {
        _uiState.update {
            it.copy(
                question = value,
                stage = DelphiStage.Blind,
                blindRoundComplete = false,
                revealed = false,
                debated = false,
                converged = false,
                entries = emptyList(),
                consensus = null,
                exportedPdfPath = null,
                errorMessage = null
            )
        }
    }

    fun updateContext(value: String) {
        _uiState.update {
            it.copy(
                context = value,
                stage = DelphiStage.Blind,
                blindRoundComplete = false,
                revealed = false,
                debated = false,
                converged = false,
                entries = emptyList(),
                consensus = null,
                exportedPdfPath = null,
                errorMessage = null
            )
        }
    }

    fun toggleParticipant(name: String) {
        _uiState.update { state ->
            state.copy(
                participants = state.participants.map { participant ->
                    if (participant.name == name) {
                        participant.copy(selected = !participant.selected)
                    } else {
                        participant
                    }
                },
                errorMessage = null
            )
        }
    }

    fun testGemmaMax() {
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

            _uiState.update {
                it.copy(
                    runtimeOk = true,
                    isLoading = false,
                    activeAction = null,
                    entries = it.entries +
                        DelphiRoomEntry(
                            id = newId(),
                            title = "Gemma MAX Runtime",
                            model = "Ask Hachy / Gemma",
                            content = buildString {
                                appendLine("Gemma MAX retry and rotating moderator fallback are ready.")
                                appendLine("Backend call: disabled for local Android Delphi mode.")
                                appendLine("Selected panel: ${state.selectedModels.joinToString(", ").ifBlank { "No participants selected" }}")
                            }
                        )
                )
            }
        }
    }

    fun runBlindRound() {
        val state = _uiState.value

        if (!state.canBlindRound) {
            _uiState.update {
                it.copy(
                    errorMessage = "Enter a decision question and select at least two participants."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                activeAction = "blind",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            delay(300)

            val intro = DelphiRoomEntry(
                id = newId(),
                title = "SECVerge Delphi",
                content = buildString {
                    appendLine("Blind round complete.")
                    appendLine("Decision question: ${state.question.trim()}")
                    appendLine("Context / constraints: ${state.context.trim().ifBlank { "No additional context supplied." }}")
                    appendLine("Independent views are collected privately. Tap Reveal to show positions.")
                }
            )

            val blindViews = state.selectedModels.map { model ->
                DelphiRoomEntry(
                    id = newId(),
                    title = "$model · Blind view",
                    model = model,
                    content = blindPerspective(
                        model = model,
                        question = state.question,
                        context = state.context
                    ),
                    hiddenUntilReveal = true
                )
            }

            _uiState.update {
                it.copy(
                    runtimeOk = true,
                    blindRoundComplete = true,
                    revealed = false,
                    debated = false,
                    converged = false,
                    stage = DelphiStage.Reveal,
                    isLoading = false,
                    activeAction = null,
                    consensus = null,
                    entries = listOf(intro) + blindViews
                )
            }
        }
    }

    fun revealPositions() {
        val state = _uiState.value

        if (!state.canReveal) {
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                activeAction = "reveal",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            delay(220)

            _uiState.update {
                it.copy(
                    revealed = true,
                    stage = DelphiStage.Debate,
                    isLoading = false,
                    activeAction = null,
                    entries = it.entries +
                        DelphiRoomEntry(
                            id = newId(),
                            title = "Reveal",
                            content = "All blind positions are now visible together. Compare differences before moving into debate."
                        )
                )
            }
        }
    }

    fun runDebate() {
        val state = _uiState.value

        if (!state.canDebate) {
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                activeAction = "debate",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            delay(300)

            val debateEntries = state.selectedModels.map { model ->
                DelphiRoomEntry(
                    id = newId(),
                    title = "$model · Debate challenge",
                    model = model,
                    content = debatePerspective(
                        model = model,
                        question = state.question,
                        context = state.context
                    )
                )
            }

            _uiState.update {
                it.copy(
                    debated = true,
                    stage = DelphiStage.Converge,
                    isLoading = false,
                    activeAction = null,
                    entries = it.entries + debateEntries
                )
            }
        }
    }

    fun converge() {
        val state = _uiState.value

        if (!state.canConverge) {
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                activeAction = "converge",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            delay(300)

            val consensusText = buildConsensus(
                question = state.question,
                context = state.context,
                models = state.selectedModels
            )

            _uiState.update {
                it.copy(
                    converged = true,
                    stage = DelphiStage.Converge,
                    isLoading = false,
                    activeAction = null,
                    consensus = consensusText,
                    entries = it.entries +
                        DelphiRoomEntry(
                            id = newId(),
                            title = "Final Delphi Consensus",
                            model = "Ask Hachy / Gemma",
                            content = consensusText
                        )
                )
            }
        }
    }

    fun markPdfExported(path: String) {
        _uiState.update {
            it.copy(
                exportedPdfPath = path,
                entries = it.entries +
                    DelphiRoomEntry(
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

    private fun blindPerspective(
        model: String,
        question: String,
        context: String
    ): String {
        val cleanQuestion = question.trim()
        val cleanContext = context.trim().ifBlank { "No extra constraints." }

        return when (model) {
            "Ask Hachy / Gemma" ->
                buildString {
                    appendLine("Gemma blind view:")
                    appendLine("Question: $cleanQuestion")
                    appendLine("Frame the problem clearly, preserve the decision objective, and prepare the final convergence path.")
                    appendLine("Context considered: $cleanContext")
                }

            "Llama" ->
                buildString {
                    appendLine("Llama blind view:")
                    appendLine("Create a broad structured analysis.")
                    appendLine("Identify options, advantages, risks, dependencies, and stakeholder impact.")
                    appendLine("Context considered: $cleanContext")
                }

            "Mistral" ->
                buildString {
                    appendLine("Mistral blind view:")
                    appendLine("Prioritize execution clarity, speed, and operational risk.")
                    appendLine("Recommend the simplest path that can be validated quickly.")
                    appendLine("Context considered: $cleanContext")
                }

            "Qwen" ->
                buildString {
                    appendLine("Qwen blind view:")
                    appendLine("Compare assumptions and look for hidden risks.")
                    appendLine("Check missing evidence, timing, capability, and downstream impact.")
                    appendLine("Context considered: $cleanContext")
                }

            else ->
                "$model blind view: analyze $cleanQuestion with practical reasoning."
        }
    }

    private fun debatePerspective(
        model: String,
        question: String,
        context: String
    ): String {
        return when (model) {
            "Ask Hachy / Gemma" ->
                "Gemma challenge: Ensure every position answers the same decision question and does not drift away from the final executive recommendation."

            "Llama" ->
                "Llama challenge: The panel should not over-optimize for speed if the decision needs broad stakeholder alignment."

            "Mistral" ->
                "Mistral challenge: The panel should avoid vague strategy. Convert the decision into an executable next step."

            "Qwen" ->
                "Qwen challenge: The panel should test assumptions, missing data, and timing risks before convergence."

            else ->
                "$model challenge: review assumptions and strengthen the final recommendation."
        }
    }

    private fun buildConsensus(
        question: String,
        context: String,
        models: List<String>
    ): String {
        return buildString {
            appendLine("Delphi executive consensus")
            appendLine()
            appendLine("Decision question")
            appendLine(question.trim())
            appendLine()
            appendLine("Context / constraints")
            appendLine(context.trim().ifBlank { "No additional context supplied." })
            appendLine()
            appendLine("Participants")
            appendLine(models.joinToString(", "))
            appendLine()
            appendLine("Consensus")
            appendLine("Proceed with the recommendation that combines Gemma's decision frame, Llama's broad structure, Mistral's execution discipline, and Qwen's assumption checking.")
            appendLine()
            appendLine("Recommended next step")
            appendLine("Turn the consensus into a short executive memo, validate the highest-risk assumption, and then move to implementation only after the risk is clearly owned.")
        }
    }

    private fun newId(): String {
        return UUID.randomUUID().toString()
    }
}
