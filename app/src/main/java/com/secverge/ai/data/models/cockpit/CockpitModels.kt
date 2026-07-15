package com.secverge.ai.data.models.cockpit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ContextSnapshotRequest(
    @SerialName("conversation_id") val conversationId: String? = null,
    val prompt: String,
    @SerialName("from_model") val fromModel: String
)

@Serializable
data class ContextSnapshotResponse(
    @SerialName("snapshot_id") val snapshotId: String? = null,
    @SerialName("fidelity_score") val fidelityScore: Double? = null,
    val summary: String? = null,
    val raw: JsonObject? = null
)

@Serializable
data class RouteRequest(
    val prompt: String,
    @SerialName("task_type") val taskType: String? = null,
    val privacy: String? = null,
    @SerialName("from_model") val fromModel: String? = null,
    @SerialName("to_model") val toModel: String? = null
)

@Serializable
data class RouteResponse(
    @SerialName("recommended_model") val recommendedModel: String? = null,
    val reason: String? = null,
    val confidence: Double? = null,
    val raw: JsonObject? = null
)

@Serializable
data class SwitchRequest(
    @SerialName("snapshot_id") val snapshotId: String,
    @SerialName("from_model") val fromModel: String,
    @SerialName("to_model") val toModel: String
)

@Serializable
data class SwitchResponse(
    @SerialName("switch_id") val switchId: String? = null,
    @SerialName("carried_context") val carriedContext: String? = null,
    @SerialName("fidelity_score") val fidelityScore: Double? = null,
    val raw: JsonObject? = null
)

@Serializable
data class ExecuteRequest(
    @SerialName("switch_id") val switchId: String? = null,
    val prompt: String,
    val model: String
)

@Serializable
data class ExecuteResponse(
    val output: String? = null,
    val response: String? = null,
    val model: String? = null,
    val raw: JsonObject? = null
)

@Serializable
data class AdapterStatusResponse(
    val adapters: List<AdapterStatusDto> = emptyList()
)

@Serializable
data class AdapterStatusDto(
    val provider: String,
    val model: String? = null,
    val status: String,
    val configured: Boolean = false
)
