package com.secverge.ai.data.network

import java.io.BufferedReader
import java.io.EOFException
import java.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Contract of the live /api/v1/multillm/stream provider, not the legacy execute API. */
object ProviderStreamParser {
    data class Answer(val text: String, val providerModel: String)

    fun read(reader: BufferedReader, expectedModel: String): Answer {
        val text = StringBuilder()
        while (true) {
            val line = reader.readLine() ?: throw EOFException("Model stream ended before completion.")
            if (!line.startsWith("data:")) continue
            val payload = line.removePrefix("data:").trim()
            if (payload.isEmpty()) continue
            val event = Json.parseToJsonElement(payload).jsonObject
            fun field(name: String) = event[name]?.jsonPrimitive?.contentOrNull
            when (field("type")) {
                "chunk" -> text.append(field("text").orEmpty())
                "error" -> throw IOException(field("message") ?: "Selected model request failed.")
                "done" -> {
                    val actual = field("provider_model")
                    if (event["fallback_used"]?.jsonPrimitive?.booleanOrNull == true || actual != expectedModel) {
                        throw IOException("The server did not complete with the selected model.")
                    }
                    if (text.isBlank()) throw IOException("Selected model returned an empty answer.")
                    return Answer(text.toString().trim(), actual)
                }
            }
        }
    }
}
