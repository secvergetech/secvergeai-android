package com.secverge.ai.ui.council

import com.secverge.ai.data.models.council.*
import com.secverge.ai.data.network.SECVergeApi

class AICouncilRepository(private val api: SECVergeApi) {
    private fun backendModel(displayModel: String) = when (displayModel) {
        "Llama" -> "llama3.2:3b"
        "Mistral" -> "mistral:latest"
        "Qwen" -> "qwen2.5:7b"
        else -> "gemma3:4b-it-q8_0"
    }
    private fun verified(result: CouncilProviderResponse, expected: String): CouncilProviderResponse {
        val text = result.content.takeIf { it.isNotBlank() }
            ?: result.consensus?.takeIf { it.isNotBlank() }
            ?: result.answer?.takeIf { it.isNotBlank() }
            ?: result.response?.takeIf { it.isNotBlank() }
        check(result.ok != false && result.status == "model_backed" && !result.fallback &&
            result.providerModel == expected && !text.isNullOrBlank()) {
            "${result.label} did not return a verified live response (${result.status})."
        }
        return result.copy(content = requireNotNull(text))
    }
    suspend fun perspective(question: String, displayModel: String) = runCatching {
        val model = backendModel(displayModel)
        val providerId = when (displayModel) {
            "Llama" -> "llama"
            "Mistral" -> "mistral"
            "Qwen" -> "qwen"
            else -> "gemma"
        }
        verified(api.councilPerspective(CouncilPerspectiveRequest(question, providerId)), model)
    }
    suspend fun consensus(question: String, contributions: List<CouncilContribution>) = runCatching {
        val latest = contributions.groupBy { it.model }.values.map { it.last() }
        verified(api.councilConsensus(CouncilConsensusRequest(question,
            latest.map { CouncilPerspective(it.model, it.content) })), "gemma3:4b-it-q8_0")
    }
}
