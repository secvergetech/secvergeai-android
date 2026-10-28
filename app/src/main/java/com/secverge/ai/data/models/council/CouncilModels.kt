package com.secverge.ai.data.models.council

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CouncilCreateRequest(val topic: String)
@Serializable
data class CouncilCreateResponse(val ok: Boolean, val room: CouncilRoom)
@Serializable
data class CouncilRoom(
    @SerialName("room_id") val roomId: String,
    val participants: List<CouncilParticipantDto> = emptyList()
)
@Serializable
data class CouncilParticipantDto(val name: String, val model: String, val role: String)
@Serializable
data class CouncilDebateRequest(@SerialName("room_id") val roomId: String)
@Serializable
data class CouncilDebateResponse(val ok: Boolean, val round: CouncilRound)
@Serializable
data class CouncilRound(
    val round: Int,
    val views: List<CouncilContributionDto>,
    val moderator: CouncilContributionDto? = null
)
@Serializable
data class CouncilContributionDto(val speaker: String, val model: String, val response: String)
@Serializable
data class CouncilFinalizeRequest(@SerialName("room_id") val roomId: String)
@Serializable
data class CouncilFinalizeResponse(val ok: Boolean, val final: CouncilFinal)
@Serializable
data class CouncilFinal(val consensus: String, val model: String)

@Serializable
data class CouncilPerspectiveRequest(val question: String, val model: String)
@Serializable
data class CouncilPerspective(val label: String, val content: String, val status: String = "model_backed")
@Serializable
data class CouncilConsensusRequest(val question: String, val perspectives: List<CouncilPerspective>)
@Serializable
data class CouncilProviderResponse(
    val ok: Boolean? = null,
    val status: String,
    val model: String? = null,
    val label: String,
    @SerialName("provider_model") val providerModel: String,
    val fallback: Boolean = false,
    val content: String = "",
    val consensus: String? = null,
    val answer: String? = null,
    val response: String? = null
)
