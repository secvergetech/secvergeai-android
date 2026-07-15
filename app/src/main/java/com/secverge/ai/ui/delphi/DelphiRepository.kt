package com.secverge.ai.ui.delphi

import com.secverge.ai.data.models.delphi.DelphiBlindSubmitRequest
import com.secverge.ai.data.models.delphi.DelphiConvergeRequest
import com.secverge.ai.data.models.delphi.DelphiCreateRequest
import com.secverge.ai.data.models.delphi.DelphiDebateRequest
import com.secverge.ai.data.network.SECVergeApi

class DelphiRepository(
    private val api: SECVergeApi
) {
    suspend fun create(question: String, models: List<String>) = runCatching {
        api.createDelphi(DelphiCreateRequest(question = question, models = models))
    }

    suspend fun collectBlindSubmissions(sessionId: String) = runCatching {
        api.blindSubmitDelphi(DelphiBlindSubmitRequest(sessionId = sessionId))
    }

    suspend fun debate(sessionId: String) = runCatching {
        api.debateDelphi(DelphiDebateRequest(sessionId = sessionId))
    }

    suspend fun converge(sessionId: String) = runCatching {
        api.convergeDelphi(DelphiConvergeRequest(sessionId = sessionId))
    }
}
