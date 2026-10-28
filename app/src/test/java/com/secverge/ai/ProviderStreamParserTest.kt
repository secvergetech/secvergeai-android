package com.secverge.ai

import com.secverge.ai.data.network.ProviderStreamParser
import java.io.EOFException
import java.io.IOException
import java.io.StringReader
import org.junit.Assert.assertEquals
import org.junit.Test

class ProviderStreamParserTest {
    private fun read(events: String) = ProviderStreamParser.read(StringReader(events).buffered(), "llama3.2:3b")

    @Test fun joinsRealChunksOnlyAfterVerifiedCompletion() {
        val answer = read("""
            data: {"type":"accepted"}

            data: {"type":"meta","provider_model":"llama3.2:3b"}

            data: {"type":"chunk","text":"TLS secures "}

            data: {"type":"chunk","text":"communication."}

            data: {"type":"done","provider_model":"llama3.2:3b","fallback_used":false}
        """.trimIndent())
        assertEquals("TLS secures communication.", answer.text)
        assertEquals("llama3.2:3b", answer.providerModel)
    }
    @Test(expected = EOFException::class) fun rejectsPartialTextWithoutDone() {
        read("data: {\"type\":\"chunk\",\"text\":\"partial\"}\n\n")
    }
    @Test(expected = IOException::class) fun rejectsApplicationErrorAfterHttp200() {
        read("data: {\"type\":\"error\",\"message\":\"Llama unavailable\"}\n\n")
    }
    @Test(expected = IOException::class) fun rejectsModelSubstitution() {
        read("data: {\"type\":\"chunk\",\"text\":\"other answer\"}\n\ndata: {\"type\":\"done\",\"provider_model\":\"gemma3:4b-it-q8_0\",\"fallback_used\":true}\n\n")
    }
}
