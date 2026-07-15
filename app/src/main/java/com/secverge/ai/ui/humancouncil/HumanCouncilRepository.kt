package com.secverge.ai.ui.humancouncil

import com.secverge.ai.data.models.humancouncil.HumanCouncilContributeRequest
import com.secverge.ai.data.models.humancouncil.HumanCouncilCreateRequest
import com.secverge.ai.data.models.humancouncil.HumanCouncilDirectRequest
import com.secverge.ai.data.network.SECVergeApi

class HumanCouncilRepository(private val api: SECVergeApi) {
    suspend fun create(topic: String, models: List<String>) = runCatching { api.createHumanCouncil(HumanCouncilCreateRequest(topic, models)) }
    suspend fun contribute(sessionId: String, message: String) = runCatching { api.contributeHumanCouncil(HumanCouncilContributeRequest(sessionId, message)) }
    suspend fun direct(sessionId: String, model: String, message: String) = runCatching { api.directHumanCouncil(HumanCouncilDirectRequest(sessionId, model, message)) }
}
