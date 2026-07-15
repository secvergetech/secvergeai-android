package com.secverge.ai.data.models.humancouncil

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HumanCouncilCreateRequest(
    val topic: String,
    val models: List<String>
)

@Serializable
data class HumanCouncilCreateResponse(
    @SerialName("session_id") val sessionId: String? = null,
    val status: String? = null
)

@Serializable
data class HumanCouncilContributeRequest(
    @SerialName("session_id") val sessionId: String,
    val message: String
)

@Serializable
data class HumanCouncilContributeResponse(
    val responses: List<HumanCouncilMessageDto> = emptyList(),
    val status: String? = null
)

@Serializable
data class HumanCouncilDirectRequest(
    @SerialName("session_id") val sessionId: String,
    val model: String,
    val message: String
)

@Serializable
data class HumanCouncilDirectResponse(
    val response: HumanCouncilMessageDto? = null,
    val status: String? = null
)

@Serializable
data class HumanCouncilMessageDto(
    val speaker: String,
    val content: String,
    val model: String? = null
)
