package com.secverge.ai.data.models.delphi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DelphiCreateRequest(val prompt: String)
@Serializable
data class DelphiCreateResponse(val ok: Boolean, val run: DelphiRun)
@Serializable
data class DelphiRun(@SerialName("run_id") val runId: String, val cards: List<DelphiCard>)
@Serializable
data class DelphiCard(@SerialName("model_label") val modelLabel: String)
@Serializable
data class DelphiBlindSubmitRequest(
    @SerialName("run_id") val runId: String,
    @SerialName("model_label") val modelLabel: String
)
@Serializable
data class DelphiBlindSubmitResponse(val ok: Boolean, val submission: DelphiSubmissionDto)
@Serializable
data class DelphiSubmissionDto(
    @SerialName("model_label") val modelLabel: String,
    val response: String
)
@Serializable
data class DelphiDebateRequest(@SerialName("run_id") val runId: String)
@Serializable
data class DelphiDebateResponse(val ok: Boolean, val debate: DelphiDebate)
@Serializable
data class DelphiDebate(val response: String, val model: String)
@Serializable
data class DelphiConvergeRequest(@SerialName("run_id") val runId: String)
@Serializable
data class DelphiConvergeResponse(val ok: Boolean, val convergence: DelphiConvergence)
@Serializable
data class DelphiConvergence(val consensus: String, val model: String)
