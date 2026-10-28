package com.secverge.ai

import com.secverge.ai.data.models.cockpit.ContextSnapshotRequest
import com.secverge.ai.data.models.cockpit.ContextSnapshotResponse
import com.secverge.ai.data.models.cockpit.SwitchResponse
import com.secverge.ai.data.models.cockpit.ExecuteRequest
import com.secverge.ai.data.models.cockpit.ExecuteResponse
import com.secverge.ai.data.models.council.*
import com.secverge.ai.data.models.delphi.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.*
import org.junit.Test

/** Minimal, non-secret payloads matching the public responses observed on 2026-10-28. */
class ProductionContractTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test fun executeReadsNestedAnswerAndActualIdentity() {
        val result = json.decodeFromString<ExecuteResponse>("""{"ok":true,"execution":{"execution_id":"test","selected_model":"gemma3:4b-it-q8_0","response":"TLS protects data in transit."}}""")
        assertEquals("TLS protects data in transit.", result.execution?.response)
        assertEquals("gemma3:4b-it-q8_0", result.execution?.selectedModel)
        val body = json.parseToJsonElement(json.encodeToString(ExecuteRequest(prompt = "TLS?", model = "llama3.2:3b"))).jsonObject
        assertEquals("llama3.2:3b", body["force_model"]?.jsonPrimitive?.content)
    }

    @Test fun contextCarryReadsLiveSnapshotAndSwitchEnvelopes() {
        val body = json.parseToJsonElement(json.encodeToString(ContextSnapshotRequest(prompt = "TLS discussion", fromModel = "qwen2.5:7b"))).jsonObject
        assertEquals("qwen2.5:7b", body["current_model"]?.jsonPrimitive?.content)
        assertFalse(body.containsKey("from_model"))
        val snapshot = json.decodeFromString<ContextSnapshotResponse>("""{"ok":true,"snapshot":{"snapshot_id":"test-snapshot","current_model":"qwen2.5:7b"}}""")
        assertEquals("test-snapshot", snapshot.snapshot?.snapshotId)
        val switched = json.decodeFromString<SwitchResponse>("""{"ok":true,"switch":{"switch_id":"test-switch","context_fidelity":97,"message":"Context carried forward."}}""")
        assertEquals("test-switch", switched.switch?.switchId)
        assertEquals(97.0, switched.switch?.contextFidelity)
    }

    @Test fun councilUsesTopicRoomAndNestedResults() {
        val body = json.parseToJsonElement(json.encodeToString(CouncilCreateRequest("TLS?"))).jsonObject
        assertEquals(setOf("topic"), body.keys)
        val room = json.decodeFromString<CouncilCreateResponse>("""{"ok":true,"room":{"room_id":"test-room","participants":[{"name":"Technical","model":"qwen2.5-coder:14b","role":"technical_implementation"}]}}""")
        assertEquals("test-room", room.room.roomId)
        val debate = json.decodeFromString<CouncilDebateResponse>("""{"ok":true,"round":{"round":1,"views":[{"speaker":"Technical","model":"qwen2.5-coder:14b","response":"Encryption."}],"moderator":{"speaker":"Moderator","model":"gemma3:4b-it-q8_0","response":"Agreed."}}}""")
        assertEquals("Encryption.", debate.round.views.single().response)
        assertEquals("Agreed.", debate.round.moderator?.response)
        val final = json.decodeFromString<CouncilFinalizeResponse>("""{"ok":true,"final":{"consensus":"Use TLS.","model":"gemma3:4b-it-q8_0"}}""")
        assertEquals("Use TLS.", final.final.consensus)
        assertTrue(json.encodeToString(CouncilDebateRequest("test-room")).contains("room_id"))
    }

    @Test fun delphiPreservesDistinctBlindLabelsAndNestedResults() {
        val run = json.decodeFromString<DelphiCreateResponse>("""{"ok":true,"run":{"run_id":"test-run","cards":[{"model_label":"Model A","sealed":true},{"model_label":"Model B","sealed":true}]}}""")
        assertEquals(listOf("Model A", "Model B"), run.run.cards.map { it.modelLabel })
        val body = json.parseToJsonElement(json.encodeToString(DelphiBlindSubmitRequest("test-run", "Model B"))).jsonObject
        assertEquals("test-run", body["run_id"]?.jsonPrimitive?.content)
        assertEquals("Model B", body["model_label"]?.jsonPrimitive?.content)
        val submission = json.decodeFromString<DelphiBlindSubmitResponse>("""{"ok":true,"submission":{"model_label":"Model B","response":"Authentication.","actual_model_hidden":true}}""")
        assertEquals("Authentication.", submission.submission.response)
        val debate = json.decodeFromString<DelphiDebateResponse>("""{"ok":true,"debate":{"response":"Compare encryption and authentication.","model":"gemma3:4b-it-q8_0"}}""")
        assertTrue(debate.debate.response.isNotBlank())
        val final = json.decodeFromString<DelphiConvergeResponse>("""{"ok":true,"convergence":{"consensus":"Use both.","model":"gemma3:4b-it-q8_0","stages_complete":["Blind","Reveal(client-side)","Debate","Converge"]}}""")
        assertEquals("Use both.", final.convergence.consensus)
    }
}
