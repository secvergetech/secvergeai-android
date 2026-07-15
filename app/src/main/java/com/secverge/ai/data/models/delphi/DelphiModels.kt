package com.secverge.ai.data.models.delphi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DelphiCreateRequest(
    val question: String,
    val models: List<String>
)

@Serializable
data class DelphiCreateResponse(
    @SerialName("session_id") val sessionId: String? = null,
    val status: String? = null
)

@Serializable
data class DelphiBlindSubmitRequest(
    @SerialName("session_id") val sessionId: String
)

@Serializable
data class DelphiBlindSubmitResponse(
    val submissions: List<DelphiSubmissionDto> = emptyList(),
    val status: String? = null
)

@Serializable
data class DelphiDebateRequest(
    @SerialName("session_id") val sessionId: String
)

@Serializable
data class DelphiDebateResponse(
    val contributions: List<DelphiContributionDto> = emptyList(),
    val status: String? = null
)

@Serializable
data class DelphiConvergeRequest(
    @SerialName("session_id") val sessionId: String
)

@Serializable
data class DelphiConvergeResponse(
    val consensus: String? = null,
    val confidence: Double? = null,
    val status: String? = null
)

@Serializable
data class DelphiSubmissionDto(
    val model: String,
    val content: String
)

@Serializable
data class DelphiContributionDto(
    val model: String,
    val content: String,
    val round: Int? = null
)
