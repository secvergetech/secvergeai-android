package com.secverge.ai.ui.askhachy

import com.secverge.ai.data.models.ask.AskRequest
import com.secverge.ai.data.network.SECVergeApi

class AskHachyRepository(
    private val api: SECVergeApi
) {
    suspend fun ask(
        prompt: String,
        conversationId: String?
    ): Result<AskHachyResult> {
        return runCatching {
            val response = api.ask(
                AskRequest(
                    prompt = prompt,
                    conversationId = conversationId
                )
            )

            val answer = response.answer
                ?.takeIf { it.isNotBlank() }
                ?: response.response
                    ?.takeIf { it.isNotBlank() }
                ?: response.message
                    ?.takeIf { it.isNotBlank() }
                ?: error("Ask Hachy returned an empty response.")

            AskHachyResult(
                answer = answer,
                conversationId = response.conversationId
                    ?: conversationId
            )
        }
    }
}

data class AskHachyResult(
    val answer: String,
    val conversationId: String?
)
