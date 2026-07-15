package com.secverge.ai.data.models.ask

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AskRequest(
    val prompt: String,
    @SerialName("conversation_id") val conversationId: String? = null
)

@Serializable
data class AskResponse(
    val answer: String? = null,
    val response: String? = null,
    val message: String? = null,
    @SerialName("conversation_id") val conversationId: String? = null
)
