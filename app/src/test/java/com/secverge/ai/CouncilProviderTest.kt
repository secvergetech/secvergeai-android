package com.secverge.ai

import com.secverge.ai.data.models.council.*
import com.secverge.ai.data.network.SECVergeApi
import com.secverge.ai.ui.council.AICouncilRepository
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.*
import org.junit.Test

class CouncilProviderTest {
    private fun api(answer: CouncilProviderResponse) = Proxy.newProxyInstance(
        SECVergeApi::class.java.classLoader, arrayOf(SECVergeApi::class.java)
    ) { _, _, _ -> answer } as SECVergeApi

    @Test fun parsesCurrentProviderContractAndConsensusPayload() {
        val json = Json { ignoreUnknownKeys = true }
        val answer = json.decodeFromString<CouncilProviderResponse>("""{"ok":true,"status":"model_backed","model":"llama","label":"Llama","provider_model":"llama3.2:3b","fallback":false,"content":"TLS protects data."}""")
        assertEquals("llama3.2:3b", answer.providerModel)
        val payload = json.parseToJsonElement(json.encodeToString(CouncilConsensusRequest("TLS?", listOf(CouncilPerspective("Llama", answer.content))))).jsonObject
        assertEquals(setOf("question", "perspectives"), payload.keys)
    }
    @Test fun parsesActivePremiumConsensusWithoutLegacyOkOrContentFields() {
        val json = Json { ignoreUnknownKeys = true }
        val result = json.decodeFromString<CouncilProviderResponse>("""{"status":"model_backed","label":"Gemma Final Council Consensus","provider_model":"gemma3:4b-it-q8_0","consensus":"Use TLS for sensitive data."}""")
        assertEquals("Use TLS for sensitive data.", result.consensus)
        assertNull(result.ok)
    }
    @Test fun usesCouncilShortIdWhileVerifyingConcreteResponseModel() = runBlocking {
        val answer = CouncilProviderResponse(true, "model_backed", "llama", "Llama", "llama3.2:3b", false, "TLS protects data.")
        val contractApi = Proxy.newProxyInstance(SECVergeApi::class.java.classLoader,
            arrayOf(SECVergeApi::class.java)) { _, method, args ->
            assertEquals("councilPerspective", method.name)
            assertEquals("llama", (args[0] as CouncilPerspectiveRequest).model)
            answer
        } as SECVergeApi
        assertTrue(AICouncilRepository(contractApi).perspective("TLS?", "Llama").isSuccess)
    }
    @Test fun acceptsOnlyVerifiedModelBackedPerspectives() = runBlocking {
        val answer = CouncilProviderResponse(true, "model_backed", "llama", "Llama", "llama3.2:3b", false, "A real model answer.")
        assertTrue(AICouncilRepository(api(answer)).perspective("TLS?", "Llama").isSuccess)
        assertTrue(AICouncilRepository(api(answer.copy(providerModel = "gemma3:4b-it-q8_0"))).perspective("TLS?", "Llama").isFailure)
    }
    @Test fun rejectsHttpSuccessWithUnavailableFallback() = runBlocking {
        val fallback = CouncilProviderResponse(true, "temporarily_unavailable", "qwen", "Qwen", "qwen2.5:7b", true, "Try again later.")
        assertTrue(AICouncilRepository(api(fallback)).perspective("TLS?", "Qwen").isFailure)
    }
}
