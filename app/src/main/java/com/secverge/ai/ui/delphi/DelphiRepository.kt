package com.secverge.ai.ui.delphi

import com.secverge.ai.data.models.delphi.*
import com.secverge.ai.data.models.cockpit.ExecuteRequest
import com.secverge.ai.data.network.SECVergeApi

class DelphiRepository(private val api: SECVergeApi) {
    suspend fun create(prompt: String) = runCatching {
        api.createDelphi(DelphiCreateRequest(prompt)).also {
            check(it.ok && it.run.runId.isNotBlank() && it.run.cards.size >= 2) { "Delphi returned no blind panel." }
        }.run
    }
    suspend fun submit(runId: String, label: String) = runCatching {
        api.blindSubmitDelphi(DelphiBlindSubmitRequest(runId, label)).also {
            check(it.ok && it.submission.response.isNotBlank() && it.submission.modelLabel == label) {
                "Delphi returned an invalid blind submission."
            }
        }.submission
    }
    suspend fun debate(runId: String) = runCatching {
        api.debateDelphi(DelphiDebateRequest(runId)).also {
            check(it.ok && it.debate.response.isNotBlank()) { "Delphi returned no debate." }
        }.debate
    }
    suspend fun converge(runId: String) = runCatching {
        api.convergeDelphi(DelphiConvergeRequest(runId)).also {
            check(it.ok && it.convergence.consensus.isNotBlank()) { "Delphi returned no consensus." }
        }.convergence
    }
    suspend fun testRuntime() = runCatching {
        api.execute(ExecuteRequest(prompt = "Reply in one sentence: what is TLS?", model = "gemma3:4b-it-q8_0")).also {
            check(it.ok && it.execution?.selectedModel == "gemma3:4b-it-q8_0" && !it.execution.response.isNullOrBlank()) {
                "Gemma did not return a verified live answer."
            }
        }.execution!!.response!!
    }
}
