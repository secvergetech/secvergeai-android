package com.secverge.ai.ui.council

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AICouncilViewModel(
    private val repository: AICouncilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AICouncilUiState())
    val uiState: StateFlow<AICouncilUiState> = _uiState.asStateFlow()

    fun updateQuestion(value: String) {
        _uiState.update {
            it.copy(
                question = value,
                errorMessage = null
            )
        }
    }

    fun toggleModel(name: String) {
        _uiState.update { state ->
            if (state.stage != CouncilStage.Configure) {
                state
            } else {
                state.copy(
                    participants = state.participants.map { participant ->
                        if (participant.name == name) {
                            participant.copy(
                                selected = !participant.selected
                            )
                        } else {
                            participant
                        }
                    },
                    errorMessage = null
                )
            }
        }
    }

    fun dismissError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    fun createCouncil() {
        val state = _uiState.value

        if (!state.canStart) {
            _uiState.update {
                it.copy(
                    errorMessage = "Choose at least two models and enter a question."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            delay(350)

            _uiState.update {
                it.copy(
                    sessionId = "local-council-${UUID.randomUUID()}",
                    stage = CouncilStage.Debate,
                    isLoading = false,
                    contributions = emptyList(),
                    consensus = null
                )
            }
        }
    }

    fun runDebate() {
        val state = _uiState.value

        if (!state.canDebate) {
            return
        }

        val round = (state.contributions.maxOfOrNull { it.round } ?: 0) + 1
        val question = state.question.trim()
        val selectedModels = state.selectedModels

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            delay(500)

            val newContributions = selectedModels.map { model ->
                CouncilContribution(
                    id = UUID.randomUUID().toString(),
                    model = model,
                    round = round,
                    content = localModelContribution(
                        model = model,
                        question = question,
                        round = round
                    )
                )
            }

            _uiState.update {
                it.copy(
                    contributions = it.contributions + newContributions,
                    isLoading = false
                )
            }
        }
    }

    fun finalizeCouncil() {
        val state = _uiState.value

        if (!state.canFinalize) {
            return
        }

        val question = state.question.trim()
        val selectedModels = state.selectedModels
        val contributions = state.contributions

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            delay(500)

            val consensusText = buildGemmaConsensus(
                question = question,
                selectedModels = selectedModels,
                contributions = contributions
            )

            _uiState.update {
                it.copy(
                    consensus = consensusText,
                    stage = CouncilStage.Consensus,
                    isLoading = false
                )
            }
        }
    }

    private fun localModelContribution(
        model: String,
        question: String,
        round: Int
    ): String {
        return when (model) {
            "Llama" ->
                "Llama perspective, round $round: For \"$question\", start with a broad structured explanation, define the core concept, then break it into practical parts that are easy to understand."

            "Mistral" ->
                "Mistral perspective, round $round: For \"$question\", focus on concise reasoning, key mechanisms, and a clean step-by-step explanation without unnecessary complexity."

            "Qwen" ->
                "Qwen perspective, round $round: For \"$question\", compare the topic from multiple angles, include examples, and highlight where deeper analysis may be useful."

            else ->
                "$model perspective, round $round: Analyze \"$question\" with clear reasoning and practical examples."
        }
    }

    private fun buildGemmaConsensus(
        question: String,
        selectedModels: List<String>,
        contributions: List<CouncilContribution>
    ): String {
        val modelLine = selectedModels.joinToString(", ")

        val latestRound = contributions
            .maxOfOrNull { it.round }
            ?: 1

        return buildString {
            appendLine("Gemma moderator consensus")
            appendLine()
            appendLine("Question:")
            appendLine(question)
            appendLine()
            appendLine("Council members:")
            appendLine(modelLine)
            appendLine()
            appendLine("Debate rounds reviewed:")
            appendLine(latestRound.toString())
            appendLine()
            appendLine("Final synthesis:")
            appendLine("The strongest answer should combine Llama's broad structure, Mistral's concise reasoning, and Qwen's comparative analysis. Start with a simple definition, explain the main parts, show practical examples, and then summarize the most important takeaway.")
            appendLine()
            appendLine("Recommended response style:")
            appendLine("1. Explain the concept in simple language.")
            appendLine("2. Break it into key components.")
            appendLine("3. Give one practical example.")
            appendLine("4. End with a short summary.")
            appendLine()
            appendLine("Mode:")
            appendLine("Local Android council mode. No backend call was made.")
        }
    }
}
