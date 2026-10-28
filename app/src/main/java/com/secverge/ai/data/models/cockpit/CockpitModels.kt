package com.secverge.ai.data.models.cockpit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class ContextSnapshotRequest(
    @SerialName("conversation_id") val conversationId: String? = null,
    val prompt: String,
    @SerialName("current_model") val fromModel: String,
    val messages: List<String>? = null
)

@Serializable
data class ContextSnapshotResponse(
    val snapshot: ContextSnapshotPayload? = null,
    @SerialName("snapshot_id") val snapshotId: String? = null,
    @SerialName("context_snapshot_id") val contextSnapshotId: String? = null,
    val id: String? = null,
    @SerialName("fidelity_score") val fidelityScore: Double? = null,
    @SerialName("context_fidelity") val contextFidelity: Double? = null,
    val summary: String? = null,
    val message: String? = null,
    val raw: JsonObject? = null
)

@Serializable
data class RouteRequest(
    val prompt: String,
    @SerialName("task_type") val taskType: String? = null,
    val privacy: String? = null,
    @SerialName("privacy_mode") val privacyMode: String? = privacy,
    @SerialName("from_model") val fromModel: String? = null,
    @SerialName("to_model") val toModel: String? = null
)

@Serializable
data class RouteResponse(
    @SerialName("route_id") val routeId: String? = null,
    val id: String? = null,
    @SerialName("recommended_model") val recommendedModel: String? = null,
    val reason: String? = null,
    val confidence: Double? = null,
    val raw: JsonObject? = null
)

@Serializable
data class SwitchRequest(
    @SerialName("snapshot_id") val snapshotId: String,
    @SerialName("from_model") val fromModel: String,
    @SerialName("to_model") val toModel: String,
    @SerialName("preserve_context") val preserveContext: Boolean = true
)

@Serializable
data class SwitchResponse(
    val switch: SwitchPayload? = null,
    @SerialName("switch_id") val switchId: String? = null,
    val id: String? = null,
    @SerialName("carried_context") val carriedContext: String? = null,
    @SerialName("context") val context: String? = null,
    @SerialName("fidelity_score") val fidelityScore: Double? = null,
    @SerialName("context_fidelity") val contextFidelity: Double? = null,
    @SerialName("context_transferred") val contextTransferred: Boolean? = null,
    val summary: String? = null,
    val message: String? = null,
    val raw: JsonObject? = null
)

@Serializable
data class ExecuteRequest(
    @SerialName("switch_id") val switchId: String? = null,
    @SerialName("snapshot_id") val snapshotId: String? = null,
    @SerialName("route_id") val routeId: String? = null,
    val prompt: String,
    val model: String,
    @SerialName("force_model") val forceModel: String = model
)

@Serializable
data class ExecuteResponse(
    val ok: Boolean = true,
    val execution: ExecutionPayload? = null,
    val output: JsonElement? = null,
    val response: JsonElement? = null,
    val answer: JsonElement? = null,
    val result: JsonElement? = null,
    val content: JsonElement? = null,
    val message: JsonElement? = null,
    val model: String? = null,
    val provider: String? = null,
    val raw: JsonObject? = null
)
@Serializable
data class AdapterStatusResponse(
    val adapters: List<AdapterStatusDto> = emptyList()
)

@Serializable
data class AdapterStatusDto(
    val provider: String = "",
    val model: String? = null,
    val name: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val status: String = "unknown",
    val configured: Boolean = false
)

@Serializable
data class ExecutionPayload(
    val response: String? = null,
    @SerialName("selected_model") val selectedModel: String? = null
)

@Serializable
data class ContextSnapshotPayload(
    @SerialName("snapshot_id") val snapshotId: String,
    @SerialName("current_model") val currentModel: String? = null
)

@Serializable
data class SwitchPayload(
    @SerialName("switch_id") val switchId: String,
    @SerialName("context_fidelity") val contextFidelity: Double? = null,
    val message: String? = null
)

@Serializable
data class ProviderStreamRequest(
    val prompt: String,
    @SerialName("target_model") val targetModel: String
)
