package com.secverge.ai.ui.council

import com.secverge.ai.data.models.council.CouncilCreateRequest
import com.secverge.ai.data.models.council.CouncilDebateRequest
import com.secverge.ai.data.models.council.CouncilFinalizeRequest
import com.secverge.ai.data.network.SECVergeApi

class AICouncilRepository(private val api: SECVergeApi) {
    suspend fun create(question: String, models: List<String>) = runCatching {
        api.createCouncil(CouncilCreateRequest(question, models))
    }
    suspend fun debate(sessionId: String, round: Int) = runCatching {
        api.debateCouncil(CouncilDebateRequest(sessionId, round))
    }
    suspend fun finalize(sessionId: String) = runCatching {
        api.finalizeCouncil(CouncilFinalizeRequest(sessionId))
    }
}
