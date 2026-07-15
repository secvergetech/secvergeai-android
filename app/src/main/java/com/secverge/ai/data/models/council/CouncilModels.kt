package com.secverge.ai.data.models.council

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CouncilCreateRequest(
    val question: String,
    val models: List<String>
)

@Serializable
data class CouncilCreateResponse(
    @SerialName("session_id") val sessionId: String? = null,
    val status: String? = null
)

@Serializable
data class CouncilDebateRequest(
    @SerialName("session_id") val sessionId: String,
    val round: Int? = null
)

@Serializable
data class CouncilDebateResponse(
    val contributions: List<CouncilContributionDto> = emptyList(),
    val status: String? = null
)

@Serializable
data class CouncilFinalizeRequest(
    @SerialName("session_id") val sessionId: String
)

@Serializable
data class CouncilFinalizeResponse(
    val consensus: String? = null,
    val summary: String? = null,
    val status: String? = null
)

@Serializable
data class CouncilContributionDto(
    val model: String,
    val content: String,
    val round: Int? = null
)
